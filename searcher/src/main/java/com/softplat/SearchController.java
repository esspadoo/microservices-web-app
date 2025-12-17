package com.softplat;

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

    @GetMapping("/searchDocuments")
    public ResponseEntity<?> searchDocuments(@RequestParam(value = "query") String query) {
        try {
            List<Document> results = searchService.searchDocuments(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            //ALTERNATIVA:
            //throw new RuntimeException(e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}


