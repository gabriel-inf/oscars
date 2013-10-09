package net.es.oscars.topoUtil.beans;


import java.util.ArrayList;

public class Device {
    protected String name;
    protected String loopback;
    protected String model;
    protected ArrayList<Port> ports = new ArrayList<Port>();

    public ArrayList<Port> getPorts() {
        return ports;
    }

    public void setPorts(ArrayList<Port> ports) {
        this.ports = ports;
    }

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
}
