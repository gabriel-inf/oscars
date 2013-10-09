package net.es.oscars.topoUtil.beans.viz;

import java.util.ArrayList;

public class VizRoot {
    protected ArrayList<VizDevice> devices = new ArrayList<VizDevice>();

    public ArrayList<VizDevice> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<VizDevice> devices) {
        this.devices = devices;
    }
}
