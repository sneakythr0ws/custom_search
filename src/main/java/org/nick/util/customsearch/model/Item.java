package org.nick.util.customsearch.model;

import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public class Item implements Searchable {
    private final String link;
    private Set<WarehouseDTO> activeWarehouses = Collections.emptySet();
}
