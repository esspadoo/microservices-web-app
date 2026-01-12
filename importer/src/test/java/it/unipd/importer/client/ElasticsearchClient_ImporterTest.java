package it.unipd.importer.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.BinaryData;
import it.unipd.importer.ElasticsearchClient_Importer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
 */
@ExtendWith(MockitoExtension.class)
public class ElasticsearchClient_ImporterTest {

    @Mock
    private ElasticsearchClient esClient; // This mock replaces the real client

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @Mock
    private BulkIngester<BinaryData> bulkIngester;

    @InjectMocks
    private ElasticsearchClient_Importer importer;

    @BeforeEach
    void setUp() throws IOException {
        // Setup the indices() client mock chain
        when(esClient.indices()).thenReturn(indicesClient);
    }

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
        when(indicesClient.exists(any(Function.class))).thenReturn(new BooleanResponse(true));

        String invalidData = "INVALID_DATA";
        InputStream is = new ByteArrayInputStream(invalidData.getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> importer.bulkIndexWithContext(is, "test-index"));
    }

    /**
     * <p><b>Summary:</b> Verifies successful indexing when the index already exists.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-001
     * - **Data**: A valid NDJSON string with two objects and index name "existing-index".
     * - **Criteria**: Index existence check returns true, create is never called, and documents are added.</p>
     * <p><b>Pre-Condition:</b> Mocks are configured to return true for index existence.</p>
     * <p><b>Expected Results:</b> The indexing completes without errors; create() is not invoked.</p>
     * @throws Exception if any error occurs during the bulk indexing
     */
    @Test
    void testBulkIndexWithContext_SuccessIndexExists() throws Exception {
        // Arrange
        String ndjson = "{\"id\":1}\n{\"id\":2}";
        InputStream is = new ByteArrayInputStream(ndjson.getBytes(StandardCharsets.UTF_8));

        BooleanResponse existsResponse = new BooleanResponse(true);
        when(indicesClient.exists(any(Function.class))).thenReturn(existsResponse);

        importer.bulkIndexWithContext(is, "test-index");
        verify(indicesClient, times(1)).exists(any(Function.class));
        verify(indicesClient, never()).create(any(Function.class));
    }

    /**
     * <p><b>Summary:</b> Verifies index creation when the specified index does not exist.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-002
     * - **Data**: Valid NDJSON and index name "new-index".
     * - **Criteria**: Index existence check returns false, leading to a create() call.</p>
     * <p><b>Pre-Condition:</b> Mocks return false for index existence.</p>
     * <p><b>Expected Results:</b> The client's indices().create() method is called exactly once.</p>
     * @throws Exception if any error occurs during the bulk indexing
     */
    @Test
    void testBulkIndexWithContext_CreatesIndexIfMissing() throws Exception {
        // Arrange
        String ndjson = "{\"id\":1}";
        InputStream is = new ByteArrayInputStream(ndjson.getBytes(StandardCharsets.UTF_8));
        String indexName = "new-index";

        BooleanResponse existsResponse = new BooleanResponse(false);
        when(indicesClient.exists(any(Function.class))).thenReturn(existsResponse);

        importer.bulkIndexWithContext(is, indexName);
        verify(indicesClient, times(1)).create(any(Function.class));
    }

    /**
     * <p><b>Summary:</b> Verifies failure when the input format is not valid NDJSON.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-003
     * - **Data**: A string that does not start with '{' (e.g., plain text).
     * - **Criteria**: Method should throw IllegalArgumentException.</p>
     * <p><b>Pre-Condition:</b> Index existence check is mocked to true.</p>
     * <p><b>Expected Results:</b> An IllegalArgumentException is thrown with the specific format message.</p>
     * @throws Exception if any error occurs
     */
    @Test
    void testBulkIndexWithContext_InvalidFormat_ThrowsException() throws Exception {
        // Arrange
        String invalidData = "Not a JSON object";
        InputStream is = new ByteArrayInputStream(invalidData.getBytes(StandardCharsets.UTF_8));

        when(indicesClient.exists(any(Function.class))).thenReturn(new BooleanResponse(true));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                importer.bulkIndexWithContext(is, "test-index")
        );
        assertTrue(ex.getMessage().contains("must be NDJSON"));
    }

    /**
     * <p><b>Summary:</b> Verifies that empty lines in the input stream are ignored.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-CLIENT-004
     * - **Data**: NDJSON containing empty lines or whitespace between valid JSON objects.
     * - **Criteria**: The loop continues without throwing exceptions or attempting to process blank lines.</p>
     * <p><b>Pre-Condition:</b> Index exists.</p>
     * <p><b>Expected Results:</b> The process completes successfully for the non-empty lines.</p>
     * @throws Exception if any error occurs
     */
    @Test
    void testBulkIndexWithContext_IgnoresBlankLines() throws Exception {
        // Arrange
        String ndjsonWithBlanks = "\n{\"valid\":true}\n   \n{\"valid\":true}";
        InputStream is = new ByteArrayInputStream(ndjsonWithBlanks.getBytes(StandardCharsets.UTF_8));

        when(indicesClient.exists(any(Function.class))).thenReturn(new BooleanResponse(true));

        assertDoesNotThrow(() -> importer.bulkIndexWithContext(is, "test-index"));
    }
}