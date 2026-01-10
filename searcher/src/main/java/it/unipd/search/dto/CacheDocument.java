package it.unipd.search.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

/**
 * MongoDB document representing cached search results.
 *
 * <p>Each instance maps a search query to the list of documents
 * produced for that query.</p>
 */
@org.springframework.data.mongodb.core.mapping.Document
public class CacheDocument {

    /**
     * Unique identifier of the cache document.
     */
    @Id
    private String id;

    /**
     * Search query associated with this cache entry.
     */
    @Indexed(unique = true)
    private String query;

    /**
     * List of documents retrieved and cached for the query.
     */
    private List<Document> documents;

    /**
     * Returns the document identifier.
     *
     * @return cache document ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the associated search query.
     *
     * @return search query string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the cached documents.
     *
     * @return list of documents
     */
    public List<Document> getDocuments() {
        return documents;
    }

    /**
     * Sets the search query for this cache entry.
     *
     * @param query search query string
     */
    public void setQuery(String query) {
        this.query = query;
    }


    /**
     * Sets the list of cached documents.
     *
     * @param documents list of documents
     */
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
