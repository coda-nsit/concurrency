package Leetcode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/// // This is the HtmlParser's API interface.
/// // You should not implement it, or speculate about its implementation
/// interface HtmlParser {
///     public List<String> getUrls(String url) {}
/// }
/// Based on: https://leetcode.com/problems/web-crawler-multithreaded/solutions/1659956/java-solution-using-executorservice-and-t8ghl
/// No need for infinite loops.
/// No need for queue.
/// No need for conditions.
/// Task can call Task (recursive) with the new URL the task finds.

class UrlProcessingTask  implements Runnable {

    private Solution solution;
    private String currentUrl;

    public UrlProcessingTask(Solution solution, String currentUrl) {
        this.solution = solution;
        this.currentUrl = currentUrl;
    }

    @Override
    public void run()  {
        solution.lock.lock();
        solution.visitedUrls.add(currentUrl);
        solution.lock.unlock();

        List<String> newUrls = solution.htmlParser.getUrls(currentUrl);

        solution.lock.lock();
        try {
            for (String newUrl : newUrls) {
                if (solution.hostName.equals(solution.getHostNameFromUrl(newUrl))
                        && !solution.visitedUrls.contains(newUrl)) {
                    solution.executorService.submit(new UrlProcessingTask(solution, newUrl));
                }
            }
        } finally {
            solution.lock.unlock();
        }
    }
}

class Solution {

    public String hostName;

    public HtmlParser htmlParser;

    public volatile Set<String> visitedUrls = new HashSet<>();

    public final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public final ReentrantLock lock = new ReentrantLock();

    public String getHostNameFromUrl(String url) {
        return url.split("/")[2];
    }

    public List<String> crawl(String startUrl, HtmlParser htmlParser) {

        this.htmlParser = htmlParser;

        hostName = getHostNameFromUrl(startUrl);
        try {
            executorService.submit(new UrlProcessingTask(this, startUrl));
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }

        return new ArrayList<>(visitedUrls);
    }
}

public class WebCrawlerMultithreaded_1242_3 {
}
