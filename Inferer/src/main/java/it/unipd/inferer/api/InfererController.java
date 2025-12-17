package it.unipd.inferer.api;

import it.unipd.inferer.dto.Document;
import it.unipd.inferer.service.DocService;
import it.unipd.inferer.service.DocServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class InfererController {

    private DocService docService;

    public InfererController(DocServiceImpl docService){
        this.docService = docService;
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test, service UP!";
    }

    @PostMapping("/infer")
    public Document inferTopics(@RequestBody Document doc) throws Exception {
        return docService.infer(doc);
    }

    @PostMapping("/infer")
    public List<Document> inferBatchTopics(@RequestBody List<Document> doc) throws Exception {
        return docService.inferBatch(doc);
    }
}
