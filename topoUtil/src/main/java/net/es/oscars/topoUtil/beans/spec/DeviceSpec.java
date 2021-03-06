package net.es.oscars.topoUtil.beans.spec;


import java.util.ArrayList;

public class DeviceSpec {

    protected String name;
    protected String loopback;
    protected String model;
    protected ArrayList<PortSpec> ports = new ArrayList<PortSpec>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoopback() {
        return loopback;
    }

    public void setLoopback(String loopback) {
        this.loopback = loopback;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public ArrayList<PortSpec> getPorts() {
        return ports;
    }

    public void setPorts(ArrayList<PortSpec> ports) {
        this.ports = ports;
    }
}
