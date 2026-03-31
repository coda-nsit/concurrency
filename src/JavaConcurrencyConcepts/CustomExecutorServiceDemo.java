package JavaConcurrencyConcepts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This file tries to replicate a Java ExecutorService class from scratch using only Threads.
 * 1. CustomExecutorServiceDemo is the Client which
 * 2. CustomExecutorService is the actual class which manages the thread pool
 */

class Task implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.printf("Thread named:%s starting execution\n", Thread.currentThread().getName());
        System.out.printf("Thread named:%s going to sleep\n", Thread.currentThread().getName());
        Thread.sleep(1000);
        System.out.printf("Thread named:%s woke up. Exiting now. \n", Thread.currentThread().getName());
        return 0;
    }
}

// This task is performed by the worker threads in the executor service.
class Worker implements Runnable {
    CustomExecutorService customExecutorService;
    public Worker(CustomExecutorService customExecutorService) {
        this.customExecutorService = customExecutorService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                customExecutorService.runTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class QueueEnqueueTask implements Runnable {
    CustomExecutorService customExecutorService;
    Callable<Integer> taskToQueue;

    public QueueEnqueueTask(CustomExecutorService customExecutorService, Callable<Integer> taskToQueue) {
        this.customExecutorService = customExecutorService;
        this.taskToQueue = taskToQueue;
    }

    @Override
    public void run() {
        customExecutorService.addTask(taskToQueue);
        return;
    }
}

/**
 * 1. taskQ.size() <= maxTasks
 * 2.
 */
class CustomExecutorService {
    private final Queue<Callable<Integer>> taskQ;
    private final List<Thread> threads;
    private final int maxThreads;
    private final int maxTasks;
    private final ReentrantReadWriteLock qRWLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock qWLock = qRWLock.writeLock();
    private final Condition qEmptyCondition = qWLock.newCondition();
    private final Condition qFullCondition = qWLock.newCondition();

    private int currentNumberOfTasks;

    public CustomExecutorService(int maxThreads, int maxTasks) {
        this.taskQ = new ArrayDeque<>();
        this.threads = new ArrayList<>();
        this.maxThreads = maxThreads;
        this.maxTasks = maxTasks;
        this.currentNumberOfTasks = 0;
    }

    public void addTask(Callable<Integer> task) {
        try {
            qWLock.lock();
            while (taskQ.size() >= maxTasks) {
                qFullCondition.await();
            }
            currentNumberOfTasks += 1;
            taskQ.add(task);
            qEmptyCondition.signalAll();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            qWLock.unlock();
        }
    }

    private Callable<Integer> dequeueTask() {
        try {
            qWLock.lock();
            while (taskQ.isEmpty()) {
                qEmptyCondition.await();
            }
            Callable<Integer> taskToExecute = taskQ.poll();
            currentNumberOfTasks -= 1;
            qFullCondition.signalAll();
            return taskToExecute;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            qWLock.unlock();
        }
    }

    public void runTask() throws Exception {
        Callable<Integer> taskToRun = dequeueTask();
        Integer taskValue = taskToRun.call();
    }

    // This function should be non-blocking.
    // It just adds the task to the taskQ for threads to pick them up and execute.
    public void submit(Callable<Integer> callable) {
        if (currentNumberOfTasks >= maxTasks) {
            throw new RuntimeException("Maximum number of tasks exceeded. Please submit tasks later.");
        }
        // Create a thread to just put the item in task queue.
        Thread thread = new Thread(new QueueEnqueueTask(this, callable));
        thread.start();
    }

    public void close() {
        for (Thread thread: threads) {
            thread.interrupt();
        }
    }
}

public class CustomExecutorServiceDemo {
    public static void main(String[] args) {
        var customExecutorService = new CustomExecutorService(5, 100);
        List<Callable<Integer>> tasksToDo = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasksToDo.add(new Task());
        }

        for (Callable<Integer> task: tasksToDo) {
            customExecutorService.submit(task);
        }
    }
}
