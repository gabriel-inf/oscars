package net.es.nsi.cli.config;

import net.es.nsi.client.types.NsiWebService;

import javax.persistence.*;

@Entity
public class CliProviderServer implements NsiWebService {
    protected Long id;
    protected String url;
    protected CliNsiAuth auth;
    protected String busConfig;
    protected String name;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


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

    @OneToOne(cascade = CascadeType.ALL)
    public CliNsiAuth getAuth() {
        return auth;
    }

    public void setAuth(CliNsiAuth auth) {
        this.auth = auth;
    }

    public String getBusConfig() {
        return busConfig;
    }

    public void setBusConfig(String busConfig) {
        this.busConfig = busConfig;
    }

}
