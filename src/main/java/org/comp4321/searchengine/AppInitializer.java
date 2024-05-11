package org.comp4321.searchengine;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.comp4321.searchengine.SearchEngine.*;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String rootLink = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        String PagesFilePath = sce.getServletContext().getRealPath("/") + "WEB-INF/Pages";
        String IndexerFilePath = sce.getServletContext().getRealPath("/") + "WEB-INF/Indexer";
        String StopWordFilePath = sce.getServletContext().getRealPath("/") + "WEB-INF/stopwords.txt";

        try (Spider spider = new Spider(PagesFilePath, IndexerFilePath, StopWordFilePath)) {
            spider.BFS_OnRootLink(rootLink);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
