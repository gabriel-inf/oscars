package net.es.oscars.bss.policy;

import java.util.*;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

public class BandwidthFilter implements PolicyFilter {
    private Logger log;

	public BandwidthFilter() {
        this.log = Logger.getLogger(this.getClass());
	}
	
	public void applyFilter(PathInfo pathInfo, 
	        CtrlPlaneHopContent[] hops,
			List<Link> localLinks,
			Reservation newReservation, 
			List<Reservation> activeReservations) throws BSSException {
		
        List<BandwidthIntervalAggregator> aggrs =  null;
        
        /* Create a hash with each link as the key, and the intervals it
        is in use as the value. Initialize with linkIntervals from the new path
        and set the first interval to the reservation time.        
        This is a special case because we want to put all the linkIntervals from the
        new reservation in this map.
        */
        Map<Link,List<BandwidthIntervalAggregator>> linkIntervals =
            this.getLocalLinkIntervals(pathInfo, localLinks, newReservation.getStartTime(),
                          newReservation.getEndTime(),
                          newReservation.getBandwidth());
        

        /* Insert into linkIntervals the intervals from the common links between the 
         * new reservation and all the existing reservations */
        for (Reservation resv: activeReservations) {
            // make sure we don't check the reservation against itself!
            if (!resv.getGlobalReservationId().equals(newReservation.getGlobalReservationId())) {
                this.getMatchingLinkIntervals(linkIntervals, resv, newReservation);
            }
        }
        
        /* Go through each link in the path and check for oversubscription in the port */
        for (Link link: linkIntervals.keySet()) {
            // TODO:  handling where link capacity would be less
            Port port = link.getPort();
            if (port == null) {
                BSSException ex = new BSSException("Database error: A hop in the path does NOT have an " +
                        "associated physical interface. Hop: ["+link.getFQTI()+"]");
                this.log.error(ex);
                throw ex;
            }
            Long maximumReservableCapacity = port.getMaximumReservableCapacity();
            if (maximumReservableCapacity == 0) {
                throw new BSSException("A hop in the path has maximum reservable capacity = 0. Hop: ["+link.getFQTI()+"]");
            }
            aggrs = linkIntervals.get(link);
            BandwidthIntervalAggregator newAgg = aggrs.get(0);
            // get full list of segments for first (new) reservation
            // adding segments for each pending or active reservation's in turn
            for (int i=1; i < aggrs.size(); i++) {
                newAgg.add(aggrs.get(i).getIntervals());
            }
            Long capacitySum = newAgg.getMax();
            if (capacitySum > maximumReservableCapacity) {
                throw new BSSException(
                  "Port (" + link.getPort().getTopologyIdent() +
                  ") oversubscribed:  " + capacitySum +
                  " bps > " + maximumReservableCapacity + " bps");
            }
        }
       
	}

	
	
	 /**
     * Retrieves link bandwidth intervals given a PathInfo instance.
     *
     * @param pathInfo PathInfo instance containing path parameters
     * @param localLinks the local links to check
     * @param startTime start time for the new reservation
     * @param endTime end time for the new reservation
     * @param capacity capacity requested
     * @return links map with initial Link instances as keys
     */
    private Map<Link,List<BandwidthIntervalAggregator>>
        getLocalLinkIntervals(PathInfo pathInfo, List<Link> localLinks,
            Long startTime, Long endTime, Long capacity) throws BSSException {

        this.log.info("getLocalLinkIntervals.start");
        Map<Link,List<BandwidthIntervalAggregator>> linkIntervals =
                new HashMap<Link,List<BandwidthIntervalAggregator>>();
        
        if (localLinks == null) {
            throw new BSSException("no local links provided to getLocalLinkIntervals");
        }
        for (Link link : localLinks) {
            // initialize aggregator array for link
            List<BandwidthIntervalAggregator> aggrs = new ArrayList<BandwidthIntervalAggregator>();
            // intervals are the same in this case
            aggrs.add(new BandwidthIntervalAggregator(startTime, endTime, startTime, endTime, capacity));
            linkIntervals.put(link, aggrs);
        }
        this.log.info("getLocalLinkIntervals.end");
        return linkIntervals;
    }
    

    /**
     * Add to list of Link instances matching linkIntervals in the new reservation.
     *
     * @param linkIntervals map structure containing lists of matching linkIntervals
     * @param resv an existing reservation during the time period of the new reservation
     * @param newResv the reservation that is being created
     * @throws BSSException
     */
    private void getMatchingLinkIntervals(Map<Link,List<BandwidthIntervalAggregator>> linkIntervals,
                                  Reservation resv, Reservation newResv) throws BSSException{

        Link link = null;

        // get start of path
        PathElem pathElem = resv.getPath().getPathElem();

        while (pathElem != null) {
            link = pathElem.getLink();
            if (linkIntervals.containsKey(link)) {
                // update bandwidth resources
                linkIntervals.get(link).add(
                    new BandwidthIntervalAggregator(resv.getStartTime(),
                        resv.getEndTime(), newResv.getStartTime(),
                        newResv.getEndTime(), resv.getBandwidth()));
            }
            pathElem = pathElem.getNextElem();
        }
    }


	
}
