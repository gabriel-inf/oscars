package net.es.oscars.nsibridge.config;


import net.es.nsi.lib.client.config.ClientConfig;

import java.util.HashMap;
import java.util.Map;

public class RequestersConfig {
    private HashMap<String, net.es.nsi.lib.client.config.ClientConfig> requesters;

    public Map<String, ClientConfig> getRequesters() {
        return requesters;
    }

    public void setRequesters(Map<String, ClientConfig> requesters) {
        this.requesters = new HashMap<String, ClientConfig>();
        this.requesters.putAll(requesters);
    }
    public ClientConfig getClientConfig(String requesterUrl) {

        if (this.requesters.get(requesterUrl) != null) {
            return this.requesters.get(requesterUrl);
        }
        if (requesterUrl.startsWith("https")) {
            return this.requesters.get("https");
        } else if (requesterUrl.startsWith("http")) {
            return this.requesters.get("http");
        }
        return null;
    }
}
