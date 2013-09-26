package net.es.nsi.cli.config;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RequesterProfile {
    protected String name;
    protected String requesterId;
    protected String url;
    protected String busConfig;
    protected Long id;

    public String toString() {

        String out = "\n";
        out += "\n  name:         "+name;
        out += "\n  url:          "+url;
        out += "\n  requesterId:  "+requesterId;
        out += "\n  busConfig:    "+busConfig;
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
