package net.es.oscars.pathfinder;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.es.oscars.LogWrapper;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.Ipaddr;

/**
 * This class contains methods for handling PCE's (path computation elements)
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PCEManager {
    private LogWrapper log;
    private PCE pathfinder;

    public PCEManager() {
        this.log = new LogWrapper(this.getClass());
    }

    /**
     * Finds path from source to destination, taking into account ingress
     *    and egress routers if specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @return path A path instance
     * @throws BSSException
     */
    public Path findPath(String srcHost, String destHost,
                         String ingressRouterIP, String egressRouterIP)
            throws BSSException {

        List<String> hops;
        Path path = null;

        this.log.info("PCEManager.findPath", "start");
        String pathMethod = this.getPathMethod();
        this.log.info("PCEManager.method", pathMethod);
        if (pathMethod == null) { return null; }
        this.pathfinder = new PathfinderFactory().createPathfinder(pathMethod);
        path = this.pathfinder.findPath(srcHost, destHost,
                                        ingressRouterIP, egressRouterIP);
        return path;
    }

    public String getPathMethod() throws BSSException {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("pathfinder", true);

        /* Determine whether to perform path calculation.  Path calculation
         * may not be desired if testing other components. */
        String findPath = props.getProperty("findPath");
        if (findPath == null || findPath.equals("0")) {
            return null;
        }

        String pathMethod = props.getProperty("pathMethod");
        if (pathMethod == null) {
            throw new BSSException("No path computation method specified in oscars.properties.");
        }
        if (!pathMethod.equals("traceroute") && !pathMethod.equals("narb")) {
            throw new BSSException("Path computation method specified in oscars.properties must be either traceroute or narb.");
        }
        return pathMethod;
    }

    public String getNextHop() {
        return this.pathfinder.getNextHop();
    }

    /**
     * Given the starting path instance, returns a string representation.
     *
     * @param path a path instance
     * @param retType a string, either "ip" or "host"
     * @return string representation of the path
     */
    public String pathToString(Path path, String retType) {
        StringBuilder sb = new StringBuilder();
        InetAddress inetAddress = null;

        List<Ipaddr> ipaddrs = this.getIpaddrs(path);
        if (retType.equals("host")) {
            for (Ipaddr ipaddr: ipaddrs) {
                try {
                    inetAddress = inetAddress.getByName(ipaddr.getIp());
                    sb.append(" " + inetAddress.getHostName());
                } catch (UnknownHostException e) {
                    sb.append(" " + ipaddr.getIp());
                }
            }
        }
        else { 
            for (Ipaddr ipaddr: ipaddrs) {
                sb.append(" " + ipaddr.getIp());
            }
        }
        return sb.substring(1);
    }

    /**
     * Gets list of addresses, with loopbacks if possible.
     * @param path beginning path instance
     * @return ipaddrs list of ipaddr instances
     */
    public List<Ipaddr> getIpaddrs(Path path) {

        boolean ingressFound = false;
        String addressType = null;

        List<Ipaddr> ipaddrs = new ArrayList<Ipaddr>();
        while (path != null) {
            addressType = path.getAddressType();
            if (addressType == null) { addressType = ""; }
            if (!ingressFound) {
                if (addressType.equals("ingress")) { ingressFound = true; }
            }
            if (ingressFound) {
                ipaddrs.add(path.getIpaddr());
            }
            if (addressType.equals("egress")) { break; }
            path = (Path) path.getNextPath();
        }
        return ipaddrs;
    }

    /**
     * Gets IP addresses of physical interfaces in path.
     * @param path beginning path instance
     * @return hops list of strings
     */
    public List<String> getHops(Path path) {

        Ipaddr ipaddr = null;
        boolean ingressFound = false;
        String addressType = null;

        List<String> hops = new ArrayList<String>();
        while (path != null) {
            ipaddr = path.getIpaddr();
            hops.add(ipaddr.getIp());
            path = (Path) path.getNextPath();
        }
        return hops;
    }
}
