package org.ac.cst8277.gilbert.joey.messageservice.Service_Connector;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public Mono<Object> retrieveUmsData(String uri) {
        WebClient client = WebClient.builder()
                .baseUrl(uriUmsHost + ":" + uriUmsPort + uriUmsUserPath)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
System.out.println(uri);
        return client.method(HttpMethod.GET)
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(Charset.forName("UTF-8"))
                .retrieve()
                .bodyToMono(Object.class);
    }
}

