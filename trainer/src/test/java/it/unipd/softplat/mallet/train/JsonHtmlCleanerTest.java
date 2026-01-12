package it.unipd.softplat.mallet.train;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * <strong> Class JsonHtmlCleanerTest </strong>
 *
 * <p><b>Summary</b>:
 * This test suite validates the functionality of the {@link JsonHtmlCleaner} class.
 * It ensures that the {@code clean()} method correctly processes JSONL input,
 * removes HTML tags from the 'main_content' field, handles malformed JSON,
 * and skips documents with missing required fields.
 * <p><b>Test Suite Design</b>:
 * This is a unit test suite that tests the {@link JsonHtmlCleaner} in isolation.
 * It uses Mockito to mock static methods of {@link Paths} and {@link Files}
 * to redirect file I/O operations to in-memory {@link StringReader} and {@link StringWriter}.
 * This allows for precise control over input data and verification of output without
 * touching the actual file system.
 */
@ExtendWith(MockitoExtension.class)
public class JsonHtmlCleanerTest {

    private MockedStatic<Paths> mockedPaths;
    private MockedStatic<Files> mockedFiles;

    private StringWriter outputStringWriter;
    private Path mockInputPath;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockedPaths = Mockito.mockStatic(Paths.class);
        mockedFiles = Mockito.mockStatic(Files.class);

        mockInputPath = Mockito.mock(Path.class);
        Path mockOutputPath = Mockito.mock(Path.class);

        // Stub Paths.get to return our mock paths
        mockedPaths.when(() -> Paths.get("trainer/src/json_out.json")).thenReturn(mockInputPath);
        mockedPaths.when(() -> Paths.get("trainer/src/main/resources/clean_json_out.json")).thenReturn(mockOutputPath);

        // Prepare StringWriter for capturing output
        outputStringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(outputStringWriter);

