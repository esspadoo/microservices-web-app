package it.unipd.importer;

import it.unipd.importer.service.ImporterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * <strong> Class ImporterServiceTest </strong>
 *
 * <p><b>Summary</b>: Provides unit tests for the ImporterService layer using Mockito.
 * Tests focus on the correct delegation of parameters to the Elasticsearch client.</p>
 *
 * <p><b>Test Case Design</b>: The test utilizes MockitoExtension to mock the
 * ElasticsearchClient_Importer dependency. This ensures the service logic is tested
 * in isolation from the infrastructure layer.</p>
 *
 * @author Leonardo Ongaro - 2197813
 */
@ExtendWith(MockitoExtension.class)
class ImporterServiceTest {

    @Mock
    private ElasticsearchClient_Importer elasticsearchClient;

    @InjectMocks
    private ImporterService importerService;

    /**
     * <p><b>Summary:</b> Verifies that the service correctly calls the client method.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SERVICE-001
     * - **Data**: A temporary file and a string "test-index".
     * - **Criteria**: The client's bulkIndexWithContext method must be called exactly once.</p>
     * <p><b>Pre-Condition:</b> Mocks are initialized.</p>
     * <p><b>Expected Results:</b> The client receives the identical InputStream and indexName.</p>
     * @throws Exception if any error occurs during article indexing or during the bulk indexing
     */
    @Test
    void shouldCallClientWithCorrectParameters() throws Exception {
        File tempFile = File.createTempFile("test-import", ".ndjson");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("test data".getBytes());
        }
        String indexName = "test-index";
        String jobId = "test-job-id";

        importerService.indexArticles(tempFile, indexName, jobId);

        verify(elasticsearchClient, times(1)).bulkIndexWithContext(any(InputStream.class), eq(indexName));
    }

    /**
     * <p><b>Summary:</b> Verifies that the service correctly handles the job status UNKNOWN.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SERVICE-002
     * - **Data**: A temporary file and a string "test-index".
     * - **Criteria**: The job status must be coherent with the result of the indexArticles function.</p>
     * <p><b>Pre-Condition:</b> Mocks are initialized.</p>
     * <p><b>Expected Results:</b> The status of a job after completion must be UNKNOWN before calling the function</p>
     * @throws Exception if any error occurs during temp file creation
     */
    @Test
    void verifyJobUnknown() throws Exception {
        File tempFile = File.createTempFile("test-import", ".ndjson");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("test data".getBytes());
        }
        String jobId = "test-job-id";

        String statusBefore = importerService.getJobStatus(jobId);
        assert statusBefore.equals("UNKNOWN");
    }

    /**
     * <p><b>Summary:</b> Verifies that the service correctly handles the job status COMPLETED.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SERVICE-003
     * - **Data**: A temporary file and a string "test-index".
     * - **Criteria**: The job status must be coherent with the result of the indexArticles function.</p>
     * <p><b>Pre-Condition:</b> Mocks are initialized.</p>
     * <p><b>Expected Results:</b> The status of a job after completion must be COMPLETED if the function completes successfully</p>
     * @throws Exception if any error occurs during temp file creation
     */
    @Test
    void verifyJobCompleted() throws Exception {
        File tempFile = File.createTempFile("test-import", ".ndjson");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("test data".getBytes());
        }
        String indexName = "test-index";
        String jobId = "test-job-id";

        importerService.indexArticles(tempFile, indexName, jobId);
        String statusAfter1 = importerService.getJobStatus(jobId);
        assert statusAfter1.equals("COMPLETED");
    }

    /**
     * <p><b>Summary:</b> Verifies that the service correctly handles the job status FAILED.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-SERVICE-004
     * - **Data**: A temporary file and a string "test-index".
     * - **Criteria**: The job status must be coherent with the result of the indexArticles function.</p>
     * <p><b>Pre-Condition:</b> Mocks are initialized.</p>
     * <p><b>Expected Results:</b> The status of a job after completion must be COMPLETED if the function fails</p>
     * @throws Exception if any error occurs during temp file creation
     */
    @Test
    void verifyJobFailed() throws Exception {
        File tempFile = File.createTempFile("test-import", ".ndjson");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("test data".getBytes());
        }
        String jobId = "test-job-id";

        importerService.indexArticles(null, null, jobId);
        String statusAfter2 = importerService.getJobStatus(jobId);
        assert statusAfter2.contains("FAILED");
    }
}
