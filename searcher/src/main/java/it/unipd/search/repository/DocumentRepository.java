package it.unipd.search.repository;

import it.unipd.search.dto.CacheDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository interface for {@link CacheDocument} entities.
 *
 * <p>Spring Data automatically generates query implementations
 * based on method naming conventions.</p>
 */
@Repository
public interface  DocumentRepository extends MongoRepository<CacheDocument, String > {

    // custom query: spring derives the query from the method name
    /**
     * Finds cached documents associated with a specific query string.
     *
     * @param query search query
     * @return list of matching {@link CacheDocument} instances
     */
    List<CacheDocument> findByQuery(String query);
}
