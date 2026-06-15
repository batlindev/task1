package com.example.config;

import java.awt.Color;

/**
 * Configuration for the Bear patrol routine.
 *
 * The bear walks between three player-placed minimap marks, each identified by
 * a unique color, in a ping-pong order (1 -> 2 -> 3 -> 2 -> 1 -> ...).
 *
 * The minimap panel is fixed on screen while the map scrolls underneath it, so
 * the player ("white cross") is always at the geometric center of the
 * {@code (mapX, mapY, mapW, mapH)} rectangle. A mark's pixel position inside
 * that rectangle therefore tells us, every tick, in which direction it lies.
 *
 * Built through {@link Builder} to mirror {@link WaspConfig}.
 */
public final class BearConfig {

    /** Screen rectangle of the minimap's inner (scrolling) area. */
    public final int mapX;
    public final int mapY;
    public final int mapW;
    public final int mapH;

    /** Per-channel +/- tolerance when matching a mark color (terrain is brown,
     *  marks are distinct, so this can stay small). */
    public final int colorTolerance;

    /** A mark counts as "reached" once its centroid is within this many pixels
     *  of the minimap center (the player). */
    public final int arriveThreshold;

    /** The three marks, in patrol order [point1, point2, point3]. */
    public final Color[] points;

    /** Pixel read while attacking on a point: turns WHITE when a hit lands. */
    public final int targetX;
    public final int targetY;
    public final Color targetColor;

    /** "Monster present" probe: equals robakColor when there is NO monster, so
     *  the attack on a point is finished and we may move to the next point. */
    public final int robakX;
    public final int robakY;
    public final Color robakColor;

    /** Loot-message probe: when this pixel reads {@code lootColor} a loot message
     *  appeared, so we (optionally right-click lootTiles and) notify Telegram. */
    public final int lootX;
    public final int lootY;
    public final Color lootColor;
    /** Tiles to right-click to collect loot; may be {@code null} (then skipped). */
    public final int[][] lootTiles;

    /** HP pixel: drinks a potion when pixel color differs from healColor. */
    public final int healX;
    public final int healY;
    public final Color healColor;

    public final String telegramToken;
    public final String telegramChatId;

    private BearConfig(Builder b) {
        this.mapX = b.mapX;
        this.mapY = b.mapY;
        this.mapW = b.mapW;
        this.mapH = b.mapH;
        this.colorTolerance = b.colorTolerance;
        this.arriveThreshold = b.arriveThreshold;
        this.points = b.points;
        this.targetX = b.targetX;
        this.targetY = b.targetY;
        this.targetColor = b.targetColor;
        this.robakX = b.robakX;
        this.robakY = b.robakY;
        this.robakColor = b.robakColor;
        this.lootX = b.lootX;
        this.lootY = b.lootY;
        this.lootColor = b.lootColor;
        this.lootTiles = b.lootTiles;
        this.healX = b.healX;
        this.healY = b.healY;
        this.healColor = b.healColor;
        this.telegramToken = b.telegramToken;
        this.telegramChatId = b.telegramChatId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int mapX;
        private int mapY;
        private int mapW;
        private int mapH;
        private int colorTolerance = 10;
        private int arriveThreshold = 5;
        private Color[] points;
        private int targetX;
        private int targetY;
        private Color targetColor;
        private int robakX;
        private int robakY;
        private Color robakColor;
        private int lootX;
        private int lootY;
        private Color lootColor;
        private int[][] lootTiles;
        private int healX;
        private int healY;
        private Color healColor;
        private String telegramToken = "";
        private String telegramChatId = "";

        public Builder minimap(int x, int y, int w, int h) {
            this.mapX = x; this.mapY = y; this.mapW = w; this.mapH = h; return this;
        }

        public Builder colorTolerance(int tol) { this.colorTolerance = tol; return this; }

        public Builder arriveThreshold(int px) { this.arriveThreshold = px; return this; }

        public Builder points(Color point1, Color point2, Color point3) {
            this.points = new Color[] { point1, point2, point3 };
            return this;
        }

        public Builder target(int x, int y, Color color) {
            this.targetX = x; this.targetY = y; this.targetColor = color; return this;
        }

        public Builder robak(int x, int y, Color color) {
            this.robakX = x; this.robakY = y; this.robakColor = color; return this;
        }

        public Builder loot(int x, int y, Color color) {
            this.lootX = x; this.lootY = y; this.lootColor = color; return this;
        }

        public Builder lootTiles(int[][] tiles) { this.lootTiles = tiles; return this; }

        public Builder heal(int x, int y, Color color) { this.healX = x; this.healY = y; this.healColor = color; return this; }

        public Builder telegram(String token, String chatId) {
            this.telegramToken = token; this.telegramChatId = chatId; return this;
        }

        public BearConfig build() {
            return new BearConfig(this);
        }
    }
}
