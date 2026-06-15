package com.example.bot.wasp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.config.WaspConfig;
import com.example.util.RobotActions;

/** Owns the lifecycle of the Wasp routine's scheduled tasks. */
public class WaspController {

    // One worker thread runs every task in submission order, so the mouse is
    // never driven by two tasks at once (no cursor fighting) and key/mouse
    // actions stay cleanly serialized.
    private ScheduledExecutorService executor;

    public void start(WaspConfig config) {
        if (executor != null && !executor.isShutdown()) {
            return;
        }
        System.out.println("WASP START za...");
        for (int i = 3; i > 0; i--) {
            System.out.println(i + "...");
            RobotActions.sleep(1000);
        }
        System.out.println("WASP GO!");

        executor = Executors.newSingleThreadScheduledExecutor();

        long foodPeriod = (long) ((13 + Math.random()) * 60 * 1000);
        // One state-machine step per tick, so this delay IS the gap between
        // attack/climb actions. ~600ms: responsive but not frantic.
        long pixelDelay = (long) (500 + Math.random() * 250);
        long lootDelay = (long) ((0.4 + Math.random()) * 5 * 1000);
        long healDelay = (long) ((0.9 + Math.random()) * 5 * 1000);

        // Pixel tracker submitted FIRST so it acts immediately at startup, before
        // WaspFoodTask blocks the thread eating.
        // scheduleWithFixedDelay avoids overlapping executions if a task runs long.
        executor.scheduleWithFixedDelay(guard(new WaspPixelTrackerTask(config)), 0, pixelDelay, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(guard(new WaspFoodTask(config)), 0, foodPeriod, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(guard(new WaspMessageLootTracker(config)), 0, lootDelay, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(guard(new WaspHealTask(config)), 0, healDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Wraps a task so a thrown exception is logged instead of silently
     * cancelling the schedule (a ScheduledExecutorService kills any task that
     * throws, stopping all future runs without a message).
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
