package it.unipd.search.service;

import it.unipd.search.ElasticsearchClient_Search;
import it.unipd.search.dto.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.List;

// Business logic for REST API
@Service
public class SearchService {

    @Autowired
    private ElasticsearchClient_Search elasticsearchClient_Search;

    public List<Document> searchDocuments(String query) throws IOException {
        //String indexName = elasticsearchClient_Search.getIndexName();

        // PER ORA STATICA
        List<String> indexName = List.of("guardian", "owi");

        return this.elasticsearchClient_Search.searchDocuments(indexName, "title", "main_content", query);
    }

}
