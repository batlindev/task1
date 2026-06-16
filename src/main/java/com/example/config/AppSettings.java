package com.example.config;

import java.util.LinkedHashMap;
import java.util.Map;

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

    /** Current value for a key: live field text if bound, else cached (null if unknown). */
    public String get(String key) {
        JTextField f = live.get(key);
        return f != null ? f.getText() : cached.get(key);
    }

    /** Snapshot every known key (live fields + cached) for saving a preset. */
    public Map<String, String> snapshot() {
        Map<String, String> out = new LinkedHashMap<>(cached);
        for (Map.Entry<String, JTextField> e : live.entrySet()) {
            out.put(e.getKey(), e.getValue().getText());
        }
        return out;
    }

    /** Apply loaded preset values: update any live fields, cache the rest. */
    public void apply(Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet()) {
            cached.put(e.getKey(), e.getValue());
            JTextField f = live.get(e.getKey());
            if (f != null) {
                f.setText(e.getValue());
            }
        }
    }
}
