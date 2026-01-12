package it.unipd.softplat.mallet.train;

import cc.mallet.types.Instance;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <strong> Class DocumentIteratorTest </strong>
 *
 * <p><b>Summary</b>:
 * This test suite validates the functionality of the {@link DocumentIterator} class.
 * It ensures that the iterator correctly parses JSONL input streams, extracts document
 * information, creates {@link Instance} objects, and handles various input scenarios
 * including valid JSON, malformed JSON, and documents with missing fields.
 * <p><b>Test Suite Design</b>:
 * This is a unit test suite that tests the {@link DocumentIterator} in isolation.
 * It uses {@link ByteArrayInputStream} to simulate input streams with different
 * JSONL content, allowing for precise control over test data.
 */
public class DocumentIteratorTest {

    /**
     * <p><b>Summary:</b> Tests the iteration over a stream with valid JSONL documents.
     * <p><b>Test Case Design:</b> A {@link ByteArrayInputStream} is created with two valid JSONL strings.
     * The test verifies that {@code hasNext()} correctly identifies available documents and {@code next()}
     * returns {@link Instance} objects with the expected data.
     * <p><b>Test Description:</b> This test ensures that the {@code DocumentIterator} can successfully
     * parse well-formed JSONL input, extract the required fields (url, title, main_content), and
     * construct {@link Instance} objects correctly.
     * <p><b>Pre-Condition:</b> The input stream contains valid JSONL where each line represents a document
     * with all required fields.
     * <p><b>Post-Condition:</b> Two {@link Instance} objects are returned, each containing the correct
     * data from the input JSONL. {@code hasNext()} returns false after all documents are consumed.
     * <p><b>Expected Results:</b> The {@code Instance} objects' data, name, and source fields match
     * the input JSONL.
     */
    @Test
    void testValidJsonlInput() {
        String jsonlInput =
                "{\"url\":\"http://example.com/doc1\",\"title\":\"Title 1\",\"main_content\":\"Content of document 1.\"}\n" +
                "{\"url\":\"http://example.com/doc2\",\"title\":\"Title 2\",\"main_content\":\"Content of document 2.\"}\n";
        InputStream inputStream = new ByteArrayInputStream(jsonlInput.getBytes(StandardCharsets.UTF_8));

        DocumentIterator iterator = new DocumentIterator(inputStream);

        assertTrue(iterator.hasNext());
        Instance instance1 = iterator.next();
        assertNotNull(instance1);
        assertEquals("Content of document 1.", instance1.getData().toString());
        assertEquals("Title 1", instance1.getTarget().toString());
        assertEquals("http://example.com/doc1", instance1.getName().toString());

        assertTrue(iterator.hasNext());
        Instance instance2 = iterator.next();
        assertNotNull(instance2);
        assertEquals("Content of document 2.", instance2.getData().toString());
        assertEquals("Title 2", instance2.getTarget().toString());
        assertEquals("http://example.com/doc2", instance2.getName().toString());

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    /**
     * <p><b>Summary:</b> Tests the iterator's behavior with an empty input stream.
     * <p><b>Test Case Design:</b> An empty {@link ByteArrayInputStream} is provided.
     * <p><b>Test Description:</b> This test verifies that an empty input stream results in an
     * iterator that immediately reports no more elements.
     * <p><b>Pre-Condition:</b> The input stream is empty.
     * <p><b>Post-Condition:</b> {@code hasNext()} returns false, and calling {@code next()} throws
     * a {@link NoSuchElementException}.
     * <p><b>Expected Results:</b> No documents are processed, and the iterator behaves as if it's
     * at the end of its sequence.
     */
    @Test
    void testEmptyInput() {
        InputStream inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        DocumentIterator iterator = new DocumentIterator(inputStream);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    /**
     * <p><b>Summary:</b> Tests the iterator's ability to skip malformed JSONL lines.
     * <p><b>Test Case Design:</b> A {@link ByteArrayInputStream} is created with a mix of valid
     * and malformed JSONL lines.
     * <p><b>Test Description:</b> This test ensures that the iterator gracefully handles lines
     * that are not valid JSON, skipping them and continuing to process subsequent valid lines.
     * <p><b>Pre-Condition:</b> The input stream contains both valid and invalid JSONL lines.
     * <p><b>Post-Condition:</b> Only the valid JSONL documents are processed and returned as
     * {@link Instance} objects. Malformed lines are ignored.
     * <p><b>Expected Results:</b> The iterator returns only the {@link Instance} corresponding
     * to the valid JSONL line.
     */
    @Test
    void testMalformedJsonlLines() {
        String jsonlInput =
                "{\"url\":\"http://example.com/doc1\",\"title\":\"Title 1\",\"main_content\":\"Content of document 1.\"}\n" +
                "this is not json\n" +
                "{\"url\":\"http://example.com/doc2\",\"title\":\"Title 2\",\"main_content\":\"Content of document 2.\"}\n";
        InputStream inputStream = new ByteArrayInputStream(jsonlInput.getBytes(StandardCharsets.UTF_8));

        DocumentIterator iterator = new DocumentIterator(inputStream);

        assertTrue(iterator.hasNext());
        Instance instance1 = iterator.next();
        assertNotNull(instance1);
        assertEquals("http://example.com/doc1", instance1.getName().toString());

        assertTrue(iterator.hasNext());
        Instance instance2 = iterator.next();
        assertNotNull(instance2);
        assertEquals("http://example.com/doc2", instance2.getName().toString());

        assertFalse(iterator.hasNext());
    }

    /**
     * <p><b>Summary:</b> Tests the iterator's ability to skip JSONL documents with missing required fields.
     * <p><b>Test Case Design:</b> A {@link ByteArrayInputStream} is created with JSONL lines where
     * some documents are missing 'url', 'title', or 'main_content' fields.
     * <p><b>Test Description:</b> This test verifies that documents lacking essential fields are
     * skipped, and the iterator proceeds to the next structurally valid document.
     * <p><b>Pre-Condition:</b> The input stream contains JSONL documents, some of which are missing
     * one or more of the 'url', 'title', or 'main_content' fields.
     * <p><b>Post-Condition:</b> Only documents with all required fields are processed and returned.
     * <p><b>Expected Results:</b> The iterator returns only the {@link Instance} corresponding
     * to the valid JSONL line with all fields.
     */
    @Test
    void testMissingRequiredFields() {
        String jsonlInput =
                "{\"url\":\"http://example.com/doc1\",\"title\":\"Title 1\"}\n" + // Missing main_content
                "{\"title\":\"Title 2\",\"main_content\":\"Content of document 2.\"}\n" + // Missing url
                "{\"url\":\"http://example.com/doc3\",\"main_content\":\"Content of document 3.\"}\n" + // Missing title
                "{\"url\":\"http://example.com/doc4\",\"title\":\"Title 4\",\"main_content\":\"Content of document 4.\"}\n"; // Valid
        InputStream inputStream = new ByteArrayInputStream(jsonlInput.getBytes(StandardCharsets.UTF_8));

        DocumentIterator iterator = new DocumentIterator(inputStream);

        assertTrue(iterator.hasNext());
        Instance instance = iterator.next();
        assertNotNull(instance);
        assertEquals("http://example.com/doc4", instance.getName().toString());

        assertFalse(iterator.hasNext());
    }

    /**
     * <p><b>Summary:</b> Tests the {@code remove()} method to ensure it throws an {@link UnsupportedOperationException}.
     * <p><b>Test Case Design:</b> An attempt is made to call the {@code remove()} method on the iterator.
     * <p><b>Test Description:</b> This test confirms that the {@code DocumentIterator} explicitly
     * does not support the {@code remove()} operation, as indicated by the thrown exception.
     * <p><b>Pre-Condition:</b> A {@code DocumentIterator} instance is created.
     * <p><b>Post-Condition:</b> An {@link UnsupportedOperationException} is thrown.
     * <p><b>Expected Results:</b> An {@link UnsupportedOperationException} is thrown when {@code remove()} is called.
     */
    @Test
    void testRemoveThrowsUnsupportedOperationException() {
        String jsonlInput = "{\"url\":\"http://example.com/doc1\",\"title\":\"Title 1\",\"main_content\":\"Content of document 1.\"}\n";
        InputStream inputStream = new ByteArrayInputStream(jsonlInput.getBytes(StandardCharsets.UTF_8));
        DocumentIterator iterator = new DocumentIterator(inputStream);

        assertTrue(iterator.hasNext());
        iterator.next(); // Consume one element to ensure iterator is in a state where remove might be called

        assertThrows(IllegalStateException.class, iterator::remove);
    }

    /**
     * <p><b>Summary:</b> Tests the iterator with an input stream containing only malformed lines.
     * <p><b>Test Case Design:</b> A {@link ByteArrayInputStream} is created with only malformed JSONL lines.
     * <p><b>Test Description:</b> This test ensures that if the entire input stream consists of
     * malformed data, the iterator correctly identifies that there are no valid documents.
     * <p><b>Pre-Condition:</b> The input stream contains only malformed JSONL lines.
     * <p><b>Post-Condition:</b> {@code hasNext()} returns false, and calling {@code next()} throws
     * a {@link NoSuchElementException}.
     * <p><b>Expected Results:</b> No documents are processed, and the iterator behaves as if it's
     * at the end of its sequence.
     */
    @Test
    void testOnlyMalformedLines() {
        String jsonlInput = "not json\nanother invalid line\n";
        InputStream inputStream = new ByteArrayInputStream(jsonlInput.getBytes(StandardCharsets.UTF_8));
        DocumentIterator iterator = new DocumentIterator(inputStream);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}
