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

/**
 * Client component responsible for communicating with the external
 * inference service.
 *
 * <p>The service enriches documents with inferred metadata
 * such as topics or classifications.</p>
 */
@Service
public class InfererClient {

    /** REST client used to communicate with the inference service. */
    private final RestTemplate restTemplate;

    /** Configuration properties for the inference service. */
    private final InfererProperties infererProperties;

    private static final Logger log = LoggerFactory.getLogger(InfererClient.class);

    /**
     * Constructs an {@code InfererClient}.
     *
     * @param restTemplate REST client
     * @param infererProperties inference service configuration
     */
    public InfererClient(
            RestTemplate restTemplate,
            InfererProperties infererProperties) {
        this.restTemplate = restTemplate;
        this.infererProperties = infererProperties;
    }

    /**
     * Sends a batch of documents to the inference service and
     * returns enriched results.
     *
     * @param requests list of documents to process
     * @return list of inferred documents
     * @throws IllegalStateException if the service URL is invalid
     */
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
