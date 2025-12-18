package it.unipd.inferer.service;

import it.unipd.inferer.dto.Document;

import java.util.List;

public interface DocService {

    Document infer(Document doc) throws Exception;

    List<Document> inferBatch(List<Document> doc) throws Exception;
}
