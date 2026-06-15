package com.example.bot.bear;

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
public final class BearMapScanner {

    private final Robot robot;
    private final Rectangle area;
    private final int tolerance;

    public BearMapScanner(Robot robot, int x, int y, int w, int h, int tolerance) {
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

    /** Chebyshev (king-move) distance between two screen points. */
    public static int distance(Point a, Point b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }
}
