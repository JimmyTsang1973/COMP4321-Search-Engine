package org.comp4321.searchengine.SearchEngine;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PageInfo implements Serializable {
    private static final long serialVersionUID = 8573700063823237542L;
    private int pageId;
    private String title;
    private String body;
    private String url;
    private Date lastModificationDate;
    private long pageSize;
    private Map<Integer, Integer> titleKeywords;
    private Map<Integer, Integer> bodyKeywords;
    private Vector<String> parentLinks;
    private Vector<String> childLinks;

    public PageInfo(int pageId, String title, String body, String url, Date lastModificationDate, long pageSize
            , Map<Integer, Integer> titleKeywords, Map<Integer, Integer> bodyKeywords, Vector<String>parentLinks, Vector<String> childLinks) {
        this.pageId = pageId;
        this.title = title;
        this.body = body;
        this.url = url;
        this.lastModificationDate = lastModificationDate;
        this.pageSize = pageSize;
        this.titleKeywords = titleKeywords;
        this.bodyKeywords = bodyKeywords;
        this.parentLinks = parentLinks;    // add when some website links to this website
        this.childLinks = childLinks;
    }

    public int getPageId() { return pageId; }

    public String getTitle() { return title; }

    public String getBody() { return body; }

    public String getUrl() { return url; }

    public Date getLastModificationDate() { return lastModificationDate; }

    public long getPageSize() { return pageSize; }

    public Map<Integer, Integer> getKeywords() {
        Map<Integer, Integer> keywords = new HashMap<>(bodyKeywords);
        titleKeywords.forEach((word, freq) -> keywords.merge(word, freq, Integer::sum));

        return keywords;
    }
    public Vector<String> getParentVector() { return parentLinks; }

    public Vector<String> getChildVector() { return childLinks; }

    public int getTF(int wordId) { return titleKeywords.getOrDefault(wordId, 0) * 4 + bodyKeywords.getOrDefault(wordId, 0); }

    public int getMaxTF() {
        int max1 = titleKeywords.values().stream().mapToInt(v -> v * 4).max().orElse(0);
        int max2 = Collections.max(bodyKeywords.values());
        return Math.max(max1, max2);
    }

    public void updateParentLink(String URL) {
        if (!parentLinks.contains(URL))
            parentLinks.add(URL);
    }
}
