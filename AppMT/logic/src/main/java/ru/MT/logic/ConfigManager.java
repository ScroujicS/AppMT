package ru.MT.logic;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private Properties properties;

    public ConfigManager() {
        properties = new Properties();
        File configFile =  new File(System.getProperty("user.home"), "config.properties");

        try {
            if (configFile.exists()){
                try (InputStream input = new FileInputStream(configFile)){
                    properties.load(input);
                }
            }else{
                //Загружаем дефолтные настройки из JAR
                try (InputStream input = ApplicationMintrance.class.getClassLoader().getResourceAsStream("config.properties")) {
                    if (input != null) {
                        properties.load(input);
                    } else {
                        System.err.println("Default config file not found in JAR.");
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getKey() {
        return properties.getProperty("apiKey");
    }
    public String getMode() {
        return properties.getProperty("mode");
    }
    public boolean getAvoidTolls() {
        return Boolean.parseBoolean(properties.getProperty("avoid_tolls"));
    }

    public void setKey(String key) {
        properties.setProperty("apiKey", key);
        File configFile = new File(System.getProperty("user.home"), "config.properties");
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void setMode(String mode) {
        properties.setProperty("mode", mode);
        File configFile = new File(System.getProperty("user.home"), "config.properties");
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setAvoidTolls(Boolean avoidTolls) {
        properties.setProperty("avoidTolls", String.valueOf(avoidTolls));
        File configFile = new File(System.getProperty("user.home"), "config.properties");
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
