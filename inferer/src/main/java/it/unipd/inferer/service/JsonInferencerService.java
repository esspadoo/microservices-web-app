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

import java.io.File;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
//changed loading of the pipe instead of hardcoded
//added method load pipe, make tempfile method private(not used), removed tmp stoplist file.
/**
 * Utility service for topic inference and topic model introspection using MALLET.
 *
 * <p>
 * This class provides a collection of static helper methods to perform topic
 * inference on textual documents using a pre-trained
 * {@link cc.mallet.topics.ParallelTopicModel}.
 * It is responsible for:
 * </p>
 *
 * <ul>
 *   <li>Preprocessing raw text using a MALLET-compatible pipeline</li>
 *   <li>Inferring topic distributions for unseen documents</li>
 *   <li>Identifying the most prevalent topic for a document</li>
 *   <li>Extracting human-readable topic representations</li>
 * </ul>
 *
 * <p>
 * The class is stateless and thread-safe, assuming the underlying MALLET
 * {@link cc.mallet.topics.TopicInferencer} is thread-safe.
 * </p>
 *
 * <p>
 * This service is intentionally designed as a utility class and is not managed
 * directly by the Spring container.
 * </p>
 */
public class JsonInferencerService {

    /**
     * Infers the most prevalent topic for a given document.
     * <p>
     * The document text is preprocessed using a pipeline that must match the
     * preprocessing configuration used during model training. The resulting
     * topic distribution is computed using Gibbs sampling and the topic with
     * the highest probability is selected as the prevalent one.
     * </p>
     *
     * <p>
     * The returned {@link Document} is enriched with a human-readable
     * representation of the inferred topic, expressed through its most
     * representative words.
     * </p>
     *
     * @param inferencer the MALLET topic inferencer derived from a trained model
     * @param doc the input document to be analyzed
     * @param topicTopWords a mapping between topic identifiers and their top words
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
        return new Document("",url, title, "", prevalentTopicWords);
    }


    private static Pipe loadPipe() throws Exception {
        ClassPathResource resource = new ClassPathResource("inferer/model.pipe");
        try (ObjectInputStream in = new ObjectInputStream(resource.getInputStream())) {
            return (Pipe) in.readObject();
        }
    }


    /**
     * Returns the index of the maximum value in a numeric array.
     * <p>
     * This utility method is used to identify the most prevalent topic
     * by selecting the topic with the highest probability in the inferred
     * topic distribution.
     * </p>
     *
     * @param values an array of double values
     * @return the index corresponding to the maximum value
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
     * terms ranked by their weight and concatenates them into a single string.
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

    /**
     * Copies the stopword list from the application classpath into a temporary file.
     * <p>
     * MALLET APIs require a physical file for stopword removal. This method
     * extracts the stopword resource and makes it available as a temporary file
     * that is automatically deleted when the JVM terminates.
     * </p>
     *
     * @return a temporary {@link File} containing the stopword list
     * @throws Exception if the resource cannot be accessed or written
     */
    private static File resourceToTempFile() throws Exception {

        ClassPathResource resource = new ClassPathResource("stoplist.txt");

        File tempFile = File.createTempFile("mallet-stoplist", ".txt");
        tempFile.deleteOnExit();

        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }
}

