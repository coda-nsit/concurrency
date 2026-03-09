import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class BoundedBlockingQueue {

    private final ReentrantReadWriteLock.WriteLock reentrantWriteLock;
    private final Condition  notFull;
    private final Condition notEmpty;

    private final Queue<Integer> queue;

    private int capacity;

    public BoundedBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new ArrayDeque<>(capacity);

        final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        this.reentrantWriteLock = reentrantReadWriteLock.writeLock();

        this.notEmpty = this.reentrantWriteLock.newCondition();
        this.notFull = this.reentrantWriteLock.newCondition();
    }

    public void enqueue(int element) throws InterruptedException {
        reentrantWriteLock.lock();
        try {
            while (size() == this.capacity) {
                notFull.await();
            }
            queue.add(element);
            notEmpty.signal();
        } finally {
            reentrantWriteLock.unlock();
        }
    }

    public int dequeue() throws InterruptedException {
        reentrantWriteLock.lock();
        try {
            while (size() == 0) {
                notEmpty.await();
            }
            int qElement = queue.poll();
            notFull.signal();
            return qElement;
        } finally {
            reentrantWriteLock.unlock();
        }
    }

    public int size() {
        return queue.size();
    }
}

public class DesignBoundedBlockingQueue_1188 {
    public static void main(String[] args) {}
}
