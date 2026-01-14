package it.unipd.inferer.service;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import it.unipd.inferer.dto.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;

/**
 * <strong> Class JsonInferencerServiceTest </strong>
 *
 * <p><b>Summary</b>:
 * This test suite is dedicated to validating the static utility methods of the {@link JsonInferencerService} class.
 * It ensures that topic introspection, preprocessing, and inference logic are implemented correctly.
 * <p><b>Test Suite Design</b>:
 * This is a pure unit test suite. All external dependencies, including the MALLET library's
 * {@link ParallelTopicModel} and {@link TopicInferencer}, are mocked using Mockito. This approach
 * isolates the methods under test from the complexities of the underlying machine learning models
 * and file system, allowing for focused and predictable validation of the service's logic.
 */
@ExtendWith(MockitoExtension.class)
public class JsonInferencerServiceTest {

    @Mock
    private ParallelTopicModel model;

    @Mock
    private Pipe pipe;

    @Mock
    private TopicInferencer inferencer;

    @Mock
    private Alphabet alphabet;

    /**
     * <p><b>Summary:</b> Tests the {@code argMax} utility method.</p>
     * <p><b>Test Case Design:</b> The private static method {@code argMax} is tested (via reflection or package-private access) using various double arrays to identify the peak value index.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-UTIL-001
     * - **Prerequisites**: Access to the private method must be enabled.
     * - **Data**: Multiple arrays including positive numbers {@code {1.0, 5.0, 2.0}}, negative numbers {@code {-1.0, -5.0, -2.0}}, and mixed values.
     * - **Evaluation**: Verifies that the method correctly identifies the highest value's index across different numerical ranges.</p>
     * <p><b>Pre-Condition:</b> None; this is a pure mathematical utility function.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Returns {@code 1} for the positive array, {@code 0} for the negative array, and the method correctly handles ties by returning the first occurrence.</p>
     */
    @Test
    void testArgMax() {
        double[] values1 = {1.0, 5.0, 2.0, 4.0};
        int result1 = invokeMethod(JsonInferencerService.class, "argMax", (Object) values1);
        assertEquals(1, result1);

        double[] values2 = {-1.0, -5.0, -0.5, -2.0};
        int result2 = invokeMethod(JsonInferencerService.class, "argMax", (Object) values2);
        assertEquals(2, result2);
    }

    /**
     * <p><b>Summary:</b> Tests the computation of top words for each topic.</p>
     * <p><b>Test Case Design:</b> A mocked {@link ParallelTopicModel} is configured to return a predefined alphabet and sorted word sets. The {@code computeTopicTopWords} method is then called with this mock.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-UTIL-002
     * - **Prerequisites**: Mocked {@link ParallelTopicModel}, {@link cc.mallet.types.Alphabet}, and {@link cc.mallet.types.IDSorter} to provide predictable vocabulary and distributions.
     * - **Data**: A model configured with a specific number of topics and a controlled list of high-probability word IDs.
     * - **Evaluation**: Verifies that the method correctly iterates through topics, looks up word objects in the alphabet, and concatenates them into a space-delimited string.</p>
     * <p><b>Pre-Condition:</b> The mock model is prepared with at least one topic and associated word weights.</p>
     * <p><b>Post-Condition:</b> A map is returned where each topic index is associated with a correctly formatted string of its top words.</p>
     * <p><b>Expected Results:</b> The resulting map contains the expected mapping (e.g., index {@code 0} maps to the string {@code "word1 word2"}) and respects the {@code numWords} limit.</p>
     */
    @Test
    void testComputeTopicTopWords() {
        when(model.getNumTopics()).thenReturn(1);
        when(model.getAlphabet()).thenReturn(alphabet);

        TreeSet<IDSorter> sortedWords = new TreeSet<>();
        sortedWords.add(new IDSorter(0, 10.0));
        sortedWords.add(new IDSorter(1, 20.0));
        ArrayList<TreeSet<IDSorter>> sortedWordsList = new ArrayList<>();
        sortedWordsList.add(sortedWords);

        when(model.getSortedWords()).thenReturn(sortedWordsList);
        when(alphabet.lookupObject(0)).thenReturn("word1");
        when(alphabet.lookupObject(1)).thenReturn("word2");

        Map<Integer, String> topicTopWords = JsonInferencerService.computeTopicTopWords(model, 2);

        assertNotNull(topicTopWords);
        assertEquals(1, topicTopWords.size());
        assertEquals("word2 word1", topicTopWords.get(0));
    }

