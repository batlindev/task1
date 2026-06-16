package com.example.util;

import java.awt.Color;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;

/**
 * Small reusable {@link Robot} interactions shared across bots, so the
 * duplicated "eat food" / "heal" loops live in one place.
 */
public final class RobotActions {

    private RobotActions() {
    }

    /** Left-click at the given screen coordinates. */
    public static void clickMouse(Robot robot, int x, int y) {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /** Press the eat key ("X") {@code times} times with a short randomized gap. */
    public static void eatFood(Robot robot, int times) {
        for (int i = 0; i < times; i++) {
            robot.keyPress(KeyEvent.VK_X);
            System.out.println(LocalTime.now() + " EAT X");
            robot.keyRelease(KeyEvent.VK_X);
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
            robot.keyPress(KeyEvent.VK_C);
            System.out.println(LocalTime.now() + " POTION");
            robot.keyRelease(KeyEvent.VK_C);
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
