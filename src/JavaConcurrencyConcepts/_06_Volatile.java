import java.util.concurrent.Executors;


public class _06_Volatile {

    // We need loopBreaker to be visible to both looper and loopBreakerSetter thread
    // 1. volatile variables can't be put inside functions.
    // 2. if a volatile variable is passed to a function, it no longer remains volatile. To pass a volatile variable
    //    pass its owning class object i.e. pass by reference.
    private static volatile boolean loopBreaker = false;
    private static int counter = 0;

    public static void main(String[] args) {

        Runnable looper = () -> {
            while (!loopBreaker) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("Value of counter: %d\n", counter++);
            }
        };

        Runnable loopBreakerSetter = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            loopBreaker = true;
        };

        try (var executorService = Executors.newFixedThreadPool(2)) {
            executorService.submit(looper);
            executorService.submit(loopBreakerSetter);
        }
    }
}
