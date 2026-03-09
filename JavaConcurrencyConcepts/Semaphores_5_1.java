/*
    We have a server. We want to do active throttling based on the number of users who are connected.
    If number of users exceed 1000, throttle.
*/

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

class Server {
    private static Server singletonInstance;

    private final int maxNumberOfConnections = 50;
    private final Semaphore maxConnections = new Semaphore(maxNumberOfConnections);

    private Server() {}

    public static Server getServer()
    {
        synchronized (Server.class)
        {
            if (singletonInstance == null) {
                singletonInstance = new Server();
            }
            return singletonInstance;
        }
    }

    public void setConnections(int numConnectionsRequired) throws InterruptedException {
        this.maxConnections.acquire(numConnectionsRequired);
    }

    public void freeConnections(int numConnectionsRequired) throws InterruptedException {
        this.maxConnections.release(numConnectionsRequired);
    }

    public int numberOfConnectionsRemaining() {
        return this.maxConnections.availablePermits();
    }
}

class ServerWorker implements Runnable {

    private final String threadName;
    private final int numConnectionsRequired;

    public ServerWorker(String threadName, int numConnectionsRequired) {
        this.threadName = threadName;
        this.numConnectionsRequired = numConnectionsRequired;
    }

    @Override
    public void run() {
        System.out.printf("***** Thread named %s started *****\n", this.threadName);
        try {
            // make the thread request for server connections
            Server server = Server.getServer();
            System.out.printf("Current connections remaining: %d\n", server.numberOfConnectionsRemaining());
            // reserve resources
            server.setConnections(numConnectionsRequired);
            // do some work
            Thread.sleep(1000);
            // work is done, now release it
            server.freeConnections(numConnectionsRequired);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("##### Thread named %s ended #####\n\n", this.threadName);
    }
}

public class Semaphores_5_1 {
    public static void main(String[] args) {
        final int numThreads = 10;
        try (ExecutorService executorService = Executors.newFixedThreadPool(numThreads)) {
            for (int i = 1; i <= numThreads; i++) {
                executorService.submit(new ServerWorker("Thread-" + i, i % 11));
            }
        }
    }
}
