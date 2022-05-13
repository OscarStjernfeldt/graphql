package com.example.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class GraphQLDataFetchers {

    private final Logger logger = LoggerFactory.getLogger(GraphQLDataFetchers.class);

    private final ObjectMapper mapper;

    public GraphQLDataFetchers(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private static final List<Map<String, String>> books = Arrays.asList(
            ImmutableMap.of("id", "book-1",
                    "name", "Harry Potter and the Philosopher's Stone",
                    "pageCount", "223",
                    "authorId", "author-1"),
            ImmutableMap.of("id", "book-2",
                    "name", "Moby Dick",
                    "pageCount", "635",
                    "authorId", "author-2"),
            ImmutableMap.of("id", "book-3",
                    "name", "Interview with the vampire",
                    "pageCount", "371",
                    "authorId", "author-3")
    );

    private static final List<Map<String, String>> authors = Arrays.asList(
            ImmutableMap.of("id", "author-1",
                    "firstName", "Joanne",
                    "lastName", "Rowling"),
            ImmutableMap.of("id", "author-2",
                    "firstName", "Herman",
                    "lastName", "Melville"),
            ImmutableMap.of("id", "author-3",
                    "firstName", "Anne",
                    "lastName", "Rice")
    );

    public DataFetcher<Map<String, String>> getBookByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String bookId = dataFetchingEnvironment.getArgument("id");
            return books
                    .stream()
                    .filter(book -> book.get("id")
                            .equals(bookId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher<Map<String, String>> getAuthorDataFetcher() {
        return dataFetchingEnvironment -> {
            Map<String, String> book = dataFetchingEnvironment.getSource();
            String authorId = book.get("authorId");
            return authors
                    .stream()
                    .filter(author -> author.get("id")
                            .equals(authorId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher<Map<String, String>> getShortUrlByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String shortUrlId = dataFetchingEnvironment.getArgument("id");
            return fetch().stream()
                    .filter(shortUrl -> shortUrl.get("id")
                            .equals(shortUrlId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher<List<Map<String, String>>> getAllShortUrlDataFetcher() {
        return dataFetchingEnvironment -> fetch();
    }

    private List<Map<String, String>> fetch() throws JsonProcessingException {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(con -> con.addHandlerLast(new ReadTimeoutHandler(5000,
                                TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        var uriSpec = client.get();

        var responseBody = uriSpec.uri(URI.create("http://localhost:8081/short"))
                .retrieve()
                .bodyToMono(String.class);

        var body = responseBody.block();

        return mapper.readValue(body, new TypeReference<>() {
        });
    }
}
