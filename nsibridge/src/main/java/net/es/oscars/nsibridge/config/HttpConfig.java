package net.es.oscars.nsibridge.config;


public class HttpConfig {
    protected String url;
    protected String proxyUrl;

    protected String bus;
    protected String sslBus;
    protected String trustedSSLProxy;
    protected boolean basicAuth;

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

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
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

    public boolean isBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(boolean basicAuth) {
        this.basicAuth = basicAuth;
    }
}
