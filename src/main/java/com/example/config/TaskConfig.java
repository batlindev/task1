package com.example.config;

import java.awt.Color;
import java.util.List;

/**
 * Configuration for the Task patrol routine.
 *
 * The task walks an ordered loop of {@link PatrolStep}s built in the generator
 * panel, cycled linearly 1..N..1. Each color step is a player-placed minimap
 * mark identified by a unique color.
 *
 * The minimap panel is fixed on screen while the map scrolls underneath it, so
 * the player ("white cross") is always at the geometric center of the
 * {@code (mapX, mapY, mapW, mapH)} rectangle. A mark's pixel position inside
 * that rectangle therefore tells us, every tick, in which direction it lies.
 *
 * Built through {@link Builder}.
 */
public final class TaskConfig {

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

    /** The patrol loop: an ordered list of steps, cycled linearly 1..N..1, built
     *  by the generator panel. */
    public final List<PatrolStep> steps;

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

    /** Optional LADDER_UP confirm point (settings point 14). After Ctrl+left-click
     *  opens the use-dialog, the bot moves here and left-clicks. {@code null} when
     *  not configured — then the ladder step only opens the dialog and moves on.
     *  Only ever read by the LADDER_UP step, so an unused value does nothing. */
    public final int[] ladderPoint;

    /** HP pixel: drinks a potion when pixel color differs from healColor. */
    public final int healX;
    public final int healY;
    public final Color healColor;

    public final String telegramToken;
    public final String telegramChatId;

    /** Optional machine actions toggled in the UI generator panel. */
    public final boolean lootEnabled;
    public final boolean healEnabled;
    /** When true, every SPACE attack also fires a Telegram message. */
    public final boolean telegramOnAttack;
    /** When true, grabbing loot fires a Telegram message. */
    public final boolean telegramOnLoot;

    private TaskConfig(Builder b) {
        this.mapX = b.mapX;
        this.mapY = b.mapY;
        this.mapW = b.mapW;
        this.mapH = b.mapH;
        this.colorTolerance = b.colorTolerance;
        this.arriveThreshold = b.arriveThreshold;
        this.steps = b.steps;
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
        this.ladderPoint = b.ladderPoint;
        this.healX = b.healX;
        this.healY = b.healY;
        this.healColor = b.healColor;
        this.telegramToken = b.telegramToken;
        this.telegramChatId = b.telegramChatId;
        this.lootEnabled = b.lootEnabled;
        this.healEnabled = b.healEnabled;
        this.telegramOnAttack = b.telegramOnAttack;
        this.telegramOnLoot = b.telegramOnLoot;
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
        private List<PatrolStep> steps;
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
        private int[] ladderPoint;
        private int healX;
        private int healY;
        private Color healColor;
        private String telegramToken = "";
        private String telegramChatId = "";
        private boolean lootEnabled = true;
        private boolean healEnabled = true;
        private boolean telegramOnAttack = false;
        private boolean telegramOnLoot = true;

        public Builder minimap(int x, int y, int w, int h) {
            this.mapX = x; this.mapY = y; this.mapW = w; this.mapH = h; return this;
        }

        public Builder colorTolerance(int tol) { this.colorTolerance = tol; return this; }

        public Builder arriveThreshold(int px) { this.arriveThreshold = px; return this; }

        /** The patrol loop, built by the generator panel. */
        public Builder steps(List<PatrolStep> steps) { this.steps = steps; return this; }

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

        /** Optional LADDER_UP confirm point (point 14); {@code null} = unset. */
        public Builder ladderPoint(int[] p) { this.ladderPoint = p; return this; }

        public Builder heal(int x, int y, Color color) { this.healX = x; this.healY = y; this.healColor = color; return this; }

        public Builder telegram(String token, String chatId) {
            this.telegramToken = token; this.telegramChatId = chatId; return this;
        }

        public Builder lootEnabled(boolean v) { this.lootEnabled = v; return this; }

        public Builder healEnabled(boolean v) { this.healEnabled = v; return this; }

        public Builder telegramOnAttack(boolean v) { this.telegramOnAttack = v; return this; }

        public Builder telegramOnLoot(boolean v) { this.telegramOnLoot = v; return this; }

        public TaskConfig build() {
            if (steps == null) {
                steps = List.of();
            }
            return new TaskConfig(this);
        }
    }
}
