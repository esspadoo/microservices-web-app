package it.unipd.inferer.service;

import cc.mallet.pipe.*;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;

import it.unipd.inferer.dto.Document;
import org.springframework.core.io.ClassPathResource;

import java.io.ObjectInputStream;
import java.util.*;

/**
 * Utility service for topic inference and model introspection using MALLET.
 *
 * <p>
 * This class provides a set of static helper methods to perform topic inference
 * on textual documents using a pre-trained {@link ParallelTopicModel}.
 * It handles:
 * </p>
 *
 * <ul>
 *   <li>Preprocessing raw text using a MALLET-compatible pipeline</li>
 *   <li>Inferring topic distributions for unseen documents</li>
 *   <li>Determining the most prevalent topic for a document</li>
 *   <li>Extracting human-readable topic representations</li>
 * </ul>
 *
 * <p>
 * The class is stateless and thread-safe, assuming the underlying
 * {@link TopicInferencer} is thread-safe.
 * </p>
 *
 * <p>
 * This service is designed as a utility class and is not managed
 * by the Spring container.
 * </p>
 */
public class JsonInferencerService {

    /**
     * Infers the most prevalent topic for a given document.
     * <p>
     * The document text is preprocessed using a pipeline matching the configuration
     * used during model training. The topic distribution is computed using Gibbs
     * sampling, and the topic with the highest probability is selected.
     * </p>
     *
     * <p>
     * The returned {@link Document} is enriched with a human-readable representation
     * of the inferred topic, expressed by its most representative words.
     * </p>
     *
     * @param inferencer the MALLET topic inferencer derived from a trained model
     * @param doc the input document to analyze
     * @param topicTopWords a mapping of topic identifiers to their top words
     * @return a new {@link Document} containing the inference result
     * @throws Exception if preprocessing, inference, or resource loading fails
     */
    public static Document inferPrevalentTopicJsonl(
            TopicInferencer inferencer,
            Document doc,
            Map<Integer, String> topicTopWords
    ) throws Exception {
        String url = doc.getUrl();
        String title = doc.getTitle();
        String text = doc.getMain_content();

        // Build Pipe (must match training configuration)
        Pipe pipe = loadPipe();
        InstanceList instances = new InstanceList(pipe);

        // Create single-instance input
        Instance instance = new Instance(text, null, "doc", null);
        instances.addThruPipe(instance);

        Instance processedInstance = instances.getFirst();

        // Infer topic distribution
        double[] distribution =
                inferencer.getSampledDistribution(
                        processedInstance, 1000, 100, 10
                );

        int prevalentTopic = argMax(distribution);

        String prevalentTopicWords = topicTopWords.get(prevalentTopic);

        // Build output Document
        return new Document("", url, title, "", prevalentTopicWords);
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


    /**
     * Returns the index of the maximum value in a numeric array.
     * <p>
     * This utility is used to identify the most prevalent topic by selecting
     * the topic with the highest probability in the inferred distribution.
     * </p>
     *
     * @param values an array of double values
     * @return the index of the maximum value
     */
    private static int argMax(double[] values) {
        int maxIndex = 0;
        double maxValue = values[0];

        for (int i = 1; i < values.length; i++) {
            if (values[i] > maxValue) {
                maxValue = values[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Computes a human-readable representation of topics by extracting
     * the most representative words for each topic.
     *
     * <p>
     * For each topic in the model, the method retrieves the top {@code numWords}
     * terms ranked by weight and concatenates them into a single string.
     * </p>
     *
     * @param model the trained {@link ParallelTopicModel}
     * @param numWords the number of top words to extract per topic
     * @return a map associating each topic index with its representative words
     */
    public static Map<Integer, String> computeTopicTopWords(
            ParallelTopicModel model,
            int numWords
    ) {
        Alphabet alphabet = model.getAlphabet();
        ArrayList<TreeSet<IDSorter>> sortedWords = model.getSortedWords();

        Map<Integer, String> topicTopWords = new HashMap<>();

        for (int topic = 0; topic < model.getNumTopics(); topic++) {
            Iterator<IDSorter> iterator = sortedWords.get(topic).iterator();
            StringBuilder sb = new StringBuilder();
            int count = 0;

            while (iterator.hasNext() && count < numWords) {
                IDSorter idCountPair = iterator.next();
                String word = (String) alphabet.lookupObject(idCountPair.getID());
                sb.append(word).append(' ');
                count++;
            }

            topicTopWords.put(topic, sb.toString().trim());
        }

        return topicTopWords;
    }

}
