package com.example.bot.task;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * Reads the minimap panel and locates colored marks within it.
 *
 * One {@link Robot#createScreenCapture} per scan (cheap) instead of hundreds of
 * {@link Robot#getPixelColor} calls (each of which grabs the whole screen).
 */
public final class TaskMapScanner {

    private final Robot robot;
    private final Rectangle area;
    private final int tolerance;

    public TaskMapScanner(Robot robot, int x, int y, int w, int h, int tolerance) {
        this.robot = robot;
        this.area = new Rectangle(x, y, w, h);
        this.tolerance = tolerance;
    }

    /** The player: always the geometric center of the minimap panel (screen coords). */
    public Point center() {
        return new Point(area.x + area.width / 2, area.y + area.height / 2);
    }

    /**
     * Centroid (in screen coordinates) of all pixels matching {@code target}
     * within the minimap, or {@code null} if the color is not currently visible
     * (mark out of minimap range, or hidden under the player cross).
     */
    public Point findColor(Color target) {
        BufferedImage img = robot.createScreenCapture(area);
        long sumX = 0;
        long sumY = 0;
        int count = 0;
        for (int y = 0; y < area.height; y++) {
            for (int x = 0; x < area.width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (Math.abs(r - target.getRed()) <= tolerance
                        && Math.abs(g - target.getGreen()) <= tolerance
                        && Math.abs(b - target.getBlue()) <= tolerance) {
                    sumX += x;
                    sumY += y;
                    count++;
                }
            }
        }
        if (count == 0) {
            return null;
        }
        return new Point(area.x + (int) (sumX / count), area.y + (int) (sumY / count));
    }

    /**
     * Screen point of the matching pixel NEAREST the minimap center, matched
     * EXACTLY (tolerance ignored), or {@code null} if none is visible. Used for
     * rope/ladder yellow markers, which are a precise UI color and may appear in
     * several spots — we head for the closest one.
     */
    public Point findNearestExact(Color target) {
        BufferedImage img = robot.createScreenCapture(area);
        int cx = area.width / 2;
        int cy = area.height / 2;
        int bestDist = Integer.MAX_VALUE;
        int bestX = -1;
        int bestY = -1;
        for (int y = 0; y < area.height; y++) {
            for (int x = 0; x < area.width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r == target.getRed() && g == target.getGreen() && b == target.getBlue()) {
                    int d = Math.max(Math.abs(x - cx), Math.abs(y - cy));
                    if (d < bestDist) {
                        bestDist = d;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }
        if (bestX < 0) {
            return null;
        }
        return new Point(area.x + bestX, area.y + bestY);
    }

    /**
     * Screen point of the RIGHTMOST exact-matching pixel (max x; ties broken by the
     * one nearest the vertical center). Used by STAIRS: stairs render as an
     * elongated yellow mark and the right end is the stair tile to step onto.
     * {@code null} if the color is absent. One capture.
     */
    public Point findRightmostExact(Color target) {
        BufferedImage img = robot.createScreenCapture(area);
        int cy = area.height / 2;
        int r = target.getRed();
        int g = target.getGreen();
        int b = target.getBlue();
        int bestX = -1;
        int bestY = -1;
        for (int y = 0; y < area.height; y++) {
            for (int x = 0; x < area.width; x++) {
                int rgb = img.getRGB(x, y);
                if (((rgb >> 16) & 0xFF) == r && ((rgb >> 8) & 0xFF) == g && (rgb & 0xFF) == b) {
                    if (x > bestX || (x == bestX && Math.abs(y - cy) < Math.abs(bestY - cy))) {
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }
        if (bestX < 0) {
            return null;
        }
        return new Point(area.x + bestX, area.y + bestY);
    }

    /**
     * Screen point of the exact-matching pixel FARTHEST from the minimap center
     * (max Chebyshev distance), or {@code null} if none is visible. Mirror of
     * {@link #findNearestExact}: used by the "_FAR" waypoint variants when two
     * yellow markers sit side by side (e.g. one passage-up next to another) and
     * we want the farther one. Ties break toward the RIGHTMOST pixel (then the
     * one nearest the vertical center) — so STAIRS_FAR lands on the right end of
     * the farther stair mark, matching {@link #findRightmostExact}.
     */
    public Point findFarthestExact(Color target) {
        BufferedImage img = robot.createScreenCapture(area);
        int cx = area.width / 2;
        int cy = area.height / 2;
        int bestDist = -1;
        int bestX = -1;
        int bestY = -1;
        for (int y = 0; y < area.height; y++) {
            for (int x = 0; x < area.width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r == target.getRed() && g == target.getGreen() && b == target.getBlue()) {
                    int d = Math.max(Math.abs(x - cx), Math.abs(y - cy));
                    boolean better = d > bestDist
                            || (d == bestDist && x > bestX)
                            || (d == bestDist && x == bestX && Math.abs(y - cy) < Math.abs(bestY - cy));
                    if (better) {
                        bestDist = d;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }
        if (bestX < 0) {
            return null;
        }
        return new Point(area.x + bestX, area.y + bestY);
    }

    /** Chebyshev (king-move) distance between two screen points. */
    public static int distance(Point a, Point b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }
}
