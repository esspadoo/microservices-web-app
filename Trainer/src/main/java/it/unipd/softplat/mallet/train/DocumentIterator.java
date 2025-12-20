package it.unipd.softplat.mallet.train;

import cc.mallet.types.Instance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Iterator implementation that reads {@link Instance} objects from a JSONL input stream.
 * <p>
 * Each line of the input stream is expected to be a JSON object with the following structure:
 * <ul>
 *     <li>url</li>
 *     <li>title</li>
 *     <li>main_content</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class allows sequential access to {@link Instance} objects for use in
 * topic modeling or other processing pipelines (e.g., with MALLET).
 * </p>
 *
 * <p>
 * The iterator does not support the {@link #remove()} operation.
 * </p>
 */
public class DocumentIterator implements Iterator<Instance> {

    /**
     * Scanner used to read the input stream line by line.
     */
    private final Scanner scanner;

    /**
     * ObjectMapper used to parse each line from JSON into a {@link JsonNode}.
     */
    private final ObjectMapper mapper;

    /**
     * Constructs a new {@link DocumentIterator} from the provided input stream.
     * <p>
     * Each line in the input stream should be a valid JSON object representing a document.
     * </p>
     *
     * @param dataInputStream the input stream containing JSONL-formatted document data
     */
    public DocumentIterator(InputStream dataInputStream) {
        this.scanner = new Scanner(dataInputStream);
        this.mapper = new ObjectMapper();
    }

    /**
     * Checks if the iterator has more documents to read.
     *
     * @return {@code true} if there is at least one more line in the input stream, {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    /**
     * Returns the next {@link Instance} parsed from the input stream.
     * <p>
     * Each line is parsed as JSON and converted into an {@link Instance} object.
     * The {@code text} field is mapped from "main_content", the {@code target} field
     * is mapped from "title", the {@code name} field is mapped from "url", and
     * the {@code source} field is left empty.
     * </p>
     *
     * @return the next {@link Instance} object
     * @throws RuntimeException if the JSON line cannot be parsed
     */
    @Override
    public Instance next() {
        try {
            String line = scanner.nextLine();

            JsonNode json = mapper.readTree(line);

            // Structure: url, title, main_content
            String docId = json.get("url").asText();
            String title = json.get("title").asText();
            String text  = json.get("main_content").asText();

            return new Instance(text, title, docId, "");

        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSONL line", e);
        }
    }

    /**
     * This iterator does not support removal of elements.
     *
     * @throws IllegalStateException always thrown when this method is called
     */
    @Override
    public void remove() {
        throw new IllegalStateException(
                "This Iterator<Instance> does not support remove()."
        );
    }
}
