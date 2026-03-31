import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Task: Implement a blocking queue. Multiple threads can't push or remove data to the queue.
// there is one lock on the queue with 2 conditions.
// 1. which ensures queue size > 0 while polling from queue
// 2. which ensures queue size < maxSize while pushing

class QueueReader implements Callable<Double> {

    private final PubSubQueue pubSubQueue;

    public QueueReader(PubSubQueue pubSubQueue) {
        this.pubSubQueue = pubSubQueue;
    }

    @Override
    public Double call() throws InterruptedException {
        this.pubSubQueue.queueReadLock.lock();
        boolean wasWaitTimeEnough = true;
        try {
            while (this.pubSubQueue.queue.isEmpty() && wasWaitTimeEnough) {
                // following is incorrect as queueNotEmpty is a condition on this.pubSubQueue.queueWriteLock and
                // here the lock is this.pubSubQueue.queueReadLock.lock()
                // If a condition is on a lock named LockA then the it can only be used when LockA is acquired.
                // If that condition is used on some other lock then you will get IllegalMonitorException
                wasWaitTimeEnough = this.pubSubQueue.queueNotEmpty.await(1, TimeUnit.SECONDS);
            }
            Integer queueValue = this.pubSubQueue.queue.peek();
            assert queueValue != null;
            return queueValue.doubleValue();
        } finally {
            this.pubSubQueue.queueReadLock.unlock();
        }
    }
}

class Publisher implements Runnable {
    // if queue size < queueSize
    //     insert data into the queue
    // else
    //     block the thread until the queue size becomes less than queueSize

    private final PubSubQueue pubSubQueue;
    private final Integer dataToPublish;

    public Publisher(PubSubQueue pubSubQueue, Integer dataToPublish) {
        this.pubSubQueue = pubSubQueue;
        this.dataToPublish = dataToPublish;
    }

    @Override
    public void run() {
        boolean wasWaitTimeEnough = true;
        this.pubSubQueue.queueWriteLock.lock();
        try {
            // You might have the question-- if .await() is a blocking call then why do we need the while loop?
            // This is because sometimes Java signals even though the programmer didn't ask for it. In such cases,
            // the control goes back to the while condition.
            // What's the purpose of having .await()? This puts the thread to sleep and stops it from using CPU core.
            while (this.pubSubQueue.queue.size() >= this.pubSubQueue.maxQueueSize && wasWaitTimeEnough) {
                try {
                    // this timeout is needed because if the producer produces too much data and there are less consumers
                    // then this will just keep waiting
                    wasWaitTimeEnough = this.pubSubQueue.queueNotFull.await(1, TimeUnit.SECONDS); // blocks the thread
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (wasWaitTimeEnough) {
                this.pubSubQueue.queue.add(this.dataToPublish);
                // signalAll() vs signal() -> in signalAll() all the threads who wait on this condition are signaled.
                // JVM then picks one of them and starts executing. Its not known which one will start executing.
                // signal() JVM picks the longest waiting thread and starts executing it.
                this.pubSubQueue.queueNotEmpty.signal();
            }
        } finally {
            this.pubSubQueue.queueWriteLock.unlock();
        }
    }
}

class Subscriber implements Callable<Double> {

    // if queue is not empty
    //     pulls data (int) from the queue and adds a random jitter to it
    // else
    //     block the thread until there is some data in the queue

    private final PubSubQueue pubSubQueue;

    public Subscriber(PubSubQueue pubSubQueue) {
        this.pubSubQueue = pubSubQueue;
    }

    @Override
    public Double call() throws InterruptedException {
        boolean wasWaitTimeEnough = true;
        this.pubSubQueue.queueWriteLock.lock();
        try {
            while (this.pubSubQueue.queue.isEmpty() && wasWaitTimeEnough) {
                // here timeout is needed as there might not be enough data produced in which case the consumer
                // will keep waiting
                wasWaitTimeEnough = this.pubSubQueue.queueNotEmpty.await(1, TimeUnit.SECONDS);
            }
            Integer queueElement = -1;
            if (wasWaitTimeEnough) {
                queueElement = this.pubSubQueue.queue.poll();
                if (this.pubSubQueue.queue.size() < this.pubSubQueue.maxQueueSize) {
                    this.pubSubQueue.queueNotFull.signal();
                }
                return queueElement + Math.random();
            }
            return (double)queueElement;
        } finally {
            this.pubSubQueue.queueWriteLock.unlock();
        }
    }
}

class PubSubQueue {
    private static PubSubQueue pubSubQueue = null;
    public final Queue<Integer> queue = new LinkedList<>();

    // ReentrantLock(fairness) -> if fairness is true, JVM ensures when the lock is released the thread which was waiting
    // longest gets the lock.
    public final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(true);

    public final ReentrantReadWriteLock.ReadLock queueReadLock = reentrantReadWriteLock.readLock();

    public final ReentrantReadWriteLock.WriteLock queueWriteLock = reentrantReadWriteLock.writeLock();
    public final Condition queueNotEmpty = queueWriteLock.newCondition();
    public final Condition queueNotFull = queueWriteLock.newCondition();

    public int maxQueueSize;

    private PubSubQueue(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    static PubSubQueue getInstance()
    {
        if (pubSubQueue == null)
        {
            pubSubQueue =  new PubSubQueue(5);
        }
        return pubSubQueue;
    }
}

public class ReentrantReadWriteLock_7 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final int numThreads = Runtime.getRuntime().availableProcessors();
        final Set<Future<Double>> doneFutureSet = new HashSet<>();
        final List<Future<Double>> toDoFutureList = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(numThreads)) {
            for (int i = 0; i < 1000; i++) {
                // flip a coin, if head then produce or else consume
                int coinFlipResult = (int)(Math.random()*100);
                if (coinFlipResult % 2 == 0) {
                    int dataToInsert = (int)(Math.random()*100);
                    System.out.printf("Inserting %d into the queue.\n", dataToInsert);
                    executorService.submit(new Publisher(PubSubQueue.getInstance(), dataToInsert));
                } else {
                    toDoFutureList.add(executorService.submit(new Subscriber(PubSubQueue.getInstance())));
                    for (Future<Double> future : toDoFutureList) {
                        if (future.isDone() && !doneFutureSet.contains(future)) {
                            System.out.printf("value from consumer: %f\n", future.get());
                            doneFutureSet.add(future);
                        }
                    }
                }
            }
        }
        System.out.println("Tasks should be done now. Processing remaining futures.");
        for (Future<Double> future : toDoFutureList) {
            if (future.isDone() && !doneFutureSet.contains(future)) {
                System.out.printf("value from consumer: %f\n", future.get());
                doneFutureSet.add(future);
            }
        }
    }
}
