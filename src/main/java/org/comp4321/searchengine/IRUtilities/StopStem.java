package org.comp4321.searchengine.IRUtilities;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

public class StopStem {
    private Porter porter;
    private HashSet<String> stopWords;

    public StopStem(String StopwordFile) {
        super();
        porter = new Porter();
        stopWords = new HashSet<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(StopwordFile));
            String stopword;
            while ((stopword = bufferedReader.readLine())!=null) {
                stopWords.add(stopword);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isStopWord(String string) {
        return stopWords.contains(string);
    }

    public Vector<String> removeStopWords(Vector<String> originalString) {
        Vector<String> returnString = new Vector<>();

        for (String word: originalString) {
            if (!isStopWord(word))
                returnString.add(word);
        }
        return returnString;
    }

    public String stem(String string) {
        return porter.stripAffixes(string);
    }

    public Vector<String> stem(Vector<String> originalString) {
        Vector<String> returnString = new Vector<>();

        for (String word: originalString) {
            returnString.add(porter.stripAffixes(word));
        }
        return returnString;
    }
}
