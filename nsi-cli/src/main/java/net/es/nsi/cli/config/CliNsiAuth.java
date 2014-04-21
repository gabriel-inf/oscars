package net.es.nsi.cli.config;

import net.es.nsi.client.types.NsiAuth;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class CliNsiAuth extends NsiAuth {
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
