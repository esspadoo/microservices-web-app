package it.unipd.importer.service;

import it.unipd.importer.ElasticsearchClient_Importer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer responsible for coordinating the import and indexing process.
 *
 * <p>This service delegates the actual indexing logic to the
 * {@link ElasticsearchClient_Importer} and executes it asynchronously.
 */
@Service
public class ImporterService {

    private final ElasticsearchClient_Importer elasticsearchClientIndexer;
    private final Map<String, String> jobStatus = new ConcurrentHashMap<>();

    /**
     * Constructs the ImporterService.
     *
     * @param elasticsearchClientIndexer Elasticsearch indexing component
     */
    public ImporterService(ElasticsearchClient_Importer elasticsearchClientIndexer) {
        this.elasticsearchClientIndexer = elasticsearchClientIndexer;
    }

    /**
     * Starts the indexing of articles from the given file into Elasticsearch, handles the job status accordingly.
     *
     * <p>The method is executed asynchronously, allowing the HTTP request to
     * return immediately while the indexing continues in background.
     * The file is deleted after processing.
     *
     * @param file      the NDJSON file to process
     * @param indexName name of the Elasticsearch index
     * @param jobId     unique identifier for the import job
     */
    @Async
    public void indexArticles(File file, String indexName, String jobId) {
        jobStatus.put(jobId, "IN_PROGRESS");
        try (InputStream inputStream = new FileInputStream(file)) {
            this.elasticsearchClientIndexer.bulkIndexWithContext(inputStream, indexName);
            jobStatus.put(jobId, "COMPLETED");
        } catch (Exception e) {
            jobStatus.put(jobId, "FAILED: " + e.getMessage());
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Retrieves the status of an import job.
     *
     * @param jobId the unique identifier of the job
     * @return the current status of the job, or "UNKNOWN" if not found
     */
    public String getJobStatus(String jobId) {
        return jobStatus.getOrDefault(jobId, "UNKNOWN");
    }
}
