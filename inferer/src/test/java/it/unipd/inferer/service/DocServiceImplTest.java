package it.unipd.inferer.service;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import it.unipd.inferer.dto.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * <strong> Class DocServiceImplTest </strong>
 *
 * <p><b>Summary</b>:
 * This test suite validates the implementation of the {@link DocServiceImpl} class.
 * It ensures that the service correctly handles single and batch document inference requests
 * by delegating to the underlying inference utilities.
 * <p><b>Test Suite Design</b>:
 * This is a unit test suite that tests the {@link DocServiceImpl} in isolation.
 * Dependencies, such as {@link ParallelTopicModel}, are mocked using Mockito.
 * The static methods of {@link JsonInferencerService} are also mocked to control
 * the inference behavior and verify that the service interacts with them correctly.
 * Reflection is used to inject the mocked model, bypassing the file-loading logic
 * in the {@code @PostConstruct} method.
 * @author Leonardo Ongaro - 2197813
 */
@ExtendWith(MockitoExtension.class)
public class DocServiceImplTest {

    @Mock
    private ParallelTopicModel model;

    @Mock
    private TopicInferencer topicInferencer;

    @InjectMocks
    private DocServiceImpl docService;

    private MockedStatic<JsonInferencerService> jsonInferencerServiceMock;

    @BeforeEach
    void setUp() {
        // Mock the static JsonInferencerService class
        jsonInferencerServiceMock = Mockito.mockStatic(JsonInferencerService.class);

        // Mock the behavior of the model and its inferer
        when(model.getInferencer()).thenReturn(topicInferencer);

        // Use reflection to inject the mock model and a dummy map for topic words,
        // bypassing the @PostConstruct init() method that loads the real model from a file.
        Map<Integer, String> topicTopWords = new HashMap<>();
        topicTopWords.put(0, "test topic words");
        ReflectionTestUtils.setField(docService, "model", model);
        ReflectionTestUtils.setField(docService, "topicTopWords", topicTopWords);
    }

    @AfterEach
    void tearDown() {
        // Close the static mock after each test to avoid state leakage
        jsonInferencerServiceMock.close();
    }

    /**
     * <p><b>Summary:</b> Tests topic inference for a single document.
     * <p><b>Test Case Design:</b> The {@code infer} method is called with a sample document. The static {@code JsonInferencerService.inferPrevalentTopicJsonl} method is mocked to return a predictable, processed document.
     * <p><b>Test Description:</b> This test verifies that the {@code infer} method correctly invokes the underlying static inference utility and returns its result without modification.
     * <p><b>Pre-Condition:</b> The {@code DocServiceImpl} is initialized with a mocked {@code ParallelTopicModel}. The {@code JsonInferencerService} is mocked to return a document with an inferred topic.
     * <p><b>Post-Condition:</b> The method returns the exact document object provided by the mocked static method.
     * <p><b>Expected Results:</b> The returned document's topic field should match the value set by the mocked service ("inferred_topic").
     * @throws Exception if any error occurs during the inference
     */
    @Test
    void testInfer() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        Document expectedDoc = new Document("id1", "url1", "title1", "main_content1", "inferred_topic");
        Map<Integer, String> topicTopWords = (Map<Integer, String>) ReflectionTestUtils.getField(docService, "topicTopWords");

        jsonInferencerServiceMock.when(() -> JsonInferencerService.inferPrevalentTopicJsonl(
                eq(topicInferencer),
                any(Document.class),
                eq(topicTopWords)
        )).thenReturn(expectedDoc);

        Document resultDoc = docService.infer(inputDoc);

        assertNotNull(resultDoc);
        assertEquals("inferred_topic", resultDoc.getTopic());
        assertEquals(expectedDoc, resultDoc);
    }

    /**
     * <p><b>Summary:</b> Tests topic inference for a batch of documents.
     * <p><b>Test Case Design:</b> The {@code inferBatch} method is called with a list containing a single sample document. The static {@code JsonInferencerService.inferPrevalentTopicJsonl} method is mocked to return a predictable, processed document.
     * <p><b>Test Description:</b> This test ensures that the {@code inferBatch} method iterates through the list of documents, calls the underlying static inference utility for each one, and collects the results into a list.
     * <p><b>Pre-Condition:</b> The {@code DocServiceImpl} is initialized with a mocked {@code ParallelTopicModel}. The {@code JsonInferencerService} is mocked to return a document with an inferred topic for each input document.
     * <p><b>Post-Condition:</b> The method returns a list of processed documents.
     * <p><b>Expected Results:</b> The returned list should contain one document, and its topic field should be "inferred_topic".
     * @throws Exception if any error occurs during the batch inference
     */
    @Test
    void testInferBatch() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        List<Document> inputList = Collections.singletonList(inputDoc);
        Document expectedDoc = new Document("id1", "url1", "title1", "main_content1", "inferred_topic");
        Map<Integer, String> topicTopWords = (Map<Integer, String>) ReflectionTestUtils.getField(docService, "topicTopWords");

        jsonInferencerServiceMock.when(() -> JsonInferencerService.inferPrevalentTopicJsonl(
                eq(topicInferencer),
                any(Document.class),
                eq(topicTopWords)
        )).thenReturn(expectedDoc);

        List<Document> resultList = docService.inferBatch(inputList);

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals("inferred_topic", resultList.getFirst().getTopic());
        assertEquals(expectedDoc, resultList.getFirst());
    }
}
