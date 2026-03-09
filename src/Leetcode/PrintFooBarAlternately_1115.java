import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class FooBar {
    private int n;

    private final ReentrantReadWriteLock reentrantReadWriteLock;
    private final Lock reentrantWriteLock;

    private final Condition fooCondition;
    private final Condition barCondition;

    private String whoseTurn;

    public FooBar(int n) {
        this.n = n;
        this.reentrantReadWriteLock = new ReentrantReadWriteLock();
        this.reentrantWriteLock =  reentrantReadWriteLock.writeLock();

        this.fooCondition = reentrantWriteLock.newCondition();
        this.barCondition = reentrantWriteLock.newCondition();

        this.whoseTurn = "Foo";
    }

    public void foo(Runnable printFoo) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            reentrantWriteLock.lock();
            try {
                while (!this.whoseTurn.equals("Foo")) {
                    this.fooCondition.await();
                }
                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();
                this.whoseTurn = "Bar";
                this.barCondition.signal();
            } finally {
                reentrantWriteLock.unlock();
            }
        }
    }

    public void bar(Runnable printBar) throws InterruptedException {

        for (int i = 0; i < n; i++) {
            reentrantWriteLock.lock();
            try {
                while (!this.whoseTurn.equals("Bar")) {
                    this.barCondition.await();
                }
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();
                this.whoseTurn = "Foo";
                this.fooCondition.signal();
            } finally {
                reentrantWriteLock.unlock();
            }
        }
    }
}

public class PrintFooBarAlternately_1115 {
    public static void main(String[] args) {

    }
}
