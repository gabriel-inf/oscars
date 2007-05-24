package net.es.oscars.bss;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pathfinder.CommonPathElem;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;

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
     *     on an interface.
     *
     * @param activeReservations existing reservations
     * @param path a list of CommonPathElem's to check for oversubscription
     * @param bandwidth Long with the desired bandwidth
     * @throws BSSException
     */
    public void checkOversubscribed(
               List<Reservation> activeReservations, List<CommonPathElem> path,
               Long bandwidth)
            throws BSSException {

        List<Ipaddr> ipaddrs = null;
        Map<Interface,Long> xfaceSums = new HashMap<Interface,Long>();
        Interface ipaddrXface = null;
        double maxUtilization = 0.0;
        double maxPercentUtilization = 0.0;

        ipaddrs = this.getIpaddrs(path);
        // initialize sums to requested bandwidth for each link in path
        for (Ipaddr ipaddr: ipaddrs) {
            ipaddrXface = ipaddr.getInterface();
            xfaceSums.put(ipaddrXface, bandwidth);
        }
        for (Reservation resv: activeReservations) {
            ipaddrs = this.getIpaddrs(resv.getPath());
            this.addPathBandwidths(resv.getBandwidth(), ipaddrs, xfaceSums);
        }
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("reservation", true);
        maxPercentUtilization = Double.valueOf(props.getProperty("maxPercentUtilization"));

        // now for each of those interface instances
        for (Interface xface: xfaceSums.keySet()) {
            Long speed = xface.getSpeed();
            // speed will be 0 for ingress or egress router
            if ((speed == null) || (speed == 0)) {
                continue; 
            }

            maxUtilization = xface.getSpeed() * maxPercentUtilization;
            if (((Long)xfaceSums.get(xface)) > maxUtilization) {
                throw new BSSException(
                      "Router (" + xface.getRouter().getName() + ") oversubscribed:  " + xfaceSums.get(xface) +
                      " bps > " + maxUtilization + " bps");
            }
        }
    }

    /**
     * Add a current reservation's bandwidth to a running total of all
     * links that are in the requested path.
     *
     * @param bandwidth the bandwidth for an active or pending reservation
     * @param ipaddrs a list of ipaddr instances in the reservation's path
     * @param xfaceSums a mapping from interfaces to the current sum of
     *                  bandwidths for all reservations utilizing that link
     * @throws BSSException
     */
    public void addPathBandwidths(Long bandwidth, List<Ipaddr> ipaddrs,
                 Map<Interface,Long> xfaceSums) throws BSSException {

        Interface xface = null;

        for (Ipaddr ipaddr: ipaddrs) {
            xface = ipaddr.getInterface();
            if (!xface.isValid() || (xface.getSpeed() <= 0)) {
                continue;
            }
            // if not in xfaceSums, not part of requested path
            Long totalBandwidth = xfaceSums.get(xface);
            if (totalBandwidth != null) {
                xfaceSums.put(xface, bandwidth + totalBandwidth);
            }
        }
    }

    /**
     * Gets list of ipaddr instances given a start of a path.
     *
     * @param path start of a path to check
     * @return ipaddrs list of ipaddr instances
     */
    private List<Ipaddr> getIpaddrs(Path path) {

        List<Ipaddr> ipaddrs = new ArrayList<Ipaddr>();
        PathElem pathElem = path.getPathElem();
        while (pathElem != null) {
            ipaddrs.add(pathElem.getIpaddr());
            pathElem = pathElem.getNextElem();
        }
        return ipaddrs;
    }

    /**
     * Gets list of ipaddr instances given a list of common path elements.
     *
     * @param path list of elements
     * @return ipaddrs list of ipaddr instances
     */
    private List<Ipaddr> getIpaddrs(List<CommonPathElem> path) {

        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = new ArrayList<Ipaddr>();
        // TODO:  error checking, assumes local
        for (int i = 0; i < path.size(); i++) {
            Ipaddr ipaddr = ipaddrDAO.getIpaddr(path.get(i).getIP(), true);
            ipaddrs.add(ipaddr);
        }
        return ipaddrs;
    }
}
