package com.example.bot.wasp;

import java.awt.Color;
import java.awt.event.InputEvent;

import com.example.config.WaspConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;
import com.example.util.TelegramClient;

/** When the loot-message pixel lights up, right-clicks every loot tile and notifies Telegram. */
public class WaspMessageLootTracker extends RobotTask {

    private final WaspConfig config;

    public WaspMessageLootTracker(WaspConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        Color current = robot.getPixelColor(config.lootX, config.lootY);
        System.out.println("WASP loot " + current);

        if (current.equals(config.lootColor)) {
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
            System.out.println("WASP zbieram loota");
            TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "honeycomb");
        }
    }
}
