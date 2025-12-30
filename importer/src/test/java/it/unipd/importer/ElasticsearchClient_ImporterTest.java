package it.unipd.importer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import org.elasticsearch.client.RestClient;
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
 * <p><b>Summary</b>: Tests the parsing logic and validation rules of the Elasticsearch client.
 * Specifically validates that the NDJSON format is strictly enforced.</p>
 *
 * <p><b>Test Case Design</b>: Uses Mockito to stub the Elasticsearch indices client 
 * to prevent network calls while testing the internal BufferedReader loop.</p>
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
     * <p><b>Test Description</b>: Verifies formatting validation.
     * - **ID**: TC-ELASTIC-01
     * - **Expected Results**: Throws IllegalArgumentException for invalid line starts.</p>
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