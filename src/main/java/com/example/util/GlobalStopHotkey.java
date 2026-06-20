package com.example.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

/**
 * System-wide STOP hotkey backed by JNativeHook — the one external dependency
 * (see CLAUDE.md "External dependency exception").
 *
 * Pure Java Swing key bindings only fire while the bot window is focused, but
 * while botting the game window holds focus, so a Swing shortcut never fires.
 * JNativeHook installs an OS-level keyboard hook that catches the key no matter
 * which window is active, and never touches the keyboard output, so it cannot
 * conflict with the bot pressing its own keys.
 *
 * The hook is installed once for the whole app session. Pressing the STOP key
 * runs {@code onStop} on the EDT — harmless when no bot is running.
 */
public final class GlobalStopHotkey {

    /** Default STOP key: F12. */
    public static final int STOP_KEY = NativeKeyEvent.VC_F12;

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
            System.out.println("[GlobalStopHotkey] Nie udalo sie zarejestrowac globalnego skrotu: "
                    + ex.getMessage());
            return;
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == STOP_KEY) {
                    System.out.println("[GlobalStopHotkey] F12 wcisniety, STOP.");
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
        System.out.println("[GlobalStopHotkey] Globalny STOP: F12 (dziala z dowolnego okna).");
    }
}
