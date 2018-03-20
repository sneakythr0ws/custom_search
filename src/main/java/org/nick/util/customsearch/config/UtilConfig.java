package org.nick.util.customsearch.config;

import jdk.incubator.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.ProxySelector;

@Configuration
public class UtilConfig {
    @Bean
    public HttpClient httpClient(@Value("${proxy.host:localhost}") String proxyHost,
                                 @Value("${proxy.host:5566}") int proxyPort) {
        return HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}
