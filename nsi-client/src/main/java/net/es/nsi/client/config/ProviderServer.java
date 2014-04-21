package net.es.nsi.client.config;

import net.es.nsi.client.types.NsiAuth;

public class ProviderServer {
    protected String url;
    protected NsiAuth auth;
    protected String busConfig;

    public String toString() {

        String out = "";
        out += "\n      url:        "+url;
        out += "\n      busConfig:  "+busConfig;
        if (auth != null) {
            out += "\n      auth:"+auth.toString();
        } else {
            out += "\n      auth: null\n";
        }
        return out;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public NsiAuth getAuth() {
        return auth;
    }

    public void setAuth(NsiAuth auth) {
        this.auth = auth;
    }

    public String getBusConfig() {
        return busConfig;
    }

    public void setBusConfig(String busConfig) {
        this.busConfig = busConfig;
    }
}
