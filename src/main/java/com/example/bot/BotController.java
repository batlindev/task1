package com.example.bot;

import java.util.Timer;

import com.example.config.BotSettings;
import com.example.util.RobotActions;

/**
 * Owns the lifecycle of the main bot's scheduled tasks. Replaces the static
 * {@code Timer} / start / stop methods that used to sit on {@code Main}.
 */
public class BotController {

    private volatile Timer timer;

    /**
     * Starts the bot after the original 5-second delay. The delay (and the
     * scheduling) runs off the UI thread so the window no longer freezes.
     *
     * @param onAlert invoked when the Telegram watcher sees its color match
     */
    public void start(BotSettings settings, Runnable onAlert) {
        if (timer != null) {
            return;
        }
        new Thread(() -> {
            RobotActions.sleep(5000); // DELAY START

            Timer t = new Timer();
            t.scheduleAtFixedRate(new PressXTask(settings.food), 0, (long) ((13 + Math.random()) * 60 * 1000));
            t.scheduleAtFixedRate(new PixelTrackerTask(settings), 0, (long) ((0.9 + Math.random()) * 3 * 1000));
            t.scheduleAtFixedRate(new HealTask(settings), 0, (long) ((0.9 + Math.random()) * 5 * 1000));
            t.scheduleAtFixedRate(new TelegramAutoClickTask(settings, onAlert), 0, 10_000);
            timer = t;
        }, "bot-start").start();
    }

    public void stop() {
        Timer t = timer;
        if (t != null) {
            t.cancel();
            timer = null;
        }
    }
}
