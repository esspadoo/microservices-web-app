package it.unipd.search.service;

import it.unipd.search.dto.CacheDocument;
import it.unipd.search.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for managing cached search results stored in MongoDB.
 *
 * <p>This class abstracts persistence logic and provides a clean API
 * for retrieving and storing cached documents.</p>
 */
@Service
public class DocumentService {

    /**
     * Repository used to access MongoDB-stored cached documents.
     */
    private final DocumentRepository documentRepository;

    /**
     * Constructs a {@code DocumentService} with the required repository dependency.
     *
     * @param documentRepository MongoDB repository for cached documents
     */
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Retrieves cached documents associated with a given query.
     *
     * @param query search query string
     * @return list of cached documents matching the query
     */
    public List<CacheDocument> getDocumentsByQuery(String query) {
        return documentRepository.findByQuery(query);
    }


    /**
     * Persists a cache entry containing documents related to a search query.
     *
     * @param documents cache object to store
     * @return stored {@link CacheDocument}
     */
    public CacheDocument insertDocuments(CacheDocument documents) {
        return documentRepository.insert(documents);
    }
}
