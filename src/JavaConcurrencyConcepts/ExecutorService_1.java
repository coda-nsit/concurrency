import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class CustomCallable implements Callable<Integer>
{
    private final String threadName;
    private final Integer customCallableGuid = (int)(Math.random() * 1000000);

    public CustomCallable(String threadName)
    {
        this.threadName = threadName;
    }
    @Override
    public Integer call() {
        Thread.currentThread().setName(threadName);

        try {
            System.out.printf("%s is running.\n", threadName);
            Thread.sleep(1000);
            System.out.printf("%s is done running.\n", threadName);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.printf("%s is interrupted.\n", threadName);
        }
        return customCallableGuid;
    }
}

public class ExecutorService_1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final int numThreads = 3;
        List<Future<Integer>> futures = new ArrayList<>();
        try (ExecutorService executorService = Executors.newFixedThreadPool(numThreads)) {
            for (int i = 0; i < 100; i++) {
                Future<Integer> temp = executorService.submit(new CustomCallable("Task" + i));
                futures.add(temp);
            }
            for (int i = 0; i < futures.size(); i++) {
                // Future::get() is a blocking call
                // Integer result = futures.get(i).get();

                // Future::get(timeoutDuration, TimeUnit::SECONDS) throws TimeoutException if Future::get doesn't return in
                // timeoutDuration
                // Integer result = futures.get(i).get(1, TimeUnit.SECONDS);

                // if you want to tell JVM that "I no longer want to execute the task". This only works if the task has not
                // already been started executing. If we want to interrupt in case the task has started pass a
                // mayInterruptIfRunning:true to Future::cancel
                // future.cancel(true);

                // check if a future is cancelled
                // bool isFutureCancelled = future.isCancelled();

                // check if the future is done
                // bool isFutureDone = future.isDone();

                Future<Integer> future = futures.get(i);
                try {
                    int result = future.get(1050, TimeUnit.MICROSECONDS);
                    System.out.printf("Task %d ran with GUID %d.\n", i, result);
                } catch (TimeoutException timeoutException) {
                    System.out.printf("Task %d is timed out.\n", i);
                }
            }
        } // if we had put Future<Integer> future = futures.get(i).get(1050, TimeUnit.MICROSECONDS); here
          // nothing would have timed out as the try block exits only when the ExecutorService is done running the tasks
    }
}
