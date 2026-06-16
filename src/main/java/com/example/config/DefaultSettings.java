package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Default field values for the Bot Control / Task Control inputs.
 *
 * Kept out of the Java source so the panels start empty-of-magic-numbers and the
 * defaults can be tweaked without a rebuild.
 *
 * Lookup order:
 *   1. {@code ./defaults.properties} in the working directory (highest priority).
 *   2. {@code defaults.properties} on the classpath (shipped fallback).
 *
 * Any unknown key returns {@code ""} so a missing entry just yields a blank field.
 */
public final class DefaultSettings {

    private static final Properties PROPS = load();

    private DefaultSettings() {
    }

    private static Properties load() {
        Properties props = new Properties();

        Path external = Path.of("defaults.properties");
        if (Files.isReadable(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                System.out.println("Could not read ./defaults.properties: " + e.getMessage());
            }
        }

        try (InputStream in = DefaultSettings.class.getClassLoader().getResourceAsStream("defaults.properties")) {
            if (in != null) {
                props.load(in);
                return props;
            }
        } catch (IOException e) {
            System.out.println("Could not read classpath defaults.properties: " + e.getMessage());
        }

        System.out.println("No defaults.properties found; fields will start empty.");
        return props;
    }

    /** Default text for a field key, or {@code ""} if undefined. */
    public static String get(String key) {
        return PROPS.getProperty(key, "");
    }
}
