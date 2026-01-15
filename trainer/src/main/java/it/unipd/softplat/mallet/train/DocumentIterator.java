package it.unipd.softplat.mallet.train;

import cc.mallet.types.Instance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Iterates over a stream of JSONL documents, parsing each document into a Mallet {@link Instance}.
 * Each line in the input stream is expected to be a JSON object containing "url", "title", and "main_content" fields.
 * Documents with malformed JSON or missing required fields are skipped.
 */
public class DocumentIterator implements Iterator<Instance> {

    private final Scanner scanner;
    private final ObjectMapper mapper;

    // Prefetched next valid instance
    private Instance nextInstance;

    /**
     * Constructs a new DocumentIterator.
     *
     * @param dataInputStream The input stream containing JSONL documents.
     */
    public DocumentIterator(InputStream dataInputStream) {
        this.scanner = new Scanner(dataInputStream);
        this.mapper = new ObjectMapper();
        advance();
    }

    /**
     * Advance scanner until a valid Instance is found or EOF is reached.
     */
    private void advance() {
        nextInstance = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            try {
                JsonNode json = mapper.readTree(line);

                JsonNode urlNode   = json.get("url");
                JsonNode titleNode = json.get("title");
                JsonNode textNode  = json.get("main_content");

                if (urlNode == null || titleNode == null || textNode == null) {
                    // Skip structurally invalid documents
                    continue;
                }

                String docId = urlNode.asText();
                String title = titleNode.asText();
                String text  = textNode.asText();

                nextInstance = new Instance(text, title, docId, "");
                return;

            } catch (JsonProcessingException e) {
                // Malformed JSON line — skip and continue
                System.err.println("Skipping malformed JSONL line");
            } catch (Exception e) {
                // Defensive: skip any unexpected runtime issues
                System.err.println("Skipping invalid document: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean hasNext() {
        return nextInstance != null;
    }

    @Override
    public Instance next() {
        if (nextInstance == null) {
            throw new NoSuchElementException("No more valid documents");
        }

        Instance current = nextInstance;
        advance();
        return current;
    }

    @Override
    public void remove() {
        throw new IllegalStateException(
                "This Iterator<Instance> does not support remove()."
        );
    }
}
