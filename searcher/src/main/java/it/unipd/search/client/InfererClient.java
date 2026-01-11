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
 * Client component responsible for communicating with an external
 * inference service via HTTP.
 *
 * <p>This client sends batches of documents to the inference service,
 * which enriches them with inferred metadata (e.g. topics, categories
 * or classifications) and returns the enriched documents.</p>
 */
@Service
public class InfererClient {

    /** REST client used to perform HTTP calls to the inference service. */
    private final RestTemplate restTemplate;

    /** Configuration properties containing the inference service endpoint. */
    private final InfererProperties infererProperties;

    /** Logger used for tracing inference service interactions. */
    private static final Logger log = LoggerFactory.getLogger(InfererClient.class);

    /**
     * Constructs an {@code InfererClient} with the required dependencies.
     *
     * <p>Dependencies are injected via constructor injection to promote
     * immutability and ease of testing.</p>
     *
     * @param restTemplate      REST client used for HTTP communication
     * @param infererProperties configuration properties of the inference service
     */
    public InfererClient(RestTemplate restTemplate, InfererProperties infererProperties) {
        this.restTemplate = restTemplate;
        this.infererProperties = infererProperties;
    }

    /**
     * Sends a batch of documents to the inference service and returns
     * the enriched documents.
     *
     * <p>The method performs the following steps:</p>
     * <ol>
     *   <li>Retrieve the inference service base URL from configuration</li>
     *   <li>Validate the URL format</li>
     *   <li>Send an HTTP POST request containing the documents</li>
     *   <li>Deserialize and return the enriched documents</li>
     * </ol>
     *
     * @param requests list of documents to be processed by the inference service
     * @return list of documents enriched with inferred metadata
     * @throws IllegalStateException if the inference service URL is missing,
     *                               blank, or not a valid HTTP URL
     */
    public List<Document> inferBatch(List<Document> requests) {

        String baseUrl = infererProperties.getBaseUrl();

        log.info("Inferer base URL = '{}'", baseUrl);

        if (baseUrl == null || baseUrl.isBlank() || !baseUrl.startsWith("http")) {
            throw new IllegalStateException(
                    "Invalid inferer.base-baseUrl1: '" + baseUrl +
                            "'. Expected something like http://inferer:5050 (in the default configuration)"
            );
        }

        log.info("Calling Inferer endpoint: {}", baseUrl);

        ResponseEntity<List<Document>> response =
                restTemplate.exchange(
                        baseUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(requests),
                        new ParameterizedTypeReference<>() {
                        }
                );

        return response.getBody();
    }

}
