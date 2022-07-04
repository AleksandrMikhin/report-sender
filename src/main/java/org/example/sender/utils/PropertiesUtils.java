package org.example.sender.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesUtils {
    final static private Properties properties;

    static {
        try (InputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties = new Properties();
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Something went wrong", e);
        }
    }

    public static String getProperty(final String key) {
        return properties.getProperty(key);
    }
}
