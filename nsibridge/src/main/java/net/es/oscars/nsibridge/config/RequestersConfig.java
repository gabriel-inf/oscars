package net.es.oscars.nsibridge.config;


import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class RequestersConfig {
    private static final Logger log = Logger.getLogger(RequestersConfig.class);
    /*
    private static RequestersConfig instance;

    private RequestersConfig() {

    }

    public static RequestersConfig getInstance() {
        if (instance == null) {
            instance = new RequestersConfig();
        }
        return instance;
    }
    */


    private HashMap<String, String> requesters = new HashMap<String, String>();

    public Map<String, String> getRequesters() {
        return requesters;
    }

    public void setRequesters(Map<String, String> requesters) {
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.putAll(requesters);
        for (String key : requesters.keySet()) {
            log.debug(key+" "+requesters.get(key));
        }
        this.requesters = temp;
    }

    public ClientConfig getClientConfig(String url) {
        ClientConfig cfg = new ClientConfig();
        cfg.setBusConfigPath(requesters.get(url));
        return cfg;

    }

}
