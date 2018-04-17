package org.nick.util.customsearch.service;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.nick.util.customsearch.model.Item;
import org.nick.util.customsearch.model.Query;
import org.nick.util.customsearch.model.Searchable;
import org.nick.util.customsearch.model.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.time.Duration;
import java.util.*;
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

import static java.util.Optional.ofNullable;
import static jdk.incubator.http.HttpRequest.newBuilder;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

/**
 * Created by VNikolaenko on 08.07.2015.
 */
//@Slf4j
@Service
//@RequiredArgsConstructor
public class SearchService {
    public static final Pattern HREF_PATTERN = Pattern.compile("^.*href=\"(.*)\" title=.*$");
    public static final int HREF_PATTERN_GROUP = 1;
    public static final String PROTOCOL_HTTPS = "https";
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private static final String SELECTOR_LIST_ITEM_STORE = "list-item store";
    private static final String SELECTOR_LIST_ITEM = "history-item product";

    private static final ExecutorService POOL_WEBKIT = Executors.newCachedThreadPool();

    private static final String AEP_USUC_F = "aep_usuc_f";
    private static final String SITE_GLO_REGION_RU_B_LOCALE_EN_US_C_TP_RUB = "site=glo&region=RU&b_locale=en_US&c_tp=RUB";
    private static final List<HttpCookie> SEARCH_COOKIES = Collections.singletonList(new HttpCookie(AEP_USUC_F, SITE_GLO_REGION_RU_B_LOCALE_EN_US_C_TP_RUB));
    private static final String PATH_SEARCH_QUERY_ORIGIN = "origin";
    private static final String PATH_SEARCH_IN_STORE = "/search";

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

    private final HttpClient httpClient;

    public SearchService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private Stream<String> getElements(URI uri, String selector) {
        try {
            final HttpResponse<String> httpResponse = httpClient.send(newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .setHeader("cookie","aep_usuc_f=site=glo&region=RU&b_locale=en_US&c_tp=EUR")
                    .build(), asString());

            return Stream.of(httpResponse.body().split("\\r?\\n"))
                    .filter(line -> line.contains(selector))
                    .map(this::extractHref)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (IOException | InterruptedException e) {
            log.warn("URI error {}", uri);
        }

        return Stream.empty();
    }

    private Optional<String> extractHref(String line) {
        final Matcher matcher = HREF_PATTERN.matcher(line);
        if (matcher.matches()) {
            return Optional.of(fromUriString(matcher.group(HREF_PATTERN_GROUP)).scheme(PROTOCOL_HTTPS).toUriString());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Element Store mapper
     *
     * @param uri - page uri
     * @return - stream of found stores
     */
    private Stream<Store> elementStoreMapper(final URI uri) {
        return getElements(uri, SELECTOR_LIST_ITEM_STORE)
                .map(storeNode -> new Store(storeNode, storeNode));
    }

    /**
     * Element Item mapper
     *
     * @param uri - page uri
     * @return - stream of found items
     */
    public Stream<Item> elementItemMapper(final URI uri) {
        return getElements(uri, SELECTOR_LIST_ITEM).map(Item::new);
    }

    public Set<Item> searchQueryItems(final Query query) {
        return searchableStreamByQuery(query, this::elementItemMapper)
                .map(requestAndSetSippingMethods(query))
                .collect(Collectors.toList()).stream().map(CompletableFuture::join) //for async
                .filter(filterWarehouse(query))
                .collect(Collectors.toSet());
    }

    private static final String RUSSIAN_WAREHOUSE = "";

    private Predicate<Item> filterWarehouse(Query query) {
        return item -> !query.isShipFromRussia() || item.getActiveWarehouses().contains(RUSSIAN_WAREHOUSE);
    }

    private Function<Item, CompletableFuture<Item>> requestAndSetSippingMethods(Query query) {
        return item -> httpClient
                .sendAsync(buildItemRequest(item), asString())
                .thenApplyAsync(itemHttpResponse(query, item))
                .exceptionally(exception -> { // handle exceptions
                    log.warn("[FILTER EXCEPTION] {} [QUERY] {}", exception.getMessage(), query);
                    log.debug("[EXCEPTION] " + exception.getMessage() + " [QUERY] " + query, exception);
                    return item;
                });
    }

    private HttpRequest buildItemRequest(Item item) {
        return newBuilder(fromUriString(item.getLink()).build().toUri()).build();
    }

    private Function<HttpResponse<String>, Item> itemHttpResponse(Query query, Item item) {
        return response -> {
            if (response.statusCode() == 200) {
                return item.setActiveWarehouses(getActiveWarehouses(response.body()));
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

    private List<String> getActiveWarehouses(String body) {
        return Collections.emptyList();
    }

    //todo simplify
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

    private <T extends Searchable> Stream<T> searchableStreamByQuery(final Query query, final Function<URI, Stream<T>> mapper) {
        return IntStream.range(1, ofNullable(query.getPages4Processing()).orElse(1) + 1).boxed()
                .map(page -> CompletableFuture
                        .supplyAsync(() -> buildSearchURL(mainPath, query, page).stream().flatMap(mapper), POOL_WEBKIT)
                        .exceptionally(exception -> {
                            log.warn("[STORES EXCEPTION] {} [QUERY] {}", exception.getMessage(), query);
                            log.debug("[EXCEPTION] " + exception.getMessage() + " [QUERY] " + query, exception);
                            return Stream.empty();
                        })
                ).collect(Collectors.toList()) //for async
                .stream().map(CompletableFuture::join) //start http
                .flatMap(x -> x);
    }

    //todo simplify
    Stream<Store> storeStreamByQuery(final Query query) {
        return searchableStreamByQuery(query, this::elementStoreMapper);
    }

    public Set<Store> findStores(final List<Query> queries) {
        Assert.isTrue(queries.size() > 1, "Less than 2 queries");

        final List<Query> subQueryList = queries.stream().skip(1).collect(Collectors.toList());
        final Optional<Query> firstQuery = queries.stream().findFirst();

        if (firstQuery.isPresent()) {
            return storeStreamByQuery(firstQuery.get())
                    .distinct()
                    .filter(store -> filterStores(store, subQueryList))
                    .peek(store -> log.info("Found {}", store.getTitle()))
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    Optional<URI> buildSearchURL(String mainPath, Query query, Integer page) {
        final UriComponentsBuilder builder = fromUriString(mainPath)
                .path(query.getSearchText() + ".html")
                .queryParam(PARAM_MAIN_QUERY, query.getSearchText());

        ofNullable(page).ifPresent(e -> builder.queryParam(PARAM_PAGE, e));
        ofNullable(query.getMinPrice()).ifPresent(minPrice -> builder.queryParam(PARAM_MIN_PRICE, minPrice));
        ofNullable(query.getMaxPrice()).ifPresent(maxPrice -> builder.queryParam(PARAM_MAX_PRICE, maxPrice));
        if (query.isShipFromRussia()) builder.queryParam(PARAM_SHIP_FROM, "ru");

        return Optional.of(builder.build().toUri());
    }

    @PreDestroy
    private void preDestroy() {
        POOL_WEBKIT.shutdown();
    }
}