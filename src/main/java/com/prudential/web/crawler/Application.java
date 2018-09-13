package com.prudential.web.crawler;

import com.prudential.web.crawler.manager.WebCrawlerManager;

import java.net.URL;
import java.util.Set;

public class Application {

    public static void main(String[] args) {

        WebCrawlerManager webCrawlerManager = new WebCrawlerManager();
        webCrawlerManager.crawl("http://www.prudential.co.uk/");

        final Set<URL> urlSet = webCrawlerManager.getVisitedURLs();

        if (urlSet == null || urlSet.isEmpty()) {
            System.out.println("No URLS crawled");
            return;
        }

        System.out.println("No.of URLs crawled: " + urlSet.size() + ". Below URLs were crawled: ");
        for (URL url : urlSet) {
            System.out.println(url);
        }
    }
}
