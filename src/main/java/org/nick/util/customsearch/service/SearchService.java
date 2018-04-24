package org.nick.util.customsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nick.util.customsearch.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.of;
import static java.util.Optional.ofNullable;
import static jdk.incubator.http.HttpRequest.newBuilder;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

/**
 * Created by VNikolaenko on 08.07.2015.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private static final Pattern HREF_PATTERN = Pattern.compile("^.*href=\"(.*)\" title=.*$");
    private static final String PROTOCOL_HTTPS = "https";

    private static final String SELECTOR_LIST_STORE = "list-item store";
    private static final String SELECTOR_LIST_ITEM = "history-item product";

    private static final ExecutorService POOL_WEBKIT = Executors.newCachedThreadPool();

    private static final String PATH_SEARCH_QUERY_ORIGIN = "origin";
    private static final String PATH_SEARCH_IN_STORE = "/buildSearchURI";

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_MIN_PRICE = "minPrice";
    private static final String PARAM_MAX_PRICE = "maxPrice";
    private static final String PARAM_SHIP_FROM = "shipFromCountry";

    private static final String PARAM_MAIN_QUERY = "SearchText";

    @Value("${app.loginAttemptLimit:5}")
    private int loginAttemptLimit;

    @Value("${app.proxy.host}")
    private String proxyHost;

    @Value("${app.proxy.port}")
    private Integer proxyPort;

    @Value("${app.path}")
    private String mainPath;

    @Value("${app.shippingPath}")
    private String shippingPath;

    private final HttpClient httpClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private Stream<String> getMainElements(Query query, int page, String selector) {
        try {
            final String uri = buildSearchURI(mainPath, query, page);
            final HttpHeaders headers = new HttpHeaders();
            if (query.isShipFromRussia()) {
                headers.add("Cookie", "aep_usuc_f=site=glo&region=RU&b_locale=en_US&c_tp=EUR");
            }

            final ResponseEntity<String> responseEntity = restTemplate.exchange(uri, GET, new HttpEntity<String>(headers), String.class);

            //todo use jdk http client after cookiehandler fix

            return Stream.of(responseEntity.getBody().split("\\r?\\n"))
                    .filter(line -> line.contains(selector))
                    .map(this::extractHref)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private Optional<String> extractHref(String line) {
        return extractGroup(line).map(group -> fromUriString(group).scheme(PROTOCOL_HTTPS).toUriString());
    }

    public Set<Item> searchQueryItems(final Query query) {
        return searchableStreamByQuery(query, SELECTOR_LIST_ITEM, Item::new)
                .map(requestAndSetSippingMethods(query))
                .collect(Collectors.toList()).stream().map(CompletableFuture::join) //for async
                .filter(filterWarehouse(query))
                .collect(Collectors.toSet());
    }

    private Predicate<Item> filterWarehouse(Query query) {
        return item -> !query.isShipFromRussia() || !item.getActiveWarehouses().isEmpty();
    }

    private Function<Item, CompletableFuture<Item>> requestAndSetSippingMethods(Query query) {
        return item -> httpClient
                .sendAsync(buildShippingRequest(item), asString())
                .thenApplyAsync(itemHttpResponse(query, item))
                .exceptionally(exception -> { // handle exceptions
                    log.warn("[FILTER EXCEPTION] {} [QUERY] {}", exception.getMessage(), query);
                    log.debug("[EXCEPTION] " + exception.getMessage() + " [QUERY] " + query, exception);
                    return item;
                });
    }

    URI buildShippingURI(String shippingPath, String productId) {
        return fromUriString(shippingPath).build(of("productId", productId));
    }

    private HttpRequest buildShippingRequest(Item item) {
        final UriComponents uriComponents = fromUriString(item.getLink()).build();
        final String path = uriComponents.getPath();
        final String productId = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf("."));

        return newBuilder(buildShippingURI(shippingPath, productId)).build();
    }

    private Function<HttpResponse<String>, Item> itemHttpResponse(Query query, Item item) {
        return response -> {
            if (response.statusCode() == 200) {
                try {
                    return item.setActiveWarehouses(objectMapper.readValue(response.body().substring(1, response.body().length() - 1), WarehousesDTO.class).warehouses);
                } catch (IOException e) {
                    log.warn("Shipping company error {}", item.getLink());
                    return item;
                }
            } else { //log non 200
                log.warn("Not 200 status code {} [ITEM] {} [QUERY] {}", response.statusCode(), item, query);
                return item;
            }
        };
    }

    private Function<Throwable, Optional<Item>> processStoreHttpException(Query query) {
        return exception -> { // handle exceptions
            log.warn("[FILTER EXCEPTION] {} [QUERY] {}", exception.getMessage(), query);
            log.debug("[EXCEPTION] " + exception.getMessage() + " [QUERY] " + query, exception);
            return Optional.empty();
        };
    }

    private Optional<String> extractGroup(String input) {
        final Matcher matcher = SearchService.HREF_PATTERN.matcher(input);
        if (matcher.matches()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }

    boolean filterStores(Store store, List<Query> queries) {
        return queries.stream().map(query -> httpClient
                .sendAsync(newBuilder(createStoreFilterURI(store.getLink(), query)).timeout(Duration.ofSeconds(15)).build(), asString())
                .thenApplyAsync(storeHttpResponse(store, query))
                .exceptionally(processStoreHttpException(query)))
                .collect(Collectors.toList()).stream().map(CompletableFuture::join)//for async
                .map(Optional::isPresent).reduce((a, b) -> a & b).orElse(false); // merge responses
    }

    private Function<HttpResponse<String>, Optional<?>> storeHttpResponse(Store store, Query query) {
        return response -> { // handle response
            if (response.statusCode() == 200) {
                return Optional.of(storeHasItem(response.body()));
            } else { //log non 200
                log.warn("Not 200 status code {} [STORE] {} [QUERY] {}", response.statusCode(), store, query);
                return Optional.empty();
            }
        };
    }

    boolean storeHasItem(String responseBody) {
        return responseBody.contains("items-list util-clearfix");
    }

    URI createStoreFilterURI(String storeLink, Query criteria) {
        return fromUriString(storeLink).scheme("http").path(PATH_SEARCH_IN_STORE)
                .queryParam(PATH_SEARCH_QUERY_ORIGIN, "y")
                .queryParam(PARAM_MAIN_QUERY, criteria.getSearchText()).build().toUri();
    }

    private <T extends Searchable> Stream<T> searchableStreamByQuery(final Query query, final String selector, Function<String, T> mapper) {
        //todo use async jdk http client after cookie handler fix
        return IntStream.range(1, ofNullable(query.getPages4Processing()).orElse(1) + 1).boxed()
                .map(page -> CompletableFuture
                        .supplyAsync(() -> getMainElements(query, page, selector).map(mapper), POOL_WEBKIT)
                        .exceptionally(exception -> {
                            log.warn("[MAIN QUERY EXCEPTION] {} {}", exception.getMessage(), query);
                            log.debug("[EXCEPTION] " + exception.getMessage() + " [QUERY] " + query, exception);
                            return Stream.empty();
                        })
                ).collect(Collectors.toList()) //for async
                .stream().map(CompletableFuture::join) //start http
                .flatMap(x -> x);
    }

    Stream<Store> storeStreamByQuery(final Query query) {
        return searchableStreamByQuery(query, SELECTOR_LIST_STORE, Store::new);
    }

    public Set<Store> findStores(final List<Query> queries) {
        Assert.isTrue(queries.size() > 1, "Less than 2 queries");

        final List<Query> subQueryList = queries.stream().skip(1).collect(Collectors.toList());
        final Optional<Query> firstQuery = queries.stream().findFirst();

        if (firstQuery.isPresent()) {
            return storeStreamByQuery(firstQuery.get())
                    .distinct()
                    .filter(store -> filterStores(store, subQueryList))
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    String buildSearchURI(String mainPath, Query query, Integer page) {
        final UriComponentsBuilder builder = fromUriString(mainPath)
                .queryParam(PARAM_MAIN_QUERY, query.getSearchText());

        ofNullable(page).ifPresent(e -> builder.queryParam(PARAM_PAGE, e));
        ofNullable(query.getMinPrice()).ifPresent(minPrice -> builder.queryParam(PARAM_MIN_PRICE, minPrice));
        ofNullable(query.getMaxPrice()).ifPresent(maxPrice -> builder.queryParam(PARAM_MAX_PRICE, maxPrice));
        if (query.isShipFromRussia()) builder.queryParam(PARAM_SHIP_FROM, "ru");

        return builder.build().toUriString();
    }

    @PreDestroy
    private void preDestroy() {
        POOL_WEBKIT.shutdown();
    }
}