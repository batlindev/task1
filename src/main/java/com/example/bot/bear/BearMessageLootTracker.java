package com.example.bot.bear;

import java.awt.Color;
import java.awt.event.InputEvent;

import com.example.config.BearConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;
import com.example.util.TelegramClient;

/**
 * Analogous to {@code WaspMessageLootTracker}: when the loot-message pixel lights
 * up it right-clicks every loot tile (if any) and notifies Telegram.
 *
 * Difference from Wasp: it only scans while on a point or briefly after a kill
 * (see {@link BearPixelTrackerTask#shouldScanLoot()}) and the Telegram text is
 * "bear paw".
 */
public class BearMessageLootTracker extends RobotTask {

    private final BearConfig config;
    private final BearPixelTrackerTask tracker;

    public BearMessageLootTracker(BearConfig config, BearPixelTrackerTask tracker) {
        this.config = config;
        this.tracker = tracker;
    }

    @Override
    public void run() {
        if (!tracker.shouldScanLoot()) {
            return; // skanujemy loota na punkcie oraz krotko po zabiciu (karencja)
        }

        Color current = robot.getPixelColor(config.lootX, config.lootY);
        System.out.println("BEAR loot " + current);

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
            System.out.println("BEAR zbieram loota");
            TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "bear paw");
        }
    }
}
