package JavaConcurrencyConcepts;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


// runnable1 interrupts a long-running callable2
// In Java thread1 can't force kill thread2. thread1 can just request thread2 to be interrupted, it is upto thread2
// to decide what to do when it's interrupted.

public class _08_ThreadInterrupts {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final AtomicReference<Thread> thread2Ref = new AtomicReference<>();

        Runnable runnable1 = () -> {
            try {
                System.out.println("runnable1 is running");
                Thread.sleep(500);
                System.out.println("Interrupting callable2 from runnable1");
                thread2Ref.get().interrupt();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        Callable<Integer> callable2 = () -> {
            try {
                System.out.println("callable2 is running");
                thread2Ref.set(Thread.currentThread());
                for (int i = 1; i <= 10; i++) {
                    System.out.printf("callable2 has value of i: %d\n", i);
                    Thread.sleep(100);
                }
                // dummy example where once this callable is complete, it interrupts itself.
                System.out.println("I completed execution. I will interrupt myself.");
                Thread.currentThread().interrupt();
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("I interrupted myself. I am throwing.");
                    throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("callable2 has been interrupted");
                    throw e;
                } else {
                    throw new RuntimeException("callable2 was interrupted by runnable1 but callable2 says not");
                }
            }
            return null;
        };

        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            executorService.submit(runnable1);
            executorService.submit(callable2);
        }
    }
}
