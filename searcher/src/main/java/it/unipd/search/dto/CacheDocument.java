package it.unipd.search.dto;


import org.springframework.data.annotation.Id;

import java.util.List;

@org.springframework.data.mongodb.core.mapping.Document
public class CacheDocument {
    @Id
    private String id;

    private String query;

    private List<Document> documents;

    public String getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
