package com.example.bot.legacy;
// package com.example.bot;

// import java.awt.Color;
// import java.time.LocalTime;

// import com.example.config.BotSettings;
// import com.example.util.RobotActions;
// import com.example.util.RobotTask;
// import com.example.util.TelegramClient;

// /**
//  * Watches the "alert" pixel; when it matches the configured color, fires a few
//  * Telegram notifications and then asks the caller to stop the bot.
//  */
// public class TelegramAutoClickTask extends RobotTask {

//     private static final int NOTIFY_COUNT = 5;

//     private final BotSettings settings;
//     private final Runnable onMatch;

//     public TelegramAutoClickTask(BotSettings settings, Runnable onMatch) {
//         this.settings = settings;
//         this.onMatch = onMatch;
//     }

//     @Override
//     public void run() {
//         Color pixelColor = robot.getPixelColor(settings.telegramCheckX, settings.telegramCheckY);
//         System.out.println(LocalTime.now() + " AUTO CHECK at ("
//                 + settings.telegramCheckX + "," + settings.telegramCheckY + "): " + pixelColor);

//         if (pixelColor.equals(settings.telegramColor)) {
//             System.out.println(LocalTime.now() + " COLOR MATCH - notifying " + NOTIFY_COUNT + " times");
//             for (int i = 0; i < NOTIFY_COUNT; i++) {
//                 TelegramClient.sendMessage(settings.telegramToken, settings.telegramChatId, "wiadomosc");
//                 RobotActions.sleep(1000);
//             }
//             System.out.println(LocalTime.now() + " Stopping program.");
//             onMatch.run();
//         }
//     }
// }
