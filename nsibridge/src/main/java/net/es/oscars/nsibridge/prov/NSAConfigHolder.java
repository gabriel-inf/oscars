package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.StpConfig;

public class NSAConfigHolder {
    private NSAConfigHolder() {}
    private static NSAConfigHolder instance;
    public static NSAConfigHolder getInstance() {
        if (instance == null) instance = new NSAConfigHolder();
        return instance;
    }

    private NsaConfig nsaConfig;
    private StpConfig[] stpConfigs;

    public NsaConfig getNsaConfig() {
        return nsaConfig;
    }

    public void setNsaConfig(NsaConfig nsaConfig) {
        this.nsaConfig = nsaConfig;
    }


}
