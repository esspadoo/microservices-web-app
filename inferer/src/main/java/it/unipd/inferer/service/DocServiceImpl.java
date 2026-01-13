package it.unipd.inferer.service;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import it.unipd.inferer.dto.Document;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service implementation providing document topic inference functionality.
 * <p>
 * This class represents the concrete implementation of the {@link DocService}
 * interface and encapsulates the logic required to load a trained topic model
 * and perform inference on documents, both individually and in batch.
 * </p>
 *
 * <p>
 * The service is managed by Spring as a singleton bean and initialized
 * automatically at application startup.
 * </p>
 */
@Service
public class DocServiceImpl implements DocService{

    /**
     * Trained topic model used for inference.
     * <p>
     * This model is loaded once during application initialization and reused
     * for all subsequent inference requests to ensure efficiency.
     * </p>
     */
    private ParallelTopicModel model;

    /**
     * Mapping between topic identifiers and their most representative words.
     * <p>
     * This structure is precomputed after model loading and is used to enrich
     * inference results with human-readable topic descriptions.
     * </p>
     */
    private Map<Integer, String> topicTopWords;

    /**
     * Pipeline used to preprocess documents.
     * <p>
     * This pipeline is loaded once during application initialization and reused
     * for all subsequent inference requests to ensure efficiency.
     * </p>
     */
    private Pipe pipe;

    /**
     * Service initialization method.
     * <p>
     * This method is automatically invoked by the Spring container after
     * dependency injection is completed. It loads the trained topic model
     * from the application classpath and precomputes the top words for each topic.
     * </p>
     *
     * @throws Exception if the model cannot be loaded or initialized correctly
     */
    @PostConstruct
    public void init() throws Exception {
        this.model = loadModel();
        this.topicTopWords =
                JsonInferencerService.computeTopicTopWords(model, 10);
        this.pipe = loadPipe();
    }

    /**
     * Performs topic inference on a single document.
     * <p>
     * The document is processed using the model inferencer and enriched with
     * the most prevalent topic and its associated keywords.
     * </p>
     *
     * @param doc the document to be analyzed
     * @return the document enriched with inference results
     * @throws Exception if an error occurs during inference
     */
    public Document infer(Document doc) throws Exception {
        return JsonInferencerService.inferPrevalentTopicJsonl(
                model.getInferencer(),
                doc,
                topicTopWords,
                pipe
        );
    }

    /**
     * Performs topic inference on a batch of documents.
     * <p>
     * Each document in the input list is processed independently using
     * the same trained model. The results are collected and returned
     * in a new list preserving the original order.
     * </p>
     *
     * @param doc the list of documents to be analyzed
     * @return a list of documents enriched with inference results
     * @throws Exception if an error occurs during batch inference
     */
    public List<Document> inferBatch(List<Document> doc) throws Exception {
        List<Document> results = new ArrayList<>(doc.size());

        for (Document d : doc) {
            results.add(
                    JsonInferencerService.inferPrevalentTopicJsonl(
                            model.getInferencer(), d, topicTopWords, pipe
                    )
            );
        }
        return results;
    }

    /**
     * Loads the trained topic model from the application classpath.
     * <p>
     * The model is deserialized from a binary file packaged within the
     * application resources. This method is intended to be invoked only
     * during service initialization.
     * </p>
     *
     * @return the deserialized {@link ParallelTopicModel}
     * @throws Exception if the model file cannot be found or deserialized
     */
    private ParallelTopicModel loadModel() throws Exception {
        ClassPathResource resource = new ClassPathResource("inferer/inferer.model");
        try (ObjectInputStream in = new ObjectInputStream(resource.getInputStream())) {
            return (ParallelTopicModel) in.readObject();
        }
    }


    /**
     * Loads a pre-configured MALLET {@link Pipe} from the classpath.
     *
     * <p>
     * This method reads the serialized pipeline object from the resource
     * file <code>inferer/model.pipe</code> and deserializes it into a {@link Pipe}
     * instance. The returned pipeline must match the configuration used during
     * the training of the topic model.
     * </p>
     *
     * <p>
     * The method uses a try-with-resources block to safely handle the input stream
     * and ensure it is closed after reading the object.
     * </p>
     *
     * @return a deserialized {@link Pipe} object ready for preprocessing documents
     * @throws Exception if the resource cannot be found, read, or deserialized
     */
    private static Pipe loadPipe() throws Exception {
        ClassPathResource resource = new ClassPathResource("inferer/model.pipe");
        try (ObjectInputStream in = new ObjectInputStream(resource.getInputStream())) {
            return (Pipe) in.readObject();
        }
    }
}
