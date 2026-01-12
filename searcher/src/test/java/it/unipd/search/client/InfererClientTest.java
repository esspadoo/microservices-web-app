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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
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
     * <p><b>Summary:</b> Tests failure when the base URL is null.</p>
     * <p><b>Test Case Design:</b> Configure the properties mock to return {@code null} for the base URL.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-002
     * - **Prerequisites**: InfererProperties returns null.
     * - **Data**: Valid document list.
     * - **Evaluation**: Verifies that an IllegalStateException is thrown with the correct message.</p>
     * <p><b>Pre-Condition:</b> Configuration for inferer.base-url is missing.</p>
     * <p><b>Post-Condition:</b> No HTTP call is attempted.</p>
     * <p><b>Expected Results:</b> Throws {@link IllegalStateException} containing "Invalid inferer.base-baseUrl1".</p>
     */
    @Test
    void testInferBatch_Failure_NullUrl() {
        List<Document> sampleDocs = List.of(new Document("id", "title", "url", "main_content", null));
        when(infererProperties.getBaseUrl()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                infererClient.inferBatch(sampleDocs)
        );

        assertTrue(exception.getMessage().contains("null"));
        verifyNoInteractions(restTemplate);
    }

    /**
     * <p><b>Summary:</b> Tests failure when the base URL is null.</p>
     * <p><b>Test Case Design:</b> Configure the properties mock to return {@code null} for the base URL.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-003
     * - **Prerequisites**: InfererProperties returns null.
     * - **Data**: Valid document list.
     * - **Evaluation**: Verifies that an IllegalStateException is thrown with the correct message.</p>
     * <p><b>Pre-Condition:</b> Configuration for inferer.base-url is missing.</p>
     * <p><b>Post-Condition:</b> No HTTP call is attempted.</p>
     * <p><b>Expected Results:</b> Throws {@link IllegalStateException} containing "Invalid inferer.base-baseUrl1".</p>
     */
    @Test
    void testInfererBatch_UrlNull() {
        when(infererProperties.getBaseUrl()).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> infererClient.inferBatch(List.of()));
        verifyNoInteractions(restTemplate);
    }

    /**
     * <p><b>Summary:</b> Tests failure when the base URL is blank.</p>
     * <p><b>Test Case Design:</b> Configure the properties mock to return an empty string.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-004
     * - **Prerequisites**: InfererProperties returns "".
     * - **Data**: Valid document list.
     * - **Evaluation**: Verifies that a blank URL triggers the validation logic.</p>
     * <p><b>Pre-Condition:</b> Configuration is empty.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Throws {@link IllegalStateException}.</p>
     */
    @Test
    void testInfererBatch_UrlBlank() {
        when(infererProperties.getBaseUrl()).thenReturn("    ");
        assertThrows(IllegalStateException.class, () -> infererClient.inferBatch(List.of()));
        verifyNoInteractions(restTemplate);
    }

    /**
     * <p><b>Summary:</b> Tests failure when the URL does not start with http.</p>
     * <p><b>Test Case Design:</b> Provide a malformed URL protocol (e.g., "ftp://...").</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-005
     * - **Prerequisites**: InfererProperties returns "ftp://invalid-url".
     * - **Data**: Valid document list.
     * - **Evaluation**: Verifies the protocol validation check.</p>
     * <p><b>Pre-Condition:</b> Incorrect protocol in configuration.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Throws {@link IllegalStateException}.</p>
     */
    @Test
    void testInferBatch_InvalidProtocol() {
        when(infererProperties.getBaseUrl()).thenReturn("ftp://invalid");
        assertThrows(IllegalStateException.class, () -> infererClient.inferBatch(List.of()));
        verifyNoInteractions(restTemplate);
    }

    /**
     * <p><b>Summary:</b> Tests handling of REST client exceptions (e.g., Connection Timeout).</p>
     * <p><b>Test Case Design:</b> Mock {@code restTemplate.exchange} to throw a {@link RestClientException}.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-006
     * - **Prerequisites**: Valid URL; RestTemplate throws exception.
     * - **Data**: Valid document list.
     * - **Evaluation**: Verifies that the client propagates the network-level exception.</p>
     * <p><b>Pre-Condition:</b> The external service is down or timing out.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> The method propagates the {@link RestClientException}.</p>
     */
    @Test
    void testInferBatch_Failure_ConnectionError() {
        List<Document> sampleDocs = List.of(new Document("id", "title", "url", "main_content", null));
        String validUrl = "http://inferer:5050";
        when(infererProperties.getBaseUrl()).thenReturn(validUrl);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Connection Refused"));

        assertThrows(RestClientException.class, () -> infererClient.inferBatch(sampleDocs));
    }

    /**
     * <p><b>Summary:</b> Tests behavior when the inference service returns an empty body.</p>
     * <p><b>Test Case Design:</b> Return a ResponseEntity with a {@code null} body.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-007
     * - **Prerequisites**: Valid URL and successful call.
     * - **Data**: Valid document list.
     * - **Evaluation**: Verifies how the client handles a null response body from the API.</p>
     * <p><b>Pre-Condition:</b> API responds with 200 OK but no content.</p>
     * <p><b>Post-Condition:</b> Returns null to the caller.</p>
     * <p><b>Expected Results:</b> The method returns {@code null} without throwing an exception.</p>
     */
    @Test
    void testInferBatch_EmptyResponseBody() {
        List<Document> sampleDocs = List.of(new Document("id", "title", "url", "main_content", null));
        String validUrl = "http://inferer:5050";
        when(infererProperties.getBaseUrl()).thenReturn(validUrl);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>((Object) null, HttpStatus.OK));

        List<Document> result = infererClient.inferBatch(sampleDocs);

        assertNull(result);
    }
}
