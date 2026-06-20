package com.example.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Tiny terminal logger with a fixed layout: the time is ALWAYS on the left,
 * followed by the message. Two channels keep the terminal readable:
 *
 * <ul>
 *   <li>{@link #action} — only the things worth seeing: waypoint/action changes,
 *       heal, loot, point advance. Use this sparingly.</li>
 *   <li>{@link #input} — every physical input the bot drives (key press, mouse
 *       click). ALWAYS printed, tagged {@code IN}, so you can see exactly what
 *       the bot pressed on screen.</li>
 * </ul>
 */
public final class Log {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private Log() {
    }

    /** Important state change (action / waypoint / heal / loot). */
    public static void action(String fmt, Object... args) {
        System.out.println(now() + " | " + String.format(fmt, args));
    }

    /** A physical input driven on screen (key or mouse). Always shown. */
    public static void input(String fmt, Object... args) {
        System.out.println(now() + " | IN  " + String.format(fmt, args));
    }

    private static String now() {
        return LocalTime.now().format(TIME);
    }
}
