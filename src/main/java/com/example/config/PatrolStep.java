package com.example.config;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Serialize one step. Color steps carry their {@code TYPE:R,G,B}; waypoint
     * steps use the fixed yellow marker so they encode as bare {@code TYPE}.
     */
    public String encode() {
        if (isWaypoint()) {
            return type.name();
        }
        return type.name() + ":" + color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    /** Parse one encoded step; throws on an unknown type or malformed color. */
    public static PatrolStep decode(String token) {
        String s = token.trim();
        int colon = s.indexOf(':');
        Type t = Type.valueOf(colon < 0 ? s : s.substring(0, colon));
        switch (t) {
            case ROPE_DOWN:
                return ropeDown();
            case LADDER_UP:
                return ladderUp();
            case ROPE_UP:
                return ropeUp();
            default:
                return new PatrolStep(t, parseColor(s.substring(colon + 1)));
        }
    }

    /** Encode a whole loop as {@code step;step;...} (empty list = empty string). */
    public static String encodeList(List<PatrolStep> steps) {
        StringBuilder sb = new StringBuilder();
        for (PatrolStep s : steps) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(s.encode());
        }
        return sb.toString();
    }

    /** Parse an encoded loop; bad steps are skipped (logged), never thrown. */
    public static List<PatrolStep> decodeList(String text) {
        List<PatrolStep> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        for (String part : text.split(";")) {
            if (part.trim().isEmpty()) {
                continue;
            }
            try {
                out.add(decode(part));
            } catch (RuntimeException ex) {
                System.out.println("Pomijam zly krok petli: " + part + " (" + ex.getMessage() + ")");
            }
        }
        return out;
    }

    private static Color parseColor(String rgb) {
        String[] p = rgb.split(",");
        return new Color(Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim()),
                Integer.parseInt(p[2].trim()));
    }
}
