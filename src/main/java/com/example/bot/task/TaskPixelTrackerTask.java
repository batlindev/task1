package com.example.bot.task;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;

import com.example.config.PatrolStep;
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

    // Position in config.steps; cycles linearly 0..N-1..0. The route (and any
    // ping-pong) is whatever the step list encodes.
    private int seqPos = 0;

    // Set once we've clicked toward the current step's mark. For yellow waypoint
    // steps, the marker vanishes (replaced by the player cross) the moment we
    // stand on it — so "was walking, now gone" means we arrived.
    private boolean clickedTowardCurrent = false;

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
        if (config.steps == null || config.steps.isEmpty()) {
            return;
        }
        PatrolStep current = config.steps.get(seqPos);
        int pointNumber = seqPos + 1;

        System.out.printf("%s [DIAG] tick: seqPos=%d/%d typ=%s flow=%s kolor=(%d,%d,%d)%n",
                LocalTime.now(), seqPos, config.steps.size(), current.type(), step,
                current.color().getRed(), current.color().getGreen(), current.color().getBlue());

        switch (step) {
            case WALK:
                walkToward(pointNumber, current);
                break;
            case ATTACK:
                attackIfNeeded(pointNumber);
                break;
        }
    }

    /** Advance to the next step in the loop, wrapping around. */
    private void advance() {
        seqPos = (seqPos + 1) % config.steps.size();
        step = TaskFlowStep.WALK;
        clickedTowardCurrent = false;
    }

    private void walkToward(int pointNumber, PatrolStep current) {
        Color target = current.color();
        // Yellow rope/ladder markers: head for the one nearest center (exact
        // match). Color marks: centroid with the configured tolerance.
        Point mark = current.isWaypoint() ? scanner.findNearestExact(target) : scanner.findColor(target);
        Point center = scanner.center();

        if (mark == null) {
            // Yellow marker we were walking toward disappeared = we stepped onto
            // it (the player cross now covers it). Treat as arrival.
            if (current.isWaypoint() && clickedTowardCurrent) {
                System.out.printf("%s TASK [punkt %d] marker zniknal → DOTARLEM (stoje na nim)%n",
                        LocalTime.now(), pointNumber);
                onArrived(pointNumber, current);
                return;
            }
            System.out.printf("%s TASK [punkt %d] znacznik (%d,%d,%d) POZA ZASIEGIEM minimapy → czekam%n",
                    LocalTime.now(), pointNumber,
                    target.getRed(), target.getGreen(), target.getBlue());
            return;
        }

        int dist = TaskMapScanner.distance(mark, center);
        System.out.printf("%s TASK [punkt %d] znacznik ekran=(%d,%d) srodek=(%d,%d) dist=%d%n",
                LocalTime.now(), pointNumber, mark.x, mark.y, center.x, center.y, dist);

        if (dist <= config.arriveThreshold) {
            onArrived(pointNumber, current);
        } else {
            System.out.printf("  [punkt %d] klik (%d,%d) → auto-chodzenie%n", pointNumber, mark.x, mark.y);
            RobotActions.clickMouse(robot, mark.x, mark.y);
            clickedTowardCurrent = true;
        }
    }

    /** Reached the target tile: dispatch the step's action, then advance (or
     *  hand off to the ATTACK phase for RUN_ATTACK). */
    private void onArrived(int pointNumber, PatrolStep current) {
        switch (current.type()) {
            case RUN_ATTACK:
                System.out.printf("  [punkt %d] DOTARLEM → atakuje%n", pointNumber);
                step = TaskFlowStep.ATTACK;
                break;
            case RUN:
                System.out.printf("  [punkt %d] DOTARLEM → BIEG (bez ataku) → dalej%n", pointNumber);
                advance();
                break;
            case ROPE_DOWN:
                // Standing on the marker already triggers rope-down; nothing more.
                System.out.printf("  [punkt %d] DOTARLEM → ROPE DOWN%n", pointNumber);
                advance();
                break;
            case LADDER_UP:
                System.out.printf("  [punkt %d] DOTARLEM → LADDER UP (PPM na loot 5)%n", pointNumber);
                rightClickTile5();
                advance();
                break;
            case ROPE_UP:
                System.out.printf("  [punkt %d] DOTARLEM → ROPE UP (V + LPM na loot 5)%n", pointNumber);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                RobotActions.sleep(LOOT_TILE_MS);
                leftClickTile5();
                advance();
                break;
        }
    }

    /** Loot-tile 5 (panel 11) screen coords, or {@code null} if not set. */
    private int[] tile5() {
        if (config.lootTiles != null && config.lootTiles.length >= 5) {
            return config.lootTiles[4];
        }
        return null;
    }

    private void rightClickTile5() {
        int[] t = tile5();
        if (t == null) {
            return;
        }
        robot.mouseMove(t[0], t[1]);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    private void leftClickTile5() {
        int[] t = tile5();
        if (t == null) {
            return;
        }
        robot.mouseMove(t[0], t[1]);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private void attackIfNeeded(int pointNumber) {
        robot.mouseMove(config.targetX, config.targetY);

        Color current = robot.getPixelColor(config.targetX, config.targetY);
        Color robak = robot.getPixelColor(config.robakX, config.robakY);

        System.out.printf("%s [DIAG] ATTACK [punkt %d] target(%d,%d)=(%d,%d,%d) bialy?=%b | robak(%d,%d)=(%d,%d,%d) oczek=(%d,%d,%d) match?=%b%n",
                LocalTime.now(), pointNumber,
                config.targetX, config.targetY, current.getRed(), current.getGreen(), current.getBlue(),
                current.equals(Color.WHITE),
                config.robakX, config.robakY, robak.getRed(), robak.getGreen(), robak.getBlue(),
                config.robakColor.getRed(), config.robakColor.getGreen(), config.robakColor.getBlue(),
                robak.equals(config.robakColor));

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
                System.out.printf("%s TASK [punkt %d] telegram: task atak%n", LocalTime.now(), pointNumber);
                TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task atak");
            }
        } else if (robak.equals(config.robakColor)) {
            int nextPointNumber = ((seqPos + 1) % config.steps.size()) + 1;
            System.out.printf("%s TASK [punkt %d] brak potwora → loot, potem ide do punktu %d%n",
                    LocalTime.now(), pointNumber, nextPointNumber);
            // Point cleared: the last kill's loot pops a beat later, so wait,
            // grab it, then settle before walking off. The single worker thread
            // blocks here so the player cannot leave mid-loot.
            if (config.lootEnabled) {
                RobotActions.sleep(LOOT_APPEAR_MS);
                grabLootIfPresent(pointNumber);
                RobotActions.sleep(LOOT_SETTLE_MS);
            }
            advance();
        } else {
            System.out.printf("%s [DIAG] ATTACK [punkt %d] CZEKAM (nie bialy i robak!=robakColor) → utknie tu az robak=robakColor%n",
                    LocalTime.now(), pointNumber);
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
        if (config.telegramOnLoot) {
            TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task paw");
        }
    }
}
