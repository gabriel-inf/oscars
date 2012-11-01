package net.es.oscars.nsibridge.beans;


import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.DetailedPathType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.PathType;

public class NSAConnection {
    private String connectionId;
    private String gri;
    private String providerNSA;
    private String requesterNSA;
    private String description;
    private DetailedPathType detailedPath;
    private PathType path;

    public String getConnectionId() {
        return connectionId;
    }
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    public String getGri() {
        return gri;
    }
    public void setGri(String gri) {
        this.gri = gri;
    }
    public String getProviderNSA() {
        return providerNSA;
    }
    public void setProviderNSA(String providerNSA) {
        this.providerNSA = providerNSA;
    }
    public String getRequesterNSA() {
        return requesterNSA;
    }
    public void setRequesterNSA(String requesterNSA) {
        this.requesterNSA = requesterNSA;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public DetailedPathType getDetailedPath() {
        return detailedPath;
    }
    public void setDetailedPath(DetailedPathType detailedPath) {
        this.detailedPath = detailedPath;
    }
    public PathType getPath() {
        return path;
    }
    public void setPath(PathType path) {
        this.path = path;
    }

    
}
