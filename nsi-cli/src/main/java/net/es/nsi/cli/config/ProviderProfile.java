package net.es.nsi.cli.config;


import javax.persistence.*;

@Entity
public class ProviderProfile {
    protected Long id;
    protected String name;
    protected ProviderServer providerServer;
    protected String providerNSA;
    protected String protocolVersion;
    protected String serviceType;

    public String toString() {

        String out = "\n";
        out += "\n  name:           "+name;
        out += "\n  serviceType:    "+serviceType;
        out += "\n  providerNSA:    "+providerNSA;
        out += "\n  protocolV:      "+protocolVersion;
        if (providerServer != null) {
            out += "\n  server:"+providerServer.toString();
        } else {
            out += "\n  server: null\n";
        }
        return out;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @OneToOne(cascade = CascadeType.ALL)
    public ProviderServer getProviderServer() {
        return providerServer;
    }

    public void setProviderServer(ProviderServer providerServer) {
        this.providerServer = providerServer;
    }

    public String getProviderNSA() {
        return providerNSA;
    }

    public void setProviderNSA(String providerNSA) {
        this.providerNSA = providerNSA;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}