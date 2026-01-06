package it.unipd.search.service;

import it.unipd.search.dto.CacheDocument;
import it.unipd.search.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * <strong> Class DocumentServiceTest </strong>
 *
 * <p><b>Summary</b>: This test suite provides validation for the DocumentService,
 * which manages the persistence and retrieval of cached search results in MongoDB.</p>
 *
 * <p><b>Test Suite Design</b>: The suite uses Mockito to mock the DocumentRepository.
 * It focuses on verifying that the service correctly delegates calls to the repository
 * layer for both searching by query and inserting new cache entries.</p>
 *
 * @author Leonardo Ongaro - 2197813
 */
@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    /**
     * <p><b>Summary:</b> Verifies retrieval of documents by query.</p>
     * <p><b>Test Case Design:</b> Mocks the repository to return a predefined list
     * when searched with a specific string.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOCSERV-001
     * - **Data**: query="test-query"
     * - **Evaluation**: Expects the returned list to match the repository output.</p>
     * <p><b>Pre-Condition:</b> Repository is mocked.</p>
     * <p><b>Post-Condition:</b> findByQuery is called exactly once.</p>
     * <p><b>Expected Results:</b> The service returns the list provided by the repository.</p>
     */
    @Test
    void shouldGetDocumentsByQuery() {
        String query = "test-query";
        CacheDocument cacheDoc = new CacheDocument();
        cacheDoc.setQuery(query);
        List<CacheDocument> expected = List.of(cacheDoc);

        when(documentRepository.findByQuery(query)).thenReturn(expected);

        List<CacheDocument> result = documentService.getDocumentsByQuery(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(query, result.getFirst().getQuery());
        verify(documentRepository, times(1)).findByQuery(query);
    }

    /**
     * <p><b>Summary:</b> Verifies insertion of new cache documents.</p>
     * <p><b>Test Case Design:</b> Mocks the repository's insert method.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOCSERV-002
     * - **Data**: A CacheDocument object.
     * - **Evaluation**: Expects the returned object to be the one saved.</p>
     * <p><b>Pre-Condition:</b> Repository is mocked.</p>
     * <p><b>Post-Condition:</b> insert is called exactly once.</p>
     * <p><b>Expected Results:</b> The service returns the saved CacheDocument.</p>
     */
    @Test
    void shouldInsertDocuments() {
        CacheDocument cacheDoc = new CacheDocument();
        cacheDoc.setQuery("new-query");

        when(documentRepository.insert(cacheDoc)).thenReturn(cacheDoc);

        CacheDocument result = documentService.insertDocuments(cacheDoc);

        assertNotNull(result);
        assertEquals("new-query", result.getQuery());
        verify(documentRepository, times(1)).insert(cacheDoc);
    }
}
