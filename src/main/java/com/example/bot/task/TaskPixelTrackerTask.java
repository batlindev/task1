package com.example.bot.task;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import com.example.config.PatrolStep;
import com.example.config.TaskConfig;
import com.example.util.Log;
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
 * Loot is collected synchronously at the kill moment (see {@link #grabLootIfPresent}):
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

    // Last distance (minimap px) at which we still SAW the current step's mark
    // (yellow waypoint OR a picked color). Guards the "vanished = arrived" rule:
    // a mark only counts as stepped-on if it was already close when it
    // disappeared. A mark that vanishes while still far away left the minimap
    // range — that is NOT arrival.
    private int lastSeenDist = Integer.MAX_VALUE;

    // Consecutive WALK ticks clicking toward a COLOR mark with no progress (dist
    // not shrinking). A mark on unreachable terrain (water / blocked tile) makes
    // the auto-walk click a no-op, so dist freezes and the bot would click it
    // forever. After this many stalled ticks we give up and skip to the next
    // step instead of hanging. Waypoints are exempt — they have the vanish rule.
    private int stalledTicks = 0;
    private static final int STALL_GIVEUP_TICKS = 8;

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

    // Throttle for the "waiting" attack log: print the stuck-state pixels only on
    // the first waiting tick, reset whenever we swing, so a point that never
    // clears is visible without spamming every tick.
    private boolean waitingLogged = false;

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
        // Open the first lap on the user-chosen start card; bad index = front.
        int start = config.startIndex;
        if (config.steps != null && start > 0 && start < config.steps.size()) {
            this.seqPos = start;
        }
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
        // Publish the live cursor so the UI can highlight the active card.
        if (config.progress != null) config.progress.set(seqPos);

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
        lastSeenDist = Integer.MAX_VALUE;
        stalledTicks = 0;
        waitingLogged = false;
    }

    private void walkToward(int pointNumber, PatrolStep current) {
        // Attack-in-place: no approach, jump straight to the ATTACK phase and
        // swing where we already stand.
        if (current.type() == PatrolStep.Type.ATTACK_ONLY) {
            Log.action("p%d ATTACK in place", pointNumber);
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
            // The mark we were walking toward disappeared. This means we stepped
            // onto it (the player cross now covers the pixel) ONLY if it was
            // already close last time we saw it — true for yellow waypoints AND
            // for a picked color mark that sits on a single tile. A mark that
            // vanishes while still far away just scrolled off the minimap edge —
            // not arrival, so keep waiting. A mark within (arrivePx + slack) of
            // center can only have vanished by being covered, never by scrolling.
            if (clickedTowardCurrent) {
                int arrivePx = current.isWaypoint() ? WAYPOINT_ARRIVE_PX : config.arriveThreshold;
                if (lastSeenDist <= arrivePx + VANISH_ARRIVE_SLACK) {
                    Log.action("p%d mark vanished (dist=%d) → ARRIVED", pointNumber, lastSeenDist);
                    onArrived(pointNumber, current);
                }
                // else: vanished while far = scrolled off minimap, keep waiting (no log).
                return;
            }
            return;
        }

        int dist = TaskMapScanner.distance(mark, center);
        int prevDist = lastSeenDist;
        lastSeenDist = dist;

        if (current.isWaypoint()) {
            // Waypoint actions (rope/ladder/drop) fire when the white cross sits ON
            // the marker tile = the marker is within WAYPOINT_ARRIVE_PX of center.
            if (dist <= WAYPOINT_ARRIVE_PX) {
                onArrived(pointNumber, current);
                return;
            }
            // Click the yellow ONCE, precisely; the single auto-walk click already
            // paths the player to that exact tile. Re-clicking every tick would
            // re-target the marker as it slides toward center while we walk, which
            // jitters the final position and misses the tile — so after the first
            // click we only SCAN (fast ticks, no click) and wait for arrival.
            if (!clickedTowardCurrent) {
                Log.action("p%d waypoint → click yellow once (dist=%d), then scan-only", pointNumber, dist);
                RobotActions.clickMouse(robot, mark.x, mark.y, "waypoint p" + pointNumber);
                clickedTowardCurrent = true;
                stalledTicks = 0;
                return;
            }
            // Already walking from the single click: just watch. Recover only if the
            // path is blocked — dist stops shrinking for STALL_GIVEUP_TICKS — by
            // re-clicking once. This fires only while far + frozen, never near
            // center, so it cannot reintroduce the close-range jitter.
            if (dist < prevDist) {
                stalledTicks = 0;
            } else if (++stalledTicks >= STALL_GIVEUP_TICKS) {
                Log.action("p%d waypoint stalled (dist=%d) → recovery click", pointNumber, dist);
                RobotActions.clickMouse(robot, mark.x, mark.y, "waypoint-recovery p" + pointNumber);
                stalledTicks = 0;
            }
            return;
        }

        if (dist <= config.arriveThreshold) {
            onArrived(pointNumber, current);
            return;
        }
        // No progress (dist not shrinking) = the click can't path here (mark on
        // unreachable terrain — water/blocked). Bail after STALL_GIVEUP_TICKS so
        // we don't click a dead mark forever; skip to the next step.
        if (dist < prevDist) {
            stalledTicks = 0;
        } else if (++stalledTicks >= STALL_GIVEUP_TICKS) {
            Log.action("p%d STUCK (dist=%d unreachable?) → skip to next", pointNumber, dist);
            advance();
            return;
        }
        RobotActions.clickMouse(robot, mark.x, mark.y, "walk p" + pointNumber);
        clickedTowardCurrent = true;
    }

    /** Reached the target tile: dispatch the step's action, then advance (or
     *  hand off to the ATTACK phase for RUN_ATTACK). */
    private void onArrived(int pointNumber, PatrolStep current) {
        switch (current.type()) {
            case RUN_ATTACK:
                Log.action("p%d ARRIVED → attack", pointNumber);
                step = TaskFlowStep.ATTACK;
                break;
            case RUN:
                Log.action("p%d ARRIVED → RUN → next", pointNumber);
                advance();
                break;
            case ROPE_DOWN:
                // Standing on the marker already triggers rope-down; nothing more.
                Log.action("p%d ARRIVED → ROPE DOWN", pointNumber);
                advance();
                break;
            case LADDER_UP:
                Log.action("p%d ARRIVED → LADDER UP", pointNumber);
                ladderUp();
                advance();
                break;
            case ROPE_UP:
                Log.action("p%d ARRIVED → ROPE UP", pointNumber);
                ropeUp();
                advance();
                break;
            case STAIRS:
                // Walked onto the right end of the yellow stair mark; stepping there
                // takes us up. Nothing else to do.
                Log.action("p%d ARRIVED → STAIRS", pointNumber);
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
        Log.input("CLICK L+Ctrl (%d,%d)", use[0], use[1]);

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
        Log.input("CLICK L (%d,%d)", p[0], p[1]);
    }


    /**
     * ROPE_UP action. Called once the cross is on the marker tile (arrival gate).
     * Human-paced like {@link #ladderUp()}: settle after arrival, press V (held a
     * beat) to arm the rope cursor, wait for the prompt, then move onto loot tile 5
     * and left-click — each step spaced out so it does not fire like a macro.
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
        Log.input("KEY V (rope)");
        RobotActions.sleep(ACTION_DIALOG_MS);
        robot.mouseMove(t[0], t[1]);
        RobotActions.sleep(ACTION_MOVE_SETTLE_MS);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        RobotActions.sleep(ACTION_HOLD_MS);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Log.input("CLICK L (%d,%d)", t[0], t[1]);
    }

    private void attackIfNeeded(int pointNumber) {
        // LOOT HAS PRIORITY. After a kill the loot indicator (config.lootColor,
        // e.g. 255,180,0) shows at the bottom. While it is up we ONLY collect loot
        // (right-click tiles 1-9) and do nothing else this tick — crucially we do
        // NOT press SPACE, because attacking starts a chase that drags the player
        // off the bodies before the loot is grabbed. Only once no loot is on the
        // ground do we decide whether to attack the next monster or move on.
        if (config.lootEnabled && lootPresent()) {
            grabLootIfPresent(pointNumber);
            waitingLogged = false;
            return;
        }

        robot.mouseMove(config.targetX, config.targetY);
        Color current = robot.getPixelColor(config.targetX, config.targetY);
        Color robak = robot.getPixelColor(config.robakX, config.robakY);

        if (current.equals(Color.WHITE)) {
            // A monster is targeted on the attack tile and no loot is waiting → swing.
            RobotActions.pressKey(robot, KeyEvent.VK_SPACE, "SPACE (attack)");
            waitingLogged = false;
            if (config.telegramOnAttack) {
                TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task attack");
            }
        } else if (robak.equals(config.robakColor)) {
            // No monster and no loot showing yet — but the last kill's loot pops a
            // beat AFTER the body drops, so wait once and re-check before walking
            // off. If loot appears, grab it (stay; loot-first handles the rest next
            // tick); only a truly empty point advances.
            if (config.lootEnabled) {
                RobotActions.sleep(LOOT_APPEAR_MS);
                if (lootPresent()) {
                    grabLootIfPresent(pointNumber);
                    RobotActions.sleep(LOOT_SETTLE_MS);
                    return;
                }
            }
            int nextPointNumber = ((seqPos + 1) % config.steps.size()) + 1;
            Log.action("p%d cleared → p%d", pointNumber, nextPointNumber);
            advance();
        } else if (!waitingLogged) {
            // Neither a monster on the target pixel nor the "cleared" ground color.
            // Log the two pixels ONCE so a stuck point (clear never detected) is
            // visible without spamming every tick.
            Log.action("p%d waiting: target=(%d,%d,%d) white?=no | robak=(%d,%d,%d) need=(%d,%d,%d)",
                    pointNumber,
                    current.getRed(), current.getGreen(), current.getBlue(),
                    robak.getRed(), robak.getGreen(), robak.getBlue(),
                    config.robakColor.getRed(), config.robakColor.getGreen(), config.robakColor.getBlue());
            waitingLogged = true;
        }
    }

    /** True while the loot indicator pixel reads {@code lootColor} (loot waiting). */
    private boolean lootPresent() {
        return robot.getPixelColor(config.lootX, config.lootY).equals(config.lootColor);
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
        Log.action("p%d LOOT", pointNumber);
        if (config.lootTiles != null) {
            for (int i = 0; i < config.lootTiles.length; i++) {
                int[] tile = config.lootTiles[i];
                if (tile == null) {
                    continue;
                }
                RobotActions.rightClick(robot, tile[0], tile[1]);
                RobotActions.sleep(LOOT_TILE_MS);
            }
        }
        if (config.telegramOnLoot) {
            TelegramClient.sendMessage(config.telegramToken, config.telegramChatId, "task loot");
        }
    }
}
