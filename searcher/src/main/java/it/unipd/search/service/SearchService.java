package it.unipd.search.service;

import it.unipd.search.ElasticsearchClient_Search;
import it.unipd.search.dto.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.List;

/**
 * Service layer component responsible for search-related business logic.
 *
 * <p>This service acts as an intermediary between the REST controller
 * and the Elasticsearch client abstraction.</p>
 */
@Service
public class SearchService {

    /**
     * Elasticsearch client wrapper used to execute search queries.
     */
    @Autowired
    private ElasticsearchClient_Search elasticsearchClient_Search;

    /**
     * Executes a search query against predefined Elasticsearch indices.
     *
     * <p>The indices are currently statically defined to avoid user sabotage.</p>
     *
     * @param query user-provided search query
     * @return list of documents matching the query
     * @throws IOException if Elasticsearch communication fails
     */
    public List<Document> searchDocuments(String query) throws IOException {
        List<String> indexName = List.of("guardian", "owi");

        return this.elasticsearchClient_Search.searchDocuments(indexName, "title", "main_content", query);
    }

}
