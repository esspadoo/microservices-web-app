package it.unipd.search.client;

import it.unipd.search.config.InfererProperties;
import it.unipd.search.dto.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class InfererClient {

    private final RestTemplate restTemplate;
    private final InfererProperties infererProperties;

    public InfererClient(
            RestTemplate restTemplate,
            InfererProperties infererProperties) {
        this.restTemplate = restTemplate;
        this.infererProperties = infererProperties;
    }

    public List<Document> inferBatch(List<Document> requests) {
        String url = infererProperties.getBaseUrl() + "/api/v1/inferBatch";

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
