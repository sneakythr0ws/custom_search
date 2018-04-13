package org.nick.util.customsearch.service;

import io.webfolder.ui4j.api.dom.Element;
import lombok.extern.slf4j.Slf4j;
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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes = {UtilConfig.class, ServicesTestConfig.class})
class SearchServiceTest {
    private static final Store Q1S1 = new Store("q1link1", "q1title1");
    private static final Store Q1S2 = new Store("q1link2", "q1title2");
    private static final Query QUERY_1 = Query.builder("query1").build();
    private static final Query QUERY_2 = Query.builder("query2").pages4Processing(1).build();
    private static final String TEST_MAIN_PATH = "http://domain.com";

    @MockBean
    private SearchService searchService;

    @BeforeEach
    void beforeAll() {
        //call real methods
        when(searchService.findStores(anyList())).thenCallRealMethod();
        when(searchService.storeHasItem(anyString())).thenCallRealMethod();
        when(searchService.buildSearchURL(anyString(), any(Query.class), anyInt())).thenCallRealMethod();
        when(searchService.createStoreFilterURI(anyString(), any(Query.class))).thenCallRealMethod();
        //storeStreamByQuery
        when(searchService.storeStreamByQuery(QUERY_1)).thenReturn(Stream.of(Q1S1, Q1S2));
        //filterStores
        when(searchService.filterStores(Q1S1, Collections.singletonList(QUERY_2))).thenReturn(true);
        when(searchService.filterStores(Q1S2, Collections.singletonList(QUERY_2))).thenReturn(false);
    }

    @Test
    void findInStores() {
        assertEquals(Set.of(Q1S1), searchService.findStores(List.of(QUERY_1, QUERY_2)), "");
    }

    @Test
    void storeHasItem() {
        assertAll("Store has item",
                () -> assertTrue(searchService.storeHasItem("items-list util-clearfix")),
                () -> assertTrue(searchService.storeHasItem("items-list util-clearfix some other text")),
                () -> assertFalse(searchService.storeHasItem("items-list some other text util-clearfix")),
                () -> assertFalse(searchService.storeHasItem("123"))
        );
    }

    @Test
    void buildSearchURL() {
        assertAll("buildSearchURL",
                () -> assertEquals("http://domain.com?SearchText=query1&page=4", searchService.buildSearchURL(TEST_MAIN_PATH, QUERY_1, 4).get().toString()),
                () -> assertEquals("http://domain.com?SearchText=query2&page=1", searchService.buildSearchURL(TEST_MAIN_PATH, QUERY_2, 1).get().toString())
        );
    }

    @Test
    void createStoreFilterURI() {
        assertAll("Store filter URI",
                () -> assertEquals("http:/domain.com/search?origin=y&SearchText=query1", searchService.createStoreFilterURI("domain.com", QUERY_1).toString()),
                () -> assertEquals("http://domain.com/search?origin=y&SearchText=query1", searchService.createStoreFilterURI("http://domain.com", QUERY_1).toString()),
                () -> assertEquals("http:/domain.com/search?origin=y&SearchText=query2", searchService.createStoreFilterURI("domain.com", QUERY_2).toString()),
                () -> assertEquals("http://domain.com/search?origin=y&SearchText=query2", searchService.createStoreFilterURI("http://domain.com", QUERY_2).toString())
        );
    }
}