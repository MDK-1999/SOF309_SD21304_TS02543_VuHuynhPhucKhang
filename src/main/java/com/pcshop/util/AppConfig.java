package com.pcshop.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties EXTERNAL = loadExternalProperties();

    private AppConfig() {}

    public static String get(String environmentName, String propertyName) {
        return get(environmentName, propertyName, "");
    }

    public static String get(String environmentName, String propertyName, String fallback) {
        String value = trim(System.getenv(environmentName));
        if (!value.isEmpty()) return value;
        value = trim(System.getProperty(environmentName));
        if (!value.isEmpty()) return value;
        value = trim(System.getProperty(propertyName));
        if (!value.isEmpty()) return value;
        value = trim(EXTERNAL.getProperty(environmentName));
        if (!value.isEmpty()) return value;
        value = trim(EXTERNAL.getProperty(propertyName));
        return value.isEmpty() ? fallback : value;
    }

    private static Properties loadExternalProperties() {
        Properties properties = new Properties();
        Path path = resolveConfigPath();
        if (!Files.isRegularFile(path)) return properties;
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (IOException error) {
            LOGGER.log(Level.WARNING, "Unable to read external GearAstral configuration: " + path, error);
        }
        return properties;
    }

    private static Path resolveConfigPath() {
        String configured = trim(System.getenv("PCSHOP_CONFIG_FILE"));
        if (configured.isEmpty()) configured = trim(System.getProperty("pcshop.config.file"));
        if (!configured.isEmpty()) return Path.of(configured).toAbsolutePath().normalize();
        return Path.of(System.getProperty("user.home"), ".pcshop", "pcshop.properties")
                .toAbsolutePath().normalize();
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
