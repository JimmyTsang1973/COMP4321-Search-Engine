package org.comp4321.searchengine.SearchEngine;

import org.comp4321.searchengine.IRUtilities.Extractor;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import org.htmlparser.util.ParserException;

import java.io.FileWriter;
import java.text.ParseException;
import java.util.*;
import java.io.IOException;

public class Spider implements AutoCloseable {
    private Indexer indexer;
    private RecordManager recman;
    private HTree pages;
    private HTree pageId;
    private int MaxCrawlingSize = 30;

    public Spider(String PagesFilePath,String IndexerFilePath,String StopWordFilePath) throws IOException {
        recman = RecordManagerFactory.createRecordManager(PagesFilePath);
        long hashRecid = recman.getNamedObject("Pages");
        long idRecid = recman.getNamedObject("PageId");

        if (hashRecid != 0) {
            pages = HTree.load(recman, hashRecid);
            pageId = HTree.load(recman, idRecid);
        }
        else {
            pages = HTree.createInstance(recman);
            recman.setNamedObject("Pages", pages.getRecid());
            pageId = HTree.createInstance(recman);
            recman.setNamedObject("PageId", pageId.getRecid());
        }

        indexer = new Indexer(IndexerFilePath, StopWordFilePath);
    }

    public PageInfo getPageInfo(int pageId) throws IOException {
        return (PageInfo) pages.get(pageId);
    }

    public void close() throws IOException {
        recman.commit();
        recman.close();
    }

    public void addEntry(int key, Object value) throws IOException {
        pages.put(key, value);
        pageId.put(((PageInfo)value).getUrl(), key);
        recman.commit();
    }

    public void delEntry(int key) throws IOException {
        pageId.remove(((PageInfo)pages.get(key)).getUrl());
        pages.remove(key);
        recman.commit();
    }

    public void updateEntry(int key, Object value) throws IOException {
        pageId.remove(((PageInfo)pages.get(key)).getUrl());
        pages.remove(key);
        addEntry(key, value);
        recman.commit();
    }

    public int getDatabaseSize() throws IOException {
        FastIterator iter = pageId.keys();
        String value;
        int i = 0;
        while ((value = (String) iter.next()) != null) {
            i++;
        }
        return i;
    }

    public boolean databaseContains(String URL) throws IOException {
        FastIterator iter = pageId.keys();
        String value = (String) iter.next();
        while (value != null) {
            if (value.equals(URL)) {
                return true;
            }
            value = (String) iter.next();
        }
        return false;
    }

    public void BFS_OnRootLink(String rootLink) throws ParserException, IOException, ParseException {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parentLinksOfNonSearchedLinks = new HashMap<>();
        queue.add(rootLink);

        int currentPageId = getDatabaseSize() + 1;

        if (currentPageId > MaxCrawlingSize) {
            System.out.println("Max crawling size has been reached. Please delete some entry and come back.");
        } else {
            while (!queue.isEmpty() && currentPageId <= MaxCrawlingSize) {
                String currentLink = queue.remove();

                if (!databaseContains(currentLink)) {
                    Extractor extractor = new Extractor(currentLink);
                    Vector<String> linkVector = extractor.extractLinks();

                    // extract title and titleKeywords
                    String title = extractor.extractTitle(false);
                    Map<Integer, Integer> titleKeywords = indexer.indexTitle(extractor.splitWords(title.toLowerCase()), currentPageId);

                    // extract body and bodyKeywords
                    String body = extractor.extractBody(false);
                    Map<Integer, Integer> bodyKeywords = indexer.indexBody(extractor.splitWords(body.toLowerCase()), currentPageId);

                    Vector<String> parentVector = new Vector<>();
                    if (parentLinksOfNonSearchedLinks.containsKey(currentLink)) {
                        parentVector.add(parentLinksOfNonSearchedLinks.remove(currentLink));
                    }

                    PageInfo currentPageInfo = new PageInfo(currentPageId, title, body, currentLink, extractor.extractLastModificationDate(),
                            extractor.extractPageSize(), titleKeywords, bodyKeywords, parentVector, linkVector);

                    addEntry(currentPageId, currentPageInfo);
                    currentPageId++;

                    for (String link : linkVector) {
                        if (databaseContains(link)) {
                            PageInfo linkedPageInfo = getPageInfo((Integer) pageId.get(link));
                            if (!linkedPageInfo.getParentVector().contains(currentLink)) {
                                linkedPageInfo.getParentVector().add(currentLink);
                                updateEntry((Integer) pageId.get(link), linkedPageInfo);
                            }
                        } else {
                            parentLinksOfNonSearchedLinks.put(link, currentLink);
                        }
                    }

                    queue.addAll(linkVector);
                } else {
                    Extractor extractor = new Extractor(currentLink);
                    Date lastModifiedDate = extractor.extractLastModificationDate();
                    PageInfo current = (PageInfo) pages.get(pageId.get(currentLink));
                    if (lastModifiedDate.after(current.getLastModificationDate())) {
                        // do update
                        int updatePageId = current.getPageId();
                        Vector<String> linkVector = extractor.extractLinks();

                        // extract title and titleKeywords
                        String title = extractor.extractTitle(false);
                        Map<Integer, Integer> titleKeywords = indexer.indexTitle(extractor.splitWords(title.toLowerCase()), updatePageId);

                        // extract body and bodyKeywords
                        String body = extractor.extractBody(false);
                        Map<Integer, Integer> bodyKeywords = indexer.indexBody(extractor.splitWords(body.toLowerCase()), updatePageId);

                        Vector<String> parentVector = current.getParentVector();
                        if (!parentVector.contains(currentLink)) {
                            parentVector.add(currentLink);
                        }

                        PageInfo updatedPageInfo = new PageInfo(updatePageId, title, body, currentLink, lastModifiedDate,
                                extractor.extractPageSize(), titleKeywords, bodyKeywords, parentVector, linkVector);
                        updateEntry(updatePageId, updatedPageInfo);

                        for (String link : linkVector) {
                            if (databaseContains(link)) {
                                ((PageInfo) pages.get(pageId.get(link))).updateParentLink(currentLink);
                            } else {
                                parentLinksOfNonSearchedLinks.put(link, currentLink);
                            }
                        }

                        queue.addAll(linkVector);
                    }
                }
            }
        }
    }
}
