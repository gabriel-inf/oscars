package net.es.oscars.nsibridge.beans;

public class SimpleRequest extends GenericRequest {
    protected String connectionId;
    protected SimpleRequestType requestType;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public SimpleRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(SimpleRequestType requestType) {
        this.requestType = requestType;
    }
}
