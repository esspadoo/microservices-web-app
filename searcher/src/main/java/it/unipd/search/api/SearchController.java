package it.unipd.search.api;

import it.unipd.search.client.InfererClient;
import it.unipd.search.dto.CacheDocument;
import it.unipd.search.service.DocumentService;
import it.unipd.search.service.SearchService;
import it.unipd.search.dto.Document;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/searcher", produces = "application/json")
public class SearchController {

    @Autowired
    private SearchService searchService;
    @Autowired
    private InfererClient infererClient;

    private final DocumentService documentService;

    public SearchController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test, service SEARCHER UP!";
    }

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
                List<Document> results = infererClient.inferBatch(documents);
                return ResponseEntity.ok(results);
            }

            // from result --> inferer che ritorna qui con i topic che poi gestiamo
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}


