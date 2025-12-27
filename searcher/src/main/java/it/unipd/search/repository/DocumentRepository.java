package it.unipd.search.repository;

import it.unipd.search.dto.CacheDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  DocumentRepository extends MongoRepository<CacheDocument, String > {
    // custom query: spring derives the query from the method name
    List<CacheDocument> findByQuery(String query);
}
