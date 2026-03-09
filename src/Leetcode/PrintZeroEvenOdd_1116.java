import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntConsumer;

class ZeroEvenOdd {
     private int n;

    private final Lock reentrantWriteLock;

    private final Condition printZeroCondition;
    private final Condition printEvenCondition;
    private final Condition printOddCondition;

    private int whoseTurn;
    private int nextNumber;

    public ZeroEvenOdd(int n) {
        this.n = n;

        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        this.reentrantWriteLock = reentrantReadWriteLock.writeLock();

        this.printZeroCondition = this.reentrantWriteLock.newCondition();
        this.printEvenCondition = this.reentrantWriteLock.newCondition();
        this.printOddCondition = this.reentrantWriteLock.newCondition();

        this.whoseTurn = 0; // only 3 values -> 0, 1, 2
        this.nextNumber = 1; // 1, 2, 3, 4, 5, 6, 7, ...
    }

    // printNumber.accept(x) outputs "x", where x is an integer.
    public void zero(IntConsumer printNumber) throws InterruptedException {
        System.out.printf("entering zero => n: %d; whoseTurn: %d; nextNumber:%d\n", n, whoseTurn, nextNumber);
        this.reentrantWriteLock.lock();
        try {
            while (this.n > 0) {
                while (this.whoseTurn != 0) {
                    this.printZeroCondition.await();
                }
                this.n -= 1;
                printNumber.accept(0);
                if (this.nextNumber % 2 == 0) {
                    this.whoseTurn = 2;
                    this.printEvenCondition.signalAll();
                } else  {
                    this.whoseTurn = 1;
                    this.printOddCondition.signalAll();
                }
            }
        } finally {
            this.reentrantWriteLock.unlock();
            System.out.printf("exiting zero => n: %d; whoseTurn: %d; nextNumber:%d\n", n, whoseTurn, nextNumber);
        }
    }

    public void even(IntConsumer printNumber) throws InterruptedException {
        System.out.printf("entering even => n: %d; whoseTurn: %d; nextNumber:%d\n", n, whoseTurn, nextNumber);
        this.reentrantWriteLock.lock();
        try {
            while (this.n > 0) {
                while (this.whoseTurn != 2) {
                    this.printEvenCondition.await();
                }
                printNumber.accept(this.nextNumber);
                this.nextNumber += 1;
                this.whoseTurn = 0;
                this.printZeroCondition.signalAll();
            }
        } finally {
            this.reentrantWriteLock.unlock();
            System.out.printf("exiting even => n: %d; whoseTurn: %d; nextNumber:%d\n", n, whoseTurn, nextNumber);
        }
    }

    public void odd(IntConsumer printNumber) throws InterruptedException {
        System.out.printf("entering odd => n: %d; whoseTurn: %d; nextNumber:%d\n", n, whoseTurn, nextNumber);
        this.reentrantWriteLock.lock();
        try {
            while (this.n > 0) {
                while (this.whoseTurn != 1) {
                    this.printOddCondition.await();
                }
                printNumber.accept(this.nextNumber);
                this.nextNumber += 1;
                this.whoseTurn = 0;
                this.printZeroCondition.signalAll();
            }
        } finally {
            this.reentrantWriteLock.unlock();
            System.out.printf("exiting odd => n: %d; whoseTurn: %d; nextNumber:%d\n", n, whoseTurn, nextNumber);
        }
    }
}

public class PrintZeroEvenOdd_1116 {
    public static void main(String[] args) {

    }
}
