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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class JsonInferencerService {

    public static Document inferPrevalentTopicJsonl(TopicInferencer inferencer, Document doc, Map<Integer, String> topicTopWords) throws Exception {

        File stoplistFile = resourceToTempFile();

        String url = doc.url();
        String title = doc.title();
        String text = doc.main_content();

        // Build Pipe (must match training configuration)
        ArrayList<Pipe> pipeList = new ArrayList<>();

        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(
                Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")
        ));
        pipeList.add(new TokenSequenceRemoveStopwords(
                stoplistFile,
                "UTF-8",false,false,false
        ));
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));

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
        return new Document(url, title, "",prevalentTopicWords);
    }

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

