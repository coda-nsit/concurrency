import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Foo {

    private final ReentrantReadWriteLock fooReadWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock fooWriteLock = fooReadWriteLock.writeLock();

    private final Condition printFirstCondition = fooWriteLock.newCondition();
    private final Condition printSecondCondition = fooWriteLock.newCondition();
    private final Condition printThirdCondition = fooWriteLock.newCondition();

    private int whoseTurn = 1;

    public Foo() {
    }

    public void first(Runnable printFirst) throws InterruptedException {
        fooWriteLock.lock();
        try {
            while (whoseTurn != 1) {
                printFirstCondition.await();
            }
            // printFirst.run() outputs "first". Do not change or remove this line.
            printFirst.run();
            whoseTurn = 2;
            printSecondCondition.signal();
        } finally {
            fooWriteLock.unlock();
        }
    }

    public void second(Runnable printSecond) throws InterruptedException {
        fooWriteLock.lock();
        try {
            while (whoseTurn != 2) {
                printSecondCondition.await();
            }
            // printSecond.run() outputs "second". Do not change or remove this line.
            printSecond.run();
            whoseTurn = 3;
            printThirdCondition.signal();

        } finally {
            fooWriteLock.unlock();
        }
    }

    public void third(Runnable printThird) throws InterruptedException {
        fooWriteLock.lock();
        try {
            while (whoseTurn != 3) {
                printThirdCondition.await();
            }
            // printThird.run() outputs "third". Do not change or remove this line.
            printThird.run();
            whoseTurn = 1;
            printFirstCondition.signal();

        } finally {
            fooWriteLock.unlock();
        }
    }
}

public class PrintInOrder_1114 {
    public static void main(String[] args) {

    }
}