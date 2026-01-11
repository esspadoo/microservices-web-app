package it.unipd.search.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

/**
 * MongoDB document representing cached search results.
 *
 * <p>Each instance maps a unique search query to the list of documents
 * returned for that query, allowing fast retrieval of previously
 * computed search results.</p>
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
     *
     * <p>This field is indexed and must be unique to ensure that
     * only one cache entry exists per query.</p>
     */
    @Indexed(unique = true)
    private String query;

    /**
     * List of documents retrieved and cached for the associated query.
     */
    private List<Document> documents;

    /**
     * Returns the unique identifier of this cache document.
     *
     * @return cache document identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the search query associated with this cache entry.
     *
     * @return search query string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the list of cached documents.
     *
     * @return list of cached {@link Document} instances
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
     * Sets the list of documents to be cached for this query.
     *
     * @param documents list of documents
     */
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
