package net.es.oscars.nsibridge.beans;

import org.ogf.nsi.schema.connectionTypes.ConnectionStateType;
import org.ogf.nsi.schema.connectionTypes.DetailedPathType;
import org.ogf.nsi.schema.connectionTypes.PathType;
import org.ogf.nsi.schema.connectionTypes.ServiceParametersType;

public class NSAConnection {
    private String connectionId;
    private String gri;
    private String providerNSA;
    private String requesterNSA;
    private String description;
    private ServiceParametersType serviceParams;
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
    public ServiceParametersType getServiceParams() {
        return serviceParams;
    }
    public void setServiceParams(ServiceParametersType serviceParams) {
        this.serviceParams = serviceParams;
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
    public ConnectionStateType getState() {
        return state;
    }
    public void setState(ConnectionStateType state) {
        this.state = state;
    }
    private DetailedPathType detailedPath;
    private PathType path;
    private ConnectionStateType state;
    
    
}
