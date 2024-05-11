package org.comp4321.searchengine;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.comp4321.searchengine.SearchEngine.*;

@WebServlet(name = "SearchEngineServlet", urlPatterns = {"/search"})
public class SearchEngineServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String query = request.getParameter("Query");
        if (query == null || query.trim().isEmpty()) {
            response.sendRedirect("index.jsp");
            return;
        }

        String PagesFilePath = getServletContext().getRealPath("/") + "WEB-INF/Pages";
        String IndexerFilePath = getServletContext().getRealPath("/") + "WEB-INF/Indexer";
        String StopWordFilePath = getServletContext().getRealPath("/") + "WEB-INF/stopwords.txt";

        try (SearchEngine searchEngine = new SearchEngine(PagesFilePath, "Pages", IndexerFilePath, StopWordFilePath)) {
            Map<Integer, Double> rankedPages = searchEngine.rank(query);
            Vector<Map.Entry<Integer, Double>> vec = new Vector<>(rankedPages.entrySet());
            vec.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            out.println("<html><head><title>Search Results</title></head><body>");
            out.println("<div style='text-align:center; margin-top: 20px;'>");
            out.println("<h2>Search Engine</h2>");
            out.println("<form action='search' method='get' style='margin-bottom: 20px;'>");
            out.println("<input type='text' name='Query' placeholder='Enter your search term' style='width: 300px; padding: 8px;'>");
            out.println("<button type='submit'>Search</button>");
            out.println("</form>");
            out.println("</div>");
            out.println("<div style='margin-left: 20px;'>");
            out.println("<h1>Top Search Results for '" + query + "'</h1>");
            out.println("</div>");
            int count = 0;
            boolean foundValidResult = false;

            for (Map.Entry<Integer, Double> entry : vec) {
                if (count >= 50) break;
                if (entry.getValue() == 0) continue;

                PageInfo pageInfo = searchEngine.getPageInfo(entry.getKey());
                if (pageInfo != null) {
                    out.println("<div style='margin-bottom:20px; margin-left: 20px;'>"); // Left-align the result blocks
                    out.println("<strong>Score: " + String.format("%.2f", entry.getValue()) + "</strong> - <a href='" + pageInfo.getUrl() + "'>" + pageInfo.getTitle() + "</a><br>");
                    out.println("<small>URL: " + pageInfo.getUrl() + "</small><br>");
                    out.println("Last Modified: " + pageInfo.getLastModificationDate() + ", Size: " + pageInfo.getPageSize() + " bytes<br>");

                    Map<Integer, Integer> keywords = pageInfo.getKeywords();
                    String keywordsHtml = keywords.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .limit(5)
                            .map(e -> {
                                try {
                                    return searchEngine.getIndexer().getWord(e.getKey()) + " (" + e.getValue() + ")";
                                } catch (IOException ex) {
                                    return "Error retrieving word";
                                }
                            })
                            .collect(Collectors.joining("; "));
                    out.println("Keywords: " + keywordsHtml + "<br>");

                    out.println("Parent Links:<br>");
                    pageInfo.getParentVector().stream().limit(5).forEach(link ->
                            out.println("<a href='" + link + "'>" + link + "</a><br>"));

                    out.println("Child Links:<br>");
                    pageInfo.getChildVector().stream().limit(5).forEach(link ->
                            out.println("<a href='" + link + "'>" + link + "</a><br>"));

                    out.println("</div><hr>");
                    count++;
                    foundValidResult = true;
                }
            }

            if (!foundValidResult) {
                out.println("<div style='margin-left: 20px;'><p>Your search - \"" + query + "\" - did not match any documents.</p></div>");
            }

            out.println("</body></html>");
        } catch (Exception e) {
            out.println("<p>Error processing request: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }
    }
}
