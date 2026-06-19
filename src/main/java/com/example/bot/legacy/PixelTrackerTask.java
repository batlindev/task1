package com.example.bot.legacy;
// package com.example.bot;

// import java.awt.Color;
// import java.awt.event.KeyEvent;
// import java.time.LocalTime;

// import com.example.config.BotSettings;
// import com.example.util.RobotActions;
// import com.example.util.RobotTask;

// /** Scans the target pixel and presses attack ("space") when a monster is there. */
// public class PixelTrackerTask extends RobotTask {

//     private final BotSettings settings;

//     public PixelTrackerTask(BotSettings settings) {
//         this.settings = settings;
//     }

//     @Override
//     public void run() {
//         robot.mouseMove(settings.targetX, settings.targetY);
//         RobotActions.sleep(1000);

//         Color currentColor = robot.getPixelColor(settings.targetX, settings.targetY);
//         System.out.println(LocalTime.now() + " SCAN " + currentColor);

//         if (currentColor.equals(BotSettings.TARGET_COLOR)) {
//             robot.keyPress(KeyEvent.VK_SPACE);
//             System.out.println(LocalTime.now() + " ATTACK");
//             robot.keyRelease(KeyEvent.VK_SPACE);
//             RobotActions.sleep(1000);
//         }
//     }
// }
