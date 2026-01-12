package it.unipd.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import it.unipd.search.dto.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <strong> Class ElasticsearchClient_SearchTest </strong>
 *
 * <p><b>Summary</b>: This test suite provides validation for the ElasticsearchClient_Search,
 * which is a high-level wrapper around the official Elasticsearch Java API client.</p>
 *
 * <p><b>Test Suite Design</b>: The suite uses Mockito to mock the complex ElasticsearchClient
 * and its nested response structures. It focuses on verifying the mapping logic from
 * Elasticsearch search hits to application-level Document objects.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
public class ElasticsearchClient_SearchTest {

    @Mock
    private ElasticsearchClient esClient;

    @Mock
    private SearchResponse<Document> response;

    @Mock
    private HitsMetadata<Document> hitsMetadata;

    private ElasticsearchClient_Search searchClient;

    @BeforeEach
    void setUp() {
        searchClient = new ElasticsearchClient_Search(esClient);
        // Inject the mock client into the protected field using ReflectionTestUtils
        ReflectionTestUtils.setField(searchClient, "esClient", esClient);
    }

    /**
     * <p><b>Summary:</b> Verifies successful mapping of Elasticsearch hits to Documents.</p>
     * <p><b>Test Case Design:</b> Mocks the search response to contain a list of hits
     * and ensures the client extracts the source documents correctly.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-ESCLIENT-001
     * - **Data**: Mocked SearchResponse with one hit.
     * - **Evaluation**: Expects the returned list to contain the source document.</p>
     * <p><b>Pre-Condition:</b> ElasticsearchClient is mocked to return a valid response.</p>
     * <p><b>Post-Condition:</b> Hits are processed and sources are extracted.</p>
     * <p><b>Expected Results:</b> The method returns a list containing the mocked document.</p>
     * @throws IOException if the mocked client throws an exception
     */
    @Test
    @SuppressWarnings("unchecked")
    void testSearchDocuments_Success() throws IOException {
        Document doc = new Document("1", "url", "title", "content", null);

        Hit<Document> hit = mock(Hit.class);

        when(esClient.search(any(Function.class), any(Class.class))).thenReturn(response);
        when(response.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(hit.source()).thenReturn(doc);

        List<Document> result = searchClient.searchDocuments(
                List.of("index"), "field1", "field2", "query"
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("title", result.getFirst().getTitle());
    }

    /**
     * <p><b>Summary:</b> Tests behavior when no documents match the query.</p>
     * <p><b>Test Case Design:</b> Mock the client to return a response with an empty hits list.</p>
     * <p><b>Test Description:</b> 
     * - **ID**: TC-ES-002
     * - **Prerequisites**: Mocked empty HitsMetadata.
     * - **Data**: Valid search parameters.
     * - **Evaluation**: Ensures the loop completes safely and returns an empty list.</p>
     * <p><b>Pre-Condition:</b> Elasticsearch finds zero matches.</p>
     * <p><b>Post-Condition:</b> Returns an empty ArrayList.</p>
     * <p><b>Expected Results:</b> Result list is empty and not null.</p>
     * @throws IOException if the client fails
     */
    @Test
    @SuppressWarnings("unchecked")
    void testSearchDocuments_NoMatches() throws IOException {
        when(hitsMetadata.hits()).thenReturn(Collections.emptyList());
        when(response.hits()).thenReturn(hitsMetadata);
        when(esClient.search(any(Function.class), eq(Document.class))).thenReturn(response);

        List<Document> results = searchClient.searchDocuments(
                List.of("index"), "f1", "f2", "query"
        );

        assertTrue(results.isEmpty());
    }

    /**
     * <p><b>Summary:</b> Tests failure due to network or Elasticsearch communication error.</p>
     * <p><b>Test Case Design:</b> Force the mocked client to throw an {@link IOException}.</p>
     * <p><b>Test Description:</b> 
     * - **ID**: TC-ES-003
     * - **Prerequisites**: Mocked ElasticsearchClient configured to fail.
     * - **Data**: Standard search parameters.
     * - **Evaluation**: Verifies that communication errors are propagated to the caller.</p>
     * <p><b>Pre-Condition:</b> Connection refused or timeout occurs.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Method throws an {@link IOException}.</p>
     * @throws IOException expected outcome
     */
    @Test
    @SuppressWarnings("unchecked")
    void testSearchDocuments_Failure_IOException() throws IOException {
        when(esClient.search(any(Function.class), eq(Document.class)))
                .thenThrow(new IOException("Elasticsearch unreachable"));

        assertThrows(IOException.class, () ->
                searchClient.searchDocuments(List.of("i"), "f1", "f2", "q")
        );
    }

    /**
     * <p><b>Summary:</b> Tests failure when an empty list of indices is provided.</p>
     * <p><b>Test Case Design:</b> Pass an empty list to {@code indexName}.</p>
     * <p><b>Test Description:</b> 
     * - **ID**: TC-ES-004
     * - **Prerequisites**: None.
     * - **Data**: Empty List for indexName.
     * - **Evaluation**: Verifies that the client handles an empty index string (result of {@code String.join}).</p>
     * <p><b>Pre-Condition:</b> None.</p>
     * <p><b>Post-Condition:</b> Elasticsearch may throw an error for empty index paths.</p>
     * <p><b>Expected Results:</b> The code proceeds to call the client with an empty string "", which may throw a client-level exception.</p>
     * @throws IOException if the client fails
     */
    @Test
    @SuppressWarnings("unchecked")
    void testSearchDocuments_EmptyIndices() throws IOException {
        when(hitsMetadata.hits()).thenReturn(Collections.emptyList());
        when(response.hits()).thenReturn(hitsMetadata);
        when(esClient.search(any(Function.class), eq(Document.class))).thenReturn(response);

        assertDoesNotThrow(() ->
                searchClient.searchDocuments(Collections.emptyList(), "f1", "f2", "q")
        );
    }

    /**
     * <p><b>Summary:</b> Tests resilience against null responses from the client.</p>
     * <p><b>Test Case Design:</b> Mock the client to return {@code null} instead of a SearchResponse.</p>
     * <p><b>Test Description:</b> 
     * - **ID**: TC-ES-005
     * - **Prerequisites**: Client returns null.
     * - **Data**: Standard search parameters.
     * - **Evaluation**: Determines if the class is susceptible to a NullPointerException.</p>
     * <p><b>Pre-Condition:</b> API response is malformed or null.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> The method throws a {@link NullPointerException} when attempting to access {@code response.hits()}.</p>
     */
    @Test
    @SuppressWarnings("unchecked")
    void testSearchDocuments_NullResponse() throws IOException {
        when(esClient.search(any(Function.class), eq(Document.class))).thenReturn(null);

        assertThrows(NullPointerException.class, () ->
                searchClient.searchDocuments(List.of("i"), "f1", "f2", "q")
        );
    }

    /**
     * <p><b>Summary:</b> Verifies that null sources are ignored.</p>
     * <p><b>Test Case Design:</b> Mocks a hit with a null source and ensures it's not added to results.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-ESCLIENT-006
     * - **Data**: Mocked hit with null source.
     * - **Evaluation**: Expects an empty list.</p>
     * <p><b>Pre-Condition:</b> Hit source is null.</p>
     * <p><b>Post-Condition:</b> Null source is filtered out.</p>
     * <p><b>Expected Results:</b> The method returns an empty list.</p>
     * @throws IOException if the mocked client throws an exception
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldIgnoreNullSources() throws IOException {
        Hit<Document> hit = mock(Hit.class);

        when(esClient.search(any(Function.class), any(Class.class))).thenReturn(response);
        when(response.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(hit.source()).thenReturn(null);

        List<Document> result = searchClient.searchDocuments(
                List.of("index"), "field1", "field2", "query"
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
