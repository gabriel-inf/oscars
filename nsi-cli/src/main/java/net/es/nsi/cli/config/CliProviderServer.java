package net.es.nsi.cli.config;

import net.es.nsi.client.config.ProviderServer;

import javax.persistence.*;

@Entity
public class CliProviderServer extends ProviderServer {
    protected Long id;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
