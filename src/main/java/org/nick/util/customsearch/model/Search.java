package org.nick.util.customsearch.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class Search {

    private List<Query> criterias;
}