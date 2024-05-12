package org.comp4321.searchengine.SearchEngine;

import org.comp4321.searchengine.IRUtilities.StopStem;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine implements AutoCloseable{
    private RecordManager recman;
    private HTree pages;
    private StopStem stopStem;
    private Indexer indexer;

    public SearchEngine(String spiderRecman, String pageInfoObjectName, String indexerDbName, String StopWordFilePath) throws IOException {
        recman = RecordManagerFactory.createRecordManager(spiderRecman);

        long pageInfoRecid = recman.getNamedObject(pageInfoObjectName);
        pages = HTree.load(recman, pageInfoRecid);

        indexer = new Indexer(indexerDbName, StopWordFilePath);
        stopStem = new StopStem(StopWordFilePath);
    }

    public void close() throws IOException {
        recman.commit();
        recman.close();
    }

    public PageInfo getPageInfo(int pageId) throws IOException {
        return (PageInfo) pages.get(pageId);
    }

    public Indexer getIndexer() { return indexer; }

    private Vector<String> split(String string) {
        String[] tokens = string.split("\\W+");
        Vector<String> returnString = new Vector<>();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                returnString.add(token);
            }
        }
        return returnString;
    }

    public Map<Integer, Integer> processQuery(String query) throws IOException {
        Vector<String> tokens = new Vector<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|\\b\\w+\\b");
        Matcher matcher = pattern.matcher(query.toLowerCase());

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for(String token : split(matcher.group(1))) {
                    if (!stopStem.isStopWord(token)) {
                        stringBuilder.append(stopStem.stem(token));
                        stringBuilder.append(" ");
                    }
                }
                String temp = stringBuilder.toString().trim();
                tokens.add(temp);
            }
            else {
                if (!stopStem.isStopWord(matcher.group())) {
                    tokens.add(stopStem.stem(matcher.group()));
                }
            }
        }

        Vector<Integer> integerTokens = new Vector<>();
        for(String token : tokens) {
            integerTokens.add(indexer.getWordId(token));
        }

        Map<Integer, Integer> tokensMap = new HashMap<>();
        for (Integer wordId : integerTokens) {
            tokensMap.put(wordId, tokensMap.getOrDefault(wordId, 0) + 1);
        }

        return tokensMap;
    }

    private int getSize() throws IOException {
        FastIterator iter = pages.keys();
        Integer key;
        int N = 0;
        while ((key = (Integer) iter.next()) != null) {
            N++;
        }
        return N;
    }

    private Map<Integer, Double> idf(Map<Integer, Integer> tfMap) throws IOException {
        Map<Integer, Double> returnMap = new HashMap<>();
        int N = getSize() + 1;

        Map<Integer, Integer> documentFrequency = new HashMap<>();
        FastIterator iter = pages.values();
        PageInfo value;
        while ((value = (PageInfo) iter.next()) != null) {
            for (Integer wordId : tfMap.keySet()) {
                documentFrequency.put(wordId, documentFrequency.getOrDefault(wordId, 0) + value.getTF(wordId));
            }
        }

        for (Integer wordId : tfMap.keySet()) {
            int DF = documentFrequency.get(wordId) + tfMap.get(wordId);
            double idf = Math.log((double) N / (DF == 0 ? 1 : DF));
            returnMap.put(wordId, idf);
        }

        return returnMap;
    }

    private Map<Integer, Map<Integer, Double>> tfidf() throws IOException {
        Map<Integer, Map<Integer, Double>> returnMap = new HashMap<>();
        FastIterator iter = pages.values();
        PageInfo value;

        while ((value = (PageInfo) iter.next()) != null) {
            Map<Integer, Double> tfidfMap = new HashMap<>();
            Map<Integer, Integer> tfMap = value.getKeywords();
            Map<Integer, Double> idfMap = idf(tfMap);
            int maxTF = value.getMaxTF();

            for (Integer wordId : tfMap.keySet()) {
                int TF = value.getTF(wordId);
                double idfValue = idfMap.get(wordId);
                double normalizedTF = (double) TF / maxTF;
                tfidfMap.put(wordId, idfValue * normalizedTF);
            }
            returnMap.put(value.getPageId(), tfidfMap);
        }

        return returnMap;
    }

    private Map<Integer, Double> tfidfQuery(Map<Integer, Integer> queryMap) throws IOException {
        Map<Integer, Double> tfidfMap = new HashMap<>();
        Map<Integer, Double> idfMap = idf(queryMap);
        int maxTF = Collections.max(queryMap.values());

        for (Integer wordId : queryMap.keySet()) {
            int TF = queryMap.get(wordId);
            double idfValue = idfMap.get(wordId);
            double normalizedTF = (double) TF / maxTF;
            tfidfMap.put(wordId, idfValue * normalizedTF);
        }

        return tfidfMap;
    }

    private double sim(Map<Integer, Double> document1, Map<Integer, Double> document2) throws IOException {
        double product = 0.0;
        double normDoc1 = 0.0;
        double normDoc2 = 0.0;
        
        for (Integer wordId : document1.keySet()) {
            double d1 = document1.get(wordId);
            normDoc1 += d1 * d1;
        }
        for (Integer wordId : document2.keySet()) {
            product += document1.getOrDefault(wordId, 0.0) * document2.get(wordId);
            double d2 = document2.get(wordId);
            normDoc2 += d2 * d2;
        }

        if (normDoc1 == 0 || normDoc2 == 0) {
            return 0;
        }
        else {
            return product / (Math.sqrt(normDoc1) * Math.sqrt(normDoc2));
        }
    }

    public Map<Integer, Double> rank(String query) throws IOException {
        Map<Integer, Integer> processedQuery = processQuery(query);
        Map<Integer, Map<Integer, Double>> tfidfMap = tfidf();
        Map<Integer, Double> queryWeight = tfidfQuery(processedQuery);
        Map<Integer, Double> returnMap = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Double>> entry : tfidfMap.entrySet()) {
            int pageId = entry.getKey();
            Map<Integer, Double> documentWeight = entry.getValue();
            double similarity = sim(documentWeight, queryWeight);
            returnMap.put(pageId, similarity);
        }
        return returnMap;
    }
}
