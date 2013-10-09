package net.es.oscars.topoUtil.beans;


import java.util.ArrayList;

public class Network {
    protected ArrayList<Device> devices = new ArrayList<Device>();
    private String idcId;
    private String domainId;
    private String topologyId;

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<Device> devices) {
        this.devices = devices;
    }

    public Device findDeviceByName(String name) {
        for (Device device : devices) {
            if (device.getName().equals(name)) {
                return device;
            }

        }
        return null;
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