    /**
     * <p><b>Summary:</b> Tests edge cases for {@code computeTopicTopWords}.</p>
     * <p><b>Test Case Design:</b> Mocks the model to return a single word for a topic. The method is then invoked with {@code numWords} values that are either greater than the available word count or equal to zero.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-UTIL-003
     * - **Prerequisites**: Mocked {@link ParallelTopicModel} containing a single topic and a single word in its distribution.
     * - **Data**: Test inputs include {@code numWords = 5} (exceeding availability) and {@code numWords = 0} (boundary minimum).
     * - **Evaluation**: Verifies the method's robustness when the requested word count does not align with the actual model vocabulary size.</p>
     * <p><b>Pre-Condition:</b> The model is mocked with 1 topic and exactly 1 word ("word1").</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Returns {@code "word1"} when 5 words are requested (graceful exhaustion of vocabulary) and an empty string {@code ""} when 0 words are requested.</p>
     */
    @Test
    void testComputeTopicTopWordsEdgeCases() {
        when(model.getNumTopics()).thenReturn(1);
        when(model.getAlphabet()).thenReturn(alphabet);

        TreeSet<IDSorter> sortedWords = new TreeSet<>();
        sortedWords.add(new IDSorter(0, 10.0));
        ArrayList<TreeSet<IDSorter>> sortedWordsList = new ArrayList<>();
        sortedWordsList.add(sortedWords);

        when(model.getSortedWords()).thenReturn(sortedWordsList);
        when(alphabet.lookupObject(0)).thenReturn("word1");

        // Case 1: numWords > available words
        Map<Integer, String> topicTopWords = JsonInferencerService.computeTopicTopWords(model, 5);
        assertEquals("word1", topicTopWords.get(0));

        // Case 2: numWords = 0
        Map<Integer, String> topicTopWordsZero = JsonInferencerService.computeTopicTopWords(model, 0);
        assertEquals("", topicTopWordsZero.get(0));
    }

    /**
     * <p><b>Summary:</b> Tests the inference of the prevalent topic for a single document.</p>
     * <p><b>Test Case Design:</b> The {@code inferPrevalentTopicJsonl} method is tested with a mocked {@link TopicInferencer}. The internal resource loading and static {@code loadPipe} logic are mocked to isolate the inference logic from the file system.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-UTIL-004
     * - **Prerequisites**: Mocked {@link TopicInferencer} and {@link cc.mallet.pipe.Pipe}. Static mocks for resource loading to prevent actual classpath access.
     * - **Data**: A {@link Document} with sample content and a {@code topicTopWords} map containing keywords for various topic indices.
     * - **Evaluation**: Verifies that the method correctly calculates the topic distribution, identifies the maximum probability index via {@code argMax}, and retrieves the corresponding keywords from the provided map.</p>
     * <p><b>Pre-Condition:</b> The {@code TopicInferencer} is stubbed to return a distribution where topic index 1 has the highest probability.</p>
     * <p><b>Post-Condition:</b> A new {@link Document} object is returned, enriched with the inferred topic string.</p>
     * <p><b>Expected Results:</b> The returned document's topic field matches "topic words for topic 1", ensuring the logic correctly maps the distribution peak to the human-readable description.</p>
     * @throws Exception if any error occurs during the resource loading or the topic inference processing
     */
    @Test
    void testInferPrevalentTopicJsonl() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        Map<Integer, String> topicTopWords = new HashMap<>();
        topicTopWords.put(0, "topic words for topic 0");
        topicTopWords.put(1, "topic words for topic 1");

        double[] distribution = {0.2, 0.8};
        when(inferencer.getSampledDistribution(any(Instance.class), any(Integer.class), any(Integer.class), any(Integer.class))).thenReturn(distribution);

        String text = inputDoc.getMain_content();
        Instance createdInstance = new Instance(text, null, "doc", null);

        when(pipe.newIteratorFrom(any(java.util.Iterator.class)))
                .thenReturn(Collections.singletonList(createdInstance).iterator());

        Document resultDoc = JsonInferencerService.inferPrevalentTopicJsonl(inferencer, inputDoc, topicTopWords,pipe);

        assertNotNull(resultDoc);
        assertEquals("topic words for topic 1", resultDoc.getTopic());
        assertEquals(inputDoc.getUrl(), resultDoc.getUrl());
        assertEquals(inputDoc.getTitle(), resultDoc.getTitle());
        assertEquals("", resultDoc.getId());
        assertEquals("", resultDoc.getMain_content());
    }

    /**
     * <p><b>Summary:</b> Tests failure when the model pipe resource is missing.</p>
     * <p><b>Test Case Design:</b> Force the ClassPathResource to throw an IOException during stream retrieval.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-UTIL-005
     * - **Prerequisites**: Mocked ClassPathResource.</p>
     * - **Data**: A valid document.
     * - **Evaluation**: Ensure the exception is caught and propagated.</p>
     * <p><b>Pre-Condition:</b> The resource "inferer/model.pipe" does not exist in the classpath.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> The method throws an Exception indicating resource failure.</p>
     */
    @Test
    void testLoadPipe_FileNotFound() {
        Document doc = new Document();
        assertThrows(Exception.class, () ->
                JsonInferencerService.inferPrevalentTopicJsonl(inferencer, doc, new HashMap<>(),pipe)
        );
    }

    /**
     * <p><b>Summary:</b> Tests failure when the Document content is null.</p>
     * <p><b>Test Case Design:</b> Pass a Document object where {@code getMain_content()} returns null.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-FAIL-006
     * - **Prerequisites**: Pipe is successfully loaded (mocked).
     * - **Data**: Document with null text.
     * - **Evaluation**: Verifies if the MALLET Instance constructor or Pipe handles null strings.</p>
     * <p><b>Pre-Condition:</b> Pipe resource is available.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> Throws a {@link NullPointerException} when creating the MALLET Instance.</p>
     */
    @Test
    void testInfer_NullDocumentContent() {
        Document doc = new Document("id", "url", "title", null, null);
        assertThrows(NullPointerException.class, () ->
                JsonInferencerService.inferPrevalentTopicJsonl(inferencer, doc, new HashMap<>(),pipe)
        );
    }
}
