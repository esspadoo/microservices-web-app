package com.softplat;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Provides a simple search-oriented wrapper around the official Elasticsearch
 * Java API client.
 * <p>
 * This class is responsible for creating and holding an
 * {@link ElasticsearchClient} connected to a local Elasticsearch node and
 * exposing a utility method for performing text-based search queries on a
 * given index.
 * </p>
 * <p>
 * The client connects by default to {@code https://localhost:9200}. Connection
 * details (authentication, SSL configuration, timeouts) are assumed to be
 * handled externally or via default settings.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 * <li>Initialize an {@link ElasticsearchClient} using the REST transport.</li>
 * <li>Execute {@code match} or {@code match_phrase} search queries.</li>
 * <li>Collect and return matching documents.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>
 * {@link ElasticsearchClient} instances are thread-safe. This class can be
 * shared across threads provided that lifecycle management of the underlying
 * REST client is handled appropriately.
 * </p>
 */
@Component
public class ElasticsearchClient_Search {

    /**
     * URL of the Elasticsearch server this client connects to.
     */
    private static final String SERVER_URL = "http://elasticsearch:9200";


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
     * The constructor is {@code protected} to restrict instantiation to the
     * same package or subclasses, suggesting controlled lifecycle or factory
     * usage.
     * </p>
     *
     */

    protected ElasticsearchClient_Search() {
    //public ElasticsearchClient_Search() {
        RestClient restClient = RestClient.builder(HttpHost.create(SERVER_URL)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        // And create the API client
        esClient = new ElasticsearchClient(transport);
    }


    /**
     * Executes a search query on the specified Elasticsearch index and returns
     * the matching documents.
     * <p>
     * Depending on the value of {@code type}, the method performs either:
     * </p>
     * <ul>
     * <li>a {@code match_phrase} query (if {@code type.equals("matchPhrase")} ), or</li>
     * <li>a standard {@code match} query (for any other value of {@code type}).</li>
     * </ul>
     * <p>
     * The search is executed against a single field and the textual query is
     * provided verbatim to Elasticsearch.
     * </p>
     * <p>
     * Only documents with a non-null source are included in the result list.
     * Metadata such as document score and ID are intentionally ignored.
     * </p>
     *
     * must not be {@code null}
     * @param indexName the name of the index to search in
     * @param fieldToSearch the document field on which the query is executed
     * @param queryText the textual query value
     * {@code "matchPhrase"}, a {@code match_phrase} query
     * is used, otherwise a {@code match} query is executed
     * @return a {@link List} of {@link Document} instances representing the
     * sources of the matching Elasticsearch documents
     * @throws IOException if an error occurs while communicating with
     * Elasticsearch
     */
    public List<Document> searchDocuments(String indexName, String fieldToSearch, String queryText) throws IOException {

        List<Document> results = new ArrayList<>();

        SearchResponse<Document> response = esClient.search(s -> s
                        .index(indexName)
                        .query(q -> q.matchPhrase(t -> t.field(fieldToSearch).query(queryText))
                        ),
                Document.class
        );

        /*
        TotalHits total = response.hits().total();

        assert total != null;
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            System.out.println("Number of results: " + total.value());
        } else {
            System.out.println("There are more than " + total.value() + " results");
        }*/

        List<Hit<Document>> hits = response.hits().hits();
        for (Hit<Document> hit : hits) {
//            Document doc = hit.source();

            if (hit.source() != null) {
                results.add(hit.source());
            }
  //          results.add(doc);
  //          System.out.println("Found document " + doc.getId() + ", score " + hit.score());
        }

        return results;
    }



}
