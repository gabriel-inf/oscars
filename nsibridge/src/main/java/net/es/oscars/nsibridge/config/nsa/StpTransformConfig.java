package net.es.oscars.nsibridge.config.nsa;

public class StpTransformConfig {
    private String match;
    private String replace;
    public StpTransformConfig() {

    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }
}
