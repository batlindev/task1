package com.example.bot.task;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;

import com.example.config.TaskConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;
import com.example.util.TelegramClient;

/**
 * Drives the Task patrol as a small state machine, one step per scheduled run.
 *
 * Each target point is a uniquely-colored minimap mark. The player is always at
 * the center of the minimap, so we scan for the current target's color, click
 * where it sits (the game auto-walks there), and treat "mark reached center" as
 * arrival. Then we advance to the next point in the ping-pong sequence.
 *
 * Loot is collected synchronously at the kill moment (see {@link #collectLoot}):
 * when the point is cleared we stand still, wait for the loot message to pop,
 * grab it, then move on — so the player never walks off before looting.
 */
public final class TaskPixelTrackerTask extends RobotTask {

    private final TaskConfig config;
    private final TaskMapScanner scanner;

    // Ping-pong patrol: indices into config.points giving 1,2,3,2,1,2,3,2,...
    // (0-based: 0,1,2,1 repeating). Edit here to change the route.
    private static final int[] SEQUENCE = { 0, 1, 2, 1 };
    private int seqPos = 0;

    // Loot timing at the kill moment: the loot message appears a beat AFTER the
    // monster dies, so wait before scanning; then settle before walking off.
    private static final long LOOT_APPEAR_MS = 400;
    private static final long LOOT_SETTLE_MS = 500;
    // Pause after each loot-tile right-click. Same for every tile.
    private static final long LOOT_TILE_MS = 200;

    private enum TaskFlowStep {
        WALK,
        ATTACK
    }

    private volatile TaskFlowStep step = TaskFlowStep.WALK;

    public TaskPixelTrackerTask(TaskConfig config) {
        this.config = config;
        this.scanner = new TaskMapScanner(robot, config.mapX, config.mapY, config.mapW, config.mapH,
                config.colorTolerance);
    }

    @Override
    public void run() {
        int pointIndex = SEQUENCE[seqPos];
        int pointNumber = pointIndex + 1;
        Color target = config.points[pointIndex];

        switch (step) {
            case WALK:
                walkToward(pointNumber, target);
                break;
            case ATTACK:
                attackIfNeeded(pointNumber);
                break;
        }
    }

    private void walkToward(int pointNumber, Color target) {
        Point mark = scanner.findColor(target);
        Point center = scanner.center();

        if (mark == null) {
            System.out.printf("%s TASK [punkt %d] znacznik (%d,%d,%d) POZA ZASIEGIEM minimapy → czekam%n",
                    LocalTime.now(), pointNumber,
                    target.getRed(), target.getGreen(), target.getBlue());
            return;
        }

        int dist = TaskMapScanner.distance(mark, center);
        System.out.printf("%s TASK [punkt %d] znacznik ekran=(%d,%d) srodek=(%d,%d) dist=%d%n",
                LocalTime.now(), pointNumber, mark.x, mark.y, center.x, center.y, dist);

        if (dist <= config.arriveThreshold) {
            System.out.printf("  [punkt %d] DOTARLEM (dist<=%d) → atakuje%n", pointNumber, config.arriveThreshold);
            step = TaskFlowStep.ATTACK;
        } else {
            System.out.printf("  [punkt %d] klik (%d,%d) → auto-chodzenie%n", pointNumber, mark.x, mark.y);
            RobotActions.clickMouse(robot, mark.x, mark.y);
        }
    }

    private void attackIfNeeded(int pointNumber) {
        robot.mouseMove(config.targetX, config.targetY);

        Color current = robot.getPixelColor(config.targetX, config.targetY);
        Color robak = robot.getPixelColor(config.robakX, config.robakY);

        if (current.equals(Color.WHITE)) {
            // Multi-monster: a corpse from a previous kill may already show loot
            // (gold). Grab it BEFORE swinging at the next monster, otherwise we
            // run off toward the next body and leave this loot behind.
            if (config.lootEnabled) {
                grabLootIfPresent(pointNumber);
            }
            System.out.printf("%s TASK [punkt %d] pixel BIALY → SPACJA%n", LocalTime.now(), pointNumber);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.keyRelease(KeyEvent.VK_SPACE);
            if (config.telegramOnAttack) {
                TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task atak");
            }
        } else if (robak.equals(config.robakColor)) {
            int nextSeqPos = (seqPos + 1) % SEQUENCE.length;
            System.out.printf("%s TASK [punkt %d] brak potwora → loot, potem ide do punktu %d%n",
                    LocalTime.now(), pointNumber, SEQUENCE[nextSeqPos] + 1);
            // Point cleared: the last kill's loot pops a beat later, so wait,
            // grab it, then settle before walking off. The single worker thread
            // blocks here so the player cannot leave mid-loot.
            if (config.lootEnabled) {
                RobotActions.sleep(LOOT_APPEAR_MS);
                grabLootIfPresent(pointNumber);
                RobotActions.sleep(LOOT_SETTLE_MS);
            }
            seqPos = nextSeqPos;
            step = TaskFlowStep.WALK;
        }
    }

    /**
     * If the loot pixel reads {@code lootColor} (gold), right-click every loot
     * tile to collect and ping Telegram. Returns immediately when no loot is
     * shown, so it is cheap to call before every attack.
     */
    private void grabLootIfPresent(int pointNumber) {
        Color loot = robot.getPixelColor(config.lootX, config.lootY);
        if (!loot.equals(config.lootColor)) {
            return;
        }
        if (config.lootTiles != null) {
            for (int i = 0; i < config.lootTiles.length; i++) {
                int[] tile = config.lootTiles[i];
                if (tile == null) {
                    continue;
                }
                robot.mouseMove(tile[0], tile[1]);
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                RobotActions.sleep(LOOT_TILE_MS);
            }
        }
        System.out.printf("%s TASK [punkt %d] zbieram loota%n", LocalTime.now(), pointNumber);
        TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task paw");
    }
}
