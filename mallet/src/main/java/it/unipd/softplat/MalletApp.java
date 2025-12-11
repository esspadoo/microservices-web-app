/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public static InstanceList createInstanceList(InputStream dataInputStream, File stoplist) throws FileNotFoundException, UnsupportedEncodingException {

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
        InputStream dataInputStream = Files.newInputStream(Paths.get(System.getProperty("user.dir") + "src/main/resources/clean_json_out.json"));

        // to read the file from the resource folder src/main/java/resources
        // the stoplist file is from https://github.com/mimno/Mallet/blob/master/stoplists/en.txt
        File stoplist = new File(MalletApp.class.getClassLoader().getResource("stoplist.txt").getFile());

        InstanceList instances = createInstanceList(dataInputStream, stoplist);

        System.out.println(String.format("Number of instances (docs): %s", instances.size()));

        Alphabet alphabet = instances.getDataAlphabet();

        System.out.println(String.format("%s [index] => %s [object]", 0, alphabet.lookupObject(0)));
        System.out.println(String.format("%s [object] => %s [index]", "chatgpt", alphabet.lookupIndex("chatgpt")));
        System.out.println(String.format("%s [object] => %s [index]", "ethics", alphabet.lookupIndex("ethics")));

        System.out.println("\n...training the topic model");

        int numTopics = 10;
        int numIterations = 1000;
        int numTopWords = 25;
        ParallelTopicModel topicModel = trainTopicModel(instances, numTopics, numIterations, numTopWords);

        // get topics top words
        System.out.println("\nExtracted topics:");

        for (int t = 0; t < numTopics; t++) {
            List<String> topWords = new ArrayList<>();
            System.out.print("Topic " + t + ":");
            for (Object obj : topicModel.getTopWords(numTopWords)[t]) {
                topWords.add((String) obj);
                System.out.print(" " + obj);
            }
            System.out.println();

        }

    }

}
