# prudential-web-crawler
prudential-web-crawler is a Java based web crawler that parses all the links under a domain.

The WebCrawlerManager takes the seed URL to be crawled under that domain.

I have used ExecutorService to take the advantage of the parallel processing.

As of now application is storing the crawled URLs as in java.utl.set, but can be enhanced to store the information in a database.

I have used FutureTask to identify if the crawling process is completed. The application can be enhanced by using JMS/Blocking queue (producer-consumer) pattern where the URLs to be crawled are submitted to a queue and listeners crawl those URLs. The advantage of using messaging we can deploy multiple instances of the crawler and crawl more URLs.

#How to run the application
You can run the application as explained below, after you clone the project
<li> Import the project as a maven project in your favourite IDE and run the project as a java project, com.prudential.web.crawler.Application is the main class