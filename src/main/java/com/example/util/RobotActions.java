package com.example.util;

import java.awt.Color;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Small reusable {@link Robot} interactions shared across bots, so the
 * duplicated "eat food" / "heal" loops live in one place.
 *
 * Every input helper here logs through {@link Log#input} so each key press and
 * mouse click the bot drives is always visible in the terminal.
 */
public final class RobotActions {

    private RobotActions() {
    }

    /** Left-click at the given screen coordinates. */
    public static void clickMouse(Robot robot, int x, int y) {
        clickMouse(robot, x, y, "");
    }

    /** Left-click, logged with a short {@code why} tag so the terminal says what
     *  the click was for (walk / waypoint / ladder / …). */
    public static void clickMouse(Robot robot, int x, int y, String why) {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Log.input("CLICK L (%d,%d)%s", x, y, why.isEmpty() ? "" : " " + why);
    }

    /** Right-click at the given screen coordinates. */
    public static void rightClick(Robot robot, int x, int y) {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        Log.input("CLICK R (%d,%d)", x, y);
    }

    /** Tap a key (press + release) and log it by name. */
    public static void pressKey(Robot robot, int keyCode, String name) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
        Log.input("KEY %s", name);
    }

    /** Press the eat key ("X") {@code times} times with a short randomized gap. */
    public static void eatFood(Robot robot, int times) {
        for (int i = 0; i < times; i++) {
            pressKey(robot, KeyEvent.VK_X, "X (eat)");
            sleep((long) (900 + Math.random()));
        }
    }

    /**
     * Heal when the pixel at (x, y) is not the expected "full health" color —
     * i.e. health dropped, so press the potion key ("C").
     */
    public static void healIfNeeded(Robot robot, int x, int y, Color healColor) {
        Color current = robot.getPixelColor(x, y);
        if (!current.equals(healColor)) {
            pressKey(robot, KeyEvent.VK_C, "C (potion)");
        }
    }

    /** Uninterruptible-friendly sleep helper that preserves the interrupt flag. */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
