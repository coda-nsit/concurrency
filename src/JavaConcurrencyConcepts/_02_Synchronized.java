package JavaConcurrencyConcepts;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Counter {
    private int counter;
    private int threadSafeCounter;

    public Counter(int counter, int threadSafeCounter) {
        this.counter = counter;
        this.threadSafeCounter = threadSafeCounter;

    }

    public int getCounter() {
        return counter;
    }

    public int getThreadSafeCounter() {
        return threadSafeCounter;
    }

    // the threads are stamping on each other
    // this.counter++ =>
    // temp = this.counter + 1
    // this.counter = temp
    public void incrementCounter() {
        this.counter++;

        // synchronized (this) {} marks a critical section. So when one thread is inside it
        // other threads cant enter the block.
        // Every object in Java has a monitor. We can lock and unlock a monitor.
        // When a monitor is locked, no other threads can write on that object.
        // When we say synchronized(this) we are basically locking "this" objects monitor.
        // Flow:
        // 1. "this" has no lock
        // 2. thread1 comes and acquires the lock on this
        // 3. thread2 comes in and tries to enter the {} but fails because thread1 had acquired the lock.
        //    thread2 is put in the monitors queue
        // 4. thread3 comes in and tries to enter the {} but fails because thread1 had acquired the lock.
        //    thread3 is put in the monitors queue
        // 5. thread1 exits the {} and releases the lock
        // 6. JVM picks one of the waiting threads (not guaranteed FIFO)
        synchronized (this) {
            this.threadSafeCounter++;
        }
    }
}

class CounterIncrementor implements Runnable {

    private final String threadName;
    private final Counter counter;

    public CounterIncrementor(String threadName, Counter counter) {
        this.threadName = threadName;
        this.counter = counter;
    }

    @Override
    public void run() {
        System.out.printf("%s: is incrementing the counter.\n", this.threadName);
        for (int i = 0; i < 10000; i++) {
            this.counter.incrementCounter();
        }
    }
}

public class _02_Synchronized {
    public static void main(String[] args) {

        int numThreads = 100;
        Counter counter = new Counter(0, 0);

        try (ExecutorService executorService = Executors.newFixedThreadPool(numThreads)) {
            for (int i = 0; i < numThreads; i++) {
                executorService.submit(new CounterIncrementor("Thread-" + i, counter));
            }
        }
        System.out.printf("Value of counter: %d\n", counter.getCounter());
        System.out.printf("Value of thread safe counter: %d\n", counter.getThreadSafeCounter());
    }
}
