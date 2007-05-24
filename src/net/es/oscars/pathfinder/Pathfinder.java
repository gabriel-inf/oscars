package net.es.oscars.pathfinder;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.topology.*;

/**
 * This class is intended to be subclassed by NARBPathfinder and
 * TraceroutePathfinder.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class Pathfinder {
    protected String nextHop;   // TODO:  FIX use of global.
    private Logger log;
    private String dbname;

    public Pathfinder() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Returns next hop past local domain.
     *
     * @return nextHop string with next hop (global)
     */
    public String nextExternalHop() {
        return this.nextHop;
    }

    /**
     * Returns path with hops that are in the local domain, given a list
     * of hops.  Also sets nextHop to the first hop past the local domain.
     *
     * @param hops list of strings containing IP addresses
     * @param ingressIP string with address of ingress IP
     * @param egressIP string with address of egress IP
     * @return returns start of path containing only local hops
     * @throws PathfinderException
     */
    public List<CommonPathElem> pathFromHops(List<String> hops,
                             String ingressIP, String egressIP)
            throws PathfinderException {

        List<CommonPathElem> path = new ArrayList<CommonPathElem>();
        CommonPathElem pathElem = null;

        this.log.info("pathFromHops.start");
        // fill in PathElem elements
        for (String hop: hops) {
            pathElem = new CommonPathElem();
            // assuming strict if given series of hops
            pathElem.setLoose(false);
            if (hop == ingressIP) {
                pathElem.setDescription("ingress");
            } else if (hop == egressIP) {
                pathElem.setDescription("egress");
            }
            pathElem.setIP(hop);
            path.add(pathElem);
        }
        this.log.info("pathFromHops.finish");
        return path;
    }

    /**
     * Returns path with hops that are in the local domain, given the start of
     * a path.  Also sets nextHop to the first hop past the local domain.
     *
     * @param path list containing elems to check
     * @return list of elements containing only local hops
     * @throws PathfinderException
     */
    public List<CommonPathElem> getLocalPath(List<CommonPathElem> path)
            throws PathfinderException {

        CommonPathElem pathElem = null;
        CommonPathElem localPathElem = null;
        List<CommonPathElem> localPath = new ArrayList<CommonPathElem>();
        boolean hopFound = false;
        String hop = null;

        for (int i = 0; i < path.size(); i++) {
            pathElem = path.get(i);
            hop = pathElem.getIP();
            if (this.isLocalHop(hop)) {
                //copy path element
                localPathElem = new CommonPathElem();
                localPathElem.setIP(hop);
                localPathElem.setLoose(pathElem.isLoose());
                localPathElem.setDescription(pathElem.getDescription());
                localPath.add(localPathElem);
                this.log.info("getLocalPath, local: " + hop);
                hopFound = true;
            } else if (hopFound) {
                this.log.info("getLocalPath, not local: " + hop);
                this.nextHop = hop;
                break;
            }
        }
        // throw error if no local path found
        if (localPath == null) { 
            throw new PathfinderException(
                "No local hops found in path");
        }
        this.setIngressLoopback(localPath);
        this.setEgressLoopback(localPath);
        return localPath;
    }
    
    /**
     * Returns list of hops that are in the local domain, given a list of hops
     * as strings.  Also sets nextHop to the first hop past the local domain.
     *
     * @param hops List of Strings representing hops
     * @return returns list of strings representing only hops within the domain
     * @throws PathfinderException
     */
    public List<String> getLocalHops(List<String> hops)
            throws PathfinderException {

        ArrayList<String> localHops = new ArrayList<String>();
        boolean hopFound = false;
        
        this.log.info("getLocalHops.start");
        // get hops with loopbacks 
        for (String hop: hops)  {
            if (this.isLocalHop(hop)) {
                localHops.add(hop);
                this.log.info("getLocalHops, local: " + hop);
                hopFound = true;
            } else if (hopFound) {
                this.log.info("getLocalHops, not local: " + hop);
                this.nextHop = hop;
                break;
            }
        }
        // throw error if no local path found
        if (localHops.size() == 0) { 
            throw new PathfinderException(
                "No local hops found in path");
        }
        this.log.info("getLocalHops.finish");
        return localHops;
    }

    
    protected void setDatabase(String dbname) {
        this.dbname = dbname;
    }

    /**
     * Get loopback, if any, for given router.  This is probably not the
     * right place for db operations.
     *
     * @param string with router IP
     * @return string with router's loopback, if any
     */
    protected String getLoopback(String ip) {

        Router router = null;
        String loopbackIP = null;

        RouterDAO routerDAO = new RouterDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        router = routerDAO.fromIp(ip);
        if ((router != null) && (router.getName() != null)) {
            loopbackIP = ipaddrDAO.getIpType(router.getName(), "wan-loopback");
            if (loopbackIP == null) {
                loopbackIP = ipaddrDAO.getIpType(router.getName(),
                                                 "oscars-loopback");
            }
        }
        return loopbackIP;
    }

    /**
     * Determines whether hop is potentially part of path by seeing if
     * it has an associated primary loopback.
     *
     * @param string with router IP
     * @return boolean with whether has a loopback
     */
    public boolean isLocalHop(String ip) {
        return this.getLoopback(ip) != null;
    }

    /**
     * Get OSCARS loopback, if any, for given router.  This is probably not
     * the right place for db operations.
     *
     * @param string with router IP
     * @return string with router's  OSCARS loopback, if any
     */
    protected String getOSCARSLoopback(String ip) {

        Router router = null;
        String loopbackIP = null;

        RouterDAO routerDAO = new RouterDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        router = routerDAO.fromIp(ip);
        if ((router != null) && (router.getName() != null)) {
            loopbackIP = ipaddrDAO.getIpType(router.getName(),
                                             "oscars-loopback");
        }
        return loopbackIP;
    }

    /**
     * Gets ingress loopback IP, given list of hops representing local path
     *
     * @param hops list of strings containing IP addresses
     * @return ingressLoopback string with the ingress loopback IP
     * @throws PathfinderException
     */
    protected String getIngressLoopback(List<String> hops)
            throws PathfinderException {

        String loopbackIP = null;

        for (String hop: hops) {
            loopbackIP = this.getOSCARSLoopback(hop);
            if (loopbackIP != null) {
                return loopbackIP;
            }
        }
        return null;
    }

    /**
     * Sets ingress loopback IP, given start of path.
     *
     * @param path list of elems in path
     * @throws PathfinderException
     */
    private void setIngressLoopback(List<CommonPathElem> path)
            throws PathfinderException {

        String loopbackIP = null;

        if (path == null) {
            throw new PathfinderException(
                    "null path given when setting ingress loopback");
        }
        for (int i = 0; i < path.size(); i++) {
            String hop = path.get(i).getIP();
            loopbackIP = this.getOSCARSLoopback(hop);
            if (loopbackIP != null) {
                path.get(i).setDescription("ingress");
                break;
            }
        }
        if (loopbackIP == null) {
            throw new PathfinderException(
                "No ingress OSCARS loopback found in path");
        }
    }

    /**
     * Sets egress loopback IP, given start of path.  Currently
     * hard-wired to last element in path.
     *
     * @param path list of elems in path
     * @throws PathfinderException
     */
    private void setEgressLoopback(List<CommonPathElem> path)
            throws PathfinderException {

        String loopbackIP = null;

        if (path == null) {
            throw new PathfinderException(
                    "null path given when setting egress loopback");
        }
        String hop = path.get(path.size()-1).getIP();
        loopbackIP = this.getLoopback(hop);
        if (loopbackIP == null) {
            throw new PathfinderException(
                "No  egress loopback found for path");
        }
        path.get(path.size()-1).setDescription("egress");
    }
}
