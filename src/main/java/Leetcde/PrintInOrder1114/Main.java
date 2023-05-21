package Leetcde.PrintInOrder1114;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Foo
{
    public Foo()
    {
        CompletableFuture<Void> printInOrderFuture = Run();
        try {
            printInOrderFuture.get();
        }
        catch (ExecutionException | InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public CompletableFuture<Void>  Run()
    {
        Runnable printFirst = () -> System.out.print("first");
        Runnable printSecond = () -> System.out.print("second");
        Runnable printThird = () -> System.out.print("third");

        ExecutorService executorService1 = Executors.newFixedThreadPool(1);
        ExecutorService executorService2 = Executors.newFixedThreadPool(1);
        ExecutorService executorService3 = Executors.newFixedThreadPool(1);
        CompletableFuture<Void> printInOrderCompletableFuture = CompletableFuture.runAsync(() -> {
            try {
                first(printFirst);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executorService1).thenRunAsync(() -> {
            try {
                second(printSecond);
            } catch (InterruptedException e) {
                throw  new RuntimeException(e);
            }
        }, executorService2).thenRunAsync(() -> {
            try {
                third(printThird);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executorService3);

         executorService1.shutdown();
         executorService2.shutdown();
         executorService3.shutdown();
        return printInOrderCompletableFuture;
    }

    public void first(Runnable printFirst) throws InterruptedException {

        // printFirst.run() outputs "first". Do not change or remove this line.
        printFirst.run();
    }

    public void second(Runnable printSecond) throws InterruptedException {

        // printSecond.run() outputs "second". Do not change or remove this line.
        printSecond.run();
    }

    public void third(Runnable printThird) throws InterruptedException {

        // printThird.run() outputs "third". Do not change or remove this line.
        printThird.run();
    }
}

public class Main
{
    public static void main(String[] args)
    {
        Foo foo = new Foo();
        foo.Run();
    }
}
