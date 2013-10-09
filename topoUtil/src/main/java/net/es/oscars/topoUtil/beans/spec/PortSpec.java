package net.es.oscars.topoUtil.beans.spec;

import java.util.ArrayList;

public class PortSpec {
    protected String name;
    protected Integer capacity;
    protected ArrayList<EthInternalLinkSpec> ethLinks = new ArrayList<EthInternalLinkSpec>();
    protected ArrayList<MplsInternalLinkSpec> mplsLinks = new ArrayList<MplsInternalLinkSpec>();
    protected ArrayList<CustomerLinkSpec> customerLinks = new ArrayList<CustomerLinkSpec>();
    protected ArrayList<PeeringLinkSpec> peeringLinks = new ArrayList<PeeringLinkSpec>();
    protected Integer reservable;

    public Integer getReservable() {
        return reservable;
    }

    public ArrayList<PeeringLinkSpec> getPeeringLinks() {
        return peeringLinks;
    }

    public void setPeeringLinks(ArrayList<PeeringLinkSpec> peeringLinks) {
        this.peeringLinks = peeringLinks;
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

    public ArrayList<CustomerLinkSpec> getCustomerLinks() {
        return customerLinks;
    }

    public void setCustomerLinks(ArrayList<CustomerLinkSpec> customerLinks) {
        this.customerLinks = customerLinks;
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
