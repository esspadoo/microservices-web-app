package it.unipd.inferer.service;

import it.unipd.inferer.dto.Document;

public interface DocService {

    Document infer(Document doc) throws Exception;
}
