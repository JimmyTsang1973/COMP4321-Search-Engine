package org.comp4321.searchengine;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.comp4321.searchengine.SearchEngine.*;

@WebServlet(name = "LuckyServlet", urlPatterns = {"/lucky"})
public class LuckyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String query = request.getParameter("Query");
        String similarToPageId = request.getParameter("SimilarTo");

        if ((query == null || query.trim().isEmpty()) && (similarToPageId == null || similarToPageId.trim().isEmpty())) {
            response.sendRedirect("index.jsp");
            return;
        }

        String headerTitle = "Top Search Results";
        String headerLink = "";

        String PagesFilePath = getServletContext().getRealPath("/") + "WEB-INF/Pages";
        String IndexerFilePath = getServletContext().getRealPath("/") + "WEB-INF/Indexer";
        String StopWordFilePath = getServletContext().getRealPath("/") + "WEB-INF/stopwords.txt";

        try (SearchEngine searchEngine = new SearchEngine(PagesFilePath, "Pages", IndexerFilePath, StopWordFilePath)) {
            if (similarToPageId != null && !similarToPageId.trim().isEmpty()) {
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
                headerTitle = "Getting similar pages for <a href='" + pageInfo.getUrl() + "'>" + pageInfo.getTitle() + "</a>";
                headerLink = pageInfo.getUrl();
            } else if (query != null && !query.trim().isEmpty()) {
                headerTitle = "Top Search Results for '" + query + "'";
            }

            Map<Integer, Double> rankedPages = searchEngine.rank(query);
            Vector<Map.Entry<Integer, Double>> vec = new Vector<>(rankedPages.entrySet());
            vec.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            Map.Entry<Integer, Double> entry = vec.get(0);
            PageInfo pageInfo = (PageInfo) searchEngine.getPageInfo(entry.getKey());
            out.println("<!DOCTYPE HTML>");
            out.println("<html lang=\"en-US\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<meta http-equiv=\"refresh\" content=\"0; url=" + pageInfo.getUrl() + "\">");
            out.println("<script type=\"text/javascript\">");
            out.println("window.location.href = \"" + pageInfo.getUrl() + "\"");
            out.println("</script>");
            out.println("<title>Page Redirection</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<!-- Note: don't tell people to `click` the link, just tell them that it is a link. -->");
            out.println("If you are not redirected automatically, follow this <a href='" + pageInfo.getUrl() + "'>" + pageInfo.getTitle() + "</a>.");
            out.println("</body>");
            out.println("</html>");
        } catch (Exception e) {
            out.println("<p>Error processing request: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }
    }
}