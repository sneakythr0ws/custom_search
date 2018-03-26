package org.nick.util.customsearch.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nick.util.customsearch.ServicesTestConfig;
import org.nick.util.customsearch.config.UtilConfig;
import org.nick.util.customsearch.model.Query;
import org.nick.util.customsearch.model.Store;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes = {UtilConfig.class, ServicesTestConfig.class})
class SearchServiceTest {
    private static final Store Q1S1 = new Store("q1link1", "q1title1");
    private static final Store Q1S2 = new Store("q1link2", "q1title2");
    private static final Query QUERY_1 = Query.of("query1");
    private static final Query QUERY_2 = Query.of("query2");
    @MockBean
    private SearchService searchService;

    @BeforeEach
    void beforeAll() {
        //call real methods
        when(searchService.findInStores(anyString(), anyString())).thenCallRealMethod();
        when(searchService.findInStores(anyList())).thenCallRealMethod();
        when(searchService.storeHasItem(anyString())).thenCallRealMethod();
        //allStoresByQuery
        when(searchService.allStoresByQuery(QUERY_1)).thenReturn(Stream.of(Q1S1, Q1S2));
        //filterStores
        when(searchService.filterStores(Q1S1, Collections.singletonList(QUERY_2))).thenReturn(true);
        when(searchService.filterStores(Q1S2, Collections.singletonList(QUERY_2))).thenReturn(false);
    }

    @Test
    void findInStores() {
        assertEquals(Set.of(Q1S1), searchService.findInStores(List.of(QUERY_1, QUERY_2)), "");
    }

    @Test
    void storeHasItem() {
        assertTrue(searchService.storeHasItem("items-list util-clearfix"));
        assertTrue(searchService.storeHasItem("items-list util-clearfix some other text"));
        assertFalse(searchService.storeHasItem("items-list some other text util-clearfix"));
        assertFalse(searchService.storeHasItem("123"));
    }
}