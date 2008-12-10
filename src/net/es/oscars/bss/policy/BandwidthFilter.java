package net.es.oscars.bss.policy;

import java.util.*;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import org.apache.log4j.Logger;

public class BandwidthFilter implements PolicyFilter {
    private Logger log;

    public BandwidthFilter() {
        this.log = Logger.getLogger(this.getClass());
    }
	
    public void applyFilter(Reservation newReservation, List<Reservation> activeReservations) throws BSSException {
		
        List<BandwidthIntervalAggregator> aggrs =  null;

        /* Create a hash with each link as the key, and the intervals it
        is in use as the value. Initialize with linkIntervals from the new path
        and set the first interval to the reservation time. This
        is a special case because we want to put all the linkIntervals from the
        new reservation in this map.
        */
        Path localPath = newReservation.getPath(PathType.LOCAL);
        List<PathElem> localPathElems = localPath.getPathElems();
        Map<Link,List<BandwidthIntervalAggregator>> linkIntervals =
            this.getLocalLinkIntervals(localPathElems,
                          newReservation.getStartTime(),
                          newReservation.getEndTime(),
                          newReservation.getBandwidth());

        /* Insert into linkIntervals the intervals from the common links between
         * the new reservation and all the existing reservations */
        for (Reservation resv: activeReservations) {
            // make sure we don't check the reservation against itself!
            if (!resv.getGlobalReservationId().equals(newReservation.getGlobalReservationId())) {
                this.getMatchingLinkIntervals(linkIntervals, resv, newReservation);
            }
        }
        
        /* Check each link in the path for oversubscription in the port */
        for (Link link: linkIntervals.keySet()) {
            // TODO:  handling where link capacity would be less
            Port port = link.getPort();
            if (port == null) {
                BSSException ex =
                    new BSSException("Database error: A hop in the path does NOT have an " +
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
                  "Node (" + link.getPort().getNode().getTopologyIdent() +
                  "), Port (" + link.getPort().getTopologyIdent() +
                  ") oversubscribed:  " + capacitySum +
                  " bps > " + maximumReservableCapacity + " bps");
            }
        }
	 }

	 /**
     * Retrieves link bandwidth intervals given a PathInfo instance.
     *
     * @param localPathElems the local PathElems to check
     * @param startTime start time for the new reservation
     * @param endTime end time for the new reservation
     * @param capacity capacity requested
     * @return links map with initial Link instances as keys
     */
    private Map<Link,List<BandwidthIntervalAggregator>>
        getLocalLinkIntervals(List<PathElem> localPathElems,
            Long startTime, Long endTime, Long capacity) throws BSSException {

        this.log.info("getLocalLinkIntervals.start");
        Map<Link,List<BandwidthIntervalAggregator>> linkIntervals =
                new HashMap<Link,List<BandwidthIntervalAggregator>>();
        
        if (localPathElems == null) {
            throw new BSSException("no local links provided to getLocalLinkIntervals");
        }
        for (PathElem localPathElem: localPathElems) {
            Link link = localPathElem.getLink();
            if(link == null){
                throw new BSSException("local path does not contain a linkId for elem " + localPathElem.getUrn());
            }
            // initialize aggregator array for link
            List<BandwidthIntervalAggregator> aggrs = new ArrayList<BandwidthIntervalAggregator>();
            //check granularity
            this.checkGranularity(link, capacity);
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
                                  Reservation resv, Reservation newResv)
            throws BSSException{

        Link link = null;
        // get path list
        List<PathElem> pathElems = resv.getPath("intra").getPathElems();
        for (PathElem pathElem: pathElems) {
            link = pathElem.getLink();
            if (linkIntervals.containsKey(link)) {
                // update bandwidth resources
                linkIntervals.get(link).add(
                    new BandwidthIntervalAggregator(resv.getStartTime(),
                        resv.getEndTime(), newResv.getStartTime(),
                        newResv.getEndTime(), resv.getBandwidth()));
            }
        }
    }
    
    /**
     * Checks if the requested bandwidth is of the required granularity.
     *
     * @param link the link with the granularity requirement
     * @param capacity the requested bandwidth
     * @throws BSSException thrown when capacity is not evenly divisible by granularity
     */
    private void checkGranularity(Link link, Long capacity) throws BSSException{
        long granularity = 1000000L;//default=1Mbps
        if (link.getGranularity() != null && link.getGranularity() > 0) {
            granularity = link.getGranularity();
        }
        this.log.debug("capacity=" + capacity + ", granularity=" + granularity);
        if ((capacity % granularity) != 0) {
            throw new BSSException("Requested path only supports " +
                    "requests with a bandwidth granularity of " + 
                    (granularity/1000000) + "Mbps.");
        }
    }
}
