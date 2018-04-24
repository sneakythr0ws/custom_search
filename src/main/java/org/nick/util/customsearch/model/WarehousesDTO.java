package org.nick.util.customsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class WarehousesDTO {
    @JsonProperty("freight")
    public Set<WarehouseDTO> warehouses;
}
