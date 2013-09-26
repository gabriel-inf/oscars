package net.es.nsi.cli.config;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DefaultProfiles {
    protected Long id;
    protected String resvProfileName;
    protected String provProfileName;
    protected String requesterProfileName;

    public String toString() {

        String out = "\n";
        out += "\n  resv:    "+resvProfileName;
        out += "\n  prov:    "+provProfileName;
        out += "\n  req:     "+requesterProfileName;
        return out;
    }

    public String getResvProfileName() {
        return resvProfileName;
    }

    public void setResvProfileName(String resvProfileName) {
        this.resvProfileName = resvProfileName;
    }

    public String getProvProfileName() {
        return provProfileName;
    }

    public void setProvProfileName(String provProfileName) {
        this.provProfileName = provProfileName;
    }

    public String getRequesterProfileName() {
        return requesterProfileName;
    }

    public void setRequesterProfileName(String requesterProfileName) {
        this.requesterProfileName = requesterProfileName;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
