package it.unipd.search.client;

import it.unipd.search.config.InfererProperties;
import it.unipd.search.dto.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class InfererClient {

    private final RestTemplate restTemplate;
    private final InfererProperties infererProperties;
    private static final Logger log = LoggerFactory.getLogger(InfererClient.class);

    public InfererClient(
            RestTemplate restTemplate,
            InfererProperties infererProperties) {
        this.restTemplate = restTemplate;
        this.infererProperties = infererProperties;
    }

    public List<Document> inferBatch(List<Document> requests) {

        String baseUrl = infererProperties.getBaseUrl();

        log.info("Inferer base URL = '{}'", baseUrl);

        if (baseUrl == null || baseUrl.isBlank() || !baseUrl.startsWith("http")) {
            throw new IllegalStateException(
                    "Invalid inferer.base-url: '" + baseUrl +
                            "'. Expected something like http://inferer:5050"
            );
        }

        String url = baseUrl + "/api/v1/inferBatch";

        log.info("Calling Inferer endpoint: {}", url);

        ResponseEntity<List<Document>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        new HttpEntity<>(requests),
                        new ParameterizedTypeReference<>() {
                        }
                );

        return response.getBody();
    }

}
