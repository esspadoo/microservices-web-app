package it.unipd.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import it.unipd.search.dto.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Spring-managed component providing a high-level abstraction over the
 * official Elasticsearch Java API client.
 *
 * <p>This class is responsible for executing full-text search queries
 * against one or more Elasticsearch indices and mapping the resulting
 * hits to application-level {@link Document} objects.</p>
 *
 * <p>The underlying {@link ElasticsearchClient} is injected and assumed
 * to be already configured by the application context.</p>
 *
 * <p>The search logic relies on a {@code multi_match} query, assigning
 * higher relevance to a primary field while also searching a secondary
 * field.</p>
 *
 * <p>This component is thread-safe, as the underlying Elasticsearch
 * client implementation supports concurrent usage.</p>
 */
@Component
public class ElasticsearchClient_Search {

    /**
     * High-level Elasticsearch API client used to execute search requests.
     */
    private final ElasticsearchClient esClient;

    /**
     * Constructs a new {@code ElasticsearchClient_Search} using the provided
     * Elasticsearch client.
     *
     * @param esClient configured Elasticsearch API client
     */
    public ElasticsearchClient_Search(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * Executes a full-text search query against the specified Elasticsearch indices.
     *
     * <p>The query uses a {@code multi_match} strategy, applying a relevance boost
     * to the primary field compared to the secondary field.</p>
     *
     * <p>Results are deduplicated by collapsing documents on the {@code url} field.
     * Only documents with a non-null source are included in the returned list.</p>
     *
     * @param indexName list of index names to search
     * @param field1 primary field used in the query with boosted relevance
     * @param field2 secondary field used in the query
     * @param queryText textual search query
     * @return list of {@link Document} instances matching the query
     * @throws IOException if an error occurs while communicating with Elasticsearch
     */
    public List<Document> searchDocuments(
            List<String> indexName,
            String field1,
            String field2,
            String queryText
    ) throws IOException {

        List<Document> results = new ArrayList<>();
        String indexes = String.join(",", indexName);

        SearchResponse<Document> response = esClient.search(s -> s
                        .index(indexes)
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields(field1 + "^2", field2)
                                        .query(queryText)
                                )
                        )
                        .collapse(c -> c.field("url")),
                Document.class
        );

        List<Hit<Document>> hits = response.hits().hits();
        for (Hit<Document> hit : hits) {
            if (hit.source() != null) {
                results.add(hit.source());
            }
        }

        return results;
    }
}
