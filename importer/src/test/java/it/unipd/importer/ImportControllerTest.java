package it.unipd.importer;

import it.unipd.importer.api.ImporterController;
import it.unipd.importer.service.ImporterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * <strong> Class ImportControllerTest </strong>
 *
 * <p><b>Summary</b>: This test suite provides comprehensive validation for the Importer Controller
 * REST endpoints. It focuses on the multipart file upload functionality, specifically
 * handling JSONL (JSON Lines) formatted files and associated index parameters.</p>
 *
 * <p><b>Test Suite Design</b>: The suite uses Spring Boot's @WebMvcTest to isolate the web layer.
 * It mocks the ImporterService to avoid loading the full microservice context or external
 * Dockerized dependencies (like Elasticsearch or Databases) during this slice test.
 * The tests are designed to verify correct HTTP status codes, parameter mapping, and
 * error handling for invalid request methods.</p>
 *
 * @author Leonardo Ongaro - 2197813
 */

@WebMvcTest(ImporterController.class)
public class ImportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImporterService importerService;

    /**
     * <p><b>Summary:</b> Verifies the successful start of a bulk import process via JSONL file upload.</p>
     * <p><b>Test Case Design:</b> Uses MockMvc to perform a multipart/form-data POST request.
     * It simulates the 'curl -F' behavior by attaching both a file and a string parameter.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-IMPORT-001
     * - **Prerequisites**: Spring context loaded for ImporterController.
     * - **Data**: A MockMultipartFile containing two valid JSONL lines and "indexName" set to "guardian".
     * - **Evaluation**: Expects HTTP 200 OK and "Import avviato" message.</p>
     * <p><b>Pre-Condition:</b> The REST endpoint is active and the ImporterService is mocked.</p>
     * <p><b>Post-Condition:</b> The request is successfully handed off to the service layer.</p>
     * <p><b>Expected Results:</b> The controller returns a 200 OK status and the body text "Import avviato".</p>
     * @throws Exception if any error occurs during MockMvc execution
     */
    @Test
    void shouldReturnImportAvviato() throws Exception {
        String fileContent =
                "{{\"id\":\"id1\",\"title\":\"Title1\",\"url\":\"https://www.google.com\",\"main_content\":\"Main content 1\"}" +
                "{{\"id\":\"id2\",\"title\":\"Title2\",\"url\":\"https://www.google.com\",\"main_content\":\"Main content 2\"}";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "guardian.jsonl",
                "application/x-jsonlines",
                fileContent.getBytes()
        );
        mockMvc.perform(multipart("/api/v1/importer/import")
                        .file(mockFile)
                        .param("indexName", "guardian")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Import avviato"));
    }

    /**
     * <p><b>Summary:</b> Verifies that the endpoint rejects improper request types (GET or empty Multipart).</p>
     * <p><b>Test Case Design:</b> Boundary testing of the API gateway layer to ensure only
     * valid multipart requests are accepted.</p>
     * <p><b>Test Description:</b>
     * - **ID**: TC-IMPORT-002
     * - **Data**: No file provided or incorrect HTTP Verb (GET).
     * - **Evaluation**: Expects a 4xx Client Error.</p>
     * <p><b>Pre-Condition:</b> Controller is loaded.</p>
     * <p><b>Post-Condition:</b> No system state change; request is blocked.</p>
     * <p><b>Expected Results:</b> System returns 405 Method Not Allowed or 400 Bad Request.</p>
     * @throws Exception if any error occurs during MockMvc execution
     */
    @Test
    void shouldReturn405() throws Exception {
        mockMvc.perform(get("/api/v1/importer/import"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(multipart("/api/v1/importer/import"))
                .andExpect(status().is4xxClientError());
    }

}
