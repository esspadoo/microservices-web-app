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
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
 * @author Leonardo Ongaro - 2197813
 */
@ExtendWith(MockitoExtension.class)
public class ElasticsearchClient_SearchTest {

    @Mock
    private ElasticsearchClient esClient;

    private ElasticsearchClient_Search elasticsearchClient_Search;

    @BeforeEach
    void setUp() {
        elasticsearchClient_Search = new ElasticsearchClient_Search(esClient);
        // Inject the mock client into the protected field using ReflectionTestUtils
        ReflectionTestUtils.setField(elasticsearchClient_Search, "esClient", esClient);
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
    void shouldReturnDocumentsFromSearchResponse() throws IOException {
        Document doc = new Document("1", "url", "title", "content", null);

        // Mocking the complex nested structure of Elasticsearch SearchResponse
        SearchResponse<Document> response = mock(SearchResponse.class);
        HitsMetadata<Document> hitsMetadata = mock(HitsMetadata.class);
        Hit<Document> hit = mock(Hit.class);

        when(esClient.search(any(Function.class), any(Class.class))).thenReturn(response);
        when(response.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(hit.source()).thenReturn(doc);

        List<Document> result = elasticsearchClient_Search.searchDocuments(
                List.of("index"), "field1", "field2", "query"
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("title", result.getFirst().getTitle());
    }

    /**
     * <p><b>Summary:</b> Verifies that null sources are ignored.</p>
     * <p><b>Test Case Design:</b> Mocks a hit with a null source and ensures it's not added to results.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-ESCLIENT-002
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
        SearchResponse<Document> response = mock(SearchResponse.class);
        HitsMetadata<Document> hitsMetadata = mock(HitsMetadata.class);
        Hit<Document> hit = mock(Hit.class);

        when(esClient.search(any(Function.class), any(Class.class))).thenReturn(response);
        when(response.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(hit.source()).thenReturn(null);

        List<Document> result = elasticsearchClient_Search.searchDocuments(
                List.of("index"), "field1", "field2", "query"
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
