package com.prudential.web.crawler.manager;

import com.prudential.web.crawler.task.WebCrawlerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * Class {@link WebCrawlerManager} contains gets the ball rolling for the web crawler & submits the tasks to the thread pool
 */
public class WebCrawlerManager {

    private Set<URL> visitedURLs = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final List<Future<Set<URL>>> result = new ArrayList<>();
    private String baseURL;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebCrawlerManager.class);

    /**
     * Method crawl starts the web crawling of passed URL
     *
     * @param url Base URL to be crawled
     */
    public void crawl(final String url) {

        this.visitedURLs = Collections.synchronizedSet(new HashSet<>());

        //To ensure that the we are not crawling outside the domain Prudential
        this.baseURL = url.replaceAll("(.*//.*/).*", "$1");

        try {
            this.submitURL(new URL(url));

            while (areLinksPending()) {
                LOGGER.debug("Waiting for the crawling to complete...");
            }

            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);

        } catch (MalformedURLException e) {
            LOGGER.error("Skipping malformed URL: {}", url);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException ", e);

            //Clear the interrupt exception
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Method submitURL adds submits the URL to be crawled to the executor service
     *
     * @param url URL to be crawled
     */
    private void submitURL(URL url) {

        if (!shouldVisitURL(url)) {
            return;
        }

        this.visitedURLs.add(url);

        result.add(executorService.submit(new WebCrawlerTask(url)));
    }

    /**
     * Method shouldVisitURL checks if the URL submitted to the executor should be visited or not
     *
     * @param url URL to be crawled
     * @return true if the URL can be crawled, false otherwise
     */
    private boolean shouldVisitURL(final URL url) {

        //Skip if the URL has already been visited
        if (this.visitedURLs.contains(url)) {
            return false;
        }

        final String urlString = url.toString();

        //Skip the URL if it is not in domain www.prudential.co.uk
        if (!urlString.contains(this.baseURL)) {
            return false;
        }

        //Skip the URLs that have year wise reports, presentations or any such archived data
        if (urlString.matches("^.*\\d\\d\\d\\d$")) {
            LOGGER.debug("Skipping {}", urlString);
            return false;
        }

        //Skip the URLs that download files
        return !(urlString.endsWith(".pdf") || urlString.endsWith(".PDF")
                || urlString.endsWith(".docx")
                || urlString.endsWith(".xlsx")
                || urlString.endsWith(".zip")
                || urlString.endsWith(".txt"));
    }

    /**
     * Method areLinksPending waits for the crawling process to be completed
     *
     * @return true if links are pending to be crawled, false otherwise
     * @throws InterruptedException throws InterruptedException if future task times out
     */
    private boolean areLinksPending() throws InterruptedException {

        Thread.sleep(1000);

        final Set<URL> linksToBeCrawled = new HashSet<>();
        final Iterator<Future<Set<URL>>> iterator = result.iterator();
        Future<Set<URL>> future;

        while (iterator.hasNext()) {

            future = iterator.next();

            if (future.isDone()) {

                iterator.remove();

                try {
                    linksToBeCrawled.addAll(future.get());
                } catch (ExecutionException e) {
                    LOGGER.error("ExecutionException while checking status", e);
                }
            }
        }

        for (final URL link : linksToBeCrawled) {
            submitURL(link);
        }

        return (result.size() > 0);
    }

    /*
     * Returns a copy of visited URL
     */
    public Set<URL> getVisitedURLs() {
        return new HashSet<>(this.visitedURLs);
    }
}
