package it.unipd.search.api;

import it.unipd.search.client.InfererClient;
import it.unipd.search.dto.CacheDocument;
import it.unipd.search.service.DocumentService;
import it.unipd.search.service.SearchService;
import it.unipd.search.dto.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing search-related endpoints.
 *
 * <p>This controller handles the execution of search queries, the enrichment
 * of results through an inference service, and the caching of results to
 * improve performance on repeated queries.</p>
 */
@RestController
@RequestMapping(value = "api/v1/searcher", produces = "application/json")
public class SearchController {

    /** Service responsible for executing search queries against the search backend. */
    private final SearchService searchService;

    /** Client responsible for enriching documents using an inference service. */
    private final InfererClient infererClient;

    /** Service responsible for caching and retrieving search results. */
    private final DocumentService documentService;

    /**
     * Constructs a {@code SearchController} with all required dependencies.
     *
     * <p>Constructor-based dependency injection is used to ensure immutability,
     * clarity of dependencies, and easier unit testing.</p>
     *
     * @param searchService   service responsible for performing search operations
     * @param infererClient   client used to enrich search results via inference
     * @param documentService service responsible for persisting and retrieving
     *                        cached search results
     */
    public SearchController(SearchService searchService, InfererClient infererClient, DocumentService documentService) {
        this.searchService = searchService;
        this.infererClient = infererClient;
        this.documentService = documentService;
    }

    /**
     * Simple health-check endpoint.
     *
     * <p>This endpoint can be used to verify that the SEARCHER service is up
     * and reachable.</p>
     *
     * @return a static confirmation message
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello this is a test, service SEARCHER UP!";
    }

    /**
     * Executes a search query and returns a list of enriched documents.
     *
     * <p>The execution flow is as follows:</p>
     * <ol>
     *   <li>Check if results for the given query are present in the cache</li>
     *   <li>If cached results are found, return them immediately</li>
     *   <li>If not cached, execute the search against the search backend</li>
     *   <li>Send retrieved documents to the inference service for enrichment</li>
     *   <li>Store the enriched results in the cache</li>
     *   <li>Return the enriched documents to the client</li>
     * </ol>
     *
     * <p>In case of unexpected errors, an HTTP 500 response is returned
     * containing a simple error message.</p>
     *
     * @param query the user-provided search query string
     * @return an HTTP response containing a list of documents or an error message
     */
    @GetMapping("/searchDocuments")
    public ResponseEntity<?> searchDocuments(@RequestParam(value = "query") String query) {
        try {
            List<CacheDocument> cacheDocument = documentService.getDocumentsByQuery(query);

            if (cacheDocument.isEmpty()) {
                List<Document> resultsElastic = searchService.searchDocuments(query);
                List<Document> results = infererClient.inferBatch(resultsElastic);

                CacheDocument doc = new CacheDocument();
                doc.setQuery(query);
                doc.setDocuments(results);
                documentService.insertDocuments(doc);

                return ResponseEntity.ok(results);
            } else {
                List<Document> documents = cacheDocument.getFirst().getDocuments();
                return ResponseEntity.ok(documents);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
