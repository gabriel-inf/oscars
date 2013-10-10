package net.es.oscars.topoUtil.beans.spec;

import net.es.oscars.topoUtil.beans.GenericLink;

public class PeeringLinkSpec extends GenericLink {
    protected String vlanRangeExpr;
    protected String remote;
    protected String nmlRemote;

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

    public String getVlanRangeExpr() {
        return vlanRangeExpr;
    }

    public void setVlanRangeExpr(String vlanRangeExpr) {
        this.vlanRangeExpr = vlanRangeExpr;
    }
}
