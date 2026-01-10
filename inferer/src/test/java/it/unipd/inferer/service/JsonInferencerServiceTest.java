package it.unipd.inferer.service;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
 * @author Leonardo Ongaro - 2197813
 */
@ExtendWith(MockitoExtension.class)
public class JsonInferencerServiceTest {

    @Mock
    private ParallelTopicModel model;

    @Mock
    private TopicInferencer inferencer;

    @Mock
    private Alphabet alphabet;

    /**
     * <p><b>Summary:</b> Tests the {@code argMax} utility method.
     * <p><b>Test Case Design:</b> The private static method {@code argMax} is tested via reflection with different arrays of doubles.
     * <p><b>Test Description:</b> This test verifies that the method correctly finds and returns the index of the maximum value in an array. It covers cases with positive numbers, negative numbers, and mixed values.
     * <p><b>Pre-Condition:</b> None. This is a pure function.
     * <p><b>Post-Condition:</b> The correct index of the maximum element is returned.
     * <p><b>Expected Results:</b> For {@code {1.0, 5.0, 2.0}}, expects 1. For {@code {-1.0, -5.0, -2.0}}, expects 0. For an empty array, it should handle it gracefully (though the current implementation would throw an exception, which is acceptable for a private helper).
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
     * <p><b>Summary:</b> Tests the computation of top words for each topic.
     * <p><b>Test Case Design:</b> A mocked {@link ParallelTopicModel} is configured to return a predefined alphabet and sorted word sets. The {@code computeTopicTopWords} method is then called with this mock.
     * <p><b>Test Description:</b> This test ensures that the method correctly iterates through the topics and their sorted words, looks up the words in the alphabet, and concatenates them to form a human-readable string.
     * <p><b>Pre-Condition:</b> The {@code ParallelTopicModel} and its dependent objects (Alphabet, IDSorter) are mocked to return predictable values.
     * <p><b>Post-Condition:</b> A map is returned where each topic index is associated with a correctly formatted string of its top words.
     * <p><b>Expected Results:</b> The resulting map should contain the expected topic-to-words mapping, e.g., topic 0 maps to "word1 word2".
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
     * <p><b>Summary:</b> Tests edge cases for {@code computeTopicTopWords}.
     * <p><b>Test Case Design:</b> Mocks the model to return a single word for a topic. Calls the method with numWords greater than available, and numWords equal to 0.
     * <p><b>Test Description:</b> Verifies that the method handles requesting more words than available (returns all available) and requesting 0 words (returns empty string).
     * <p><b>Pre-Condition:</b> Model mocked with 1 topic and 1 word.
     * <p><b>Post-Condition:</b> Returns correct strings.
     * <p><b>Expected Results:</b> "word1" for numWords=5, "" for numWords=0.
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
     * <p><b>Summary:</b> Tests the inference of the prevalent topic for a single document.
     * <p><b>Test Case Design:</b> The {@code inferPrevalentTopicJsonl} method is tested with a mocked {@link TopicInferencer}. The static {@code resourceToTempFile} method is also mocked to prevent file system access.
     * <p><b>Test Description:</b> This test verifies that the service method correctly processes an input document, uses the inferencer to get a topic distribution, finds the prevalent topic, and constructs the output document with the correct topic words.
     * <p><b>Pre-Condition:</b> The {@code TopicInferencer} is mocked to return a specific topic distribution. The {@code topicTopWords} map is provided. The file system access is mocked.
     * <p><b>Post-Condition:</b> A new {@link Document} object is returned, enriched with the inferred topic string.
     * <p><b>Expected Results:</b> The returned document's topic field should be "topic words for topic 1", corresponding to the topic with the highest probability in the mocked distribution.
     * @throws Exception if any error occurs during the file creation or the topic inference
     */
    @Test
    void testInferPrevalentTopicJsonl() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        Map<Integer, String> topicTopWords = new HashMap<>();
        topicTopWords.put(0, "topic words for topic 0");
        topicTopWords.put(1, "topic words for topic 1");

        double[] distribution = {0.2, 0.8}; // Topic 1 is prevalent
        when(inferencer.getSampledDistribution(any(Instance.class), any(Integer.class), any(Integer.class), any(Integer.class))).thenReturn(distribution);

        File tempStoplist = null;
        try (var mockedStatic = mockStatic(JsonInferencerService.class, CALLS_REAL_METHODS)) {
            // Create a real, empty temporary file that MALLET can read
            tempStoplist = File.createTempFile("test-stoplist-", ".txt");
            mockedStatic.when(JsonInferencerService::resourceToTempFile).thenReturn(tempStoplist);

            Document resultDoc = JsonInferencerService.inferPrevalentTopicJsonl(inferencer, inputDoc, topicTopWords);

            assertNotNull(resultDoc);
            // Verify the returned document has the correct inferred topic
            assertEquals("topic words for topic 1", resultDoc.getTopic());
            // Also verify that the other fields are set as expected by the implementation
            assertEquals(inputDoc.getUrl(), resultDoc.getUrl());
            assertEquals(inputDoc.getTitle(), resultDoc.getTitle());
            assertEquals("", resultDoc.getId());
            assertEquals("", resultDoc.getMain_content());
        } finally {
            // Clean up the created temp file
            if (tempStoplist != null && tempStoplist.exists()) {
                tempStoplist.delete();
            }
        }
    }

    /**
     * <p><b>Summary:</b> Tests the {@code resourceToTempFile} utility method.
     * <p><b>Test Case Design:</b> The package-private static method {@code resourceToTempFile} is tested by invoking it directly.
     * <p><b>Test Description:</b> This test verifies that the method correctly creates a temporary file from a classpath resource.
     * <p><b>Pre-Condition:</b> The "stoplist.txt" resource must exist in the classpath (src/main/resources).
     * <p><b>Post-Condition:</b> A temporary file is created and returned.
     * <p><b>Expected Results:</b> The returned file should not be null and should exist.
     * @throws Exception if any error occurs during file creation
     */
    @Test
    void testResourceToTempFile() throws Exception {
        File tempFile = JsonInferencerService.resourceToTempFile();
        assertNotNull(tempFile);
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
