package it.unipd.importer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "api/v1/importer", produces = "application/json")
public class ImporterController {

    private final ImporterService importerService;

    public ImporterController(ImporterService importerService) {
        this.importerService = importerService;
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test, service INDEXER UP!";
    }

    @PostMapping("/import")
    public ResponseEntity<String> import_indexFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "indexName") String indexName
    ) throws Exception {
        this.importerService.indexArticles(file.getInputStream(), indexName);
        return ResponseEntity.ok("Import avviato");
    }
}
