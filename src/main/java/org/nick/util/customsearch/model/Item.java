package org.nick.util.customsearch.model;

import lombok.Data;

import java.util.List;

@Data
public class Item implements Searchable {
    private final String link;
    private List<String> activeWarehouses;
}
