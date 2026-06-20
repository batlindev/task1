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

    // Last distance (minimap px) at which we still SAW the current waypoint's
    // marker. Guards the "vanished = arrived" rule: a marker only counts as
    // stepped-on if it was already close when it disappeared. A marker that
    // vanishes while still far away left the minimap range — that is NOT arrival.
    private int lastWaypointDist = Integer.MAX_VALUE;

    // "Cross is on the marker tile" distance (minimap px). The white cross is
    // always at minimap center; a yellow marker sitting within this many px of
    // center means we stand ON its tile (1px is just render rounding). Kept TIGHT
    // because one tile off reads ~4px here (a tile is only a few px on the zoomed
    // minimap), and firing a waypoint action one tile off misclicks. The yellow
    // pixel does NOT reliably vanish when stood on, so distance — not vanish — is
    // the arrival signal. Tune if a different minimap zoom changes the px/tile.
    private static final int WAYPOINT_ARRIVE_PX = 2;

    // Slack over WAYPOINT_ARRIVE_PX for the secondary "marker vanished = arrived"
    // check (true teleports / markers leaving view). The last visible tick can
    // read a hair past the threshold before the marker goes; out-of-range vanish
    // (dist ~ mapW/2) is far above this, so the two cases stay cleanly separated.
    private static final int VANISH_ARRIVE_SLACK = 8;

    // Loot timing at the kill moment: the loot message appears a beat AFTER the
    // monster dies, so wait before scanning; then settle before walking off.
    private static final long LOOT_APPEAR_MS = 400;
    private static final long LOOT_SETTLE_MS = 500;
    // Pause after each loot-tile right-click. Same for every tile.
    private static final long LOOT_TILE_MS = 200;

    // Human-paced timing for the click/key actions of LADDER_UP and ROPE_UP. The
    // whole sequence is spread out (settle after arrival, hold keys/buttons, settle
    // after each mouse move) so it does not fire instantly like a macro (~0.7-1s).
    private static final long ACTION_SETTLE_MS = 250;       // settle after arrival before pressing anything
    private static final long ACTION_MOVE_SETTLE_MS = 90;   // after a mouseMove, before clicking
    private static final long ACTION_HOLD_MS = 70;          // between key/button down & up transitions
    // Gap after the action that triggers a prompt (ladder use-dialog / rope cursor)
    // before the follow-up click lands.
    private static final long ACTION_DIALOG_MS = 200;

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

        System.out.printf("%s [DIAG] tick: seqPos=%d/%d type=%s flow=%s color=(%d,%d,%d)%n",
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
        lastWaypointDist = Integer.MAX_VALUE;
    }

    private void walkToward(int pointNumber, PatrolStep current) {
        // Attack-in-place: no approach, jump straight to the ATTACK phase and
        // swing where we already stand.
        if (current.type() == PatrolStep.Type.ATTACK_ONLY) {
            System.out.printf("  [point %d] ATTACK in place (without walking up) → attacking%n", pointNumber);
            step = TaskFlowStep.ATTACK;
            return;
        }

        Color target = current.color();
        // STAIRS: head for the RIGHT end of the yellow stair mark. Other yellow
        // rope/ladder markers: the one nearest center (exact match). Color marks:
        // centroid with the configured tolerance.
        Point mark;
        if (current.type() == PatrolStep.Type.STAIRS) {
            mark = scanner.findRightmostExact(target);
        } else if (current.isWaypoint()) {
            mark = scanner.findNearestExact(target);
        } else {
            mark = scanner.findColor(target);
        }
        Point center = scanner.center();

        if (mark == null) {
            // Yellow marker we were walking toward disappeared. This means we
            // stepped onto it (the player cross now covers it) ONLY if it was
            // already close last time we saw it. A marker that vanishes while
            // still far away simply scrolled off the minimap — not arrival.
            if (current.isWaypoint() && clickedTowardCurrent
                    && lastWaypointDist <= WAYPOINT_ARRIVE_PX + VANISH_ARRIVE_SLACK) {
                System.out.printf("%s TASK [point %d] marker vanished (last dist=%d) → ARRIVED (standing on it)%n",
                        LocalTime.now(), pointNumber, lastWaypointDist);
                onArrived(pointNumber, current);
                return;
            }
            if (current.isWaypoint() && clickedTowardCurrent) {
                System.out.printf("%s TASK [point %d] marker vanished but last dist=%d too large → OUT OF RANGE, waiting%n",
                        LocalTime.now(), pointNumber, lastWaypointDist);
                return;
            }
            System.out.printf("%s TASK [point %d] marker (%d,%d,%d) OUT OF minimap RANGE → waiting%n",
                    LocalTime.now(), pointNumber,
                    target.getRed(), target.getGreen(), target.getBlue());
            return;
        }

        int dist = TaskMapScanner.distance(mark, center);
        if (current.isWaypoint()) {
            lastWaypointDist = dist;
        }
        System.out.printf("%s TASK [point %d] marker screen=(%d,%d) center=(%d,%d) dist=%d%n",
                LocalTime.now(), pointNumber, mark.x, mark.y, center.x, center.y, dist);

        if (current.isWaypoint()) {
            // Waypoint actions (rope/ladder/drop) fire when the white cross sits ON
            // the marker tile = the marker is within WAYPOINT_ARRIVE_PX of center.
            // The yellow pixel does NOT reliably vanish when stood on (it can sit
            // 1px off center forever), so distance — not vanish — is the signal.
            // The threshold is tight so one tile off (~4px) keeps walking instead
            // of firing the action a tile away and desyncing the bot.
            if (dist <= WAYPOINT_ARRIVE_PX) {
                System.out.printf("  [point %d] cross on yellow (dist=%d ≤ %d) → ARRIVED%n",
                        pointNumber, dist, WAYPOINT_ARRIVE_PX);
                onArrived(pointNumber, current);
            } else {
                System.out.printf("  [point %d] yellow visible (dist=%d) → walking up to center%n",
                        pointNumber, dist);
                RobotActions.clickMouse(robot, mark.x, mark.y);
                clickedTowardCurrent = true;
            }
            return;
        }

        if (dist <= config.arriveThreshold) {
            onArrived(pointNumber, current);
        } else {
            System.out.printf("  [point %d] click (%d,%d) → auto-walk%n", pointNumber, mark.x, mark.y);
            RobotActions.clickMouse(robot, mark.x, mark.y);
            clickedTowardCurrent = true;
        }
    }

    /** Reached the target tile: dispatch the step's action, then advance (or
     *  hand off to the ATTACK phase for RUN_ATTACK). */
    private void onArrived(int pointNumber, PatrolStep current) {
        switch (current.type()) {
            case RUN_ATTACK:
                System.out.printf("  [point %d] ARRIVED → attacking%n", pointNumber);
                step = TaskFlowStep.ATTACK;
                break;
            case RUN:
                System.out.printf("  [point %d] ARRIVED → RUN (without attack) → next%n", pointNumber);
                advance();
                break;
            case ROPE_DOWN:
                // Standing on the marker already triggers rope-down; nothing more.
                System.out.printf("  [point %d] ARRIVED → ROPE DOWN%n", pointNumber);
                advance();
                break;
            case LADDER_UP:
                System.out.printf("  [point %d] ARRIVED → LADDER UP (Ctrl+LMB dialog, then point 14)%n", pointNumber);
                ladderUp();
                advance();
                break;
            case ROPE_UP:
                System.out.printf("  [point %d] ARRIVED → ROPE UP (V + LMB on loot 5)%n", pointNumber);
                ropeUp();
                advance();
                break;
            case STAIRS:
                // Walked onto the right end of the yellow stair mark; stepping there
                // takes us up. Nothing else to do.
                System.out.printf("  [point %d] ARRIVED → STAIRS (right yellow)%n", pointNumber);
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

    /**
     * LADDER_UP action. Called once the cross is already on the marker tile
     * (arrival gate). Settle briefly, then — paced out to ~1s to look human — move
     * onto loot-tile 5 and Ctrl+left-click it (Control = the game's use-with
     * modifier) to open the use-dialog, and — if point 14 is configured — move onto
     * it and left-click to confirm.
     *
     * When tile 5 is unset there is nothing to use, so we bail; when point 14 is
     * unset (it is optional) we just open the dialog and move on.
     */
    private void ladderUp() {
        // Ctrl+left-click target: loot tile 5. Nothing to use if it is unset.
        int[] use = tile5();
        if (use == null) {
            return;
        }

        // Let the arrival settle before pressing.
        RobotActions.sleep(ACTION_SETTLE_MS);

        // Slow, human-paced press sequence (~1s total; will be shortened later).
        robot.mouseMove(use[0], use[1]);
        RobotActions.sleep(ACTION_MOVE_SETTLE_MS);
        robot.keyPress(KeyEvent.VK_CONTROL);
        RobotActions.sleep(ACTION_HOLD_MS);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        RobotActions.sleep(ACTION_HOLD_MS);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        RobotActions.sleep(ACTION_HOLD_MS);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        int[] p = config.ladderPoint;
        if (p == null) {
            return;
        }
        RobotActions.sleep(ACTION_DIALOG_MS);
        robot.mouseMove(p[0], p[1]);
        RobotActions.sleep(ACTION_MOVE_SETTLE_MS);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        RobotActions.sleep(ACTION_HOLD_MS);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }


    /**
     * ROPE_UP action. Called once the cross is on the marker tile (arrival gate).
     * Human-paced like {@link #ladderUp()}: settle after arrival, press V (held a
     * beat) to arm the rope cursor, wait for the prompt, then move onto loot tile 5
     * and left-click — each step spaced out so it does not fire like a macro.
     * No-op if tile 5 is unset.
     */
    private void ropeUp() {
        int[] t = tile5();
        if (t == null) {
            return;
        }
        RobotActions.sleep(ACTION_SETTLE_MS);
        robot.keyPress(KeyEvent.VK_V);
        RobotActions.sleep(ACTION_HOLD_MS);
        robot.keyRelease(KeyEvent.VK_V);
        RobotActions.sleep(ACTION_DIALOG_MS);
        robot.mouseMove(t[0], t[1]);
        RobotActions.sleep(ACTION_MOVE_SETTLE_MS);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        RobotActions.sleep(ACTION_HOLD_MS);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private void attackIfNeeded(int pointNumber) {
        robot.mouseMove(config.targetX, config.targetY);

        Color current = robot.getPixelColor(config.targetX, config.targetY);
        Color robak = robot.getPixelColor(config.robakX, config.robakY);

        System.out.printf("%s [DIAG] ATTACK [point %d] target(%d,%d)=(%d,%d,%d) white?=%b | robak(%d,%d)=(%d,%d,%d) expected=(%d,%d,%d) match?=%b%n",
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
            System.out.printf("%s TASK [point %d] pixel WHITE → SPACE%n", LocalTime.now(), pointNumber);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.keyRelease(KeyEvent.VK_SPACE);
            if (config.telegramOnAttack) {
                System.out.printf("%s TASK [point %d] telegram: task attack%n", LocalTime.now(), pointNumber);
                TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task attack");
            }
        } else if (robak.equals(config.robakColor)) {
            int nextPointNumber = ((seqPos + 1) % config.steps.size()) + 1;
            System.out.printf("%s TASK [point %d] no monster → loot, then go to point %d%n",
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
            System.out.printf("%s [DIAG] ATTACK [point %d] WAITING (not white and robak!=robakColor) → stuck here until robak=robakColor%n",
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
        System.out.printf("%s TASK [point %d] collecting loot%n", LocalTime.now(), pointNumber);
        if (config.telegramOnLoot) {
            TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task loot");
        }
    }
}
