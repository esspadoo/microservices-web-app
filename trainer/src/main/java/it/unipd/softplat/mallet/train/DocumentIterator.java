package it.unipd.softplat.mallet.train;

import cc.mallet.types.Instance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class DocumentIterator implements Iterator<Instance> {

    private final Scanner scanner;
    private final ObjectMapper mapper;

    public DocumentIterator(InputStream dataInputStream) {
        this.scanner = new Scanner(dataInputStream);
        this.mapper = new ObjectMapper();
    }

    @Override
    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    @Override
    public Instance next() {
        try {
            String line = scanner.nextLine();

            JsonNode json = mapper.readTree(line);

            //Structure: url, title, main_content
            String docId = json.get("url").asText();
            String title = json.get("title").asText();
            String text  = json.get("main_content").asText();

            return new Instance(text, title, docId, "");

        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSONL line", e);
        }
    }

    @Override
    public void remove() {
        throw new IllegalStateException(
                "This Iterator<Instance> does not support remove()."
        );
    }
}
