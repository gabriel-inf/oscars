package net.es.oscars.bss.topology;

import java.util.*;
import java.net.UnknownHostException;
import java.net.InetAddress;

import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

import net.es.oscars.LogWrapper;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;

/**
 * This class contains convenience methods for handling and validating
 * reservation paths.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PathManager {
    private LogWrapper log;
    private Session session;

    public PathManager() {
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
     * @param xfaceSums A mapping from interfaces containing the sum of
     *                  bandwidths for all reservations utilizing that link
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

        ipaddrs = this.getIpaddrs(path);
        // initialize sums to requested bandwidth for each link in path
        for (Ipaddr ipaddr: ipaddrs) {
            ipaddrXface = ipaddr.getInterface();
            xfaceSums.put(ipaddrXface, bandwidth);
        }
        for (Path currPath: currentPaths) {
            ipaddrs = this.getIpaddrs(currPath);
            this.addPathBandwidths(ipaddrs, xfaceSums);
        }
        PropHandler propHandler =
            new PropHandler("/oscars.config/properties/oscars.properties");
        Properties props = propHandler.getPropertyGroup("reservation", true);
        maxPercentUtilization = Double.valueOf(props.getProperty("maxPercentUtilization"));

        // now for each of those interface instances
        for (Interface xface: xfaceSums.keySet()) {
            if (xface.getSpeed() == 0) { continue; }
            maxUtilization = xface.getSpeed() * maxPercentUtilization;
            if (((Long)xfaceSums.get(xface)) > maxUtilization) {
                throw new BSSException(
                      "Router oversubscribed:  " + xfaceSums.get(xface) +
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

    public Path getPath(List<String> hops,
                        String ingressIP, String egressRouterIP) {

        List<Ipaddr> ipaddrs = new ArrayList<Ipaddr>();
        Path path = null;
        Ipaddr currentIpaddr = null;
        Ipaddr ipaddr = null;
        boolean samePath = false;
        int ctr = -1;

        PathDAO pathDAO = new PathDAO();
        pathDAO.setSession(this.session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(this.session);

        for (String hop: hops) {
            ipaddrs.add(ipaddrDAO.queryByParam("ip", hop));
        }
        // check to make sure path is not already in the database
        Path currentPath = (Path)
                     pathDAO.queryByParam("ipaddrId", ipaddrs.get(0).getId());
        if (currentPath != null) {
            ctr = ipaddrs.size();
            samePath = true;
            path = currentPath;
            for (int i=0; i < ctr; i++) {
                currentIpaddr = ipaddrs.get(i);
                ipaddr = path.getIpaddr();
                if (currentIpaddr != ipaddr) {
                    samePath = false;
                    break;
                }
                path = path.getNextPath();
                // takes care of case where current path is shorter
                if ((path == null) && (i != (ctr-1))) {
                    samePath = false;
                    break;
                }
            }
            // takes care of case where new path is shorter
            if (path != null) { samePath = false; }
        }
        if (!samePath) {
            path = pathDAO.create(ipaddrs, ingressIP, egressRouterIP);
        } else { path = currentPath; }
        return path;
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

    /* If the ingress router is given, make sure it is in the database,
           and then return as is. */
    public String checkIngressLoopback(String ingressRouterIP)
            throws BSSException {

        Router router = null;
        String ingressIP = null;

        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(this.session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(this.session);

        router = routerDAO.fromIp(ingressRouterIP);
        if ((router != null) && (router.getName() != null)) {
            ingressIP = ipaddrDAO.getIpType(router.getName(), "loopback");
        }
        if (ingressIP.equals("")) {
            throw new BSSException(
                "No loopback for specified ingress router" +
                 ingressRouterIP);
        }
        return ingressIP;
    }

    /**
     * Gets last interface within this domain.
     *
     * @param hops list of IP addresses
     * @return string containing last interface address, if any
     * @throws BSSException
     */
    public String lastInterface(List<String> hops) throws BSSException {

        Ipaddr ipaddr = null;
        String ingressIP = "";
        Interface xface = null;
        
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops) {
            // get interface associated with that address
            ipaddr = ipaddrDAO.queryByParam("ip", hop);
            if (ipaddr == null) continue;
            xface = ipaddr.getInterface();
            if (xface != null) { ingressIP = hop; }
        }
        if (ingressIP.equals("")) { 
            throw new BSSException(
                "No ingress interface found by reverse traceroute");
        }
        return ingressIP;
    }

    /**
     * Gets last OSCARS interface within this domain.
     *
     * @param hops list of IP addresses
     * @return string containing last loopback address, if any
     * @throws BSSException
     */
    public String lastLoopback(List<String> hops) throws BSSException {

        Ipaddr ipaddr;
        String ingressLoopbackIp = "";
        String loopbackFound = "";

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops)  {
            Router router = routerDAO.fromIp(hop);
            if ((router != null) && (router.getName() != null)) {
                loopbackFound = ipaddrDAO.getIpType(router.getName(), "loopback");
            }
            if (!loopbackFound.equals("")) { ingressLoopbackIp = hop; }
        }
        if (ingressLoopbackIp.equals("")) { 
            throw new BSSException(
                "No ingress loopback found by reverse traceroute");
        }
        return ingressLoopbackIp;
    }

    /**
     * Gets next hop outside of this domain.
     *
     * @param hops list of IP addresses
     * @return hops list of IP addresses, with next hop added
     */
    public String nextHop(List<String> hops) {

        Ipaddr ipaddr = null;
        Interface xface = null;
        String outsideHop = "";
        boolean xfaceFound = false;
        
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops) {
            // get IP address
            ipaddr = ipaddrDAO.queryByParam("ip", hop);
            if (ipaddr != null) {
                xface = ipaddr.getInterface();
                if (xface != null) {
                    outsideHop = hop;
                    break; 
                }
            }
        }
        return outsideHop;
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
