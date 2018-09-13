package com.prudential.web.crawler.manager;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Set;

public class WebCrawlerTest {

    private WebCrawlerManager webCrawlerManager = new WebCrawlerManager();

    @Test
    public void testForValidURL() {
        String seed = "http://www.prudential.co.uk/";
        webCrawlerManager.crawl(seed);
        final Set<URL> urls = webCrawlerManager.getVisitedURLs();
        Assert.assertNotNull(urls);

        for (URL url : urls) {
            if (!url.toString().contains(seed)) {
                Assert.fail("Invalid domain visited: " + url);
            }
        }
    }

    @Test
    public void testForInvalidURL() {
        String seed = "http://www.somerandomurl.com/";
        webCrawlerManager.crawl(seed);
        final Set<URL> urls = webCrawlerManager.getVisitedURLs();
        Assert.assertEquals(1, urls.size());
    }
}
