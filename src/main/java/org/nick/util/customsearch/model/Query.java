package org.nick.util.customsearch.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by VNikolaenko on 26.06.2015.
 */
@Slf4j
@Value
@RequiredArgsConstructor(staticName = "of")
public class Query {
    private String searchText;
    private Integer minPrice;
    private Integer maxPrice;
    private String shippingMethod;
    private Integer pages4Processing;

    public static Query of(String query) {
        return new Query(query, null, null, null, 1);
    }
}
