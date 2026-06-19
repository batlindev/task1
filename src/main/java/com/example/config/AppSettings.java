package com.example.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JTextField;

/**
 * Shared settings registry backing the single preset bar in Bot Control.
 *
 * Both Bot Control and Task Control bind their input fields here under stable
 * keys, so one preset round-trips every field across both windows — even while
 * the Task window is closed, its last values stay in {@code cached}.
 */
public final class AppSettings {

    private final Map<String, JTextField> live = new LinkedHashMap<>();
    private final Map<String, String> cached = new LinkedHashMap<>();

    // Non-textfield state (e.g. the TASK2 loop) that still round-trips a preset.
    // Each key carries a getter (for snapshot) and setter (for apply/seed).
    private final Map<String, Supplier<String>> liveGetters = new LinkedHashMap<>();
    private final Map<String, Consumer<String>> liveSetters = new LinkedHashMap<>();

    /** Bind a field under a key: seed it from any cached value, then track it live. */
    public void bind(String key, JTextField field) {
        if (cached.containsKey(key)) {
            field.setText(cached.get(key));
        }
        live.put(key, field);
    }

    /** Stop tracking a window's fields (on close), keeping their last text in cache. */
    public void unbind(Iterable<String> keys) {
        for (String key : keys) {
            JTextField f = live.remove(key);
            if (f != null) {
                cached.put(key, f.getText());
            }
        }
    }

    /**
     * Bind arbitrary string state under a key: seed it from any cached value via
     * {@code setter}, then track it live through {@code getter}. Use for state
     * that is not a text field (e.g. the TASK2 generator's loop step list).
     */
    public void bindState(String key, Supplier<String> getter, Consumer<String> setter) {
        if (cached.containsKey(key)) {
            setter.accept(cached.get(key));
        }
        liveGetters.put(key, getter);
        liveSetters.put(key, setter);
    }

    /** Stop tracking bound state (on close), keeping its last value in cache. */
    public void unbindState(Iterable<String> keys) {
        for (String key : keys) {
            Supplier<String> g = liveGetters.remove(key);
            liveSetters.remove(key);
            if (g != null) {
                cached.put(key, g.get());
            }
        }
    }

    /** Current value for a key: live field text if bound, else cached (null if unknown). */
    public String get(String key) {
        JTextField f = live.get(key);
        if (f != null) {
            return f.getText();
        }
        Supplier<String> g = liveGetters.get(key);
        return g != null ? g.get() : cached.get(key);
    }

    /** Snapshot every known key (live fields + bound state + cached) for saving a preset. */
    public Map<String, String> snapshot() {
        Map<String, String> out = new LinkedHashMap<>(cached);
        for (Map.Entry<String, JTextField> e : live.entrySet()) {
            out.put(e.getKey(), e.getValue().getText());
        }
        for (Map.Entry<String, Supplier<String>> e : liveGetters.entrySet()) {
            out.put(e.getKey(), e.getValue().get());
        }
        return out;
    }

    /** Apply loaded preset values: update any live fields/state, cache the rest. */
    public void apply(Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet()) {
            cached.put(e.getKey(), e.getValue());
            JTextField f = live.get(e.getKey());
            if (f != null) {
                f.setText(e.getValue());
            }
            Consumer<String> setter = liveSetters.get(e.getKey());
            if (setter != null) {
                setter.accept(e.getValue());
            }
        }
    }
}
