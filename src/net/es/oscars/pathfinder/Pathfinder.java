package net.es.oscars.pathfinder;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.topology.*;

/**
 * This class is intended to be subclassed by NARBPathfinder and
 * TraceroutePathfinder.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Andrew Lake (alake@internet2.edu)
 */
public class Pathfinder {
    private Logger log;
    private String dbname;

    public Pathfinder() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Converts list of hops to list of CommonPathElem instances, and
     * marks hops that are local.
     *
     * @param hops list of strings representing hops
     * @return pathElems list of CommonPathElem instances
     * @throws PathfinderException
     */
    protected List<CommonPathElem> pathFromHops(List<String> hops)
            throws PathfinderException {

        List<CommonPathElem> pathElems = new ArrayList<CommonPathElem>();
        CommonPathElem prevElem = null;
        boolean hopFound = false;
        boolean egressFound = false;

        this.log.info("pathFromHops.start");
        // get hops with loopbacks 
        for (String hop: hops)  {
            CommonPathElem pathElem = new CommonPathElem();
            if (this.isLocalHop(hop)) {
                // TODO:  is this correct
                pathElem.setLoose(true);
                // Scheduler will also check second hop if first does not
                // have loopback.  I'm not fixing the error checking at
                // this point unless it looks like the Cisco fix doesn't work.
                if (!hopFound) {
                    pathElem.setDescription("ingress");
                } else {
                    pathElem.setDescription("local");
                }
                hopFound = true;
                this.log.info("pathFromHops, local: " + hop);
            } else if (hopFound) {
                if ((prevElem != null) && !egressFound) {
                    prevElem.setDescription("egress");
                    egressFound = true;
                }
                this.log.info("pathFromHops, not local: " + hop);
            }
            pathElem.setIP(hop);
            pathElems.add(pathElem);
            prevElem = pathElem;
        }
        // throw error if no local path found
        if (!hopFound) { 
            throw new PathfinderException("No local hops found in path");
        }
        this.log.info("pathFromHops.finish");
        return pathElems;
    }

    
    protected void setDatabase(String dbname) {
        this.dbname = dbname;
    }

    /**
     * Get loopback, if any, for given node.  This is probably not the
     * right place for db operations.
     *
     * @param string with node IP
     * @return string with node's loopback, if any
     */
    protected String getLoopback(String ip) {

        Node node = null;
        String loopbackIP = null;

        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        node = nodeDAO.fromIp(ip);
        if ((node != null) && (node.getName() != null)) {
            loopbackIP = ipaddrDAO.getIpType(node.getName(), "wan-loopback");
            if (loopbackIP == null) {
                loopbackIP = ipaddrDAO.getIpType(node.getName(),
                                                 "oscars-loopback");
            }
        }
        return loopbackIP;
    }

    /**
     * Determines whether hop is potentially part of path by seeing if
     * it has an associated primary loopback.
     *
     * @param string with node IP
     * @return boolean with whether has a loopback
     */
    protected boolean isLocalHop(String ip) {
        return this.getLoopback(ip) != null;
    }

    /**
     * Get OSCARS loopback, if any, for given node.  This is probably not
     * the right place for db operations.
     *
     * @param string with node IP
     * @return string with node's  OSCARS loopback, if any
     */
    protected String getOSCARSLoopback(String ip) {

        Node node = null;
        String loopbackIP = null;

        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        node = nodeDAO.fromIp(ip);
        if ((node != null) && (node.getName() != null)) {
            loopbackIP = ipaddrDAO.getIpType(node.getName(),
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
}
