package it.unipd.search.service;

import it.unipd.search.ElasticsearchClient_Search;
import it.unipd.search.dto.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service layer component responsible for search-related business logic.
 *
 * <p>This service acts as an intermediary between the REST layer and
 * the Elasticsearch client abstraction, encapsulating search execution
 * details.</p>
 */
@Service
public class SearchService {

    /**
     * Elasticsearch client wrapper used to execute search queries.
     */
    private final ElasticsearchClient_Search elasticsearchClient_Search;

    /**
     * Constructs a {@code SearchService} with the required Elasticsearch client.
     *
     * @param elasticsearchClient_Search client wrapper used to perform
     *                                   search operations on Elasticsearch
     */
    public SearchService(ElasticsearchClient_Search elasticsearchClient_Search) {
        this.elasticsearchClient_Search = elasticsearchClient_Search;
    }

    /**
     * Executes a search query against predefined Elasticsearch indices.
     *
     * <p>The target indices are statically defined to restrict searches
     * to known and controlled data sources.</p>
     *
     * @param query the user-provided search query string
     * @return list of documents matching the query
     * @throws IOException if communication with Elasticsearch fails
     */
    public List<Document> searchDocuments(String query) throws IOException {
        List<String> indexName = List.of("guardian", "owi");

        return this.elasticsearchClient_Search.searchDocuments(
                indexName,
                "title",
                "main_content",
                query
        );
    }
}
