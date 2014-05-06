package net.es.nsi.cli.config;

import net.es.nsi.client.types.RequesterProfile;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class CliRequesterProfile implements RequesterProfile {
    protected Long id;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    protected String name;
    protected String requesterId;
    protected String url;
    protected String busConfig;

    public String toString() {

        String out = "\n";
        out += "\n  name:         "+name;
        out += "\n  url:          "+url;
        out += "\n  requesterId:  "+requesterId;
        out += "\n  busConfig:    "+busConfig;
        return out;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBusConfig() {
        return busConfig;
    }

    public void setBusConfig(String busConfig) {
        this.busConfig = busConfig;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

}
