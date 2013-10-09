package net.es.oscars.topoUtil.beans.spec;

import java.util.ArrayList;

public class NetworkSpec {
    private ArrayList<DeviceSpec> devices;
    private String idcId;
    private String domainId;
    private String topologyId;


    public ArrayList<DeviceSpec> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<DeviceSpec> devices) {
        this.devices = devices;
    }

    public String getIdcId() {
        return idcId;
    }

    public void setIdcId(String idcId) {
        this.idcId = idcId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(String topologyId) {
        this.topologyId = topologyId;
    }
}
