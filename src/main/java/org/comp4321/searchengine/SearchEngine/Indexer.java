package org.comp4321.searchengine.SearchEngine;

import org.comp4321.searchengine.IRUtilities.StopStem;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

class PostingList implements Serializable{
    private static final long serialVersionUID = 1L;

    public Map<Integer, Integer> pageFreqs = new HashMap<>();

    public void add(int pageId) {
        this.pageFreqs.put(pageId, this.pageFreqs.getOrDefault(pageId, 0) + 1);
    }

    public void remove(int pageId) {
        this.pageFreqs.remove(pageId);
    }
}

public class Indexer implements AutoCloseable {
    private RecordManager recman;
    private HTree titleIndex;
    private HTree bodyIndex;
    private HTree wordToIdIndex;
    private HTree idToWordIndex;
    private HTree pageIndex;
    private long nextWordId;
    private StopStem stopStem;

    public Indexer(String dbName, String StopWordFilePath) throws IOException {
        recman = RecordManagerFactory.createRecordManager(dbName);
        titleIndex = loadOrCreateHTree("titleIndex");
        bodyIndex = loadOrCreateHTree("bodyIndex");
        wordToIdIndex = loadOrCreateHTree("wordToIdIndex");
        idToWordIndex = loadOrCreateHTree("idToWordIndex");
        pageIndex = loadOrCreateHTree("pageIndex");
        nextWordId = getNextWordId();
        stopStem = new StopStem(StopWordFilePath);
    }

    private HTree loadOrCreateHTree(String name) throws IOException {
        long recid = recman.getNamedObject(name);

        if (recid != 0) {
            return HTree.load(recman, recid);
        }
        else {
            HTree htree = HTree.createInstance(recman);
            recman.setNamedObject(name, htree.getRecid());
            return htree;
        }
    }

    private long getNextWordId() throws IOException {
        Integer storedNextWordId = (Integer) wordToIdIndex.get("nextWordId");
        return storedNextWordId != null ? storedNextWordId.longValue() : 1;
    }

    public void close() throws IOException {
        recman.commit();
        recman.close();
    }

    public Map<Integer, Integer> indexTitle(Vector<String> words, int pageId) throws IOException {
        resetPageInIndex(pageId, titleIndex);
        return indexWords(words, pageId, titleIndex);
    }

    public Map<Integer, Integer> indexBody(Vector<String> words, int pageId) throws IOException {
        resetPageInIndex(pageId, bodyIndex);
        return indexWords(words, pageId, bodyIndex);
    }

    private Vector<String> generateStemmedNGrams(Vector<String> words, int n) {
        Vector<String> ngrams = new Vector<>();
        for (int i = 0; i < words.size() - n + 1; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < n; j++) {
                String stemmedWord = stopStem.stem(words.get(i + j));
                stringBuilder.append((j > 0 ? " " : "") + stemmedWord);
            }
            ngrams.add(stringBuilder.toString());
        }
        return ngrams;
    }

    private Map<Integer, Integer> indexWords(Vector<String> words, int pageId, HTree index) throws IOException {
        Vector<String> filteredWords = stopStem.removeStopWords(words);
        Vector<String> allTokens = new Vector<>();

        for (String word : filteredWords) {
            allTokens.add(stopStem.stem(word));
        }

        allTokens.addAll(generateStemmedNGrams(filteredWords, 2));
        allTokens.addAll(generateStemmedNGrams(filteredWords, 3));

        Map<Integer, Integer> returnIndex = new HashMap<>();

        for (String token : allTokens) {
            int wordId = getWordId(token);
            returnIndex.merge(wordId, 1, Integer::sum);

            PostingList postings = (PostingList) index.get(wordId);
            if (postings == null) {
                postings = new PostingList();
            }
            postings.add(pageId);
            index.put(wordId, postings);
        }
        recman.commit();
        return returnIndex;
    }

    private void resetPageInIndex(int pageId, HTree index) throws IOException {
        if (pageIndex.get(pageId) != null) {
            FastIterator iter = index.keys();
            Integer wordId;
            while ((wordId = (Integer) iter.next()) != null) {
                PostingList postings = (PostingList) index.get(wordId);
                if (postings != null && postings.pageFreqs.containsKey(pageId)) {
                    postings.remove(pageId);
                    if (postings.pageFreqs.isEmpty()) {
                        index.remove(wordId);
                    } else {
                        index.put(wordId, postings);
                    }
                }
            }
        }
        pageIndex.put(pageId, System.currentTimeMillis());
        recman.commit();
    }
    public int getWordId(String word) throws IOException {
        Integer wordId = (Integer) wordToIdIndex.get(word);
        if (wordId == null) {
            wordId = (int) nextWordId++;
            wordToIdIndex.put(word, wordId);
            idToWordIndex.put(wordId, word);
            wordToIdIndex.put("nextWordId", (int) nextWordId);

            recman.commit();
        }
        return wordId;
    }

    public String getWord(int wordId) throws IOException {
        return (String) idToWordIndex.get(wordId);
    }

    public Vector<String> getAllIndexedWords() throws IOException {
        Vector<String> words = new Vector<>();
        FastIterator iter = idToWordIndex.keys();
        Integer wordId;
        while ((wordId = (Integer) iter.next()) != null) {
            words.add((String) idToWordIndex.get(wordId));
        }
        return words;
    }
}
