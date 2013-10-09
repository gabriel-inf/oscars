package net.es.oscars.topoUtil.beans;

public class PeeringLink extends GenericLink {
    protected String remote;
    protected VlanInfo vlanInfo;

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public VlanInfo getVlanInfo() {
        return vlanInfo;
    }

    public void setVlanInfo(VlanInfo vlanInfo) {
        this.vlanInfo = vlanInfo;
    }
}
