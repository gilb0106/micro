package org.ac.cst8277.gilbert.joey.messageservice.Service_Connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ac.cst8277.gilbert.joey.messageservice.Bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@Service
public class UMSConnector {
    private static final Logger logger = LoggerFactory.getLogger(UMSConnector.class);

    @Value("${ums.host}")
    private String uriUmsHost;

    @Value("${ums.port}")
    private String uriUmsPort;

    @Value("${ums.paths.user}")
    private String uriUmsUserPath;

    private final ObjectMapper objectMapper;

    public UMSConnector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<User> retrieveUmsData(String uri) {
        WebClient client = WebClient.builder()
                .baseUrl(uriUmsHost + ":" + uriUmsPort)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return client.method(HttpMethod.GET)
                .uri(uriUmsUserPath + uri)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(Charset.forName("UTF-8"))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        return objectMapper.readValue(responseBody, User.class);
                    } catch (Exception e) {
                        logger.error("Error mapping JSON response to User class: {}", e.getMessage());
                        throw new RuntimeException("Error mapping JSON response to User class", e);
                    }
                })
                .doOnError(error -> logger.error("Error retrieving data from UMS: {}", error.getMessage()));
    }
}
