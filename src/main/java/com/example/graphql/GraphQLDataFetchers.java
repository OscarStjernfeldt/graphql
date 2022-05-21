package com.example.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class GraphQLDataFetchers {

    @Value("${url-shortener-location}")
    private String urlShortenerUriString;

    private HttpClient httpClient;
    private WebClient client;

    @Value("${url-post-location}")
    private String urlPostString;

    private final ObjectMapper mapper;

    public GraphQLDataFetchers(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public DataFetcher<Map<String, String>> getPostByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String postId = dataFetchingEnvironment.getArgument("id");
            return fetchPostUrl()
                    .stream()
                    .filter(post -> post.get("id")
                            .equals(postId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher<Map<String, String>> getShortUrlByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String shortUrlId = dataFetchingEnvironment.getArgument("id");
            return fetchShortUrl().stream()
                    .filter(shortUrl -> shortUrl.get("id")
                            .equals(shortUrlId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher<List<Map<String, String>>> getAllShortUrlDataFetcher() {
        return dataFetchingEnvironment -> fetchShortUrl();
    }

    private List<Map<String, String>> fetchShortUrl() throws JsonProcessingException {

        httpClient = getHttpClient();

        client = getWebClient(httpClient);

        var uriSpec = client.get();

        var responseBody = uriSpec.uri(URI.create(urlShortenerUriString))
                .retrieve()
                .bodyToMono(String.class);

        var body = responseBody.block();

        return mapper.readValue(body, new TypeReference<>() {
        });
    }

    private List<Map<String, String>> fetchPostUrl() throws JsonProcessingException {

        httpClient = getHttpClient();

        client = getWebClient(httpClient);

        var uriSpec = client.get();

        var responseBody = uriSpec.uri(URI.create(urlPostString))
                .retrieve()
                .bodyToMono(String.class);

        var body = responseBody.block();

        return mapper.readValue(body, new TypeReference<>() {
        });
    }

    private WebClient getWebClient(HttpClient httpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private HttpClient getHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(con -> con.addHandlerLast(new ReadTimeoutHandler(5000,
                                TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));
    }
}
