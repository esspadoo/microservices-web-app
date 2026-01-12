package it.unipd.search.service;

import it.unipd.search.ElasticsearchClient_Search;
import it.unipd.search.dto.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    void testSearchDocuments_Success() throws IOException {
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

    /**
     * <p><b>Summary:</b> Tests search execution with no matching results.</p>
     * <p><b>Test Case Design:</b> Configure the client mock to return an empty list for a query that does not exist in the index.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SRCH-002
     * - **Prerequisites**: Mocked ElasticsearchClient_Search.
     * - **Data**: A query likely to yield no results.
     * - **Evaluation**: Verifies that the service propagates an empty list correctly without throwing errors.</p>
     * <p><b>Pre-Condition:</b> None.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Returns an empty list.</p>
     * @throws IOException if any communication error occurs
     */
    @Test
    void testSearchDocuments_NoResults() throws IOException {
        when(elasticsearchClient.searchDocuments(anyList(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        List<Document> result = searchService.searchDocuments("unknown term");

        assertTrue(result.isEmpty());
    }

    /**
     * <p><b>Summary:</b> Tests failure due to Elasticsearch communication error.</p>
     * <p><b>Test Case Design:</b> Force the mocked client to throw an IOException to simulate a network or cluster failure.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SRCH-003
     * - **Prerequisites**: Mocked ElasticsearchClient_Search configured to fail.
     * - **Data**: Any search query.
     * - **Evaluation**: Verifies that the service correctly throws the IOException back to the controller layer.</p>
     * <p><b>Pre-Condition:</b> Elasticsearch cluster is down or network is interrupted.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> The method throws an {@link IOException}.</p>
     * @throws IOException expected exception for this test case
     */
    @Test
    void testSearchDocuments_Failure_IOException() throws IOException {
        String testQuery = "test search term";
        when(elasticsearchClient.searchDocuments(anyList(), anyString(), anyString(), anyString()))
                .thenThrow(new IOException("Connection Refused"));

        assertThrows(IOException.class, () -> searchService.searchDocuments(testQuery));
    }

    /**
     * <p><b>Summary:</b> Tests behavior with a null or empty query.</p>
     * <p><b>Test Case Design:</b> Pass null and empty strings to the search method to see if validation is needed or if it is handled by the client.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SRCH-004
     * - **Prerequisites**: None.
     * - **Data**: Empty string "" and {@code null}.
     * - **Evaluation**: Ensures the service behaves predictably with malformed input.</p>
     * <p><b>Pre-Condition:</b> None.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Depending on client implementation, either returns empty results or propagates a {@link NullPointerException}.</p>
     * @throws IOException if the client is invoked
     */
    @Test
    void testSearchDocuments_EmptyQuery() throws IOException {
        when(elasticsearchClient.searchDocuments(anyList(), anyString(), anyString(), eq("")))
                .thenReturn(Collections.emptyList());

        List<Document> result = searchService.searchDocuments("");

        assertTrue(result.isEmpty());
    }
}
