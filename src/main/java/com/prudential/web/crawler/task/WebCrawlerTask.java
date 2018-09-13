package com.prudential.web.crawler.task;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Class {@link WebCrawlerTask} makes an HTTP call to the passed URL and the extracts the links to be crawled
 */
public class WebCrawlerTask implements Callable<Set<URL>> {

    private final URL url;
    private final Set<URL> urlList = new HashSet<>();
    //Setting time out for JSoups as 60 seconds
    private static final int TIME_OUT = 60000;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCrawlerTask.class);

    public WebCrawlerTask(final URL url) {
        this.url = url;
    }

    @Override
    public Set<URL> call() throws Exception {

        LOGGER.info("Calling URL {}", url);

        try {
            final Document document = Jsoup.parse(url, TIME_OUT);
            processLinks(document.select("a[href]"));
        } catch (HttpStatusException e) {
            LOGGER.debug("HTTP call to URL: {} failed", url);
        }

        return urlList;
    }

    /**
     * Method processLinks extracts all the links and adds it to the list of links
     *
     * @param links the href's in the page
     */
    private void processLinks(Elements links) {

        if (links == null || links.isEmpty()) {
            LOGGER.debug("Empty links");
            return;
        }

        String href;

        for (Element link : links) {
            href = link.attr("href");

            if (href == null || href.isEmpty() || href.startsWith("#")) {
                continue;
            }

            try {

                URL tempURL;

                if (href.contains("#")) {
                    tempURL = new URL(url, href.substring(0, href.indexOf("#")));
                } else {
                    tempURL = new URL(this.url, href);
                }

                this.urlList.add(tempURL);

            } catch (MalformedURLException e) {
                LOGGER.debug("Skipping Malformed URI: {}", href);
            }
        }
    }
}
