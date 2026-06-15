package com.example.bot.wasp;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.time.LocalTime;

import com.example.config.WaspConfig;
import com.example.util.RobotActions;
import com.example.util.RobotTask;

/**
 * Drives the Wasp climb/attack/descend loop as a small state machine, one step
 * per scheduled run.
 */
public class WaspPixelTrackerTask extends RobotTask {

    private final WaspConfig config;
    private WaspFlowStep step = WaspFlowStep.ATTACK1;

    public WaspPixelTrackerTask(WaspConfig config) {
        this.config = config;
    }

    private enum WaspFlowStep {
        ATTACK1,
        LEVEL2,
        ATTACK2,
        LEVEL3,
        ATTACK3,
        LEVEL4,
        ATTACK4,
        DOWN_LEVEL4,
        DOWN_ATTACK3,
        DOWN_LEVEL3,
        DOWN_ATTACK2,
        DOWN_LEVEL2
    }

    @Override
    public void run() {
        robot.mouseMove(config.targetX, config.targetY);

        Color currentColor = robot.getPixelColor(config.targetX, config.targetY);
        Color robak = robot.getPixelColor(config.robakX, config.robakY);

        boolean targetMatch = currentColor.equals(config.targetColor);
        boolean robakMatch = robak.equals(config.robakColor);

        System.out.printf("%s WASP SCAN [krok=%s]%n", LocalTime.now(), step);
        System.out.printf("  TARGET (%d,%d): odczyt=(%d,%d,%d) oczekiwany=(%d,%d,%d) PASUJE=%b%n",
                config.targetX, config.targetY,
                currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(),
                config.targetColor.getRed(), config.targetColor.getGreen(), config.targetColor.getBlue(),
                targetMatch);
        System.out.printf("  ROBAK  (%d,%d): odczyt=(%d,%d,%d) oczekiwany=(%d,%d,%d) PASUJE=%b%n",
                config.robakX, config.robakY,
                robak.getRed(), robak.getGreen(), robak.getBlue(),
                config.robakColor.getRed(), config.robakColor.getGreen(), config.robakColor.getBlue(),
                robakMatch);

        switch (step) {
            case ATTACK1:
                attackIfNeeded("ATTACK1", currentColor, robak, WaspFlowStep.LEVEL2);
                break;
            case LEVEL2:
                climbIfRobak("level2", config.lootTiles[4][0], config.lootTiles[4][1], WaspFlowStep.ATTACK2, 0);
                break;
            case ATTACK2:
                attackIfNeeded("ATTACK2", currentColor, robak, WaspFlowStep.LEVEL3);
                break;
            case LEVEL3:
                climbIfRobak("level3", config.rope1X, config.rope1Y, WaspFlowStep.ATTACK3, 2500);
                break;
            case ATTACK3:
                attackIfNeeded("ATTACK3", currentColor, robak, WaspFlowStep.LEVEL4);
                break;
            case LEVEL4:
                climbIfRobak("level4", config.rope2X, config.rope2Y, WaspFlowStep.ATTACK4, 1800);
                break;
            case ATTACK4:
                attackIfNeeded("ATTACK4", currentColor, robak, WaspFlowStep.DOWN_LEVEL4);
                break;
            case DOWN_LEVEL4:
                moveDownIfRobak("SCHODZE Z 4 NA 3", WaspFlowStep.DOWN_ATTACK3);
                break;
            case DOWN_ATTACK3:
                attackIfNeeded("DOWN_ATTACK3", currentColor, robak, WaspFlowStep.DOWN_LEVEL3);
                break;
            case DOWN_LEVEL3:
                dropIfRobak("3 -> 2", config.drop1X, config.drop1Y, WaspFlowStep.DOWN_ATTACK2, 1000);
                break;
            case DOWN_ATTACK2:
                attackIfNeeded("DOWN_ATTACK2", currentColor, robak, WaspFlowStep.DOWN_LEVEL2);
                break;
            case DOWN_LEVEL2:
                dropIfRobak("2 -> 1 KONIEC PETLI", config.drop2X, config.drop2Y, WaspFlowStep.ATTACK1, 2500);
                break;
        }
    }

    private void attackIfNeeded(String label, Color currentColor, Color robak, WaspFlowStep nextStep) {
        if (currentColor.equals(Color.WHITE)) {
            System.out.printf("  [%s] pixel BIAŁY (255,255,255) → SPACJA, bije, zostaję%n", label);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.keyRelease(KeyEvent.VK_SPACE);
        } else if (robak.equals(config.robakColor)) {
            System.out.printf("  [%s] pixel nie biały + kolor (70,70,70) = robaka NIE MA → ZMIANA KROKU %s → %s%n", label, step, nextStep);
            step = nextStep;
        } else {
            System.out.printf("  [%s] pixel nie biały, ale robak JEST → zostaję%n", label);
        }
    }

    private void climbIfRobak(String label, int x, int y, WaspFlowStep nextStep, long sleepMs) {
        Color robak = robot.getPixelColor(config.robakX, config.robakY);
        boolean robakMatch = robak.equals(config.robakColor);
        System.out.printf("  [%s] robak=(%d,%d,%d) PASUJE=%b%n",
                label, robak.getRed(), robak.getGreen(), robak.getBlue(), robakMatch);
        if (robakMatch) {
            System.out.printf("  [%s] robaka NIE MA (70,70,70) → naciskam V + klik (%d,%d) → ZMIANA KROKU %s → %s%n", label, x, y, step, nextStep);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            RobotActions.clickMouse(robot, x, y);
            step = nextStep;
            if (sleepMs > 0) {
                System.out.printf("  [%s] → czekam %d ms aż postać dojdzie%n", label, sleepMs);
                RobotActions.sleep(sleepMs);
            }
        } else {
            System.out.printf("  [%s] → robak nadal JEST, czekam%n", label);
        }
    }

    private void moveDownIfRobak(String label, WaspFlowStep nextStep) {
        Color robak = robot.getPixelColor(config.robakX, config.robakY);
        boolean robakMatch = robak.equals(config.robakColor);
        System.out.printf("  [%s] robak=(%d,%d,%d) PASUJE=%b%n",
                label, robak.getRed(), robak.getGreen(), robak.getBlue(), robakMatch);
        if (robakMatch) {
            System.out.printf("  [%s] robaka NIE MA (70,70,70) → naciskam W → ZMIANA KROKU %s → %s%n", label, step, nextStep);
            robot.keyPress(KeyEvent.VK_W);
            robot.keyRelease(KeyEvent.VK_W);
            step = nextStep;
        } else {
            System.out.printf("  [%s] → robak nadal JEST, czekam%n", label);
        }
    }

    private void dropIfRobak(String label, int x, int y, WaspFlowStep nextStep, long sleepMs) {
        Color robak = robot.getPixelColor(config.robakX, config.robakY);
        boolean robakMatch = robak.equals(config.robakColor);
        System.out.printf("  [%s] robak=(%d,%d,%d) PASUJE=%b%n",
                label, robak.getRed(), robak.getGreen(), robak.getBlue(), robakMatch);
        if (robakMatch) {
            System.out.printf("  [%s] robaka NIE MA (70,70,70) → klik (%d,%d) → ZMIANA KROKU %s → %s%n", label, x, y, step, nextStep);
            RobotActions.clickMouse(robot, x, y);
            step = nextStep;
            if (sleepMs > 0) {
                System.out.printf("  [%s] → czekam %d ms aż postać zejdzie%n", label, sleepMs);
                RobotActions.sleep(sleepMs);
            }
        } else {
            System.out.printf("  [%s] → robak nadal JEST, czekam%n", label);
        }
    }
}
