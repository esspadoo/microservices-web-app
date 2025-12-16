package it.unipd.softplat;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MalletApp {

    public static InstanceList createInstanceList(InputStream dataInputStream, File stoplist){

        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords(stoplist, "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        instances.addThruPipe(new DocumentIterator(dataInputStream));

        return instances;
    }

    public static ParallelTopicModel trainTopicModel(InstanceList instances, int numTopics, int numIterations, int numTopWords) throws IOException {

        ParallelTopicModel topicModel = new ParallelTopicModel(numTopics);

        topicModel.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        // statistics after every iteration.
        topicModel.setNumThreads(2);

        // Train the model for <numIterations> iterations and stop
        topicModel.setNumIterations(numIterations);
        topicModel.setTopicDisplay(100, numTopWords);

        topicModel.estimate();

        return topicModel;

    }

    public static void main(String[] args) throws Exception {

        //clean the json from html tags
        JsonHtmlCleaner.clean();

        // read data from the json_out.json
        InputStream dataInputStream = Files.newInputStream(Paths.get("mallet/src/main/resources/clean_json_out.json"));
        File stoplist = new File(MalletApp.class.getClassLoader().getResource("stoplist.txt").getFile());
        File inferer = new File("mallet/src/main/resources/inferer.model");

        InstanceList instances = createInstanceList(dataInputStream, stoplist);
        System.out.printf("Number of instances (docs): %s%n", instances.size());
        Alphabet alphabet = instances.getDataAlphabet();
        System.out.println("\n...training the topic model");


        int numTopics = 10;
        int numIterations = 1000;
        int numTopWords = 25;
        //TRAIN MODEL
        ParallelTopicModel topicModel = trainTopicModel(instances, numTopics, numIterations, numTopWords);

        System.out.println("\n*************** MODEL TRAINED --> SAVE INFERER ***************\n");

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(inferer))) {
                out.writeObject(topicModel);
        }
    }

}
