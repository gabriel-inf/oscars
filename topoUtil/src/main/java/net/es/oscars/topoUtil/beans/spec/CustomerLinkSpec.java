package net.es.oscars.topoUtil.beans.spec;

import net.es.oscars.topoUtil.beans.GenericLink;

public class CustomerLinkSpec extends GenericLink {
    protected String vlanRangeExpr;

    public String getVlanRangeExpr() {
        return vlanRangeExpr;
    }

    public void setVlanRangeExpr(String vlanRangeExpr) {
        this.vlanRangeExpr = vlanRangeExpr;
    }
}
