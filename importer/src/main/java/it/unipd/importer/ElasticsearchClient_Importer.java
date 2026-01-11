package it.unipd.importer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;

import co.elastic.clients.util.BinaryData;
import co.elastic.clients.util.ContentType;
import jakarta.annotation.PreDestroy;
import org.elasticsearch.client.RestClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

/**
 * Component responsible for interacting with Elasticsearch for bulk indexing operations.
 *
 * <p>This class reads NDJSON data from an input stream and indexes each JSON document
 * into a specified Elasticsearch index using bulk operations for efficiency.
 */
@Component
public class ElasticsearchClient_Importer {

    /** High-level Elasticsearch client used for index operations */
    public final ElasticsearchClient esClient;

    /** Low-level REST client, used for closing connections */
    private final RestClient restClient;

    /**
     * Constructs an ElasticsearchClient_Importer with the required clients.
     *
     * @param esClient   high-level Elasticsearch client
     * @param restClient low-level REST client
     */
    public ElasticsearchClient_Importer(ElasticsearchClient esClient, RestClient restClient) {
        this.esClient = esClient;
        this.restClient = restClient;
    }

    /**
     * Performs bulk indexing of documents read from an NDJSON input stream.
     *
     * <p>If the specified index does not exist, it is created automatically.
     * Each non-empty line of the input stream must be a valid JSON object.
     *
     * @param inputStream input stream containing NDJSON data
     * @param indexName   name of the Elasticsearch index
     * @throws Exception if the index operation fails
     * @throws IllegalArgumentException if the input contains invalid NDJSON lines
     */
    public void bulkIndexWithContext(InputStream inputStream, String indexName) throws Exception {
        if (!esClient.indices().exists(b -> b.index(indexName)).value()) {
            esClient.indices().create(c -> c.index(indexName));
        }

        try (BulkIngester<BinaryData> ingester = BulkIngester.of(b -> b
                .client(esClient)
                .maxOperations(1000)
                .flushInterval(1, TimeUnit.SECONDS))
        ) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;

                    if (!line.startsWith("{")) {
                        throw new IllegalArgumentException(
                                "Invalid file format: must be NDJSON (one JSON object per line)"
                        );
                    }

                    System.out.println(line);

                    BinaryData data = BinaryData.of(line.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON);

                    ingester.add(op -> op
                            .index(idx -> idx
                                    .index(indexName)
                                    .document(data)
                            )
                    );
                }
            }
        }
    }

    /**
     * Closes the Elasticsearch REST client when the application shuts down.
     *
     * @throws IOException if an error occurs while closing the client
     */
    @PreDestroy
    public void close() throws IOException {
        restClient.close();
    }

}
