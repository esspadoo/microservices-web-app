package it.unipd.softplat.mallet.train;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

//use malletpipefactory for serialize the pipeline
//write pipe file in out on the main
/**
 * Utility class for MALLET topic modeling operations.
 * <p>
 * This class provides static methods to:
 * <ul>
 *     <li>Create an {@link InstanceList} from input documents</li>
 *     <li>Train a {@link ParallelTopicModel} on the instances</li>
 *     <li>Run the full workflow including cleaning, reading, training, and saving the model</li>
 * </ul>
 * </p>
 */
public class MalletApp {

    /**
     * Creates an {@link InstanceList} from a JSON input stream.
     * <p>
     * Each document is processed through a sequence of pipes:
     * lowercase conversion, tokenization, stopword removal, and
     * mapping tokens to feature sequences.
     * </p>
     *
     * @param dataInputStream the input stream containing documents
     * @param stoplist the stopwords file to be used for filtering
     * @return an {@link InstanceList} suitable for training a topic model
     */
    public static InstanceList createInstanceList(InputStream dataInputStream, File stoplist){

        Pipe pipe = MalletPipeFactory.build(stoplist);
        InstanceList instances = new InstanceList(pipe);


        // Add documents from the iterator to the InstanceList
        instances.addThruPipe(new DocumentIterator(dataInputStream));

        return instances;
    }

    /**
     * Trains a {@link ParallelTopicModel} on the provided instances.
     * <p>
     * The method configures the number of topics, iterations, and
     * parallel threads. After training, the model is ready for
     * inference or saving.
     * </p>
     *
     * @param instances the documents represented as an {@link InstanceList}
     * @param numTopics the number of topics to generate
     * @param numIterations the number of iterations for Gibbs sampling
     * @param numTopWords the number of top words to display per topic
     * @return a trained {@link ParallelTopicModel}
     * @throws IOException if an error occurs during training (rare, typically I/O related)
     */
    public static ParallelTopicModel trainTopicModel(InstanceList instances, int numTopics, int numIterations, int numTopWords) throws IOException {

        ParallelTopicModel topicModel = new ParallelTopicModel(numTopics);

        topicModel.addInstances(instances);

        // Use parallel samplers, which each look at the corpus and combine
        // statistics after every iteration.
        int numThreads = Math.min(
                Runtime.getRuntime().availableProcessors(),
                instances.size()
        );
        topicModel.setNumThreads(numThreads);

        // Train the model for <numIterations> iterations and stop
        topicModel.setNumIterations(numIterations);
        topicModel.setTopicDisplay(100, numTopWords);

        topicModel.estimate();

        return topicModel;

    }

    /**
     * Main method executing the full workflow: cleaning JSON, reading documents,
     * creating instances, training the topic model, and saving the trained model.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if any step in the pipeline fails (I/O, cleaning, or training)
     */
    public static void main(String[] args) throws Exception {

        System.out.println("\n...START CLEANING");
        // Clean the JSON from HTML tags
        JsonHtmlCleaner.clean();
        System.out.println("\n...CLEANED JSONL");

        // Read data from the cleaned JSON file
        InputStream dataInputStream = Files.newInputStream(Paths.get("trainer/src/main/resources/clean_json_out.json"));
        File stoplist = new File(MalletApp.class.getClassLoader().getResource("stoplist.txt").getFile());
        File inferer = new File("trainer/src/main/resources/inferer.model");

        // Create InstanceList from input documents
        InstanceList instances = createInstanceList(dataInputStream, stoplist);
        System.out.printf("Number of instances (docs): %s%n", instances.size());

        // Save the pipeline
        File pipeFile = new File("trainer/src/main/resources/model.pipe");
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(pipeFile))) {
            out.writeObject(instances.getPipe());
        }

        System.out.println("\n...training the topic model");

        // Configure training parameters
        int numTopics = 20;
        int numIterations = 1000;
        int numTopWords = 25;

        // Train the topic model
        ParallelTopicModel topicModel = trainTopicModel(instances, numTopics, numIterations, numTopWords);

        System.out.println("\n*************** MODEL TRAINED --> SAVE INFERER ***************\n");

        // Save the trained model to file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(inferer))) {
            out.writeObject(topicModel);
        }
    }

}
