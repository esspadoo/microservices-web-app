package it.unipd.search.api;

import it.unipd.search.client.InfererClient;
import it.unipd.search.dto.CacheDocument;
import it.unipd.search.dto.Document;
import it.unipd.search.repository.DocumentRepository;
import it.unipd.search.service.DocumentService;
import it.unipd.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <strong> Class SearchControllerTest </strong>
 *
 * <p><b>Summary</b>: This test suite provides comprehensive validation for the Search Controller
 * REST endpoints. It focuses on the document search functionality, including health checks
 * and the search workflow involving caching and inference enrichment.</p>
 *
 * <p><b>Test Suite Design</b>: The suite uses Spring Boot's @WebMvcTest to isolate the web layer.
 * It mocks the SearchService, InfererClient, and DocumentService to avoid loading the full
 * microservice context or external dependencies (like Elasticsearch, MongoDB, or the Inferer service)
 * during this slice test. The tests verify correct HTTP status codes, response bodies, and
 * the orchestration logic of the controller.</p>
 *
 * @author Leonardo Ongaro - 2197813
 */
@WebMvcTest(SearchController.class)
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private InfererClient infererClient;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private DocumentRepository documentRepository;

    /**
     * <p><b>Summary:</b> Verifies the health-check endpoint.</p>
     * <p><b>Test Case Design:</b> Simple GET request to the /hello endpoint.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SEARCH-001
     * - **Prerequisites**: Spring context loaded for SearchController.
     * - **Data**: None.
     * - **Evaluation**: Expects HTTP 200 OK and the static greeting message.</p>
     * <p><b>Pre-Condition:</b> The REST endpoint is active.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> The controller returns a 200 OK status and the body text "Hello this is a test, service SEARCHER UP!".</p>
     * @throws Exception if any error occurs during MockMvc execution
     */
    @Test
    void shouldReturnHelloMessage() throws Exception {
        mockMvc.perform(get("/api/v1/searcher/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello this is a test, service SEARCHER UP!"));
    }

    /**
     * <p><b>Summary:</b> Verifies the search workflow when results are found in the cache.</p>
     * <p><b>Test Case Design:</b> Mocks DocumentService to return a cached entry and InfererClient to return enriched documents.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SEARCH-002
     * - **Data**: query="test"
     * - **Evaluation**: Expects HTTP 200 OK and a JSON list of enriched documents.</p>
     * <p><b>Pre-Condition:</b> DocumentService is mocked to return a non-empty list for the query.</p>
     * <p><b>Post-Condition:</b> The request is processed using the cache.</p>
     * <p><b>Expected Results:</b> System returns 200 OK and the inferred documents.</p>
     * @throws Exception if any error occurs during MockMvc execution
     */
    @Test
    void shouldReturnDocumentsFromCache() throws Exception {
        Document doc = new Document("1", "http://test.com", "Title", "Content", null);
        CacheDocument cacheDoc = new CacheDocument();
        cacheDoc.setQuery("test");
        cacheDoc.setDocuments(List.of(doc));

        Document enrichedDoc = new Document("1", "http://test.com", "Title", "Content", "Topic");

        when(documentService.getDocumentsByQuery("test")).thenReturn(List.of(cacheDoc));
        when(infererClient.inferBatch(anyList())).thenReturn(List.of(enrichedDoc));

        mockMvc.perform(get("/api/v1/searcher/searchDocuments").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'id':'1','title':'Title','topic':'Topic'}]"));
    }

    /**
     * <p><b>Summary:</b> Verifies the search workflow when results are NOT found in the cache.</p>
     * <p><b>Test Case Design:</b> Mocks DocumentService to return empty, SearchService to return results from Elastic, and InfererClient to enrich them.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SEARCH-003
     * - **Data**: query="new"
     * - **Evaluation**: Expects HTTP 200 OK, results from Elastic, and a call to cache the results.</p>
     * <p><b>Pre-Condition:</b> DocumentService returns empty list for the query.</p>
     * <p><b>Post-Condition:</b> Results are fetched from Elastic, enriched, and cached.</p>
     * <p><b>Expected Results:</b> System returns 200 OK and the enriched documents.</p>
     * @throws Exception if any error occurs during MockMvc execution or during the document searching
     */
    @Test
    void shouldReturnDocumentsFromElasticWhenCacheEmpty() throws Exception {
        Document elasticDoc = new Document("2", "http://elastic.com", "Elastic Title", "Elastic Content", null);
        Document enrichedDoc = new Document("2", "http://elastic.com", "Elastic Title", "Elastic Content", "Elastic Topic");

        when(documentService.getDocumentsByQuery("new")).thenReturn(Collections.emptyList());
        when(searchService.searchDocuments("new")).thenReturn(List.of(elasticDoc));
        when(infererClient.inferBatch(anyList())).thenReturn(List.of(enrichedDoc));

        mockMvc.perform(get("/api/v1/searcher/searchDocuments").param("query", "new"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{'id':'2','title':'Elastic Title','topic':'Elastic Topic'}]"));
    }

    /**
     * <p><b>Summary:</b> Verifies error handling when an exception occurs during search.</p>
     * <p><b>Test Case Design:</b> Mocks a service to throw an exception.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SEARCH-004
     * - **Data**: query="error"
     * - **Evaluation**: Expects HTTP 500 Internal Server Error.</p>
     * <p><b>Pre-Condition:</b> DocumentService throws an exception.</p>
     * <p><b>Post-Condition:</b> Error is caught and returned as a response.</p>
     * <p><b>Expected Results:</b> System returns 500 Internal Server Error with error message.</p>
     * @throws Exception if any error occurs during MockMvc execution
     */
    @Test
    void shouldReturn500OnError() throws Exception {
        when(documentService.getDocumentsByQuery(anyString())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/searcher/searchDocuments").param("query", "error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{'error':'Database error'}"));
    }
}
