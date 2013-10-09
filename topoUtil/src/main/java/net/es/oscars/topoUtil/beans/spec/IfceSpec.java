package net.es.oscars.topoUtil.beans.spec;

import java.util.ArrayList;

public class IfceSpec {
    protected String name;
    protected Integer capacity;
    protected ArrayList<EthInternalLinkSpec> ethLinks = new ArrayList<EthInternalLinkSpec>();
    protected ArrayList<MplsInternalLinkSpec> mplsLinks = new ArrayList<MplsInternalLinkSpec>();
    protected ArrayList<CustomerLinkSpecSpec> custLinks = new ArrayList<CustomerLinkSpecSpec>();
    protected Integer reservable;

    public Integer getReservable() {
        return reservable;
    }

    public void setReservable(Integer reservable) {
        this.reservable = reservable;
    }
    public ArrayList<EthInternalLinkSpec> getEthLinks() {
        return ethLinks;
    }

    public void setEthLinks(ArrayList<EthInternalLinkSpec> ethLinks) {
        this.ethLinks = ethLinks;
    }

    public ArrayList<MplsInternalLinkSpec> getMplsLinks() {
        return mplsLinks;
    }

    public void setMplsLinks(ArrayList<MplsInternalLinkSpec> mplsLinks) {
        this.mplsLinks = mplsLinks;
    }

    public ArrayList<CustomerLinkSpecSpec> getCustLinks() {
        return custLinks;
    }

    public void setCustLinks(ArrayList<CustomerLinkSpecSpec> custLinks) {
        this.custLinks = custLinks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

}
