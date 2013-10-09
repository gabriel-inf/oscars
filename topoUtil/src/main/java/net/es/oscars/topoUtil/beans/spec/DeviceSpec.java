package net.es.oscars.topoUtil.beans.spec;


import java.util.ArrayList;

public class DeviceSpec {

    protected String name;
    protected String loopback;
    protected String model;
    protected ArrayList<IfceSpec> ifces = new ArrayList<IfceSpec>();

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

    public ArrayList<IfceSpec> getIfces() {
        return ifces;
    }

    public void setIfces(ArrayList<IfceSpec> ifces) {
        this.ifces = ifces;
    }
}