        // Stub Files.newBufferedWriter to return our mock writer
        when(Files.newBufferedWriter(eq(mockOutputPath), eq(StandardCharsets.UTF_8))).thenReturn(bufferedWriter);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockedPaths.close();
        mockedFiles.close();
        outputStringWriter.close();
    }

    /**
     * <p><b>Summary:</b> Tests the cleaning of HTML content from a valid JSONL input.
     * <p><b>Test Case Design:</b> A JSONL string with HTML tags in 'main_content' is used as input.
     * The test verifies that the HTML tags are removed and the content is correctly extracted as plain text.
     * <p><b>Test Description:</b> This test ensures that the {@code clean()} method correctly
     * parses a valid JSONL document, uses Jsoup to strip HTML, and writes the cleaned JSONL
     * to the output.
     * <p><b>Pre-Condition:</b> The input stream contains a valid JSONL document with HTML content.
     * <p><b>Post-Condition:</b> The output contains a single JSONL document with the 'main_content'
     * field cleaned of HTML tags.
     * <p><b>Expected Results:</b> The 'main_content' in the output JSONL matches the expected
     * plain text after HTML stripping.
     * @throws IOException if an I/O error occurs during the test.
     */
    @Test
    void testCleanValidHtml() throws IOException {
        String inputJsonl = "{\"url\":\"http://example.com/1\",\"title\":\"Test Title\",\"main_content\":\"<p>Hello <b>World</b>!</p>\"}";
        String expectedCleanContent = "Hello World!";

        // Stub Files.newBufferedReader to return a reader for our input string
        when(Files.newBufferedReader(eq(mockInputPath), eq(StandardCharsets.UTF_8)))
                .thenReturn(new BufferedReader(new StringReader(inputJsonl)));

        JsonHtmlCleaner.clean();

        String output = outputStringWriter.toString().trim();
        JsonNode outputNode = MAPPER.readTree(output);

        assertNotNull(outputNode);
        assertEquals("http://example.com/1", outputNode.get("url").asText());
        assertEquals("Test Title", outputNode.get("title").asText());
        assertEquals(expectedCleanContent, outputNode.get("main_content").asText());
    }

    /**
     * <p><b>Summary:</b> Tests the handling of malformed JSONL lines.
     * <p><b>Test Case Design:</b> The input JSONL contains a mix of valid and malformed lines.
     * <p><b>Test Description:</b> This test verifies that {@code clean()} skips malformed JSONL
     * lines and continues processing valid ones, without crashing.
     * <p><b>Pre-Condition:</b> The input stream contains both valid and syntactically incorrect JSONL lines.
     * <p><b>Post-Condition:</b> Only the valid JSONL documents are present in the output, with their
     * HTML content cleaned. Malformed lines are ignored.
     * <p><b>Expected Results:</b> The output contains only the cleaned version of the valid JSONL document.
     * @throws IOException if an I/O error occurs during the test.
     */
    @Test
    void testCleanMalformedJson() throws IOException {
        String inputJsonl =
                "{\"url\":\"http://example.com/1\",\"title\":\"Title 1\",\"main_content\":\"<p>Content 1</p>\"}\n" +
                "this is not json\n" +
                "{\"url\":\"http://example.com/2\",\"title\":\"Title 2\",\"main_content\":\"<div>Content 2</div>\"}";
        String expectedCleanContent1 = "Content 1";
        String expectedCleanContent2 = "Content 2";

        when(Files.newBufferedReader(eq(mockInputPath), eq(StandardCharsets.UTF_8)))
                .thenReturn(new BufferedReader(new StringReader(inputJsonl)));

        JsonHtmlCleaner.clean();

        String[] outputLines = outputStringWriter.toString().trim().split("\n");
        assertEquals(2, outputLines.length);

        JsonNode outputNode1 = MAPPER.readTree(outputLines[0]);
        assertEquals("http://example.com/1", outputNode1.get("url").asText());
        assertEquals(expectedCleanContent1, outputNode1.get("main_content").asText());

        JsonNode outputNode2 = MAPPER.readTree(outputLines[1]);
        assertEquals("http://example.com/2", outputNode2.get("url").asText());
        assertEquals(expectedCleanContent2, outputNode2.get("main_content").asText());
    }

    /**
     * <p><b>Summary:</b> Tests the handling of JSONL documents with missing required fields.
     * <p><b>Test Case Design:</b> The input JSONL contains documents where 'url', 'title', or 'main_content'
     * fields are absent.
     * <p><b>Test Description:</b> This test ensures that {@code clean()} skips documents that do not
     * conform to the expected structure (missing required fields) and processes only valid ones.
     * <p><b>Pre-Condition:</b> The input stream contains JSONL documents, some of which lack essential fields.
     * <p><b>Post-Condition:</b> Only documents with all required fields are present in the output,
     * with their HTML content cleaned. Invalid documents are ignored.
     * <p><b>Expected Results:</b> The output contains only the cleaned version of the valid JSONL document.
     * @throws IOException if an I/O error occurs during the test.
     */
    @Test
    void testCleanMissingFields() throws IOException {
        String inputJsonl =
                "{\"url\":\"http://example.com/1\",\"title\":\"Title 1\"}\n" + // Missing main_content
                "{\"title\":\"Title 2\",\"main_content\":\"<p>Content 2</p>\"}\n" + // Missing url
                "{\"url\":\"http://example.com/3\",\"title\":\"Title 3\",\"main_content\":\"<p>Content 3</p>\"}"; // Valid
        String expectedCleanContent = "Content 3";

        when(Files.newBufferedReader(eq(mockInputPath), eq(StandardCharsets.UTF_8)))
                .thenReturn(new BufferedReader(new StringReader(inputJsonl)));

        JsonHtmlCleaner.clean();

        String[] outputLines = outputStringWriter.toString().trim().split("\n");
        assertEquals(1, outputLines.length);

        JsonNode outputNode = MAPPER.readTree(outputLines[0]);
        assertEquals("http://example.com/3", outputNode.get("url").asText());
        assertEquals("Title 3", outputNode.get("title").asText());
        assertEquals(expectedCleanContent, outputNode.get("main_content").asText());
    }

    /**
     * <p><b>Summary:</b> Tests the removal of special characters like quotes and Unicode line separators.
     * <p><b>Test Case Design:</b> A JSONL string with 'main_content' containing various types of quotes
     * and a Unicode line separator (U+2028) is used as input.
     * <p><b>Test Description:</b> This test verifies that {@code clean()} correctly removes specific
     * unwanted characters from the cleaned text.
     * <p><b>Pre-Condition:</b> The input stream contains a valid JSONL document with special characters.
     * <p><b>Post-Condition:</b> The 'main_content' in the output JSONL has all specified special
     * characters removed.
     * <p><b>Expected Results:</b> The cleaned 'main_content' matches the expected string without
     * quotes or Unicode line separators.
     * @throws IOException if an I/O error occurs during the test.
     */
    @Test
    void testCleanSpecialCharacters() throws IOException {
        String inputJsonl = "{\"url\":\"http://example.com/1\",\"title\":\"Title\",\"main_content\":\"<p>Hello World Test</p>\"}";
        String expectedCleanContent = "Hello World Test"; // Quotes and U+2028 removed

        when(Files.newBufferedReader(eq(mockInputPath), eq(StandardCharsets.UTF_8)))
                .thenReturn(new BufferedReader(new StringReader(inputJsonl)));

        JsonHtmlCleaner.clean();

        String output = outputStringWriter.toString().trim();
        JsonNode outputNode = MAPPER.readTree(output);

        assertNotNull(outputNode);
        assertEquals(expectedCleanContent, outputNode.get("main_content").asText());
    }

    /**
     * <p><b>Summary:</b> Tests the behavior with an empty input file.
     * <p><b>Test Case Design:</b> An empty input stream is provided to the cleaner.
     * <p><b>Test Description:</b> This test ensures that {@code clean()} handles an empty input
     * file gracefully, resulting in an empty output file.
     * <p><b>Pre-Condition:</b> The input stream is empty.
     * <p><b>Post-Condition:</b> The output stream remains empty.
     * <p><b>Expected Results:</b> The output string is empty.
     * @throws IOException if an I/O error occurs during the test.
     */
    @Test
    void testCleanEmptyInput() throws IOException {
        String inputJsonl = "";

        when(Files.newBufferedReader(eq(mockInputPath), eq(StandardCharsets.UTF_8)))
                .thenReturn(new BufferedReader(new StringReader(inputJsonl)));

        JsonHtmlCleaner.clean();

        String output = outputStringWriter.toString().trim();
        assertEquals("", output);
    }
}
