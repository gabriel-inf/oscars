package net.es.oscars.nsibridge.config.nsa;


import java.util.ArrayList;
import java.util.List;

public class NsaConfig {
    private String nsaId = "";
    private String protocolVersion = "";
    private String serviceType = "";
    private String networkId = "";
    private List<StpTransformConfig> stpTransforms = new ArrayList<StpTransformConfig>();

    private List<StpConfig> stps = new ArrayList<StpConfig>();

    public NsaConfig() {

    };

    public List<StpTransformConfig> getStpTransforms() {
        return stpTransforms;
    }

    public void setStpTransforms(List<StpTransformConfig> stpTransforms) {
        this.stpTransforms = stpTransforms;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

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
