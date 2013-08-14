package net.es.oscars.nsibridge.config.nsa;


import java.util.ArrayList;
import java.util.List;

public class NsaConfig {
    private String nsaId = "";
    private List<StpConfig> stps = new ArrayList<StpConfig>();

    public NsaConfig() {

    };

    public String getNsaId() {
        return nsaId;
    }

    public void setNsaId(String nsaId) {
        this.nsaId = nsaId;
    }

    public List<StpConfig> getStps() {
        return stps;
    }

    public void setStps(List<StpConfig> stps) {
        this.stps = stps;
    }
}
