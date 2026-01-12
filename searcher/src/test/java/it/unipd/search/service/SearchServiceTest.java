package it.unipd.search.service;

import it.unipd.search.ElasticsearchClient_Search;
import it.unipd.search.dto.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * <strong> Class SearchServiceTest </strong>
 *
 * <p><b>Summary</b>: This test suite provides validation for the SearchService,
 * which acts as an intermediary between the controller and the Elasticsearch client.</p>
 *
 * <p><b>Test Suite Design</b>: The suite uses Mockito to mock the ElasticsearchClient_Search.
 * It verifies that the service correctly passes the hardcoded index names and search fields
 * to the client and returns the results.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @Mock
    private ElasticsearchClient_Search elasticsearchClient;

    @InjectMocks
    private SearchService searchService;

    /**
     * <p><b>Summary:</b> Verifies successful document search execution.</p>
     * <p><b>Test Case Design:</b> Mocks the Elasticsearch client to return a list of documents
     * when called with the expected parameters (indices, fields, and query).</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SEARCHSERV-001
     * - **Data**: query="java"
     * - **Evaluation**: Expects the returned list to match the client's output.</p>
     * <p><b>Pre-Condition:</b> Elasticsearch client is mocked.</p>
     * <p><b>Post-Condition:</b> searchDocuments is called on the client with correct parameters.</p>
     * <p><b>Expected Results:</b> The service returns the list of documents from Elasticsearch.</p>
     * @throws IOException if the mocked client throws an exception
     */
    @Test
    void shouldSearchDocuments() throws IOException {
        String query = "java";
        List<String> expectedIndices = List.of("guardian", "owi");
        Document doc = new Document("1", "url", "title", "content", null);
        List<Document> expectedResults = List.of(doc);

        when(elasticsearchClient.searchDocuments(eq(expectedIndices), eq("title"), eq("main_content"), eq(query)))
                .thenReturn(expectedResults);

        List<Document> result = searchService.searchDocuments(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("title", result.getFirst().getTitle());
        verify(elasticsearchClient, times(1))
                .searchDocuments(eq(expectedIndices), eq("title"), eq("main_content"), eq(query));
    }
}
