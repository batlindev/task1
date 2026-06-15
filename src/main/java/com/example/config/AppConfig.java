package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads non-source configuration (secrets such as the Telegram token).
 *
 * Lookup order:
 *   1. {@code ./config.properties} in the working directory (lets you change
 *      secrets without rebuilding, and keeps them out of the repo).
 *   2. {@code config.properties} on the classpath.
 *   3. {@code config.properties.example} on the classpath (placeholder fallback).
 *
 * Nothing here is hard-coded, so no secret lives in the Java source anymore.
 */
public final class AppConfig {

    private static final Properties PROPS = load();

    private AppConfig() {
    }

    private static Properties load() {
        Properties props = new Properties();

        // 1. External file next to the app (highest priority).
        Path external = Path.of("config.properties");
        if (Files.isReadable(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                System.out.println("Could not read ./config.properties: " + e.getMessage());
            }
        }

        // 2./3. Classpath resource, real file first then the example placeholder.
        for (String resource : new String[] { "config.properties", "config.properties.example" }) {
            try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resource)) {
                if (in != null) {
                    props.load(in);
                    return props;
                }
            } catch (IOException e) {
                System.out.println("Could not read classpath " + resource + ": " + e.getMessage());
            }
        }

        System.out.println("No config.properties found; Telegram fields will start empty.");
        return props;
    }

    public static String telegramToken() {
        return PROPS.getProperty("telegram.token", "").trim();
    }

    public static String telegramChatId() {
        return PROPS.getProperty("telegram.chatId", "").trim();
    }
}
