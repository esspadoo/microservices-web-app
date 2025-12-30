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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchClient_Importer {

    public final ElasticsearchClient esClient;
    private final RestClient restClient;

    @Autowired
    public ElasticsearchClient_Importer(ElasticsearchClient esClient, RestClient restClient) {
        this.esClient = esClient;
        this.restClient = restClient;
    }


    public void bulkIndexWithContext(InputStream inputStream, String indexName) throws Exception {
        if (!esClient.indices().exists(b -> b.index(indexName)).value()) {
            esClient.indices().create(c -> c.index(indexName));
        }

        try(BulkIngester<BinaryData> ingester = BulkIngester.of(b -> b
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
                                "Not valid file's format: it mush be a NDJSON (a JSON for each row)"
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

    @PreDestroy
    public void close() throws IOException {
        restClient.close();
    }

}
