package it.unipd.importer.api;

import it.unipd.importer.service.ImporterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * @return HTTP 200 response indicating that the import has started
     * @throws Exception if the file cannot be read
     */
    @PostMapping("/import")
    public ResponseEntity<String> import_indexFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "indexName") String indexName
    ) throws Exception {
        this.importerService.indexArticles(file.getInputStream(), indexName);
        return ResponseEntity.ok("Import avviato");
    }
}
