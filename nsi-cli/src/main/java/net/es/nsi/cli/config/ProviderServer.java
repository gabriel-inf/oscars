package net.es.nsi.cli.config;

import javax.persistence.*;

@Entity
public class ProviderServer {
    protected Long id;
    protected String url;
    protected NsiAuth auth;
    protected String busConfig;

    public String toString() {

        String out = "";
        out += "\n      url:        "+url;
        out += "\n      busConfig:  "+busConfig;
        if (auth != null) {
            out += "\n      auth:"+auth.toString();
        } else {
            out += "\n      auth: null\n";
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @OneToOne(cascade = CascadeType.ALL)
    public NsiAuth getAuth() {
        return auth;
    }

    public void setAuth(NsiAuth auth) {
        this.auth = auth;
    }

    public String getBusConfig() {
        return busConfig;
    }

    public void setBusConfig(String busConfig) {
        this.busConfig = busConfig;
    }
}
