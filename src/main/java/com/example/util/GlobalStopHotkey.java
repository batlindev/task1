package com.example.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

/**
 * System-wide STOP trigger backed by JNativeHook — the one external dependency
 * (see CLAUDE.md "External dependency exception").
 *
 * Pure Java Swing input bindings only fire while the bot window is focused, but
 * while botting the game window holds focus, so a Swing shortcut never fires.
 * JNativeHook installs an OS-level input hook that catches the event no matter
 * which window is active.
 *
 * The trigger is the <b>middle mouse button</b> (wheel click): the hand is
 * already on the mouse when the cursor goes haywire, and the bot itself only
 * uses left/right clicks, so its own actions can never trip the panic stop.
 * The hook is installed once for the whole app session; a middle click runs
 * {@code onStop} on the EDT — harmless when no bot is running.
 */
public final class GlobalStopHotkey {

    /** jnativehook: BUTTON1 = left, BUTTON2 = right, BUTTON3 = middle/wheel. */
    private static final int STOP_BUTTON = NativeMouseEvent.BUTTON3;

    private static boolean installed = false;

    private GlobalStopHotkey() {
    }

    /** Install the global hook once. Subsequent calls add no extra listeners. */
    public static synchronized void install(Runnable onStop) {
        if (installed) {
            return;
        }

        // JNativeHook logs every event at INFO by default — silence it.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.out.println("[GlobalStopHotkey] Failed to register global hook: "
                    + ex.getMessage());
            return;
        }

        GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
            @Override
            public void nativeMousePressed(NativeMouseEvent e) {
                if (e.getButton() == STOP_BUTTON) {
                    System.out.println("[GlobalStopHotkey] Middle mouse button, STOP.");
                    SwingUtilities.invokeLater(onStop);
                }
            }
        });

        // The hook runs on a non-daemon thread; release it on JVM exit so the
        // app can close cleanly.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ignored) {
                // Shutting down anyway.
            }
        }, "GlobalStopHotkey-shutdown"));

        installed = true;
        System.out.println("[GlobalStopHotkey] Global STOP: middle mouse button "
                + "(works from any window).");
    }
}
