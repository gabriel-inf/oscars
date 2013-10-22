package net.es.nsi.cli.config;


public class ClientConfig {
    protected String bus;
    protected String sslBus;

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
