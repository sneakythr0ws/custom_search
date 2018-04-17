package org.nick.util.customsearch.model;

/**
 * Created by VNikolaenko on 26.06.2015.
 */
/*@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "hiddenBuilder")
@Getter
@ToString*/
public class Query {
    private String searchText;
    private Integer minPrice;
    private Integer maxPrice;
    private String shippingMethod;
    private boolean shipFromRussia = true;
    private Integer pages4Processing;

    /*public static QueryBuilder builder(String searchText) {
        return hiddenBuilder().searchText(searchText);
    }*/

    public static Query build(String searchText) {
        final Query query = new Query();
        query.searchText = searchText;
        return query;
    }

    public boolean isShipFromRussia() {
        return true;
    }

    public String getSearchText() {
        return searchText;
    }

    public Integer getPages4Processing() {
        return pages4Processing;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }
}
