package org.comp4321.searchengine;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import org.comp4321.searchengine.SearchEngine.*;

@WebServlet(name = "KeywordListServlet", urlPatterns = {"/keywordlist"})
public class KeywordListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String PagesFilePath = getServletContext().getRealPath("/") + "WEB-INF/Pages";
        String IndexerFilePath = getServletContext().getRealPath("/") + "WEB-INF/Indexer";
        String StopWordFilePath = getServletContext().getRealPath("/") + "WEB-INF/stopwords.txt";

        try (SearchEngine searchEngine = new SearchEngine(PagesFilePath, "Pages", IndexerFilePath, StopWordFilePath)) {
            Vector<String> allKeywords = searchEngine.getIndexer().getAllIndexedWords();

            out.println("<html><head><title>Keyword List</title>");
            out.println("<style>");
            out.println(".grid-container {");
            out.println("  display: grid;");
            out.println("  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));");
            out.println("  gap: 10px;");
            out.println("  padding: 10px;");
            out.println("}");
            out.println(".keyword {");
            out.println("  display: flex;");
            out.println("  align-items: center;");
            out.println("}");
            out.println(".keyword label {");
            out.println("  margin-left: 5px;");
            out.println("}");
            out.println("</style>");
            out.println("</head><body>");
            out.println("<form action='index.jsp' method='get'>");
            out.println("<button type='submit'>Return to Main Page</button>");
            out.println("</form>");
            out.println("<h1>Select Keywords for Searching</h1>");
            out.println("<form action='keywordlist' method='post' class='grid-container'>");
            allKeywords.forEach(keyword -> {
                out.println("<div class='keyword'>");
                out.println("<input type='checkbox' name='keywords' value='" + keyword + "' id='" + keyword + "'>");
                out.println("<label for='" + keyword + "'>" + keyword + "</label>");
                out.println("</div>");
            });
            out.println("<input type='submit' value='Search' style='grid-column: span 2;'>");
            out.println("</form>");
            out.println("</body></html>");
        } catch (Exception e) {
            out.println("<p>Error retrieving keywords: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] selectedKeywords = request.getParameterValues("keywords");
        if (selectedKeywords == null) {
            doGet(request, response);
            return;
        }

        List<String> formattedKeywords = Arrays.stream(selectedKeywords)
                .map(keyword -> keyword.contains(" ") ? "\"" + keyword + "\"" : keyword)
                .collect(Collectors.toList());

        String query = String.join(" ", formattedKeywords);

        response.sendRedirect("search?Query=" + URLEncoder.encode(query, "UTF-8"));
    }
}
