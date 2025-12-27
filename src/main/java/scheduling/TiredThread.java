package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {

    private static final Runnable POISON_PILL = () -> {}; // Special task to signal shutdown

    private final int id; // Worker index assigned by the executor
    private final double fatigueFactor; // Multiplier for fatigue calculation

    private final AtomicBoolean alive = new AtomicBoolean(true); // Indicates if the worker should keep running

    // Single-slot handoff queue; executor will put tasks here
    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false); // Indicates if the worker is currently executing a task

    private final AtomicLong timeUsed = new AtomicLong(0); // Total time spent executing tasks
    private final AtomicLong timeIdle = new AtomicLong(0); // Total time spent idle
    private final AtomicLong idleStartTime = new AtomicLong(0); // Timestamp when the worker became idle

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    /**
     * Assign a task to this worker.
     * This method is non-blocking: if the worker is not ready to accept a task,
     * it throws IllegalStateException.
     */
    public void newTask(Runnable task) {
       // TODO
        // Checks if worker is ready to accept a task, throws exception otherwise.
        if (task == null || !this.alive.get()) {
            throw new IllegalStateException("Error: worker is not ready to accept as task");
        }

        // Adds worker to the handoff blocking queue
        this.handoff.add(task);
    }

    /**
     * Request this worker to stop after finishing current task.
     * Inserts a poison pill so the worker wakes up and exits.
     */
    public void shutdown() {
       // TODO
        // Sets alive and busy to false
        this.alive.set(false);
        this.busy.set(false);

        // Hands off poison pill unless interrupted
        try {
            this.handoff.put(POISON_PILL);
        } catch(InterruptedException e) {
            this.interrupt();
        }
    }

    @Override
    public void run() {
       // TODO
        while(this.alive.get()) {
            try {
                Runnable task = handoff.take();

                // Kills thread is task is poison pill
                if (task == POISON_PILL) {
                    this.alive.set(false);
                    this.busy.set(false);
                    break;
                }

                // Before starting task, calculates idle time
                long finish_idle = System.nanoTime();
                this.timeIdle.addAndGet(finish_idle - idleStartTime.get());

                // Starts the task and measures execution time
                long start_time = System.nanoTime();

                this.busy.set(true);
                task.run();
                this.busy.set(false);
                long finish_time = System.nanoTime();
                this.timeUsed.addAndGet(finish_time - start_time);
                this.idleStartTime.set(finish_time);

            } catch(InterruptedException e) {
                this.alive.set(false);
            }
        }
    }

    @Override
    public int compareTo(TiredThread o) {
        // TODO
        // Compares the fatigue of both thread
        // Returns 0 if the fatigue is equal
        // Returns positive if this is more fatigued than other
        // Returns negative if other is more fatigued than this
        return Double.compare(this.getFatigue(), o.getFatigue());
    }
}