package org.nick.util.customsearch.model;

import java.util.Collections;
import java.util.Set;

//@Data
public class Item implements Searchable {
    private final String link;
    private Set<WarehouseDTO> activeWarehouses = Collections.emptySet();

    public Item(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public Set<WarehouseDTO> getActiveWarehouses() {
        return activeWarehouses;
    }

    public Item setActiveWarehouses(Set<WarehouseDTO> activeWarehouses) {
        this.activeWarehouses = activeWarehouses;
        return this;
    }
}
