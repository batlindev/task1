package com.example.config;

import java.awt.Color;

/**
 * Immutable snapshot of the main bot configuration, captured when START is
 * pressed. Replaces the pile of {@code static} fields that used to live on
 * {@code Main} and be mutated from the UI thread.
 */
public final class BotSettings {

    /** The "monster present" marker color the attack scan looks for. */
    public static final Color TARGET_COLOR = new Color(255, 255, 255);

    public final int targetX;
    public final int targetY;
    public final int healX;
    public final int healY;
    public final Color healColor;
    public final int food;

    public final int telegramCheckX;
    public final int telegramCheckY;
    public final Color telegramColor;
    public final String telegramToken;
    public final String telegramChatId;

    private BotSettings(Builder b) {
        this.targetX = b.targetX;
        this.targetY = b.targetY;
        this.healX = b.healX;
        this.healY = b.healY;
        this.healColor = b.healColor;
        this.food = b.food;
        this.telegramCheckX = b.telegramCheckX;
        this.telegramCheckY = b.telegramCheckY;
        this.telegramColor = b.telegramColor;
        this.telegramToken = b.telegramToken;
        this.telegramChatId = b.telegramChatId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int targetX;
        private int targetY;
        private int healX;
        private int healY;
        private Color healColor;
        private int food;
        private int telegramCheckX;
        private int telegramCheckY;
        private Color telegramColor;
        private String telegramToken = "";
        private String telegramChatId = "";

        public Builder target(int x, int y) { this.targetX = x; this.targetY = y; return this; }
        public Builder heal(int x, int y, Color color) { this.healX = x; this.healY = y; this.healColor = color; return this; }
        public Builder food(int food) { this.food = food; return this; }
        public Builder telegramCheck(int x, int y, Color color) {
            this.telegramCheckX = x; this.telegramCheckY = y; this.telegramColor = color; return this;
        }
        public Builder telegram(String token, String chatId) {
            this.telegramToken = token; this.telegramChatId = chatId; return this;
        }

        public BotSettings build() {
            return new BotSettings(this);
        }
    }
}
