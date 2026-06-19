package com.example.config;

import java.awt.Color;

/**
 * One step of the patrol loop.
 *
 * Color-marker steps ({@link Type#RUN}, {@link Type#RUN_ATTACK}) walk to the
 * user-picked {@code color} on the minimap. Waypoint steps ({@code ROPE_DOWN},
 * {@code LADDER_UP}, {@code ROPE_UP}) walk to the nearest {@link #WAYPOINT}
 * (yellow) marker and then perform a rope/ladder action.
 */
public record PatrolStep(Type type, Color color) {

    /** Minimap marker color of rope/ladder squares: pure yellow. */
    public static final Color WAYPOINT = new Color(255, 255, 0);

    public enum Type {
        /** Walk to color, then move on (no attack). */
        RUN,
        /** Walk to color, then attack until the point is clear. */
        RUN_ATTACK,
        /** Step onto the nearest yellow marker (rope down happens automatically). */
        ROPE_DOWN,
        /** Step onto yellow, then right-click loot-tile 5 (use ladder up). */
        LADDER_UP,
        /** Step onto yellow, press V, then left-click loot-tile 5 (use rope up). */
        ROPE_UP
    }

    public static PatrolStep run(Color c) { return new PatrolStep(Type.RUN, c); }

    public static PatrolStep attack(Color c) { return new PatrolStep(Type.RUN_ATTACK, c); }

    public static PatrolStep ropeDown() { return new PatrolStep(Type.ROPE_DOWN, WAYPOINT); }

    public static PatrolStep ladderUp() { return new PatrolStep(Type.LADDER_UP, WAYPOINT); }

    public static PatrolStep ropeUp() { return new PatrolStep(Type.ROPE_UP, WAYPOINT); }

    /** Yellow-marker steps locate the nearest marker to center, not a centroid. */
    public boolean isWaypoint() {
        return type == Type.ROPE_DOWN || type == Type.LADDER_UP || type == Type.ROPE_UP;
    }
}
