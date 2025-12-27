package it.unipd.search.service;

import it.unipd.search.dto.CacheDocument;
import it.unipd.search.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<CacheDocument> getDocumentsByQuery(String query) {
        return documentRepository.findByQuery(query);
    }

    public CacheDocument insertDocuments(CacheDocument documents) {
        return documentRepository.insert(documents);
    }
}
