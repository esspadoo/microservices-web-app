package com.softplat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "api/v1/searcher", produces = "application/json")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/searchDocuments")
    public List<Document> searchDocuments(@RequestParam(value = "query") String query) throws IOException {
        return searchService.searchDocuments(query);
    }


}


