package org.nick.util.customsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.incubator.http.HttpClient;
import org.nick.util.customsearch.config.UtilConfig;
import org.nick.util.customsearch.service.SearchService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.security.NoSuchAlgorithmException;

@TestConfiguration
public class ServicesTestConfig {
    @Bean
    public HttpClient httpClient() throws NoSuchAlgorithmException {
        return new UtilConfig().httpClient("localhost", 80);
    }

    @Bean
    public SearchService searchService(HttpClient httpClient, RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new SearchService(httpClient, restTemplate, objectMapper);
    }
}
