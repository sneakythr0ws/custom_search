package org.nick.util.customsearch.model;

import lombok.*;

/**
 * Created by VNikolaenko on 26.06.2015.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "hiddenBuilder")
@Getter
@ToString
public class Query {
    private String searchText;
    private Integer minPrice;
    private Integer maxPrice;
    private String shippingMethod;
    private boolean shipFromRussia = false;
    private Integer pages4Processing;

    public static QueryBuilder builder(String searchText) {
        return hiddenBuilder().searchText(searchText);
    }
}
