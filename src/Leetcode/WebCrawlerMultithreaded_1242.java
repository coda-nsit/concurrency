package Leetcode;

import java.util.*;
import java.util.concurrent.locks.*;

// HtmlParser interface given by the problem
// interface HtmlParser {
//     public List<String> getUrls(String url);
// }

/*
suggested by ChatGPT using RentrantLock + Condition. Haven't verified.
 */

class Solution_2 {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition workAvailable = lock.newCondition();

    private final Deque<String> queue = new ArrayDeque<>();
    private final Set<String> visited = new HashSet<>();

    private String startHost;
    private HtmlParser parser;

    private int working = 0;     // threads actively processing a URL
    private boolean done = false;

    public List<String> crawl(String startUrl, HtmlParser htmlParser) {
        this.parser = htmlParser;
        this.startHost = hostOf(startUrl);

        lock.lock();
        try {
            visited.add(startUrl);
            queue.addLast(startUrl);
            workAvailable.signalAll();
        } finally {
            lock.unlock();
        }

        int nThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        Thread[] workers = new Thread[nThreads];

        for (int i = 0; i < nThreads; i++) {
            workers[i] = new Thread(this::workerLoop);
            workers[i].start();
        }

        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Return in any order
        return new ArrayList<>(visited);
    }

    private void workerLoop() {
        while (true) {
            String url;

            // 1) Take work (or decide to stop)
            lock.lock();
            try {
                while (queue.isEmpty() && !done) {
                    // If nobody is working and no queued work => done
                    if (working == 0) {
                        done = true;
                        workAvailable.signalAll();
                        break;
                    }
                    workAvailable.await();
                }

                if (done) return;

                url = queue.removeFirst();
                working++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }

            // 2) Do network call without holding the lock
            List<String> outLinks = parser.getUrls(url);

            // 3) Enqueue newly discovered same-host links
            for (String next : outLinks) {
                if (!startHost.equals(hostOf(next))) continue;

                lock.lock();
                try {
                    if (visited.add(next)) {       // add returns true if it was absent
                        queue.addLast(next);
                        workAvailable.signal();    // wake one waiting thread
                    }
                } finally {
                    lock.unlock();
                }
            }

            // 4) Mark work done, possibly signal termination
            lock.lock();
            try {
                working--;
                if (queue.isEmpty() && working == 0) {
                    done = true;
                    workAvailable.signalAll();
                } else {
                    // nudge waiters in case queue became non-empty earlier
                    workAvailable.signal();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    // Extract hostname from URL: "http://host/path" => "host"
    private static String hostOf(String url) {
        int scheme = url.indexOf("://");
        int start = (scheme >= 0) ? scheme + 3 : 0;
        int slash = url.indexOf('/', start);
        return (slash >= 0) ? url.substring(start, slash) : url.substring(start);
    }
}

public class WebCrawlerMultithreaded_1242 {
}
