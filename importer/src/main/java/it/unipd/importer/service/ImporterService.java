package it.unipd.importer.service;

import it.unipd.importer.ElasticsearchClient_Importer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.InputStream;

/**
 * Service layer responsible for coordinating the import and indexing process.
 *
 * <p>This service delegates the actual indexing logic to the
 * {@link ElasticsearchClient_Importer} and executes it asynchronously.
 */
@Service
public class ImporterService {

    private final ElasticsearchClient_Importer elasticsearchClientIndexer;

    /**
     * Constructs the ImporterService.
     *
     * @param elasticsearchClientIndexer Elasticsearch indexing component
     */
    public ImporterService(ElasticsearchClient_Importer elasticsearchClientIndexer) {
        this.elasticsearchClientIndexer = elasticsearchClientIndexer;
    }

    /**
     * Starts the indexing of articles from the given input stream into Elasticsearch.
     *
     * <p>The method is executed asynchronously, allowing the HTTP request to
     * return immediately while the indexing continues in background.
     *
     * @param file      input stream of the NDJSON file
     * @param indexName name of the Elasticsearch index
     * @throws Exception if an error occurs during indexing
     */
    @Async
    public void indexArticles(InputStream file, String indexName) throws Exception {
        this.elasticsearchClientIndexer.bulkIndexWithContext(file, indexName);
    }
}
