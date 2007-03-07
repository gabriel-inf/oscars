package net.es.oscars.pathfinder;

import java.util.*;
import java.net.UnknownHostException;
import java.net.InetAddress;

import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

import net.es.oscars.LogWrapper;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.traceroute.Pathfinder;
import net.es.oscars.pathfinder.dragon.*;

/**
 * This class contains convenience methods for handling and validating
 * reservation paths.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PathManager {
    private LogWrapper log;
    private Session session;
    private Pathfinder pathfinder;
	private String nextHop;
	
    public PathManager() {
        this.log = new LogWrapper(this.getClass());
        this.pathfinder = new Pathfinder();
        this.nextHop = null;
    }

    public void setSession(Session session) {
        this.session = session;
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

        List<String> hops = null;
        List<String> reverseHops = null;
        String ingressIP = null;

        this.pathfinder.initialize();
        /* If the ingress router is given, make sure it is in the database,
           and then return as is. */
        if (ingressRouterIP != null) {
            ingressIP = this.checkIngressLoopback(ingressRouterIP);
        } else {
            reverseHops = this.pathfinder.reversePath(egressRouterIP, srcHost);
            ingressIP = this.lastLoopback(reverseHops);
        }
        this.log.info("createReservation.ingressIP", ingressIP);

        // find best valid path, contacting next domain if necessary
        hops = this.pathfinder.forwardPath(destHost, ingressIP, egressRouterIP);
		
		//set next hop for static lookup - do this now because path not yet reduced to local path
		this.nextHop = this.nextExternalHop(hops);
		
        // recalculate path to be *inside*
        String lastIface = this.lastInterface(hops);
        hops = this.pathfinder.forwardPath(ingressIP, lastIface);
        return this.getPath(hops, ingressIP, egressRouterIP);
    }
    
    /**
     * Finds path from source to destination using the NARB web service interface, 
     * taking into account ingress and egress routers if specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @return path A path instance
     * @throws BSSException
     */
    public Path findNARBPath(String srcHost, String destHost,
                         String ingressRouterIP, String egressRouterIP)
            throws BSSException {
        List<String> hops = null;
        String ingressIP = null;
		NARBPathfinder narbPathfinder = new NARBPathfinder();
   	
        /* If the ingress router is given, make sure it is in the database,
           and then return as is. */
        if (ingressRouterIP != null) {
            ingressIP = this.checkIngressLoopback(ingressRouterIP);
        }
		
		/* NARB find path */
		hops = narbPathfinder.findPath(srcHost, destHost, ingressRouterIP, egressRouterIP);
		/* get the next hop */
		this.nextHop = this.nextExternalHop(hops);
		/* create reverse list */
		ArrayList<String> ingressList = new ArrayList<String>();
		for(int i = (hops.size() - 1); i >= 0; i--){
			ingressList.add(hops.get(i));
		}
		ingressRouterIP =this.lastLoopback(ingressList);
		egressRouterIP =this.lastLoopback(hops);
		
		/* get ingress to egress path */
		ArrayList<String> inegHops = new ArrayList<String>();
		boolean ingressFound=false;
		boolean egressFound = false;
		for(int i = 0; (!egressFound) && i < hops.size(); i++){
			if(hops.get(i).equals(egressRouterIP)){
				egressFound = true;
			    inegHops.add(hops.get(i));
			}else if(hops.get(i).equals(ingressRouterIP)){
				ingressFound = true;
				inegHops.add(hops.get(i));
			}else if(ingressFound){
				inegHops.add(hops.get(i));
			}
		}
		this.log.debug("findNARBPath.firstHop", hops.get(0));
		this.log.debug("findNARBPath.ingressIP", ingressRouterIP);
		this.log.debug("findNARBPath.egressIP", egressRouterIP);
		
        return this.getPath(inegHops, ingressRouterIP, egressRouterIP);
    }

    /**
     * Finds autonomous system number of next domain, if any.
     *
     * @param path a path from source to destination
     * @return Domain an instance associated with the next domain, if any
     * @throws BSSException
     */
    public Domain getNextDomain(Path path)
            throws BSSException {

        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(this.session);
        List<Ipaddr> ipaddrs = this.getIpaddrs(path);

        List<String> hops = this.getHops(path);
        // XXX: TODO: HACK: Dave -- can we review what this was/is doing ?
        String lastIface = this.lastInterface(hops);
        //System.out.println("Last Iface = " + lastIface);

        String nextHop = this.nextHop(hops);
        //System.out.println("NextHop = " + nextHop);

        // Get router name for logging.  If unable to get, router not in db.
        Router router = routerDAO.fromIp(lastIface);
        if (router == null) {
            throw new BSSException("getAsNumber: no router in database for " + lastIface);
        }
        String routerName = routerDAO.fromIp(lastIface).getName();
        if (routerName == null) {
            throw new BSSException("getAsNumber: no router in database for " + lastIface);
        }
        String nextDomainAsNum =
            this.pathfinder.findNextDomain(routerName, nextHop);
        if (nextDomainAsNum.equals("noSuchInstance")) { return null; }

        DomainDAO domainDAO = new DomainDAO();
        domainDAO.setSession(this.session);
        Domain nextDomain = domainDAO.queryByParam(nextDomainAsNum, "asNum");
        return nextDomain;
    }
    
     /**
     * Finds next domain by looking up first hop in peerIpaddr table
     *
     * @param nextHop first IP outside of domain to lookup in database
     * @return Domain an instance associated with the next domain, if any
     * @throws BSSException
     */
    public Domain getNextDomainFromDB()
            throws BSSException {

        PeerIpaddrDAO peerDAO = new PeerIpaddrDAO();
        peerDAO.setSession(this.session);
        Domain nextDomain = null;

        this.log.info("getNextDomain.nextHop", this.nextHop);
       
       	if(nextHop != null){
       		nextDomain = peerDAO.getDomain(nextHop);
       		if(nextDomain != null){
       			this.log.info("getNextDomain.nextDomain", nextDomain.getAsNum()+"");
       		}
       	}
       	
       	return nextDomain;
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

    public Path getPath(List<String> hops,
                        String ingressIP, String egressRouterIP) 
                        throws net.es.oscars.bss.BSSException {

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
        } else { 
            path = currentPath; 
        }

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

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops)  {
            this.log.info("hop: ", hop);
            String loopbackFound = "";
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
     * Gets next hop outside of this domain.
     *
     * @param hops list of IP addresses
     * @return String representation of next hop
     */
    public String nextExternalHop(List<String> hops) {

        Ipaddr ipaddr = null;
        String outsideHop = "";
        boolean ingressFound = false;
        
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops) {
            // get IP address
            ipaddr = ipaddrDAO.queryByParam("ip", hop);
            if(ingressFound && ipaddr == null){
            	outsideHop = hop;
                break; 
            }else if(ipaddr != null){
				ingressFound = true;           	
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
