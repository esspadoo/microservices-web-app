package it.unipd.importer.api;

import it.unipd.importer.service.ImporterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.UUID;

/**
 * REST controller exposing endpoints for importing data into Elasticsearch.
 *
 * <p>This controller allows clients to upload NDJSON files and trigger
 * asynchronous bulk indexing operations.
 */
@RestController
@RequestMapping(value = "api/v1/importer", produces = "application/json")
public class ImporterController {
    private final ImporterService importerService;

    /**
     * Constructs the ImporterController.
     *
     * @param importerService service responsible for handling import operations
     */
    public ImporterController(ImporterService importerService) {
        this.importerService = importerService;
    }

    /**
     * Test endpoint used to verify that the importer service is running.
     *
     * @return a simple status message
     */
    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test, service INDEXER UP!";
    }

    /**
     * Uploads an NDJSON file and starts an asynchronous import into Elasticsearch.
     *
     * <p>The method returns immediately after the import process has been started.
     *
     * @param file      NDJSON file containing documents to be indexed
     * @param indexName name of the Elasticsearch index
     * @return
     * <ul>
     * <li>HTTP 200 response indicating that the import has started, along with a job ID</li>
     * <li>HTTP 400 response if the file is empty or invalid format</li>
     * </ul>
     * @throws Exception if the file cannot be read
     */
    @PostMapping("/import")
    public ResponseEntity<String> import_indexFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "indexName") String indexName
    ) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        
        // Save the file to a temporary location
        File tempFile = File.createTempFile("import-", ".ndjson");
        file.transferTo(tempFile);
        
        // Validate file format (basic check)
        boolean isValid = false;
        boolean isEmpty = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                isEmpty = false;
                if (line.trim().startsWith("{")) {
                    isValid = true;
                }
                break;
            }
        }

        if (isEmpty) {
            tempFile.delete();
            return ResponseEntity.badRequest().body("File contains no valid data");
        }

        if (!isValid) {
            tempFile.delete();
            return ResponseEntity.badRequest().body("Invalid file format: content must be JSONL (lines starting with '{')");
        }
        
        String jobId = UUID.randomUUID().toString();
        this.importerService.indexArticles(tempFile, indexName, jobId);
        return ResponseEntity.ok("Import avviato. Job ID: " + jobId);
    }

    /**
     * Checks the status of an import job.
     *
     * @param jobId the ID of the job to check
     * @return
     * <ul>
     * <li>HTTP 200 response indicating that the request was valid, along with the status of the job</li>
     * <li>HTTP 400 response indicating the job id was invalid</li>
     * </ul>
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<String> getStatus(@PathVariable String jobId) {
        String status = importerService.getJobStatus(jobId);
        if(status.equals("UNKNOWN")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found");
        } else {
            return ResponseEntity.ok(status);
        }
    }
}
