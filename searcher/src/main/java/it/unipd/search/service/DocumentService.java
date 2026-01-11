package it.unipd.search.service;

import it.unipd.search.dto.CacheDocument;
import it.unipd.search.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for managing cached search results stored in MongoDB.
 *
 * <p>This service acts as an abstraction layer over the persistence
 * mechanism, providing a simple API for storing and retrieving
 * cached search results.</p>
 */
@Service
public class DocumentService {

    /**
     * Repository used to access cached search results in MongoDB.
     */
    private final DocumentRepository documentRepository;

    /**
     * Constructs a {@code DocumentService} with the required repository dependency.
     *
     * @param documentRepository MongoDB repository for cached search results
     */
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Retrieves cached search results associated with the given query.
     *
     * @param query the search query string
     * @return list of cached {@link CacheDocument} entries matching the query
     */
    public List<CacheDocument> getDocumentsByQuery(String query) {
        return documentRepository.findByQuery(query);
    }

    /**
     * Persists a cache entry containing documents related to a search query.
     *
     * @param documents cache entry to store
     * @return the stored {@link CacheDocument}
     */
    public CacheDocument insertDocuments(CacheDocument documents) {
        return documentRepository.insert(documents);
    }
}
