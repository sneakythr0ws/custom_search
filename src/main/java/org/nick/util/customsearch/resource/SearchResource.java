package org.nick.util.customsearch.resource;

import org.nick.util.customsearch.model.Item;
import org.nick.util.customsearch.model.Query;
import org.nick.util.customsearch.model.Store;
import org.nick.util.customsearch.service.SearchService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
/*@Slf4j
@RequiredArgsConstructor*/
public class SearchResource {
    private final SearchService searchService;

    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/store")
    public Set<Store> findStores(@RequestBody List<Query> queries) {
        Assert.isTrue(queries.size() > 1, "Queries length must be greater than 1");
        return searchService.findStores(queries);
    }

    @GetMapping("/item")
    public Set<Item> findItems(String searchText) {
        //return searchService.searchQueryItems(Query.builder(searchText).shipFromRussia(true).build());
        return searchService.searchQueryItems(Query.build(searchText));
    }

    @GetMapping("/debug")
    public List<Item> debug() throws URISyntaxException {
        //final URI url = new URI("https://www.aliexpress.com/af/mouse.html?shipCountry=ru&groupsort=1&jump=afs&blanktest=0&isFreeShip=y&SortType=total_tranpro_desc&g=n&SearchText=mouse&page=1&shipFromCountry=ru");
        //final URI url = new URI("http://www.aliexpress.com/wholesale?shipFromCountry=ru&SearchText=mouse");
        //final URI url = new URI("https://www.aliexpress.com/wholesale?shipFromCountry=ru&shipCountry=ru&groupsort=1&isFreeShip=y&SortType=total_tranpro_desc&g=n&SearchText=mouse");
        final URI url = new URI("https://www.aliexpress.com/wholesale?minPrice=&maxPrice=&isBigSale=n&isFreeShip=y&isNew=n&isFavorite=n&isMobileExclusive=n&isLocalReturn=n&shipFromCountry=ru&shipCompanies=&SearchText=mouse&CatId=0&SortType=total_tranpro_desc&needQuery=n&groupsort=1");
        //final URI url = new URI("https://www.aliexpress.com/wholesale?shipCountry=ru&groupsort=1&isFreeShip=y&SortType=total_tranpro_desc&g=n&SearchText=mouse");

        return searchService.elementItemMapper(url).collect(Collectors.toList());
    }
}
