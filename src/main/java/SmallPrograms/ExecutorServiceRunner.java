package SmallPrograms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.System.out;

public class ExecutorServiceRunner {

    public static void main(String[] args)
    {
        var executorServiceDemo = new ExecutorServiceDemo();
        executorServiceDemo.initialize();
        executorServiceDemo.runDemo();
        executorServiceDemo.terminate();
    }
}

class ExecutorServiceDemo
{
    private List<Future<String>> futures;
    private ExecutorService executorService;

    void initialize()
    {
        final int numCores = Runtime.getRuntime().availableProcessors();
        out.println("Total cores: " + numCores);
        executorService = Executors.newFixedThreadPool((int)Math.ceil(numCores/2.0));
        futures = new ArrayList<>();
        for (int i = 0; i < 25; ++i)
        {
            futures.add(executorService.submit(new Task("Task is: " + i)));
        }
    }

    void runDemo()
    {
        try
        {
            for (Future<String> pointFuture: futures)
            {
                String stringToPrint = pointFuture.get();
                out.println(stringToPrint);
            }
        }
        catch (InterruptedException | ExecutionException exception)
        {
            exception.printStackTrace();
        }
    }

    void terminate()
    {
        out.println("Shutting down the thread pool");
        executorService.shutdown();
    }

}

class Task implements Callable<String>
{
    private String stringToPrint;
    Task(String stringToPrint)
    {
        this.stringToPrint = stringToPrint;
    }
    @Override
    public String call() throws InterruptedException
    {
        out.println(Thread.currentThread().getName() + " is sleeping.");
        Thread.sleep(2000);
        out.println(Thread.currentThread().getName() + " is awake again.");
        return "Coming from Runnable: " + stringToPrint + " with Thread: " + Thread.currentThread().getName();
    }
}
