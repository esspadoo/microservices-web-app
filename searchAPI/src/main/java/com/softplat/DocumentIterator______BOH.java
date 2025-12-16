
package com.softplat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class DocumentIterator______BOH implements Iterator<Document> {
    private Scanner scanner;
    private ObjectMapper objectMapper;


    public DocumentIterator______BOH(InputStream dataInputStream) {
        scanner = new Scanner(dataInputStream);
        objectMapper = new ObjectMapper();
    }

    public Document next() {

        String line = scanner.nextLine();

        Document doc;
        try {
            doc = objectMapper.readValue(line, Document.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return doc;
    }

    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    public void remove() {
        throw new IllegalStateException("This Iterator<Instance> does not support remove().");
    }
}
