package net.es.oscars.nsibridge.common;




import net.es.oscars.nsibridge.beans.config.OscarsConfig;
import net.es.oscars.nsibridge.beans.config.JettyConfig;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;


public class ConfigManager {

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private ConfigManager() {
    }


    public JettyConfig getJettyConfig(String filename) throws ConfigException {
        JettyConfig config = ConfigHelper.getConfiguration(filename, JettyConfig.class);
        return config;

    }


    public OscarsConfig getCoordConfig(String filename) throws ConfigException {
        OscarsConfig config = ConfigHelper.getConfiguration(filename, OscarsConfig.class);
        return config;

    }



}
