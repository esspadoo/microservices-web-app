package it.unipd.softplat.mallet.train;

import cc.mallet.pipe.*;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * A factory class for creating a Mallet {@link Pipe} for text processing.
 * This pipe is configured for common NLP tasks such as lowercasing, tokenization,
 * stopword removal, and converting token sequences to feature sequences.
 */
public final class MalletPipeFactory {

    private MalletPipeFactory() {}

    /**
     * Builds and returns a Mallet {@link Pipe} instance.
     * The pipe includes the following stages:
     * 1. {@link CharSequenceLowercase}: Converts all characters to lowercase.
     * 2. {@link CharSequence2TokenSequence}: Tokenizes the input character sequence into a sequence of tokens.
     *    It uses a regular expression to define what constitutes a token.
     * 3. {@link TokenSequenceRemoveStopwords}: Removes common stopwords from the token sequence
     *    based on the provided stoplist file.
     * 4. {@link TokenSequence2FeatureSequence}: Converts the token sequence into a feature sequence,
     *    which is suitable for Mallet's topic modeling algorithms.
     *
     * @param stoplistFile The file containing a list of stopwords, one per line.
     * @return A configured Mallet {@link Pipe} for text processing.
     */
    public static Pipe build(File stoplistFile) {
        ArrayList<Pipe> pipeList = new ArrayList<>();

        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(
                Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")
        ));
        pipeList.add(new TokenSequenceRemoveStopwords(
                stoplistFile, "UTF-8", false, false, false
        ));
        pipeList.add(new TokenSequence2FeatureSequence());

        return new SerialPipes(pipeList);
    }
}

