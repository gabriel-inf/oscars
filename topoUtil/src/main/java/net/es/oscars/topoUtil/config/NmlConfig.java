package net.es.oscars.topoUtil.config;

import java.util.ArrayList;

public class NmlConfig {
    protected String nsa;
    protected String serviceId;
    protected String serviceLink;
    protected String topologyId;
    protected String topologyName;
    protected String topologyPrefix;

    protected ArrayList<String> peers = new ArrayList<String>();
    protected String outputFile;
    protected String locationId;
    protected String latitude;
    protected String longitude;

    public String getNsa() {
        return nsa;
    }

    public void setNsa(String nsa) {
        this.nsa = nsa;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceLink() {
        return serviceLink;
    }

    public void setServiceLink(String serviceLink) {
        this.serviceLink = serviceLink;
    }

    public String getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(String topologyId) {
        this.topologyId = topologyId;
    }

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public String getTopologyPrefix() {
        return topologyPrefix;
    }

    public void setTopologyPrefix(String topologyPrefix) {
        this.topologyPrefix = topologyPrefix;
    }

    public ArrayList<String> getPeers() {
        return peers;
    }

    public void setPeers(ArrayList<String> peers) {
        this.peers = peers;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
