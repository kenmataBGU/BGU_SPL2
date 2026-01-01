package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredThreadTest {

    private TiredThread worker;
    // worker = new TiredThread(1, 1.0);
    // worker.start();

    @AfterEach
    void finish() {
        if (worker != null && worker.isAlive()) {
            worker.shutdown();
        }
        try {
            worker.join(1000);
        } catch (InterruptedException e) {
        }
    }

    @Test
    void testTasks() throws InterruptedException {
        // Initiate lock and flag
        AtomicBoolean flag = new AtomicBoolean(false);
        Object lock = new Object();

        // Initiate new worker
        worker = new TiredThread(1, 1.0);
        worker.start();

        // Test task
        worker.newTask(() -> {
            synchronized(lock) {
                flag.set(true);
                lock.notifyAll();
            }
        });

        synchronized(lock) {
            while(!flag.get()) {
                lock.wait(1000);
            }
        }

        assertTrue(flag.get());
    }

    @Test
    void testTaskStats() throws InterruptedException {
        // Initiate lock and flag
        AtomicBoolean flag = new AtomicBoolean(false);
        Object lock = new Object();

        // Initiate new worker
        worker = new TiredThread(1, 1.0);
        worker.start();

        // Run new task
        worker.newTask(() -> {
            try {
                Thread.sleep(50);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

            } finally {
                synchronized (lock) {
                    flag.set(true);
                    lock.notifyAll();
                }
            }
        });

        synchronized (lock) {
            while (!flag.get()) {
                lock.wait(1000);
            }
        }
        Thread.sleep(50);

        assertTrue(worker.getFatigue() > 0);
        assertTrue(worker.getTimeUsed() >= 50 * 1000000);
    }
}