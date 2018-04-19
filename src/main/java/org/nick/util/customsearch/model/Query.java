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

    public Query setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
        return this;
    }

    public Query setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    public Query setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
        return this;
    }

    public Query setShipFromRussia(boolean shipFromRussia) {
        this.shipFromRussia = shipFromRussia;
        return this;
    }

    public Query setPages4Processing(Integer pages4Processing) {
        this.pages4Processing = pages4Processing;
        return this;
    }

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

    @Override
    public String toString() {
        return "Query{" +
                "searchText='" + searchText + '\'' +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", shippingMethod='" + shippingMethod + '\'' +
                ", shipFromRussia=" + shipFromRussia +
                ", pages4Processing=" + pages4Processing +
                '}';
    }
}
