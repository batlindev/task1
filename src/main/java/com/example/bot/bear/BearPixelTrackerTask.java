package com.example.bot.bear;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.time.LocalTime;

import com.example.config.BearConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;

/**
 * Drives the Bear patrol as a small state machine, one step per scheduled run.
 *
 * Each target point is a uniquely-colored minimap mark. The player is always at
 * the center of the minimap, so we scan for the current target's color, click
 * where it sits (the game auto-walks there), and treat "mark reached center" as
 * arrival. Then we advance to the next point in the ping-pong sequence.
 */
public final class BearPixelTrackerTask extends RobotTask {

    private final BearConfig config;
    private final BearMapScanner scanner;

    // Ping-pong patrol: indices into config.points giving 1,2,3,2,1,2,3,2,...
    // (0-based: 0,1,2,1 repeating). Edit here to change the route.
    private static final int[] SEQUENCE = { 0, 1, 2, 1 };
    private int seqPos = 0;

    private enum BearFlowStep {
        WALK,
        ATTACK
    }

    private volatile BearFlowStep step = BearFlowStep.WALK;

    // After a kill we keep scanning loot for a short grace window, because the
    // loot message pops up just AFTER the monster dies — by then we have already
    // switched to WALK toward the next point.
    private static final long LOOT_GRACE_MS = 4000;
    private volatile long lootScanUntilMs = 0L;

    /** True while on a point (attacking) OR within the post-kill grace window —
     *  gates loot scanning so loot appearing right after a kill is still caught. */
    public boolean shouldScanLoot() {
        return step == BearFlowStep.ATTACK || System.currentTimeMillis() < lootScanUntilMs;
    }

    public BearPixelTrackerTask(BearConfig config) {
        this.config = config;
        this.scanner = new BearMapScanner(robot, config.mapX, config.mapY, config.mapW, config.mapH,
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
            System.out.printf("%s BEAR [punkt %d] znacznik (%d,%d,%d) POZA ZASIEGIEM minimapy → czekam%n",
                    LocalTime.now(), pointNumber,
                    target.getRed(), target.getGreen(), target.getBlue());
            return;
        }

        int dist = BearMapScanner.distance(mark, center);
        System.out.printf("%s BEAR [punkt %d] znacznik ekran=(%d,%d) srodek=(%d,%d) dist=%d%n",
                LocalTime.now(), pointNumber, mark.x, mark.y, center.x, center.y, dist);

        if (dist <= config.arriveThreshold) {
            System.out.printf("  [punkt %d] DOTARLEM (dist<=%d) → atakuje%n", pointNumber, config.arriveThreshold);
            step = BearFlowStep.ATTACK;
        } else {
            System.out.printf("  [punkt %d] klik (%d,%d) → auto-chodzenie%n", pointNumber, mark.x, mark.y);
            RobotActions.clickMouse(robot, mark.x, mark.y);
        }
    }

    /**
     * Attack on a point, observing the target pixel like Wasp does — SPACE is
     * pressed ONLY when the pixel is WHITE (the "attack" signal). In any other
     * state we just watch and press nothing:
     * <ul>
     *   <li>WHITE → press SPACE once (the pixel then changes, so no spamming).</li>
     *   <li>No monster (probe reads {@code robakColor}) → point cleared, go to
     *       the next point.</li>
     *   <li>Otherwise (RED = locked / GREY = idle) → do nothing, keep watching.</li>
     * </ul>
     */
    private void attackIfNeeded(int pointNumber) {
        // Najedz kursorem na cel — to wlasnie hover sprawia, ze pixel bieleje
        // (jak w Wasp), co jest sygnalem do nacisniecia SPACJI.
        robot.mouseMove(config.targetX, config.targetY);

        Color current = robot.getPixelColor(config.targetX, config.targetY);
        Color robak = robot.getPixelColor(config.robakX, config.robakY);

        if (current.equals(Color.WHITE)) {
            System.out.printf("%s BEAR [punkt %d] pixel BIALY → SPACJA%n", LocalTime.now(), pointNumber);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.keyRelease(KeyEvent.VK_SPACE);
        } else if (robak.equals(config.robakColor)) {
            int nextSeqPos = (seqPos + 1) % SEQUENCE.length;
            System.out.printf("%s BEAR [punkt %d] brak potwora → ide do punktu %d (loot skanuje jeszcze %d ms)%n",
                    LocalTime.now(), pointNumber, SEQUENCE[nextSeqPos] + 1, LOOT_GRACE_MS);
            // Kill happened → keep loot scanning alive through the transition.
            lootScanUntilMs = System.currentTimeMillis() + LOOT_GRACE_MS;
            seqPos = nextSeqPos;
            step = BearFlowStep.WALK;
        }
        // else: cel zablokowany (czerwony) lub brak celu (szary) → tylko obserwuje, nic nie naciskam
    }
}
