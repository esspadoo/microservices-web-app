package it.unipd.inferer.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfererController {

    @GetMapping("/hello")
    public String hello(){
        return "Hello this is a test";
    }
}
