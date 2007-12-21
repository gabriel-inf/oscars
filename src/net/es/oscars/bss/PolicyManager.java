package net.es.oscars.bss;

import java.util.*;
import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.bss.BSSException;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.oscars.TypeConverter;

/**
 * This class contains methods for handling reservation setup policy
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PolicyManager {
    private Logger log;
    private String dbname;

    public PolicyManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     * Checks whether adding this reservation would cause oversubscription
     *     on a port.
     *
     * @param activeReservations existing reservations
     * @param pathInfo PathInfo instance to check for oversubscription
     * @param newReservation new reservation instance
     * @throws BSSException
     */
    public void checkOversubscribed(
               List<Reservation> activeReservations,
               PathInfo pathInfo, Reservation newReservation)
            throws BSSException {

        this.log.info("checkOversubscribed.start");
        List<IntervalAggregator> aggrs =  null;
        
        /* Create a hash with each link as the key, and the intervals it
         is in use as the value. Initialize with links from the new path 
         and set the first interval to the reservation time */
        Map<Link,List<IntervalAggregator>> links =
            this.initLinks(pathInfo, newReservation.getStartTime(),
                          newReservation.getEndTime(),
                          newReservation.getBandwidth());
                          
        /* Insert into links the intervals that old reservations are using a 
        link during the new reservation*/
        for (Reservation resv: activeReservations) {
            this.getMatchingLinks(links, resv, newReservation, pathInfo);
        }
        
        /* Go through each link in the path and check for oversubscription */
        for (Link link: links.keySet()) {
            // TODO:  handling where link capacity would be less
        	Port port = link.getPort();
        	if (port == null) {
                throw new BSSException("hop in path does NOT have an " +
                        "associated physical interface: ["+TopologyUtil.getFQTI(link)+"]");
        	}
            Long maximumReservableCapacity = port.getMaximumReservableCapacity();
            if (maximumReservableCapacity == 0) {
                throw new BSSException("hop in path has maximum reservable capacity = 0: ["+TopologyUtil.getFQTI(link)+"]");            	
            }
            aggrs = links.get(link);
            IntervalAggregator newAgg = aggrs.get(0);
            // get full list of segments for first (new) reservation
            // adding segments for each pending or active reservation's in turn
            for (int i=1; i < aggrs.size(); i++) {
                newAgg.add(aggrs.get(i).getIntervals());
            }
            Long capacitySum = newAgg.getMax();
            if (capacitySum > maximumReservableCapacity) {
                throw new BSSException(
                  "Node (" + link.getPort().getNode().getTopologyIdent() +
                  ") oversubscribed:  " + capacitySum +
                  " bps > " + maximumReservableCapacity + " bps");
            }
        }
        this.log.info("checkOversubscribed.end");
    }

    /**
     * Retrieves links given a PathInfo instance.
     * Path contains series of link id's.
     *
     * @param pathInfo PathInfo instance containing path
     * @param startTime start time for the new reservation
     * @param endTime end time for the new reservation
     * @param capacity capacity requested
     * @return links map with initial Link instances as keys
     */
    private Map<Link,List<IntervalAggregator>>
        initLinks(PathInfo pathInfo, Long startTime, Long endTime,
                  Long capacity)
            throws BSSException {

        this.log.info("initLinks.start");
        Map<Link,List<IntervalAggregator>> links =
                new HashMap<Link,List<IntervalAggregator>>();
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        
        if (ctrlPlanePath == null) {
            throw new BSSException("no path provided to initLinks");
        }
        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        for (int i = 0; i < hops.length; i++) {
            this.log.info(hops[i].getLinkIdRef());
            String hopTopoId = hops[i].getLinkIdRef();
            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");
            
            if (hopType.equals("link")) {
                if (domainDAO.isLocal(domainId)) {
	                this.log.info("local: " + hopTopoId);
	                Link link = domainDAO.getFullyQualifiedLink(hopTopoId);
	                if (link == null) {
	                    throw new BSSException("unable to find link with id " +
	                                           hops[i].getLinkIdRef());
	                }
	                // initialize aggregator array for link
	                List<IntervalAggregator> aggrs =
	                        new ArrayList<IntervalAggregator>();
	                // intervals are the same in this case
	                aggrs.add(new IntervalAggregator(startTime, endTime,
	                                         startTime, endTime, capacity));
	                // initialize the vlan range if a layer 2 link
	                if (layer2Info != null) {
	                    L2SwitchingCapabilityData l2scData = 
	                        link.getL2SwitchingCapabilityData();  
	                    if (l2scData != null) {
	                        this.initL2scLink(l2scData, layer2Info);
	                    }
	                }
	                links.put(link, aggrs);
                } else {
	                this.log.info("not local: " + hops[i].getLinkIdRef());
                }
            } else {
                this.log.info("unknown type: "+hopType+"for hop: " + hopTopoId);
            }
        }
        this.log.info("initLinks.end");
        return links;
    }
    
    /**
     * Add to list of Link instances matching links in the new reservation.
     *
     * @param links map structure containing lists of matching links
     * @param path Path instance containing links
     * @return links list of link instances
     * @throws BSSException
     */
    private void getMatchingLinks(Map<Link,List<IntervalAggregator>> links,
                                  Reservation resv, Reservation newResv, 
                                  PathInfo pathInfo) throws BSSException{

        Link link = null;
        String[] srcComponentList = null;
        String[] destComponentList = null;
        Link srcLink = null;
        Link destLink = null;
        boolean srcTagged = false;
        boolean destTagged = false;

        // get start of path
        PathElem pathElem = resv.getPath().getPathElem();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        if (layer2Info != null) {
            srcComponentList = layer2Info.getSrcEndpoint().split(":");
            destComponentList = layer2Info.getDestEndpoint().split(":");
            srcLink = domainDAO.getFullyQualifiedLink(srcComponentList);
            destLink = domainDAO.getFullyQualifiedLink(destComponentList);
            srcTagged = layer2Info.getSrcVtag().getTagged();
            destTagged = layer2Info.getDestVtag().getTagged();
        }
        String sameUserGRI = null;
        if(resv.getLogin().equals(newResv.getLogin())){
            sameUserGRI = resv.getGlobalReservationId();   
        }
         
        while (pathElem != null) {
            link = pathElem.getLink();
            if (links.containsKey(link)) {
                // update bandwidth resources
                links.get(link).add(
                    new IntervalAggregator(resv.getStartTime(),
                        resv.getEndTime(), newResv.getStartTime(),
                        newResv.getEndTime(), resv.getBandwidth()));
                
                // update l2sc resources if layer 2
                if (layer2Info != null) {
                    
                    L2SwitchingCapabilityData l2scData = 
                        link.getL2SwitchingCapabilityData();
                    if(srcLink == link && (!srcTagged)){
                        throw new BSSException("Cannot use untagged VLAN on" +             
                                                " source at requested time.");
                    }else if(destLink == link && (!destTagged)){
                        throw new BSSException("Cannot use untagged VLAN on" +             
                                                " dest at requested time.");
                    }else if (l2scData != null) {
                        this.updateL2scResources(pathElem.getLinkDescr(), 
                                                 layer2Info, sameUserGRI);
                    }
                }
            }
            pathElem = pathElem.getNextElem();
        }
    }
    
    /**
     * Initialize the available VLAN tags by merging request VLANs
     * with the static list of possible VLANS on a link.
     *
     * @param l2scData the L2SwitchingCapabilityData of a link
     * @param layer2Info layer2 parameters of a reservation
     * @throws BSSException
     */
    public void initL2scLink(L2SwitchingCapabilityData l2scData, 
                             Layer2Info layer2Info) throws BSSException {
        VlanTag vtag = layer2Info.getSrcVtag();
        VlanTag vtag2 = layer2Info.getDestVtag();
        TypeConverter tc = new TypeConverter();                     
        byte[] vtagMask;
        byte[] availVtagMask = tc.rangeStringToMask(
                                l2scData.getVlanRangeAvailability());
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        String[] srcComponentList = layer2Info.getSrcEndpoint().split(":");
        String[] destComponentList = layer2Info.getDestEndpoint().split(":");
        Link srcLink = domainDAO.getFullyQualifiedLink(srcComponentList);
        Link destLink = domainDAO.getFullyQualifiedLink(destComponentList);
        Link currLink = l2scData.getLink();            
            
        if (vtag == null) {
            vtag = new VlanTag();
            vtag.setString("2-4094");
            vtag.setTagged(true);
            vtag2 = new VlanTag();
            vtag2.setTagged(true);
        }else if (vtag.getString().equals("any")) {
            vtag.setString("2-4094");
        }
        
        /* Check if link allows untagged VLAN */
        byte canBeUntagged = (byte) ((availVtagMask[0] & 255) >> 7);
        if(currLink == srcLink && (!vtag.getTagged()) && 
            canBeUntagged != 1){
            throw new BSSException("Specified source endpoint " +
                                        "cannot be untagged");
        }else if(currLink == destLink && (!vtag2.getTagged()) && 
                    canBeUntagged != 1){
            throw new BSSException("Specified destination endpoint" +
                                        " cannot be untagged");
        }
        
        vtagMask = tc.rangeStringToMask(vtag.getString());
        for (int i = 0; i < vtagMask.length; i++) {
            vtagMask[i] &= availVtagMask[i];
        }
        
        vtag.setString(tc.maskToRangeString(vtagMask));
        vtag2.setString(vtag.getString());
        layer2Info.setSrcVtag(vtag);
        layer2Info.setDestVtag(vtag2);
        
        if (vtag.getString().equals("")) {
           throw new BSSException("VLAN not available along the path. " + 
                                  "Please try a different VLAN tag.");            
        }
    }
    
    /**
     * Update l2sc resource information based on a given link description.
     * This is the method that removes VLANS because of time conflicts. 
     *
     * @param l2scData the L2SwitchingCapabilityData of a link
     * @param layer2Info layer2 parameters of a reservation
     * @throws BSSException
     */
    public void updateL2scResources(String linkDescr, Layer2Info layer2Info, 
                                    String sameUserGRI) 
                                    throws BSSException{
        VlanTag vtag = layer2Info.getSrcVtag();
        String vtagRange = vtag.getString();
        TypeConverter tc = new TypeConverter();                     
        byte[] vtagMask = tc.rangeStringToMask(vtagRange);
        int usedVtag = Integer.parseInt(linkDescr);
        if(usedVtag < 0){
            throw new BSSException("No VLAN tags available along path.");    
        }else{
            vtagMask[usedVtag/8] &= (byte) ~(1 << (7 - (usedVtag % 8)));
        }
        
        vtag.setString(tc.maskToRangeString(vtagMask));
        layer2Info.setSrcVtag(vtag);
        layer2Info.getDestVtag().setString(vtag.getString());
        
        if (vtag.getString().equals("")) {
            if(sameUserGRI != null){
                throw new BSSException("GRI: " + sameUserGRI + "\nLast VLAN" +           
                    " in range in use by a reservation you previously placed"); 
            }else{
                throw new BSSException("VLAN tag unavailable in specified " +
                 "range. If no VLAN range was specified then there are no " +
                 "available vlans along the path.");       
            }
        }
        
        //this.log.info("New Vtag Range: " + layer2Info.getVtag());
    }
}
