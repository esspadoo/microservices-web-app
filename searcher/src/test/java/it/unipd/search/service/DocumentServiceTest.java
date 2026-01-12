package it.unipd.search.service;

import it.unipd.search.dto.CacheDocument;
import it.unipd.search.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    void testGetDocumentsByQuery_Success() {
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
     * <p><b>Summary:</b> Tests retrieval when no documents match the query.</p>
     * <p><b>Test Case Design:</b> Configure the repository to return an empty list for a query that has no cache entries.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOCSERV-002
     * - **Prerequisites**: Mocked DocumentRepository.
     * - **Data**: Query string "non-existent-query".
     * - **Evaluation**: Ensures the service gracefully handles empty results from the persistence layer.</p>
     * <p><b>Pre-Condition:</b> No entries for this query exist in MongoDB.</p>
     * <p><b>Post-Condition:</b> Returns an empty list.</p>
     * <p><b>Expected Results:</b> An empty list is returned, not null.</p>
     */
    @Test
    void testGetDocumentsByQuery_EmptyResult() {
        String query = "non-existent-query";
        when(documentRepository.findByQuery(query)).thenReturn(Collections.emptyList());

        List<CacheDocument> result = documentService.getDocumentsByQuery(query);

        assertTrue(result.isEmpty());
    }

    /**
     * <p><b>Summary:</b> Tests failure of document retrieval due to repository error.</p>
     * <p><b>Test Case Design:</b> Mock the repository to throw a RuntimeException (simulating a database connection failure).</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOCSERV-003
     * - **Prerequisites**: Mocked DocumentRepository configured to fail.
     * - **Data**: Any query string.
     * - **Evaluation**: Verifies that repository-level exceptions are propagated to the caller.</p>
     * <p><b>Pre-Condition:</b> MongoDB is unreachable or the driver encountered an error.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Throws a {@link RuntimeException}.</p>
     */
    @Test
    void testGetDocumentsByQuery_RepositoryFailure() {
        when(documentRepository.findByQuery(anyString())).thenThrow(new RuntimeException("DB Connection Error"));

        assertThrows(RuntimeException.class, () -> documentService.getDocumentsByQuery("test"));
    }

    /**
     * <p><b>Summary:</b> Verifies insertion of new cache documents.</p>
     * <p><b>Test Case Design:</b> Mocks the repository's insert method.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOCSERV-004
     * - **Data**: A CacheDocument object.
     * - **Evaluation**: Expects the returned object to be the one saved.</p>
     * <p><b>Pre-Condition:</b> Repository is mocked.</p>
     * <p><b>Post-Condition:</b> insert is called exactly once.</p>
     * <p><b>Expected Results:</b> The service returns the saved CacheDocument.</p>
     */
    @Test
    void testInsertDocuments_Success() {
        CacheDocument cacheDoc = new CacheDocument();
        cacheDoc.setQuery("new-query");

        when(documentRepository.insert(cacheDoc)).thenReturn(cacheDoc);

        CacheDocument result = documentService.insertDocuments(cacheDoc);

        assertNotNull(result);
        assertEquals("new-query", result.getQuery());
        verify(documentRepository, times(1)).insert(cacheDoc);
    }

    /**
     * <p><b>Summary:</b> Tests failure when inserting a duplicate document.</p>
     * <p><b>Test Case Design:</b> Configure the mock repository to throw a {@link DuplicateKeyException}.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOCSERV-005
     * - **Prerequisites**: MongoDB unique index constraints are active.
     * - **Data**: A document that already exists in the database.
     * - **Evaluation**: Ensures the service does not catch the exception internally but lets it propagate.</p>
     * <p><b>Pre-Condition:</b> A document with the same unique key already exists.</p>
     * <p><b>Post-Condition:</b> No new data is written.</p>
     * <p><b>Expected Results:</b> Throws a {@link DuplicateKeyException}.</p>
     */
    @Test
    void testInsertDocuments_DuplicateKey() {
        CacheDocument doc = new CacheDocument();
        when(documentRepository.insert(any(CacheDocument.class))).thenThrow(new DuplicateKeyException("Duplicate key error"));

        assertThrows(DuplicateKeyException.class, () -> documentService.insertDocuments(doc));
    }

}
