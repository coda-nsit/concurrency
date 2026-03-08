import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Let H thread print if count H <= 1. If H == 2, block until O = 1
// Let O thread print if count O == 0. If O == 1, block until H = 2
// When H == 2 and O == 1, reset i.e. H = 0 and O = 0
class H2O {

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock reentrantWriteLock = reentrantReadWriteLock.writeLock();

    private final Condition hThreadCanPrintCondition = reentrantWriteLock.newCondition();
    private final Condition oThreadCanPrintCondition = reentrantWriteLock.newCondition();

    private int oAtomsCount = 0;
    private int hAtomsCount = 0;

    public H2O() {

    }

    public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
        reentrantWriteLock.lock();
        System.out.printf("entering hydrogen => h:%d; o:%d\n", hAtomsCount, oAtomsCount);
        try {
            while (hAtomsCount == 2 && oAtomsCount == 0) {
                hThreadCanPrintCondition.await();
            }
            hAtomsCount += 1;
            // releaseHydrogen.run() outputs "H". Do not change or remove this line.
            releaseHydrogen.run();
            if (hAtomsCount == 2 && oAtomsCount == 1) {
                hAtomsCount = oAtomsCount = 0;
                oThreadCanPrintCondition.signalAll();
            }
        } finally {
            reentrantWriteLock.unlock();
            System.out.printf("exiting hydrogen => h:%d; o:%d\n", hAtomsCount, oAtomsCount);
        }
    }

    public void oxygen(Runnable releaseOxygen) throws InterruptedException {
        reentrantWriteLock.lock();
        System.out.printf("entering oxygen => h:%d; o:%d\n", hAtomsCount, oAtomsCount);
        try {
            while (oAtomsCount == 1 && hAtomsCount <= 1) {
                oThreadCanPrintCondition.await();
            }
            oAtomsCount += 1;
            // releaseOxygen.run() outputs "O". Do not change or remove this line.
            releaseOxygen.run();
            if (hAtomsCount == 2 && oAtomsCount == 1) {
                hAtomsCount = oAtomsCount = 0;
                hThreadCanPrintCondition.signalAll();
            }
        } finally {
            reentrantWriteLock.unlock();
            System.out.printf("exiting oxygen => h:%d; o:%d\n", hAtomsCount, oAtomsCount);
        }
    }
}

public class BuildingH2O_1117 {
    public static void main(String[] args)  {
    }
}
