package it.unipd.searcher.service;

import it.unipd.searcher.ElasticsearchClient_Search;
import it.unipd.searcher.dto.Document;
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
        String indexName = "prova";

        return this.elasticsearchClient_Search.searchDocuments(indexName, "title", query);
    }

}
