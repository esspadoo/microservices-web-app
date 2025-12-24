package it.unipd.importer;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class ImporterService {

    private final ElasticsearchClient_Importer elasticsearchClientIndexer;

    public ImporterService(ElasticsearchClient_Importer elasticsearchClientIndexer) {
        this.elasticsearchClientIndexer = elasticsearchClientIndexer;
    }

    @Async
    public void indexArticles(InputStream file, String indexName) throws Exception {

        this.elasticsearchClientIndexer.bulkIndexWithContext(file, indexName);

    }
}
