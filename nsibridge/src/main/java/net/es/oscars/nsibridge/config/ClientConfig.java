package net.es.oscars.nsibridge.config;

import org.apache.log4j.Logger;

public class ClientConfig implements net.es.nsi.lib.client.config.ClientConfig {
    private String busConfigPath;
    private static final Logger log = Logger.getLogger(ClientConfig.class);

    @Override
    public String getBusConfigPath() {
        return busConfigPath;
    }
    @Override
    public void setBusConfigPath(String path) {
        log.info("Setting client bus config path to "+path);
        this.busConfigPath = path;
    }

}
