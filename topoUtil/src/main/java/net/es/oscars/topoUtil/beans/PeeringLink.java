package net.es.oscars.topoUtil.beans;

public class PeeringLink extends GenericLink {
    protected String remote;
    protected String nmlRemote;

    protected VlanInfo vlanInfo;

    public String getNmlRemote() {
        return nmlRemote;
    }

    public void setNmlRemote(String nmlRemote) {
        this.nmlRemote = nmlRemote;
    }

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
