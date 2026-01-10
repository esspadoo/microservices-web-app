package it.unipd.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import it.unipd.search.config.ElasticsearchProperties;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import it.unipd.search.dto.Document;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Spring-managed component providing a high-level abstraction over the
 * official Elasticsearch Java API client.
 *
 * <p>
 * This class is responsible for:
 * </p>
 * <ul>
 *   <li>Initializing and configuring a connection to an Elasticsearch cluster</li>
 *   <li>Executing full-text search queries over one or more indices</li>
 *   <li>Mapping Elasticsearch search hits to application-level {@link Document} objects</li>
 * </ul>
 *
 * <p>
 * The client connects to an Elasticsearch node using the REST transport layer
 * and the Jackson JSON mapper. The connection endpoint is currently configured
 * to target a containerized Elasticsearch instance.
 * </p>
 *
 * <p>
 * The search logic uses a {@code multi_match} query, boosting relevance for a
 * primary field (e.g. document title) while also searching a secondary field
 * (e.g. main content).
 * </p>
 *
 * <p>
 * This component is thread-safe and can be safely shared across concurrent
 * requests, as the underlying {@link ElasticsearchClient} is designed to be
 * thread-safe.
 * </p>
 *
 */
@Component
public class ElasticsearchClient_Search {

    /**
     * The underlying Elasticsearch API client used to execute search requests.
     */
    private final ElasticsearchClient esClient;


    /**
     * Creates a new {@code elasticsearchClient_Searcher} and initializes the
     * underlying {@link ElasticsearchClient}.
     * <p>
     * The constructor builds a low-level {@link RestClient}, wraps it in an
     * {@link ElasticsearchTransport} using a {@link JacksonJsonpMapper}, and
     * finally creates the high-level API client.
     * </p>
     * <p>
     * The constructor is {@code protected} to restrict uncontrolled instantiation to the
     * same package or subclasses.
     * </p>
     *
     */
    public ElasticsearchClient_Search(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * Executes a search query on the specified Elasticsearch index and returns
     * the matching documents.
     * <p>The query uses a {@code multi_match} strategy, assigning a higher boost
     * to the primary field (the document title) than the main_content.</p>
     * <p>
     * The search is executed against two fields and the textual query is
     * provided verbatim to Elasticsearch.
     * </p>
     * <p>
     * Only documents with a non-null source are included in the result list.
     * Metadata such as document score and ID are intentionally ignored.
     * </p>
     *
     * must not be {@code null}
     * @param indexName the name of the index to search in
     * @param field1 the document field on which the query is executed with a *3 boost
     * @param field2 the document field on which the query is executed
     * @param queryText the textual query value
     * @return a {@link List} of {@link Document} instances representing the
     * sources of the matching Elasticsearch documents
     * @throws IOException if an error occurs while communicating with
     * Elasticsearch
     */
    public List<Document> searchDocuments(List<String> indexName, String field1, String field2, String queryText) throws IOException {

        List<Document> results = new ArrayList<>();
        String indexes = String.join(",", indexName);

        SearchResponse<Document> response = esClient.search(s -> s
                        .index(indexes)
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields(field1 + "^2", field2) // primary boosted
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
