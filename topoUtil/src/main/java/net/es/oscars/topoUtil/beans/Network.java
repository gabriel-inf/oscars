package net.es.oscars.topoUtil.beans;


import java.util.ArrayList;

public class Network {
    protected ArrayList<Device> devices = new ArrayList<Device>();

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

}
