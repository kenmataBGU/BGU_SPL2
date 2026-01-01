package scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TiredExecutorTest {
    private TiredExecutor executor;

    @AfterEach
    void finish() throws InterruptedException {
        if (executor != null){ executor.shutdown();}
    }

    @Test
    void testTaskSubmission() {
        executor = new TiredExecutor(5);
        int number_of_tasks = 10;

        AtomicInteger count = new AtomicInteger(0);
        List<Runnable> task_list = new ArrayList<>();
        for (int i = 0; i < number_of_tasks; i++) {
            task_list.add(() -> count.incrementAndGet());
        }

        executor.submitAll(task_list);
        assertEquals(number_of_tasks, count.get());
    }

    @Test
    void testGetWorkerReport() {

        // Creates a short task and submits it
        executor = new TiredExecutor(5);
        List<Runnable> task_list = new ArrayList<>();
        task_list.add(() -> {});
        executor.submitAll(task_list);

        // Checks whether output is correct
        String output = executor.getWorkerReport();
        assertNotNull(output);
        assertTrue(output.contains("Report"));
        assertTrue(output.contains("Worker"));
    }
}