package it.unipd.inferer.service;

import cc.mallet.topics.ParallelTopicModel;
import it.unipd.inferer.dto.Document;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocServiceImpl implements DocService{

    private ParallelTopicModel model;
    private Map<Integer, String> topicTopWords;

    @PostConstruct
    public void init() throws Exception {
        this.model = loadModel();
        this.topicTopWords =
                JsonInferencerService.computeTopicTopWords(model, 10);
    }

    public Document infer(Document doc) throws Exception {
        return JsonInferencerService.inferPrevalentTopicJsonl(
                model.getInferencer(),
                doc,
                topicTopWords
        );
    }

    public List<Document> inferBatch(List<Document> doc) throws Exception {
        List<Document> results = new ArrayList<>(doc.size());

        for (Document d : doc) {
            results.add(
                    JsonInferencerService.inferPrevalentTopicJsonl(
                            model.getInferencer(), d, topicTopWords
                    )
            );
        }
        return results;
    }

    private ParallelTopicModel loadModel() throws Exception {
        ClassPathResource resource = new ClassPathResource("inferer/inferer.model");
        try (ObjectInputStream in = new ObjectInputStream(resource.getInputStream())) {
            return (ParallelTopicModel) in.readObject();
        }
    }
}