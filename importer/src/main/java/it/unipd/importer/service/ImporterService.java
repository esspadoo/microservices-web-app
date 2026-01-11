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
     * Constructs an ImporterService.
     *
     * @param elasticsearchClientIndexer the Elasticsearch indexing component
     */
    public ImporterService(ElasticsearchClient_Importer elasticsearchClientIndexer) {
        this.elasticsearchClientIndexer = elasticsearchClientIndexer;
    }

    /**
     * Starts the asynchronous indexing of articles from the given file into Elasticsearch.
     *
     * <p>The method updates the job status in {@link #jobStatus} accordingly:
     * "IN_PROGRESS" while running, "COMPLETED" if successful, and
     * "FAILED: <error message>" if an exception occurs.
     * The file is deleted after processing.
     *
     * @param file      the NDJSON file containing the documents to index
     * @param indexName the name of the Elasticsearch index
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
     * Retrieves the current status of an import job.
     *
     * @param jobId the unique identifier of the job
     * @return the status of the job ("IN_PROGRESS", "COMPLETED", "FAILED: <message>")
     *         or "UNKNOWN" if the job ID is not found
     */
    public String getJobStatus(String jobId) {
        return jobStatus.getOrDefault(jobId, "UNKNOWN");
    }
}
