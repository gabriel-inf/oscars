package net.es.oscars.nsibridge.beans;

public class NSAInfo {
    public String nsaId;
    public String providerWsdl;
    public String requesterUrl;

    public String getNsaId() {
        return nsaId;
    }
    public void setNsaId(String nsaId) {
        this.nsaId = nsaId;
    }
    public String getProviderWsdl() {
        return providerWsdl;
    }
    public void setProviderWsdl(String providerWsdl) {
        this.providerWsdl = providerWsdl;
    }
    public String getRequesterUrl() {
        return requesterUrl;
    }
    public void setRequesterUrl(String requesterUrl) {
        this.requesterUrl = requesterUrl;
    }
    
}
