package it.unipd.search.client;

import it.unipd.search.config.InfererProperties;
import it.unipd.search.dto.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * <strong> Class InfererClientTest </strong>
 *
 * <p><b>Summary</b>: This test suite provides validation for the InfererClient,
 * which is responsible for communicating with the external inference service.</p>
 *
 * <p><b>Test Suite Design</b>: The suite uses Mockito to mock the RestTemplate and
 * InfererProperties. It tests the batch inference logic, ensuring correct URL
 * construction, request handling, and error management for invalid configurations.</p>
 *
 * @author Leonardo Ongaro - 2197813
 */
@ExtendWith(MockitoExtension.class)
public class InfererClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private InfererProperties infererProperties;

    private InfererClient infererClient;

    @BeforeEach
    void setUp() {
        infererClient = new InfererClient(restTemplate, infererProperties);
    }

    /**
     * <p><b>Summary:</b> Verifies successful batch inference.</p>
     * <p><b>Test Case Design:</b> Mocks the RestTemplate to return a successful response
     * when a valid list of documents is sent.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-INFER-001
     * - **Data**: A list containing one document.
     * - **Evaluation**: Expects the returned list to match the mocked response.</p>
     * <p><b>Pre-Condition:</b> InfererProperties returns a valid base URL.</p>
     * <p><b>Post-Condition:</b> The external service is "called" via RestTemplate.</p>
     * <p><b>Expected Results:</b> The method returns the body of the ResponseEntity.</p>
     */
    @Test
    void shouldReturnInferredDocuments() {
        Document doc = new Document("1", "url", "title", "content", null);
        Document enriched = new Document("1", "url", "title", "content", "topic");
        List<Document> requests = List.of(doc);
        List<Document> expected = List.of(enriched);

        when(infererProperties.getBaseUrl()).thenReturn("http://inferer:5050");
        when(restTemplate.exchange(
                eq("http://inferer:5050"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(expected));

        List<Document> result = infererClient.inferBatch(requests);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("topic", result.getFirst().getTopic());
    }

    /**
     * <p><b>Summary:</b> Verifies that an exception is thrown for an invalid base URL.</p>
     * <p><b>Test Case Design:</b> Boundary testing with null, empty, and malformed URLs.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-INFER-002
     * - **Data**: baseUrl is null or "invalid".
     * - **Evaluation**: Expects IllegalStateException.</p>
     * <p><b>Pre-Condition:</b> InfererProperties is configured with an invalid URL.</p>
     * <p><b>Post-Condition:</b> No network call is attempted.</p>
     * <p><b>Expected Results:</b> IllegalStateException is thrown with a descriptive message.</p>
     */
    @Test
    void shouldThrowExceptionForInvalidUrl() {
        when(infererProperties.getBaseUrl()).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> infererClient.inferBatch(List.of()));

        when(infererProperties.getBaseUrl()).thenReturn("   ");
        assertThrows(IllegalStateException.class, () -> infererClient.inferBatch(List.of()));

        when(infererProperties.getBaseUrl()).thenReturn("ftp://invalid");
        assertThrows(IllegalStateException.class, () -> infererClient.inferBatch(List.of()));
    }
}
