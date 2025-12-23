package it.unipd.search.api;

import it.unipd.search.client.InfererClient;
import it.unipd.search.service.SearchService;
import it.unipd.search.dto.Document;
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


    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test, service SEARCHER UP!";
    }

    @GetMapping("/searchDocuments")
    public ResponseEntity<?> searchDocuments(@RequestParam(value = "query") String query) {
        try {
            List<Document> resultsElastic = searchService.searchDocuments(query);
            //query to mongodb to check if record is already processed
            //if not i send query to mallet and do inference
            List<Document> results = infererClient.inferBatch(resultsElastic);

            // from result --> inferer che ritorna qui con i topic che poi gestiamo
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}


