package com.softplat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.io.IOException;
import java.util.List;

// Business logic for REST API
@Service
public class SearchService {

    @Autowired
    private ElasticsearchClient_Search elasticsearchClient_Search;

    public final RestClient restClient;

    public SearchService() {
        restClient = RestClient.create();
    }

    public List<Document> searchDocuments(String query) throws IOException {
        //String indexName = elasticsearchClient_Search.getIndexName();

        // PER ORA STATICA
        String indexName = "prova";
        return this.elasticsearchClient_Search.searchDocuments(indexName, "bodyText", query);
    }

}
