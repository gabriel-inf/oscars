package net.es.oscars.topoUtil.beans;

public class CustomerLink extends GenericLink {
    protected VlanInfo vlanInfo;

    public VlanInfo getVlanInfo() {
        return vlanInfo;
    }

    public void setVlanInfo(VlanInfo vlanInfo) {
        this.vlanInfo = vlanInfo;
    }
}
