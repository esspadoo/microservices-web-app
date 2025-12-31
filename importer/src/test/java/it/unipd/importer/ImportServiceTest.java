package it.unipd.importer;

import it.unipd.importer.service.ImporterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
     * - **Data**: A ByteArrayInputStream and a string "test-index".
     * - **Criteria**: The client's bulkIndexWithContext method must be called exactly once.</p>
     * <p><b>Pre-Condition:</b> Mocks are initialized.</p>
     * <p><b>Expected Results:</b> The client receives the identical InputStream and indexName.</p>
     */
    @Test
    void shouldCallClientWithCorrectParameters() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
        String indexName = "test-index";

        importerService.indexArticles(inputStream, indexName);

        verify(elasticsearchClient, times(1)).bulkIndexWithContext(inputStream, indexName);
    }
}