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
    private final String searchText;
    private final Integer minPrice = null;
    private final Integer maxPrice = null;
    private final String shippingMethod = null;
    private final Integer pages4Processing = 1;
}
