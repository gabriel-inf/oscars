package net.es.oscars.bss;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pathfinder.CommonPath;
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
     *     on a port.
     *
     * @param activeReservations existing reservations
     * @param path CommonPath instance to check for oversubscription
     * @param bandwidth Long with the desired bandwidth
     * @throws BSSException
     */
    public void checkOversubscribed(
               List<Reservation> activeReservations, CommonPath path,
               Long bandwidth)
            throws BSSException {

        List<Ipaddr> ipaddrs = null;
        Map<Port,Long> portSums = new HashMap<Port,Long>();
        Port ipaddrXface = null;
        double maxUtilization = 0.0;

        this.log.info("checkOversubscribed.start");
        ipaddrs = this.getIpaddrs(path);
        // initialize sums to requested bandwidth for each link in path
        for (Ipaddr ipaddr: ipaddrs) {
            ipaddrXface = ipaddr.getPort();
            portSums.put(ipaddrXface, bandwidth);
        }
        for (Reservation resv: activeReservations) {
            ipaddrs = this.getIpaddrs(resv.getPath());
            this.addPathBandwidths(resv.getBandwidth(), ipaddrs, portSums);
        }
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("reservation", true);

        // now for each of those port instances
        for (Port port: portSums.keySet()) {
            Long maximumCapacity = port.getMaximumCapacity();
            // maximumCapacity will be 0 for ingress or egress node
            if ((maximumCapacity == null) || (maximumCapacity == 0)) {
                continue; 
            }

            if (((Long)portSums.get(port)) > port.getMaximumReservableCapacity()) {
                throw new BSSException(
                      "Node (" + port.getNode().getName() +
                      ") oversubscribed:  " + portSums.get(port) +
                      " bps > " + port.getMaximumReservableCapacity() + " bps");
            }
        }
        this.log.info("checkOversubscribed.end");
    }

    /**
     * Add a current reservation's bandwidth to a running total of all
     * links that are in the requested path.
     *
     * @param bandwidth the bandwidth for an active or pending reservation
     * @param ipaddrs a list of ipaddr instances in the reservation's path
     * @param portSums a mapping from ports to the current sum of
     *                  bandwidths for all reservations utilizing that link
     * @throws BSSException
     */
    public void addPathBandwidths(Long bandwidth, List<Ipaddr> ipaddrs,
                 Map<Port,Long> portSums) throws BSSException {

        Port port = null;

        for (Ipaddr ipaddr: ipaddrs) {
            port = ipaddr.getPort();
            if (!port.isValid() || (port.getMaximumCapacity() <= 0)) {
                continue;
            }
            // if not in portSums, not part of requested path
            Long totalBandwidth = portSums.get(port);
            if (totalBandwidth != null) {
                portSums.put(port, bandwidth + totalBandwidth);
            }
        }
    }

    /**
     * Gets list of ipaddr instances given a start of a path.
     *
     * @param path Path instance containing start of path
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
     * @param path CommonPath instance containing path to check
     * @return ipaddrs list of ipaddr instances
     */
    private List<Ipaddr> getIpaddrs(CommonPath path) {

        this.log.info("getIpaddrs.start");
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        List<CommonPathElem> pathElems = path.getElems();
        List<Ipaddr> ipaddrs = new ArrayList<Ipaddr>();
        for (int i = 0; i < pathElems.size(); i++) {
            CommonPathElem pathElem = pathElems.get(i);
            // don't test non-local addresses
            if (pathElem.getDescription() != null) {
                Ipaddr ipaddr = ipaddrDAO.getIpaddr(pathElem.getIP(), true);
                ipaddrs.add(ipaddr);
            }
        }
        this.log.info("getIpaddrs.end");
        return ipaddrs;
    }
}
