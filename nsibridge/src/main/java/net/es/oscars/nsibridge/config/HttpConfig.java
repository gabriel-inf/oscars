package net.es.oscars.nsibridge.config;


public class HttpConfig {
    protected String url;
    protected String bus;
    protected String sslBus;
    protected String trustedSSLProxy;

    public String getTrustedSSLProxy() {
        return trustedSSLProxy;
    }

    public void setTrustedSSLProxy(String trustedSSLProxy) {
        this.trustedSSLProxy = trustedSSLProxy;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }

    public String getSslBus() {
        return sslBus;
    }

    public void setSslBus(String sslBus) {
        this.sslBus = sslBus;
    }
}
