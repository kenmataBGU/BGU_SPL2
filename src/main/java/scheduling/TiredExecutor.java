package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        // Initiates workers array
        this.workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++){
            double fatigue_factor = 0.5 + Math.random();
            this.workers[i] = new TiredThread(i, fatigue_factor);

            // Starts the workers
            this.idleMinHeap.add(this.workers[i]);
            this.workers[i].start();
        }
    }

    public void submit(Runnable task) {
        // TODO
        try {
            inFlight.incrementAndGet();
            TiredThread worker = idleMinHeap.take();

            // Creates a task wrapper
            Runnable task_wrapper = () -> {
                try {
                    task.run();
                } finally {
                    idleMinHeap.add(worker);
                    inFlight.decrementAndGet();
                }
            };
            worker.newTask(task_wrapper);
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish

        // Submits all tasks
        for (Runnable task: tasks) {
            submit(task);
        }

        // Wait for all tasks to finish
        while (inFlight.get() > 0) {
            Thread.currentThread().yield();
        }
    }

    public void shutdown() throws InterruptedException {
        // TODO
        // Goes through all workers and shuts them down
        for (TiredThread worker : workers) {
            worker.shutdown();
        }
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        String report = "Report :\n";

        for (TiredThread worker : this.workers){
            report += "Worker id: " + worker.getWorkerId() + "\n";
            report += "Fatigue: " + worker.getFatigue() + "\n";
            report += "Time used: " + worker.getTimeUsed() + "\n";
            report += "Time Idle" + worker.getTimeIdle() + "\n";
        }
        return report;
    }
}
