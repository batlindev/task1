package com.example.bot.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.config.TaskConfig;
import com.example.util.RobotActions;

/** Owns the lifecycle of the Task patrol's scheduled task. */
public final class TaskController {

    // Single worker thread so the mouse is never driven by two ticks at once.
    private ScheduledExecutorService executor;

    public void start(TaskConfig config) {
        if (executor != null && !executor.isShutdown()) {
            return;
        }
        System.out.println("TASK START za...");
        for (int i = 3; i > 0; i--) {
            System.out.println(i + "...");
            RobotActions.sleep(1000);
        }
        System.out.println("TASK GO!");

        executor = Executors.newSingleThreadScheduledExecutor();

        // One state-machine step per tick; this delay is the gap between
        // scan/click actions. ~600ms: responsive but not frantic.
        long tickDelay = (long) (500 + Math.random() * 250);
        TaskPixelTrackerTask tracker = new TaskPixelTrackerTask(config);
        executor.scheduleWithFixedDelay(guard(tracker), 0, tickDelay, TimeUnit.MILLISECONDS);

        // Loot tracker shares the same single thread (serialized with the tracker)
        // and only scans while we are on a point — once a second.
        executor.scheduleWithFixedDelay(guard(new TaskMessageLootTracker(config, tracker)), 0, 1000,
                TimeUnit.MILLISECONDS);

        // Heal check: drink potion when HP pixel differs from "full" color (~4.5s interval).
        long healDelay = (long) (900 + Math.random() * 500) * 5;
        executor.scheduleWithFixedDelay(guard(new TaskHealTask(config)), 0, healDelay,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Wraps a task so a thrown exception is logged instead of silently
     * cancelling the schedule.
     */
    private static Runnable guard(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}
