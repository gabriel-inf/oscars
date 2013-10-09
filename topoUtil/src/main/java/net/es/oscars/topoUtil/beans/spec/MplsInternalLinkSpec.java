package net.es.oscars.topoUtil.beans.spec;

import net.es.oscars.topoUtil.beans.InternalLink;

public class MplsInternalLinkSpec extends InternalLink {
    protected String ipv4Expr;

    public String getIpv4Expr() {
        return ipv4Expr;
    }

    public void setIpv4Expr(String ipv4Expr) {
        this.ipv4Expr = ipv4Expr;
    }
}
