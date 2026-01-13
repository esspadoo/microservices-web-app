package it.unipd.inferer.service;

import cc.mallet.pipe.*;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
 */
@ExtendWith(MockitoExtension.class)
public class DocServiceImplTest {

    @Mock
    private ParallelTopicModel model;

    @Mock
    private TopicInferencer topicInferencer;

    @Mock
    private Pipe pipe;

    @InjectMocks
    private DocServiceImpl docService;

    private MockedStatic<JsonInferencerService> jsonInferencerServiceMock;

    @BeforeEach
    void setUp() {
        jsonInferencerServiceMock = Mockito.mockStatic(JsonInferencerService.class);

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
     * <p><b>Summary:</b> Tests topic inference for a single document.</p>
     * <p><b>Test Case Design:</b> The {@code infer} method is called with a sample document. The static {@code JsonInferencerService.inferPrevalentTopicJsonl} method is mocked to return a predictable, processed document.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-INF-001
     * - **Prerequisites**: The {@code DocServiceImpl} is injected with a mocked {@code ParallelTopicModel}, and {@code JsonInferencerService} static behavior is controlled via Mockito.
     * - **Data**: A single {@link Document} object.
     * - **Evaluation**: Verifies that the {@code infer} method correctly invokes the underlying static inference utility and returns its result without modification.</p>
     * <p><b>Pre-Condition:</b> Service is initialized with a model; the static inferencer is mocked to return a document with the topic "inferred_topic".</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> The returned document object is the same instance provided by the mocked static method, and its topic field matches "inferred_topic".</p>
     * @throws Exception if any error occurs during the inference processing
     */
    @Test
    void testInfer() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        Document expectedDoc = new Document("id1", "url1", "title1", "main_content1", "inferred_topic");
        Map<Integer, String> topicTopWords = (Map<Integer, String>) ReflectionTestUtils.getField(docService, "topicTopWords");

        when(model.getInferencer()).thenReturn(topicInferencer);
        jsonInferencerServiceMock.when(() -> JsonInferencerService.inferPrevalentTopicJsonl(
                eq(topicInferencer),
                any(Document.class),
                eq(topicTopWords),
                eq(pipe)
        )).thenReturn(expectedDoc);

        Document resultDoc = docService.infer(inputDoc);

        assertNotNull(resultDoc);
        assertEquals("inferred_topic", resultDoc.getTopic());
        assertEquals(expectedDoc, resultDoc);
    }

    /**
     * <p><b>Summary:</b> Verifies failure when the model inferencer is missing.</p>
     * <p><b>Test Case Design:</b> Mock the model to throw a NullPointerException when accessing the inferencer.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOC-002
     * - **Prerequisites**: ParallelTopicModel returns null for getInferencer().
     * - **Data**: Valid Document.
     * - **Evaluation**: Ensure the exception propagates upwards.</p>
     * <p><b>Pre-Condition:</b> Service is injected with a faulty model.</p>
     * <p><b>Post-Condition:</b> Exception is thrown.</p>
     * <p><b>Expected Results:</b> An Exception (NullPointerException) is thrown by the service.</p>
     */
    @Test
    void testInfer_Failure_ModelError() {
        Document inputDoc = new Document();
        when(model.getInferencer()).thenThrow(new RuntimeException("Model not ready"));

        assertThrows(Exception.class, () -> docService.infer(inputDoc));
    }

    /**
     * <p><b>Summary:</b> Tests topic inference for a batch of documents.</p>
     * <p><b>Test Case Design:</b> The {@code inferBatch} method is called with a list containing a single sample document. The static {@code JsonInferencerService.inferPrevalentTopicJsonl} method is mocked to return a predictable, processed document.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-INF-003
     * - **Prerequisites**: The {@code DocServiceImpl} is initialized with a mocked {@code ParallelTopicModel}, and {@code JsonInferencerService} static behavior is controlled via Mockito.
     * - **Data**: A {@link List} containing a single {@link Document} object.
     * - **Evaluation**: Ensures that the method correctly iterates through the list, invokes the static inference utility for each entry, and collects results into a new list.</p>
     * <p><b>Pre-Condition:</b> Service is initialized; the static utility is mocked to return a document with the topic "inferred_topic" for each input.</p>
     * <p><b>Post-Condition:</b> A list of processed documents is returned to the caller.</p>
     * <p><b>Expected Results:</b> The returned list contains one document, and its topic field is correctly set to "inferred_topic".</p>
     * @throws Exception if any error occurs during the batch inference processing
     */
    @Test
    void testInferBatch() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        List<Document> inputList = Collections.singletonList(inputDoc);
        Document expectedDoc = new Document("id1", "url1", "title1", "main_content1", "inferred_topic");
        Map<Integer, String> topicTopWords = (Map<Integer, String>) ReflectionTestUtils.getField(docService, "topicTopWords");

        when(model.getInferencer()).thenReturn(topicInferencer);
        jsonInferencerServiceMock.when(() -> JsonInferencerService.inferPrevalentTopicJsonl(
                eq(topicInferencer),
                any(Document.class),
                eq(topicTopWords),
                eq(pipe)
        )).thenReturn(expectedDoc);

        List<Document> resultList = docService.inferBatch(inputList);

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals("inferred_topic", resultList.getFirst().getTopic());
        assertEquals(expectedDoc, resultList.getFirst());
    }

    /**
     * <p><b>Summary:</b> Verifies batch inference behavior with an empty list.</p>
     * <p><b>Test Case Design:</b> Pass an empty ArrayList to inferBatch.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOC-004
     * - **Prerequisites**: Service initialized.
     * - **Data**: Empty List<Document>.
     * - **Evaluation**: Ensure no processing occurs and an empty list is returned.</p>
     * <p><b>Pre-Condition:</b> None.</p>
     * <p><b>Post-Condition:</b> None.</p>
     * <p><b>Expected Results:</b> An empty list is returned without errors.</p>
     * @throws Exception if any error occurs
     */
    @Test
    void testInferBatch_EmptyList() throws Exception {
        List<Document> results = docService.inferBatch(new ArrayList<>());
        assertTrue(results.isEmpty());
        verify(model, never()).getInferencer();
    }

    /**
     * <p><b>Summary:</b> Verifies failure during batch processing if one item fails.</p>
     * <p><b>Test Case Design:</b> Mock the static utility to throw an exception on the second item.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-DOC-005
     * - **Prerequisites**: Mocked static behavior for JsonInferencerService.
     * - **Data**: List with 2 documents.
     * - **Evaluation**: Verify that an exception in one document stops the batch execution.</p>
     * <p><b>Pre-Condition:</b> Service is initialized.</p>
     * <p><b>Post-Condition:</b> Partial processing may occur, but an exception is thrown.</p>
     * <p><b>Expected Results:</b> The method throws an Exception when the internal utility fails.</p>
     */
    @Test
    void testInferBatch_Failure_InternalError() {
        List<Document> inputList = List.of(new Document(), new Document());
        when(model.getInferencer()).thenReturn(topicInferencer);

        jsonInferencerServiceMock.when(() -> JsonInferencerService.inferPrevalentTopicJsonl(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Inference failed"));

        assertThrows(Exception.class, () -> docService.inferBatch(inputList));
    }
}
