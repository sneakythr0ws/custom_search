package org.nick.util.customsearch.model;

import java.util.List;

//@Data
public class Item implements Searchable {
    private final String link;
    private List<String> activeWarehouses;

    public Item(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public List<String> getActiveWarehouses() {
        return activeWarehouses;
    }

    public Item setActiveWarehouses(List<String> activeWarehouses) {
        this.activeWarehouses = activeWarehouses;
        return this;
    }
}
