package net.es.oscars.pss.common;

import net.es.oscars.bss.Reservation;

public interface ConfigNameGenerator {

    public String getFilterName(Reservation resv, String type);

    public String getInetFilterMarker(Reservation resv);

    public String getRoutingInstanceName(Reservation resv);
    
    public String getRoutingInstanceRibName(Reservation resv);
    
    public String[] getLayer3Filters();
    
    public String getFilterTerm(Reservation resv, String type);
    
    public String getPrefixListName(Reservation resv, boolean src);
    
    public String getInterfaceDescription(Reservation resv);

    public Integer getOscarsCommunity(Reservation resv);

    public String getL2CircuitDescription(Reservation resv);

    public String getLSPName(Reservation resv);

    public String getPathName(Reservation resv);

    public String getPolicerName(Reservation resv);

    public String getPolicyName(Reservation resv);

    public String getCommunityName(Reservation resv);
    
    
}
