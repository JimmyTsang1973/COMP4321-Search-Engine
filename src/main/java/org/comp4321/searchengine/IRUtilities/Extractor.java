package org.comp4321.searchengine.IRUtilities;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

/**
 * IRUtilities.Extractor extracts all the links or all words from the given webpage
 * and return as arrays
 */

public class Extractor {
    private String rootLink;
    // private int pageSize = -1; // an initial value that mean the website is not searched

    public Extractor(String rootLink) {
        this.rootLink = rootLink;
    }

    /**
     * extract all the links from the root_link
     * @return  all links from root_link in a URL array, empty array if root_link is null
     * @throws ParserException
     */
    public Vector<String> extractLinks() throws ParserException {
        if (rootLink != null) {
            LinkBean linkBean = new LinkBean();
            linkBean.setURL(rootLink);
            URL[] links = linkBean.getLinks();
            Vector<String> linkVector = new Vector<>();
            for (URL link : links) {
                linkVector.add(link.toString());
            }
            return linkVector;
        } else {
            return new Vector<>(); // return empty URL array if the root_link is null
        }
    }

    public String extractTitle(boolean links) {
        if (rootLink != null) {
            StringBean stringBean = new StringBean();
            stringBean.setLinks(links);         // true means the text inside the URL inside this website will be also extracted
            stringBean.setURL(rootLink);
            String extractedString = stringBean.getStrings();
            StringTokenizer stringTokenizer = new StringTokenizer(extractedString, "\n");
            return stringTokenizer.nextToken();
        } else {
            return "";
        }
    }

    public String extractBody(boolean links) {
        if (rootLink != null) {
            StringBean stringBean = new StringBean();
            stringBean.setLinks(links);         // true means the text inside the URL inside this website will be also extracted
            stringBean.setURL(rootLink);
            String extractedString = stringBean.getStrings();
            StringTokenizer stringTokenizer = new StringTokenizer(extractedString, "\n");
            Vector<String> returnString = new Vector<>();
            while (stringTokenizer.hasMoreTokens()) {
                returnString.add(stringTokenizer.nextToken());
            }
            returnString.remove(0);
            return returnString.toString();
        } else {
            return "";
        }
    }

    public Vector<String> splitWords(String string) {
        String[] tokens = string.split("\\W+");
        Vector<String> returnString = new Vector<>();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                returnString.add(token);
            }
        }
        return returnString;
    }

    public Date extractLastModificationDate() throws IOException, ParseException {
        URL link = new URL(rootLink);
        URLConnection connection = link.openConnection();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = simpleDateFormat.parse(connection.getHeaderField("Last-Modified"));
        if (date == null) {
            System.out.println(rootLink);
        }
        return date;
    }

    public int extractPageSize() throws IOException { // return the size of root_link after ExtractWord method is called
        URL link = new URL(rootLink);
        URLConnection connection = link.openConnection();
        return connection.getContentLength();
    }
}
