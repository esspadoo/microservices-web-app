package it.unipd.inferer.service;

import it.unipd.inferer.dto.Document;

import java.util.List;

/**
 * Service interface defining document inference operations.
 * <p>
 * This interface represents the service layer contract for performing
 * inference tasks on {@link Document} objects. It abstracts the underlying
 * inference logic and allows different implementations to be provided
 * without affecting higher application layers (e.g., controllers).
 * </p>
 *
 * <p>
 * Implementations of this interface are expected to handle both single
 * document processing and batch processing scenarios.
 * </p>
 */
public interface DocService {

    /**
     * Performs inference on a single document.
     * <p>
     * The method receives a {@link Document} instance as input and returns
     * the same document enriched with inferred data, such as topics,
     * classifications, or metadata, depending on the concrete implementation.
     * </p>
     *
     * @param doc the document to be analyzed and processed
     * @return the processed document containing inference results
     * @throws Exception if an error occurs during the inference process
     */
    Document infer(Document doc) throws Exception;

    /**
     * Performs inference on a batch of documents.
     * <p>
     * This method is intended for bulk processing use cases, enabling
     * efficient analysis of multiple documents within a single invocation.
     * Each document in the list is processed independently by the
     * implementing service.
     * </p>
     *
     * @param doc the list of documents to be analyzed and processed
     * @return a list of documents enriched with inference results
     * @throws Exception if an error occurs during batch inference
     */
    List<Document> inferBatch(List<Document> doc) throws Exception;
}
