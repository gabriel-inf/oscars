package net.es.oscars.topoUtil.beans;

import java.util.ArrayList;

public class Port {

    protected String name;
    protected Integer capacity;
    protected Integer reservable;
    protected ArrayList<EthInternalLink> ethLinks = new ArrayList<EthInternalLink>();
    protected ArrayList<MplsInternalLink> mplsLinks = new ArrayList<MplsInternalLink>();
    protected ArrayList<CustomerLink> customerLinks = new ArrayList<CustomerLink>();
    protected ArrayList<PeeringLink> peeringLinks = new ArrayList<PeeringLink>();

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getReservable() {
        return reservable;
    }

    public void setReservable(Integer reservable) {
        this.reservable = reservable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<EthInternalLink> getEthLinks() {
        return ethLinks;
    }

    public void setEthLinks(ArrayList<EthInternalLink> ethLinks) {
        this.ethLinks = ethLinks;
    }

    public ArrayList<MplsInternalLink> getMplsLinks() {
        return mplsLinks;
    }

    public void setMplsLinks(ArrayList<MplsInternalLink> mplsLinks) {
        this.mplsLinks = mplsLinks;
    }

    public ArrayList<CustomerLink> getCustomerLinks() {
        return customerLinks;
    }

    public void setCustomerLinks(ArrayList<CustomerLink> customerLinks) {
        this.customerLinks = customerLinks;
    }

    public ArrayList<PeeringLink> getPeeringLinks() {
        return peeringLinks;
    }

    public void setPeeringLinks(ArrayList<PeeringLink> peeringLinks) {
        this.peeringLinks = peeringLinks;
    }
}
