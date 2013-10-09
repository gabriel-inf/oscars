package net.es.oscars.topoUtil.beans;

import java.net.InetAddress;

public class MplsInternalLink extends InternalLink {
    protected Integer mask;

    protected InetAddress address;

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public Integer getMask() {
        return mask;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }
}
