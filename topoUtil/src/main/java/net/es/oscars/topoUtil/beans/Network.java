package net.es.oscars.topoUtil.beans;

import net.es.oscars.topoUtil.beans.spec.DeviceSpec;

import java.util.ArrayList;

public class Network {
    protected ArrayList<DeviceSpec> deviceSpecs = new ArrayList<DeviceSpec>();

    public ArrayList<DeviceSpec> getDeviceSpecs() {
        return deviceSpecs;
    }

    public void setDeviceSpecs(ArrayList<DeviceSpec> deviceSpecs) {
        this.deviceSpecs = deviceSpecs;
    }

    public DeviceSpec findDeviceByName(String name) {
        for (DeviceSpec deviceSpec : deviceSpecs) {
            if (deviceSpec.getName().equals(name)) {
                return deviceSpec;
            }

        }
        return null;
    }
}
