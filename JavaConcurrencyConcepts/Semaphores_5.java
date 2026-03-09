import java.util.concurrent.Semaphore;

/*
    Semaphores can control how many threads can access a resource concurrently.
    In locks only one thread can access a resource at one time.
*/
public class Semaphores_5 {
    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(5);
        System.out.printf("How many more threads can acquire lock? %s\n", semaphore.availablePermits());
        semaphore.acquire();
        System.out.printf("How many more threads can acquire lock? %s\n", semaphore.availablePermits());
        semaphore.release();
        System.out.printf("How many more threads can acquire lock? %s\n", semaphore.availablePermits());

        /*
            acquire() is a blocking call. The thread is blocked until it acquires the required number of permits.

            The acquire and release are not related to any particular thread. ThreadA might call acquire and
            ThreadB might call release. Acquire and Release are tied to the Semaphore object.
            So, be careful when acquiring and releasing semaphores.
        */

        // program hangs: we are waiting to acquire the permit which never happen.
        semaphore.acquire(10);


        // InterruptedException happens when a thread is trying to acquire a semaphore and another thread
        // comes in and interrupts the other thread.

    }
}