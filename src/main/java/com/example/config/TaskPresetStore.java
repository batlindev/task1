package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Disk-backed store for Task panel presets.
 *
 * Each preset is a single {@code .properties} file inside {@code ./task-presets/}
 * (relative to the working directory, which {@code run.sh} pins to the project
 * root). The filename, minus the {@code .properties} suffix, is the preset name.
 * Values are the raw text of every input field, so presets survive across
 * close/reopen with no rebuild.
 */
public final class TaskPresetStore {

    private static final String EXT = ".properties";

    private final Path dir;

    public TaskPresetStore() {
        this(Path.of("task-presets"));
    }

    public TaskPresetStore(Path dir) {
        this.dir = dir;
    }

    /** Preset names currently on disk, sorted alphabetically. */
    public List<String> list() {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return names;
        }
        TreeSet<String> sorted = new TreeSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*" + EXT)) {
            for (Path p : stream) {
                String file = p.getFileName().toString();
                sorted.add(file.substring(0, file.length() - EXT.length()));
            }
        } catch (IOException e) {
            System.out.println("Could not list presets: " + e.getMessage());
        }
        names.addAll(sorted);
        return names;
    }

    /** Write (overwrite) a preset with the given field values. */
    public void save(String name, Map<String, String> values) throws IOException {
        Files.createDirectories(dir);
        Properties props = new Properties();
        for (Map.Entry<String, String> e : values.entrySet()) {
            props.setProperty(e.getKey(), e.getValue() == null ? "" : e.getValue());
        }
        try (OutputStream out = Files.newOutputStream(fileFor(name))) {
            props.store(out, "Task preset: " + name);
        }
    }

    /** Read a preset's field values; empty map if the preset is missing. */
    public Map<String, String> load(String name) {
        Map<String, String> values = new LinkedHashMap<>();
        Path file = fileFor(name);
        if (!Files.isReadable(file)) {
            return values;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("Could not read preset " + name + ": " + e.getMessage());
            return values;
        }
        for (String key : props.stringPropertyNames()) {
            values.put(key, props.getProperty(key));
        }
        return values;
    }

    /** Delete a preset; no-op if it does not exist. */
    public void delete(String name) {
        try {
            Files.deleteIfExists(fileFor(name));
        } catch (IOException e) {
            System.out.println("Could not delete preset " + name + ": " + e.getMessage());
        }
    }

    private Path fileFor(String name) {
        return dir.resolve(sanitize(name) + EXT);
    }

    /** Keep only safe filename characters so a name can never escape the dir. */
    private static String sanitize(String name) {
        String cleaned = name.trim().replaceAll("[^a-zA-Z0-9 _.-]", "_");
        return cleaned.isEmpty() ? "preset" : cleaned;
    }
}
