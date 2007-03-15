package net.es.oscars.bss;

import java.util.*;

import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

import net.es.oscars.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.Path;
import net.es.oscars.pathfinder.PCEManager;

/**
 * This class contains methods for handling reservation setup policy
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PolicyManager {
    private LogWrapper log;
    private Session session;

    public PolicyManager() {
        this.log = new LogWrapper(this.getClass());
    }

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * Checks whether adding this reservation would cause oversubscription
     *     on an interface.
     *
     * @param currentPaths existing paths
     * @param path path to check for oversubscription
     * @param bandwidth Long with the desired bandwidth
     * @throws BSSException
     */
    public void checkOversubscribed(List<Path> currentPaths, Path path,
                                    Long bandwidth)
            throws BSSException {

        List<Ipaddr> ipaddrs = null;
        Map<Interface,Long> xfaceSums = new HashMap<Interface,Long>();
        Interface ipaddrXface = null;
        double maxUtilization = 0.0;
        double maxPercentUtilization = 0.0;

        PCEManager pceMgr = new PCEManager();
        ipaddrs = pceMgr.getIpaddrs(path);
        // initialize sums to requested bandwidth for each link in path
        for (Ipaddr ipaddr: ipaddrs) {
            ipaddrXface = ipaddr.getInterface();
            xfaceSums.put(ipaddrXface, bandwidth);
        }
        for (Path currPath: currentPaths) {
            ipaddrs = pceMgr.getIpaddrs(currPath);
            this.addPathBandwidths(ipaddrs, xfaceSums);
        }
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("reservation", true);
        maxPercentUtilization = Double.valueOf(props.getProperty("maxPercentUtilization"));

        // now for each of those interface instances
        for (Interface xface: xfaceSums.keySet()) {
            Long speed = xface.getSpeed();
            if (speed == null) {
                continue; 
            }

            maxUtilization = xface.getSpeed() * maxPercentUtilization;
            if (((Long)xfaceSums.get(xface)) > maxUtilization) {
                throw new BSSException(
                      "Router (" + xface.getId() + ") oversubscribed:  " + xfaceSums.get(xface) +
                      " bps > " + maxUtilization + " bps");
            }
        }
    }

    /**
     * For a given path, gets bandwidths associated with all valid interfaces,
     *     that have an associated bandwidth (some do not).  Then add it to a
     *     running sum of bandwidths for current circuits.
     *
     * @param ipaddrs a list of ipaddr instances comprising the path
     * @param xfaceSums a mapping from interfaces to the current sum of
     *                  bandwidths for all reservations utilizing that link
     * @throws BSSException
     */
    public void addPathBandwidths(List<Ipaddr> ipaddrs,
                 Map<Interface,Long> xfaceSums) throws BSSException {

        Interface xface = null;
        Long bandwidth = new Long(0);

        for (Ipaddr ipaddr: ipaddrs) {
            xface = ipaddr.getInterface();
            if (!xface.isValid() || (xface.getSpeed() <= 0)) {
                continue;
            }
            bandwidth = xface.getSpeed();
            Long currentBandwidth = xfaceSums.get(xface);
            if (currentBandwidth != null) {
                xfaceSums.put(xface, currentBandwidth + bandwidth);
            } else {
                xfaceSums.put(xface, bandwidth);
            }
        }
    }
}
