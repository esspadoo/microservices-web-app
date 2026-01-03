package it.unipd.inferer.api;

import it.unipd.inferer.dto.Document;
import it.unipd.inferer.service.DocService;
import it.unipd.inferer.service.DocServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for exposing inference-related APIs.
 * <p>
 * This controller represents the entry point of the INFERER service and
 * handles HTTP requests related to topic inference on {@link Document}
 * objects, both for single documents and batch processing.
 * </p>
 *
 * <p>
 * The controller is mapped under the base path <code>/api/v1/</code> and
 * follows RESTful design principles using Spring Boot annotations.
 * </p>
 *
 * <p>
 * The controller delegates all business logic to the service layer,
 * ensuring a clear separation of concerns.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/")
public class InfererController {

    /**
     * Service layer component responsible for executing the inference logic.
     * <p>
     * This dependency abstracts the business logic and allows the controller
     * to delegate the actual processing of documents without handling
     * implementation details.
     * </p>
     */
    private final DocService docService;

    /**
     * Constructor-based dependency injection of the {@link DocService}.
     * <p>
     * This approach promotes immutability, testability, and loose coupling
     * between the controller and the service layer.
     * </p>
     *
     * @param docService the concrete implementation of {@link DocService}
     *                   used to perform document inference operations
     */
    public InfererController(DocServiceImpl docService){
        this.docService = docService;
    }

    /**
     * Health-check and test endpoint.
     * <p>
     * This endpoint can be used to verify that the INFERER service is running
     * correctly and is able to respond to HTTP requests.
     * </p>
     *
     * @return a static confirmation message indicating that the service is up
     */
    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test, service INFERER UP!";
    }

    /**
     * Performs topic inference on a single document.
     * <p>
     * The input {@link Document} is received as a JSON payload and passed
     * to the service layer, which enriches it with inferred topics or metadata.
     * </p>
     *
     * @param doc the document to be processed, provided in the request body
     * @return the processed document containing the inference results
     * @throws Exception if an error occurs during the inference process
     */
    @PostMapping("/infer")
    public Document inferTopics(@RequestBody Document doc) throws Exception {
        return docService.infer(doc);
    }

    /**
     * Performs topic inference on a batch of documents.
     * <p>
     * This endpoint is designed for bulk processing scenarios, allowing
     * multiple documents to be analyzed within a single request.
     * </p>
     *
     * @param doc a list of documents to be processed, provided in the request body
     * @return a list of documents enriched with inference results
     * @throws Exception if an error occurs during batch inference
     */
    @PostMapping("/inferBatch")
    public List<Document> inferBatchTopics(@RequestBody List<Document> doc) throws Exception {
        return docService.inferBatch(doc);
    }
}

