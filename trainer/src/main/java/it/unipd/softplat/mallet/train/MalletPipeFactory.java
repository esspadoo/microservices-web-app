package it.unipd.softplat.mallet.train;

import cc.mallet.pipe.*;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public final class MalletPipeFactory {

    private MalletPipeFactory() {}

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

