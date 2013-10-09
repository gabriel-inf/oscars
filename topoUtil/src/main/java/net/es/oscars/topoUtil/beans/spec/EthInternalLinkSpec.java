package net.es.oscars.topoUtil.beans.spec;

import net.es.oscars.topoUtil.beans.InternalLink;

public class EthInternalLinkSpec extends InternalLink {
    protected String vlanRangeExpr;

    public String getVlanRangeExpr() {
        return vlanRangeExpr;
    }

    public void setVlanRangeExpr(String vlanRangeExpr) {
        this.vlanRangeExpr = vlanRangeExpr;
    }
}
