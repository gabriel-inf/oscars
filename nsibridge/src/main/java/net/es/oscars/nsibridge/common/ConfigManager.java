package net.es.oscars.nsibridge.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.ho.yaml.Yaml;


public class ConfigManager {
    private HashMap<String, Map<String, Object>> configurations = new HashMap<String, Map<String, Object>>();

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private ConfigManager() {
    }

    public Map<String, Object> getConfiguration() {
        Map<String, Object> configuration = getConfiguration("config.yaml");
        return configuration;
    }

    @SuppressWarnings({ "static-access", "unchecked" })
    public Map<String, Object> getConfiguration(String filename) {
        Map<String, Object> configuration = configurations.get(filename);
        if (configuration == null) {
            InputStream propFile = this.getClass().getClassLoader().getSystemResourceAsStream(filename);
            try {
                configuration = (Map<String, Object>) Yaml.load(propFile);
            } catch (NullPointerException ex) {
                try {
                    propFile = new FileInputStream(new File("config/"+filename));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                configuration = (Map<String, Object>) Yaml.load(propFile);
            }
        }
        configurations.put(filename, configuration);
        return configuration;
    }
}
