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
 *
 * <p><b>Input log convention.</b> Every physical input prints through
 * {@link #click}/{@link #key} so the wording is uniform and greppable:
 * <pre>
 *   MOUSE [L] CLICKED (x,y) &lt;ctx&gt;     mouse  (L / R / L+CTRL …)
 *   KEY [SPACE] PRESSED &lt;ctx&gt;           keyboard
 * </pre>
 * Shape is {@code <DEVICE> [<NAME>] <VERB> <details>}; {@code <ctx>} is an
 * optional reason ("walk p17 dist=42", "eat", "potion"). Add new inputs via
 * these two helpers — never hand-format an {@code IN} line — so the convention
 * stays in one place.
 */
public final class Log {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private Log() {
    }

    /** Important state change (action / waypoint / heal / loot). */
    public static void action(String fmt, Object... args) {
        System.out.println(now() + " | " + String.format(fmt, args));
    }

    /** A physical input driven on screen (key or mouse). Always shown.
     *  Prefer {@link #click}/{@link #key}; use this only for one-off inputs that
     *  do not fit the standard shape. */
    public static void input(String fmt, Object... args) {
        System.out.println(now()+ "  "  + String.format(fmt, args));
    }

    /** A mouse click in the standard shape: {@code MOUSE [<button>] CLICKED
     *  (x,y) <ctx>}. {@code button} is the label (L, R, L+CTRL …); {@code ctx}
     *  may be empty. The {@code MOUSE} tag (vs {@code KEY}) marks the device. */
    public static void click(String button, int x, int y, String ctx) {
        String tail = (ctx == null || ctx.isEmpty()) ? "" : " " + ctx;
        input("MOUSE [%s] CLICKED (%d,%d)%s", button, x, y, tail);
    }

    /** A key press in the standard shape: {@code KEY [<name>] PRESSED <ctx>}.
     *  {@code ctx} (the reason: "eat", "potion", "attack") may be empty. */
    public static void key(String name, String ctx) {
        String tail = (ctx == null || ctx.isEmpty()) ? "" : " " + ctx;
        input("KEY [%s] PRESSED%s", name, tail);
    }

    private static String now() {
        return LocalTime.now().format(TIME);
    }
}
