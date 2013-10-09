package net.es.oscars.topoUtil.beans;

public class EthInternalLink extends InternalLink {
    protected VlanInfo vlanInfo;

    public VlanInfo getVlanInfo() {
        return vlanInfo;
    }

    public void setVlanInfo(VlanInfo vlanInfo) {
        this.vlanInfo = vlanInfo;
    }
}
