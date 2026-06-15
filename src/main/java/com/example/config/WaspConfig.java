package com.example.config;

import java.awt.Color;

/**
 * Configuration for the Wasp routine.
 *
 * Fields are flat and public-final (the task classes read them directly), but
 * instances are assembled through {@link Builder} so callers no longer face a
 * 33-argument constructor.
 */
public final class WaspConfig {
    public final int food;
    public final int targetX;
    public final int targetY;
    public final Color targetColor;
    public final int robakX;
    public final int robakY;
    public final Color robakColor;
    public final int rope1X;
    public final int rope1Y;
    public final int rope2X;
    public final int rope2Y;
    public final int drop1X;
    public final int drop1Y;
    public final int drop2X;
    public final int drop2Y;
    public final int healX;
    public final int healY;
    public final Color healColor;
    public final int lootX;
    public final int lootY;
    public final Color lootColor;
    public final int localX;
    public final int localY;
    public final Color localColor;
    public final int musicX;
    public final int musicY;
    public final int[][] lootTiles;
    public final int telegramX;
    public final int telegramY;
    public final Color telegramColor;
    public final String telegramToken;
    public final String telegramChatId;

    private WaspConfig(Builder b) {
        this.food = b.food;
        this.targetX = b.targetX;
        this.targetY = b.targetY;
        this.targetColor = b.targetColor;
        this.robakX = b.robakX;
        this.robakY = b.robakY;
        this.robakColor = b.robakColor;
        this.rope1X = b.rope1X;
        this.rope1Y = b.rope1Y;
        this.rope2X = b.rope2X;
        this.rope2Y = b.rope2Y;
        this.drop1X = b.drop1X;
        this.drop1Y = b.drop1Y;
        this.drop2X = b.drop2X;
        this.drop2Y = b.drop2Y;
        this.healX = b.healX;
        this.healY = b.healY;
        this.healColor = b.healColor;
        this.lootX = b.lootX;
        this.lootY = b.lootY;
        this.lootColor = b.lootColor;
        this.localX = b.localX;
        this.localY = b.localY;
        this.localColor = b.localColor;
        this.musicX = b.musicX;
        this.musicY = b.musicY;
        this.lootTiles = b.lootTiles;
        this.telegramX = b.telegramX;
        this.telegramY = b.telegramY;
        this.telegramColor = b.telegramColor;
        this.telegramToken = b.telegramToken;
        this.telegramChatId = b.telegramChatId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int food;
        private int targetX;
        private int targetY;
        private Color targetColor;
        private int robakX;
        private int robakY;
        private Color robakColor;
        private int rope1X;
        private int rope1Y;
        private int rope2X;
        private int rope2Y;
        private int drop1X;
        private int drop1Y;
        private int drop2X;
        private int drop2Y;
        private int healX;
        private int healY;
        private Color healColor;
        private int lootX;
        private int lootY;
        private Color lootColor;
        private int localX;
        private int localY;
        private Color localColor;
        private int musicX;
        private int musicY;
        private int[][] lootTiles;
        private int telegramX;
        private int telegramY;
        private Color telegramColor;
        private String telegramToken = "";
        private String telegramChatId = "";

        public Builder food(int food) { this.food = food; return this; }
        public Builder target(int x, int y, Color color) { this.targetX = x; this.targetY = y; this.targetColor = color; return this; }
        public Builder robak(int x, int y, Color color) { this.robakX = x; this.robakY = y; this.robakColor = color; return this; }
        public Builder rope1(int x, int y) { this.rope1X = x; this.rope1Y = y; return this; }
        public Builder rope2(int x, int y) { this.rope2X = x; this.rope2Y = y; return this; }
        public Builder drop1(int x, int y) { this.drop1X = x; this.drop1Y = y; return this; }
        public Builder drop2(int x, int y) { this.drop2X = x; this.drop2Y = y; return this; }
        public Builder heal(int x, int y, Color color) { this.healX = x; this.healY = y; this.healColor = color; return this; }
        public Builder loot(int x, int y, Color color) { this.lootX = x; this.lootY = y; this.lootColor = color; return this; }
        public Builder local(int x, int y, Color color) { this.localX = x; this.localY = y; this.localColor = color; return this; }
        public Builder music(int x, int y) { this.musicX = x; this.musicY = y; return this; }
        public Builder lootTiles(int[][] tiles) { this.lootTiles = tiles; return this; }
        public Builder telegram(int x, int y, Color color, String token, String chatId) {
            this.telegramX = x; this.telegramY = y; this.telegramColor = color;
            this.telegramToken = token; this.telegramChatId = chatId; return this;
        }

        public WaspConfig build() {
            return new WaspConfig(this);
        }
    }
}
