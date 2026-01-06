package it.unipd.importer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * <strong> Class ElasticsearchClient_ImporterTest </strong>
 *
 * <p><b>Summary</b>: This test suite validates the logic of the ElasticsearchClient_Importer,
 * specifically focusing on the interaction with the Elasticsearch Java API Client and
 * data validation during the bulk indexing process.</p>
 *
 * <p><b>Test Suite Design</b>: The suite uses the Mockito Extension to perform unit testing
 * in isolation. It mocks the low-level Elasticsearch infrastructure (Client, Transport, and Indices)
 * to simulate different cluster states (e.g., index existence) without requiring a running
 * Elasticsearch instance. This ensures the tests are fast and deterministic.</p>
 *
 * @author Leonardo Ongaro - 2197813
 */
@ExtendWith(MockitoExtension.class)
public class ElasticsearchClient_ImporterTest {

    @Mock
    private ElasticsearchClient esClient; // This mock replaces the real client

    @Mock
    private ElasticsearchTransport transport; // Add this mock

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @InjectMocks
    private ElasticsearchClient_Importer importer;

    /**
     * <p><b>Summary:</b> Verifies that the importer correctly identifies and rejects invalid
     * data formats during bulk indexing.</p>
     * <p><b>Test Case Design:</b> Negative testing using state-based stubbing. The test
     * configures the mocked client to report that the target index already exists, then
     * provides malformed input to trigger validation logic.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-ES-IMPORTER-001
     * - **Prerequisites**: Mockito context initialized.
     * - **Data**: A raw string "INVALID_DATA" passed as an InputStream and "test-index" as target.
     * - **Evaluation**: Expects an IllegalArgumentException to be thrown when parsing the invalid stream.</p>
     * <p><b>Pre-Condition:</b> The Elasticsearch environment is mocked to return a successful
     * 'exists' check for the index.</p>
     * <p><b>Post-Condition:</b> The system prevents the invalid operation and throws a
     * specific exception, ensuring no malformed requests are sent to the transport layer.</p>
     * <p><b>Expected Results:</b> The call to bulkIndexWithContext results in an
     * IllegalArgumentException.</p>
     * @throws Exception if unexpected errors occur during setup or execution
     */
    @Test
    public void bulkIndex_InvalidFormat_ThrowsException() throws Exception {
        // We stub the behavior so the code thinks the index already exists
        when(esClient._transport()).thenReturn(transport);

        when(esClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(java.util.function.Function.class)))
                .thenReturn(new co.elastic.clients.transport.endpoints.BooleanResponse(true));

        String invalidData = "INVALID_DATA";
        InputStream is = new ByteArrayInputStream(invalidData.getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> importer.bulkIndexWithContext(is, "test-index"));
    }
}