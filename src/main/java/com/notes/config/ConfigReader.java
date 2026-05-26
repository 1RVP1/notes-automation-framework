package com.notes.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static Properties properties;

    static {
        try {
            properties = new Properties();
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            properties.load(fis);
            log.info("Config properties loaded successfully.");
        } catch (IOException e) {
            log.error("Failed to load config.properties: " + e.getMessage());
            throw new RuntimeException("config.properties not found", e);
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            value = value.trim().replace("\uFEFF", "");
        }
        return value;
    }

    public static String getBaseUrl()       { return get("base.url"); }
    public static String getApiBaseUrl()    { return get("api.base.url"); }
    public static String getEmail()         { return get("test.email"); }
    public static String getPassword()      { return get("test.password"); }
    public static String getBrowser()       { return get("browser"); }
    public static boolean isHeadless()      { return Boolean.parseBoolean(get("headless")); }
    public static int getImplicitWait()     { return Integer.parseInt(get("implicit.wait")); }
    public static int getExplicitWait()     { return Integer.parseInt(get("explicit.wait")); }
    public static int getPageLoadTimeout()  { return Integer.parseInt(get("page.load.timeout")); }
    public static long getApiResponseLimit(){ return Long.parseLong(get("api.response.time.limit")); }
}
