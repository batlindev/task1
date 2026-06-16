package com.example.bot.task;

import java.awt.Color;
import java.awt.event.InputEvent;

import com.example.config.TaskConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;
import com.example.util.TelegramClient;

/**
 * Analogous to {@code WaspMessageLootTracker}: when the loot-message pixel lights
 * up it right-clicks every loot tile (if any) and notifies Telegram.
 *
 * Difference from Wasp: it only scans while on a point or briefly after a kill
 * (see {@link TaskPixelTrackerTask#shouldScanLoot()}) and the Telegram text is
 * "task paw".
 */
public class TaskMessageLootTracker extends RobotTask {

    private final TaskConfig config;
    private final TaskPixelTrackerTask tracker;

    public TaskMessageLootTracker(TaskConfig config, TaskPixelTrackerTask tracker) {
        this.config = config;
        this.tracker = tracker;
    }

    @Override
    public void run() {
        if (!tracker.shouldScanLoot()) {
            return; // skanujemy loota na punkcie oraz krotko po zabiciu (karencja)
        }

        Color current = robot.getPixelColor(config.lootX, config.lootY);
        System.out.println("TASK loot " + current);

        if (current.equals(config.lootColor)) {
            if (config.lootTiles != null) {
                for (int i = 0; i < config.lootTiles.length; i++) {
                    int[] tile = config.lootTiles[i];
                    if (tile == null) {
                        continue;
                    }
                    robot.mouseMove(tile[0], tile[1]);
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                    RobotActions.sleep(i < 2 ? 400 : 100);
                }
            }
            System.out.println("TASK zbieram loota");
            TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task paw");
        }
    }
}
