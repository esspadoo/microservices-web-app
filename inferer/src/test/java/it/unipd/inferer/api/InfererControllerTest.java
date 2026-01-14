package it.unipd.inferer.api;

import it.unipd.inferer.dto.Document;
import it.unipd.inferer.service.DocService;
import it.unipd.inferer.service.DocServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <strong> Class InfererControllerTest </strong>
 *
 * <p><b>Summary</b>:
 * This test suite is responsible for verifying the functionality of the {@link InfererController}.
 * It ensures that the controller's endpoints are correctly exposed and handle HTTP requests
 * as expected. The tests cover the health-check endpoint, single document inference, and
 * batch document inference. The service layer ({@link DocService}) is mocked to isolate
 * the controller and focus on its request handling and response generation logic.
 * <p><b>Test Suite Design</b>:
 * The test suite uses Spring Boot's {@link WebMvcTest} to test the web layer without
 * a full application context. {@link MockMvc} is used to perform HTTP requests against
 * the controller, and {@link MockitoBean} is used to provide a mock implementation of the
 * {@link DocService}. This setup allows for focused, fast, and reliable tests of the
 * controller's behavior.
 */
@WebMvcTest(InfererController.class)
public class InfererControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocServiceImpl docService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * <p><b>Summary:</b> Tests the health-check endpoint.
     * <p><b>Test Case Design:</b> A GET request is sent to "/api/v1/hello".
     * <p><b>Test Description:</b> This test verifies that the "/api/v1/hello" endpoint is active and returns the expected welcome message.
     * <p><b>Pre-Condition:</b> The application is running, and the controller is up.
     * <p><b>Post-Condition:</b> The endpoint returns a 200 OK status and the correct string.
     * <p><b>Expected Results:</b> The response body should be "Hello this is a test, service INFERER UP!".
     * @throws Exception if any error occurs during MockMvc execution
     */
    @Test
    public void testHello() throws Exception {
        mockMvc.perform(get("/api/v1/inferer/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello this is a test, service INFERER UP!"));
    }

    /**
     * <p><b>Summary:</b> Tests the single document inference endpoint.
     * <p><b>Test Case Design:</b> A POST request with a sample Document object is sent to "/api/v1/infer". The DocService is mocked to return a processed document.
     * <p><b>Test Description:</b> This test ensures that the "/api/v1/infer" endpoint correctly receives a document, passes it to the service, and returns the processed document.
     * <p><b>Pre-Condition:</b> The DocService is mocked to return a specific Document when its infer method is called.
     * <p><b>Post-Condition:</b> The endpoint returns a 200 OK status and the JSON representation of the processed document.
     * <p><b>Expected Results:</b> The response body should be the JSON of the document returned by the mocked service.
     * @throws Exception if any error occurs during MockMvc execution or JSON processing.
     */
    @Test
    public void testInferTopics() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        Document outputDoc = new Document("id1", "url1", "title1", "main_content1", "inferred_topic");

        when(docService.infer(any(Document.class))).thenReturn(outputDoc);

        mockMvc.perform(post("/api/v1/inferer/infer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDoc)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(outputDoc)));
    }

    /**
     * <p><b>Summary:</b> Tests the batch document inference endpoint.
     * <p><b>Test Case Design:</b> A POST request with a list of Document objects is sent to "/api/v1/inferBatch". The DocService is mocked to return a list of processed documents.
     * <p><b>Test Description:</b> This test verifies that the "/api/v1/inferBatch" endpoint can handle a list of documents, delegate processing to the service, and return the list of results.
     * <p><b>Pre-Condition:</b> The DocService is mocked to return a specific list of Documents when its inferBatch method is called.
     * <p><b>Post-Condition:</b> The endpoint returns a 200 OK status and the JSON representation of the list of processed documents.
     * <p><b>Expected Results:</b> The response body should be the JSON of the list of documents returned by the mocked service.
     * @throws Exception if any error occurs during MockMvc execution or JSON processing.
     */
    @Test
    public void testInferBatchTopics() throws Exception {
        Document inputDoc = new Document("id1", "url1", "title1", "main_content1", null);
        Document outputDoc = new Document("id1", "url1", "title1", "main_content1", "inferred_topic");
        List<Document> inputList = Collections.singletonList(inputDoc);
        List<Document> outputList = Collections.singletonList(outputDoc);

        when(docService.inferBatch(any(List.class))).thenReturn(outputList);

        mockMvc.perform(post("/api/v1/inferer/inferBatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputList)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(outputList)));
    }
}
