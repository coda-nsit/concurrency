import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * // This is the HtmlParser's API interface.
 * // You should not implement it, or speculate about its implementation
 * interface HtmlParser {
 *     public List<String> getUrls(String url) {}
 * }
 */

/**
 * Maintain 2 queues, one for memory operations and one for network operations (getUrls call).
 * Terminating condition: memory task queue and IO task queue are empty
 */

class MemoryWorker implements Runnable {

    private final Solution solution;

    MemoryWorker(Solution solution) {
        this.solution = solution;
    }

    @Override
    public void run() {
        solution.memoryQWriteLock.lock();
        try {
            while (true) {
                while (solution.memoryQ.isEmpty()) {
                    solution.memoryQNotEmpty.await();

                    // No work left to be done
                    if (solution.memoryQ.isEmpty() && solution.networkQ.isEmpty()) {
                        return;
                    }
                }

                String currentUrl =  solution.memoryQ.removeFirst();
                solution.visitedUrls.add(currentUrl);
                solution.networkQ.addLast(currentUrl);
                solution.executorService.submit(new NetworkWorker(solution));
                solution.networkQNotEmpty.signalAll();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            solution.memoryQWriteLock.unlock();
        }
    }
}

class NetworkWorker implements Runnable {
    private final Solution solution;

    NetworkWorker(Solution solution) {
        this.solution = solution;
    }

    @Override
    public void run() {
        solution.networkQWriteLock.lock();
        try {
            while (solution.networkQ.isEmpty()) {
                solution.networkQNotEmpty.await();

                if (solution.networkQ.isEmpty() && solution.memoryQ.isEmpty()) {
                    return;
                }
            }
            String currentUrl =  solution.networkQ.peekFirst();
            List<String> newUrls = solution.htmlParser.getUrls(currentUrl);
            boolean qPopped = false;
            if (!newUrls.isEmpty()) {
                for (String newUrl : newUrls) {
                    if (solution.getHostNameFromUrl(newUrl).equals(
                            solution.getHostNameFromUrl(currentUrl)) &&
                            !solution.visitedUrls.contains(newUrl)) {
                        solution.memoryQ.addLast(newUrl);
                        solution.memoryQNotEmpty.signal();
                        if (!qPopped) {
                            qPopped = true;
                            solution.networkQ.removeFirst();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            solution.networkQWriteLock.unlock();
        }
    }
}

class Solution {

    public final ReentrantReadWriteLock.WriteLock memoryQWriteLock = new ReentrantReadWriteLock().writeLock();
    public final Condition memoryQNotEmpty = memoryQWriteLock.newCondition();

    public final ReentrantReadWriteLock.WriteLock networkQWriteLock = new ReentrantReadWriteLock().writeLock();
    public final Condition networkQNotEmpty = networkQWriteLock.newCondition();

    public Deque<String> memoryQ = new ArrayDeque<>();
    public Deque<String> networkQ = new ArrayDeque<>();

    public Set<String> visitedUrls = new HashSet<>();

    public HtmlParser htmlParser;

    public ExecutorService executorService;

    public String getHostNameFromUrl(String url) {
        return url.split("/")[2];
    }

    public List<String> crawl(String startUrl, HtmlParser htmlParser) {
        this.htmlParser = htmlParser;
        memoryQ.add(startUrl);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < 25; ++i) {
            executorService.submit(new MemoryWorker(this));
        }
        executorService.close();
        return new ArrayList<>(visitedUrls);
    }
}