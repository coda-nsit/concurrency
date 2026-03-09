import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * // This is the HtmlParser's API interface.
 * // You should not implement it, or speculate about its implementation
 * interface HtmlParser {
 *     public List<String> getUrls(String url) {}
 * }
 */

// Version 1 (multi threaded but not optimized. See below why.)
// Maintain a queue
// Start 4 threads
// Each thread does the following
// 1. Pick url from the queue if there is any URL in the queue
// 2. Mark the url visited
// 3. Call getUrls to get the new set of URLs. (the thread gets blocked here so it's not optimal.
//    Will optimize this in Version 2.)
// 4. Put the URLs obtained in 3. into the queue.

// This solution doesn't work because there is no way to know when the queue becomes empty.

class Task implements Runnable {

    private final Solution solution;
    private final HtmlParser htmlParser;

    public Task(Solution solution, HtmlParser htmlParser) {
        this.solution = solution;
        this.htmlParser =  htmlParser;
    }

    @Override
    public void run() {
        solution.reentrantWriteLock.lock();
        try {
            while (true) {
                while (solution.workQueue.isEmpty()) {
                    if (!solution.qIsNotEmpty.await(1, TimeUnit.MILLISECONDS)) {
                        return;
                    }
                }

                String currentUrl = solution.workQueue.pollFirst();
                solution.visitedUrls.add(currentUrl);
                List<String> newUrls = this.htmlParser.getUrls(currentUrl); // blocking, optimize this
                // System.out.println("currentUrl:" + currentUrl + "; newUrls:" + newUrls);
                if (!newUrls.isEmpty()) {
                    for (String newUrl: newUrls) {
                        if  (!solution.visitedUrls.contains(newUrl)
                                && solution.hostName.equals(solution.getHostNameFromUrl(newUrl))) {
                            solution.workQueue.addLast(newUrl);
                        }
                    }
                    solution.qIsNotEmpty.signalAll();
                }
                // System.out.printf("Current queue size is:%d\n", solution.workQueue.size());
                // System.out.println("visited URLs:" + solution.visitedUrls);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            solution.reentrantWriteLock.unlock();
        }
    }
}

class Solution {

    public final ReentrantReadWriteLock.WriteLock reentrantWriteLock = new ReentrantReadWriteLock().writeLock();
    public final Condition qIsNotEmpty = reentrantWriteLock.newCondition();

    public final Deque<String> workQueue = new ArrayDeque<>();
    public final Set<String> visitedUrls = new HashSet<>();

    public String hostName;

    public String getHostNameFromUrl(String url) {
        return url.split("/")[2];
    }

    public List<String> crawl(String startUrl, HtmlParser htmlParser) {;
        // this needs to be added manually or else there will be deadlock.
        // See Task.Run() -> while (solution.workQueue.isEmpty()) {} to see why
        workQueue.addLast(startUrl);

        hostName =  getHostNameFromUrl(startUrl);

        try (ExecutorService executorService =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (int i = 0; i < 100; ++i) {
                executorService.submit(new Task(this, htmlParser));
            }
        }
        return new ArrayList<>(visitedUrls);
    }
}
