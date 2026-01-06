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
 * <p>This controller orchestrates search execution, caching,
 * and inference enrichment.</p>
 */
@RestController
@RequestMapping(value = "api/v1/searcher", produces = "application/json")
public class SearchController {

    /** Service responsible for search execution. */
    private final SearchService searchService;

    /** Service responsible for inference enrichment. */
    private final InfererClient infererClient;

    /** Service responsible for caching search results. */
    private final DocumentService documentService;


    /**
     * Constructs a {@code SearchController} with all required dependencies.
     *
     * <p>Dependencies are injected via constructor injection to ensure
     * immutability, explicitness, and improved testability.</p>
     *
     * @param searchService   service responsible for executing search queries
     *                        against the search backend (e.g. Elasticsearch)
     * @param infererClient   client responsible for enriching search results
     *                        through inference processing
     * @param documentService service responsible for caching and retrieving
     *                        search results
     */
    public SearchController(SearchService searchService, InfererClient infererClient, DocumentService documentService) {
        this.searchService = searchService;
        this.infererClient = infererClient;
        this.documentService = documentService;
    }


    /**
     * Health-check endpoint used to verify service availability.
     *
     * @return static greeting message
     */
    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test, service SEARCHER UP!";
    }

    /**
     * Executes a search query and returns enriched documents.
     *
     * <p>The method follows this workflow:</p>
     * <ol>
     *   <li>Check MongoDB cache</li>
     *   <li>If absent, query Elasticsearch</li>
     *   <li>Send results to inference service</li>
     *   <li>Cache results</li>
     *   <li>Return response</li>
     * </ol>
     *
     * @param query user-provided search query
     * @return HTTP response containing a list of documents
     */
    @GetMapping("/searchDocuments")
    public ResponseEntity<?> searchDocuments(@RequestParam(value = "query") String query) {
        try {
            List<CacheDocument> cacheDocument = documentService.getDocumentsByQuery(query);
            if(cacheDocument.isEmpty()) {
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


