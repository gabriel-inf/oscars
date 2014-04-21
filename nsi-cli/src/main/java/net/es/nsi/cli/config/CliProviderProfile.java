package net.es.nsi.cli.config;

import net.es.nsi.client.config.ProviderProfile;

import javax.persistence.*;


@Entity
public class CliProviderProfile extends ProviderProfile {
    protected Long id;
    protected CliProviderServer providerServer;

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


    @OneToOne(cascade = CascadeType.ALL)
    public CliProviderServer getProviderServer() {
        return providerServer;
    }

    public void setProviderServer(CliProviderServer providerServer) {
        this.providerServer = providerServer;
    }
}
