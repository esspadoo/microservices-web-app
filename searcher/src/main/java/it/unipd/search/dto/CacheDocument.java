package it.unipd.search.dto;


import org.springframework.data.annotation.Id;

import java.util.List;

@org.springframework.data.mongodb.core.mapping.Document
public class CacheDocument {
    @Id
    private String id;

    private String query;

    private List<Document> documents;

    public CacheDocument() {}

    public CacheDocument(String id, String query, List<Document> documents) {
        this.id = id;
        this.query = query;
        this.documents = documents;
    }

    public String getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public List<Document> getDocuments() {
        return documents;
    }
}
