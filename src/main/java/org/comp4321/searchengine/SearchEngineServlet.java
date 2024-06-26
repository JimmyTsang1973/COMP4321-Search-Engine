package org.comp4321.searchengine;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.comp4321.searchengine.SearchEngine.*;

@WebServlet(name = "SearchEngineServlet", urlPatterns = {"/search"})
public class SearchEngineServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String query = request.getParameter("Query");
        String similarToPageId = request.getParameter("SimilarTo");
        boolean isSimilarSearch = similarToPageId != null && !similarToPageId.trim().isEmpty();

        if ((query == null || query.trim().isEmpty()) && !isSimilarSearch) {
            response.sendRedirect("index.jsp");
            return;
        }

        String PagesFilePath = getServletContext().getRealPath("/") + "WEB-INF/Pages";
        String IndexerFilePath = getServletContext().getRealPath("/") + "WEB-INF/Indexer";
        String StopWordFilePath = getServletContext().getRealPath("/") + "WEB-INF/stopwords.txt";

        try (SearchEngine searchEngine = new SearchEngine(PagesFilePath, "Pages", IndexerFilePath, StopWordFilePath)) {
            out.println("<html><head><title>Search Results</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; }");
            out.println(".container { text-align: left; margin-top: 20px; margin-left: 20px; }");
            out.println(".search-form { margin-bottom: 20px; }");
            out.println(".search-input { width: 300px; padding: 8px; }");
            out.println(".button { margin-left: 5px; }");
            out.println(".results-heading { text-align: left; }");
            out.println("</style>");
            out.println("<script>");
            out.println("function validateForm() {");
            out.println("    var x = document.getElementById('searchQuery').value;");
            out.println("    if (x.trim() == '') { alert('Please enter a search term.'); return false; }");
            out.println("    return true;");
            out.println("}");
            out.println("function setAction(actionPath) {");
            out.println("    document.getElementById('searchForm').action = actionPath;");
            out.println("}");
            out.println("</script>");
            out.println("</head><body>");
            out.println("<div class='container'>");
            out.println("<h2>Search Engine</h2>");
            out.println("<form id='searchForm' class='search-form' method='get' onsubmit='return validateForm()'>");
            out.println("<input type='text' id='searchQuery' name='Query' placeholder='Enter your search term' class='search-input'>");
            out.println("<button type='submit' class='button' onclick='setAction(\"search\");'>Search</button>");
            out.println("<button type='submit' class='button' onclick='setAction(\"lucky\");'>I'm Feeling Lucky</button>");
            out.println("</form>");

            if (isSimilarSearch) {
                PageInfo pageInfo = searchEngine.getPageInfo(Integer.parseInt(similarToPageId));
                query = pageInfo.getKeywords().entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(5)
                        .map(e -> {
                            try {
                                return searchEngine.getIndexer().getWord(e.getKey());
                            } catch (IOException ex) {
                                return "Error retrieving word";
                            }
                        })
                        .collect(Collectors.joining(" "));
                out.println("<h2 class='results-heading'>Similar Pages for \"" + pageInfo.getTitle() + "\"</h2>");
            } else {
                out.println("<h2 class='results-heading'>Top Search Results for \"" + query + "\"</h2>");
            }

            Map<Integer, Double> rankedPages = searchEngine.rank(query);
            Vector<Map.Entry<Integer, Double>> vec = new Vector<>(rankedPages.entrySet());
            vec.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            int count = 0;
            boolean foundValidResult = false;
            for (Map.Entry<Integer, Double> entry : vec) {
                if (count >= 50) break;
                if (entry.getValue() == 0) continue;

                PageInfo pageInfo = searchEngine.getPageInfo(entry.getKey());
                if (pageInfo != null) {
                    out.println("<div>");
                    out.println("<strong>Score: " + String.format("%.2f", entry.getValue()) + "</strong> - <a href='" + pageInfo.getUrl() + "'>" + pageInfo.getTitle() + "</a><br>");
                    out.println("<small>URL: " + pageInfo.getUrl() + "</small><br>");
                    out.println("Last Modified: " + pageInfo.getLastModificationDate() + ", Size: " + pageInfo.getPageSize() + " bytes<br>");

                    String keywordsHtml = pageInfo.getKeywords().entrySet().stream()
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

                    out.println("<br>");
                    out.println("<form action='search' method='get' style='display:inline;'>");
                    out.println("<input type='hidden' name='SimilarTo' value='" + entry.getKey() + "'>");
                    out.println("<button type='submit'>Get similar pages</button>");
                    out.println("</form>");
                    out.println("</div><hr>");
                    count++;
                    foundValidResult = true;
                }
            }

            if (!foundValidResult) {
                out.println("<div class='results-heading'><p>Your search - \"" + query + "\" - did not match any documents.</p></div>");
            }

            out.println("</div></body></html>");
        } catch (Exception e) {
            out.println("<p>Error processing request: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }
    }
}
