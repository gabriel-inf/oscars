package net.es.oscars.pathfinder.traceroute;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pathfinder.*;

/**
 * TraceroutePathfinder performs traceroutes to find path from
 * source to destination within the local domain.
 */
public class TraceroutePathfinder extends Pathfinder implements PCE {
    private Logger log;
    private Properties props;

    public TraceroutePathfinder(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("traceroute", true);
        super.setDatabase(dbname);
    }

    /**
     * Finds path from source to destination, taking into account ingress
     * and egress nodes if specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressNodeIP string with address of ingress node, if any
     * @param egressNodeIP string with address of egress node, if any
     * @return hops A list of elements containing the hops
     * @throws PathfinderException
     */
    public List<CommonPathElem> findPath(String srcHost, String destHost,
                                String ingressNodeIP, String egressNodeIP)
            throws PathfinderException {

        List<String> hops = null;
        List<String> reverseHops = null;
        List<String> previousHops = new ArrayList<String>();
        List<String> laterHops = null;
        String finalIngressIP = null;

        this.log.debug("findPath.start");
        if (srcHost == null) {
            throw new PathfinderException( "no source for path given");
        }
        if (destHost == null) {
            throw new PathfinderException( "no destination for path given");
        }
        if (ingressNodeIP != null) {
            // make sure the given ingress node has an OSCARS loopback
            finalIngressIP = this.getOSCARSLoopback(ingressNodeIP);
            if (finalIngressIP == null) {
                throw new PathfinderException(
                    "given ingress node " + ingressNodeIP +
                    " does not have an OSCARS loopback");
            }
            // since storing complete path, need hops before ingress
            reverseHops = this.reversePath(ingressNodeIP, srcHost);
            // go in forward direction for later addition to the path
            for (int i=reverseHops.size()-1; i >= 0; i--) {
                previousHops.add(reverseHops.get(i));
            }
        // else if the ingress is not given, find it by doing a reverse path
        } else {
            reverseHops = this.reversePath(egressNodeIP, srcHost);
            // go in forward direction to get Juniper ingress
            for (int i=reverseHops.size()-1; i >= 0; i--) {
                previousHops.add(reverseHops.get(i));
            }
            finalIngressIP = this.getIngressLoopback(previousHops);
            if (finalIngressIP == null) {
                throw new PathfinderException("path between src and host " +
                    "does not contain an OSCARS loopback");
            }

        }
        // if no explicit egress, just find path from ingress to destination
        if (egressNodeIP == null) {
            hops = this.traceroute(finalIngressIP, destHost);
        } else {
            // make sure the given egress node is in the database
            if (!this.isLocalHop(egressNodeIP)) {
                throw new PathfinderException(
                    "given egress node is not in the database");
            }
            // get hops from egress to destHost (to get next hop)
            laterHops = this.traceroute(egressNodeIP, destHost);
            if (laterHops.size() == 1) {
                throw new PathfinderException("no hops past egress node: " +
                                              egressNodeIP);
            }
            // get interior path
            hops = this.traceroute(finalIngressIP, egressNodeIP);
        }
        List<String> completeHops = this.stitch(previousHops, hops, laterHops);
        List<CommonPathElem> path = this.pathFromHops(completeHops);
        this.log.debug("findPath.End");
        return path;
    }

    /**
     * Marks hops that are in the local domain, given a path.
     *
     * @param path CommonPath instance containing hops of complete path
     * @throws PathfinderException
     */
    public void findPath(CommonPath path)
            throws PathfinderException {

        this.log.debug("findPath.explicitPath.start");
        List<CommonPathElem> pathElems = path.getElems();
        // check for errors
        if (pathElems ==  null) {
            throw new PathfinderException(
                    "null explicit path provided to traceroute component");
        }
        this.setLocalHops(path);
        for (int i=0; i < pathElems.size(); i++) {
            CommonPathElem pathElem = pathElems.get(i);
            if ((pathElem.getDescription() != null) && pathElem.isLoose()) {
                throw new PathfinderException("traceroute component " +
                    "does not currently accept loose local hops");
            }
        }
        this.log.debug("findPath.explicitPath.finish");
    }

    /**
     * Given sections of a traceroute, eliminate duplicates and build complete
     * list of hops.  Some of the sections may be null.  Note that there are
     * some redundant checks between this and pathFromHops for locality to
     * the domain.  The check requires a database query.  Caching the IP addresses
     * from the database in a data structure would improve performance.
     *
     * @param previousHops list of addresses that are prior to the ingress
     * @param hops list of addresses from the ingress onwards
     * @param laterHops list of addresses further on than the egress
     * @return completeHops list of addresses from source to destination
     */
    private List<String> stitch(List<String> previousHops, List<String> hops,
                                List<String> laterHops) {

        this.log.info("stitch.start");
        List<String> completeHops = new ArrayList<String>();
        for (String hop: previousHops) {
            if (!this.isLocalHop(hop)) {
                this.log.debug("previous: " + hop);
                completeHops.add(hop);
            }
        }
        for (String hop: hops) {
            if (this.isLocalHop(hop)) {
                this.log.debug("local hop: " + hop);
                completeHops.add(hop);
            } else if (laterHops == null) {
                completeHops.add(hop);
                this.log.debug("non-local hop: " + hop);
            }
        }
        if (laterHops != null) {
            for (String hop: laterHops) {
                if (!this.isLocalHop(hop)) {
                    this.log.debug("non-local later hop: " + hop);
                    completeHops.add(hop);
                }
            }
        }
        this.log.info("stitch.end");
        return completeHops;
    }

    /**
     * Given a path, indicates which hops are in the local domain.
     *
     * @param path CommonPath instance with list containing hops to check
     * @return list of elements containing only local hops
     * @throws PathfinderException
     */
    private void setLocalHops(CommonPath path)
        throws PathfinderException {

        List<CommonPathElem> pathElems = path.getElems();
        CommonPathElem pathElem = null;
        CommonPathElem prevElem = null;
        boolean hopFound = false;
        boolean ingressFound = false;
        boolean egressFound = false;
        String hop = null;

        for (int i = 0; i < pathElems.size(); i++) {
            pathElem = pathElems.get(i);
            hop = pathElem.getIP();
            if (this.isLocalHop(hop)) {
                // set description
                if (hopFound) {
                    pathElem.setDescription("local");
                } else {
                    pathElem.setDescription("ingress");
                    ingressFound = true;
                }
                hopFound = true;
                this.log.info("setLocalHops, local: " + hop);
            } else if (hopFound && !egressFound) {
                if (prevElem != null) {
                    prevElem.setDescription("egress");
                }
                egressFound = true;
            }
            prevElem = pathElem;
        }
        // throw error if no local path found
        if (!hopFound) { 
            throw new PathfinderException(
                "No local hops found for specified path");
        }
        if (!ingressFound) {
            throw new PathfinderException(
                "No ingress loopback found for specified path");
        }
        if ((pathElems.size() > 1) && !egressFound) {
            throw new PathfinderException(
                "No egress loopback found for specified path");
        }
    }
    
    /**
     * Performs reverse traceroute to source.
     *
     * @param egressNodeIP string with egress node, if any
     * @param srcHost string with IP address of source host
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    private List<String> reversePath(String egressNodeIP, String srcHost)
            throws PathfinderException {

        String src = null;
        List<String> hops = null;

        // use egress node, if given, as the source of the traceroute
        if (egressNodeIP != null) {
            src = egressNodeIP;

        } else { // otherwise use default node as source
            src = this.props.getProperty("jnxSource");
        }
        // run the reverse traceroute to the reservation source
        hops = traceroute(src, srcHost);
        return hops;
    }

    /**
     * Performs traceroute.
     *
     * @param src source string
     * @param dest destination string
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    private List<String> traceroute(String src, String dest)
            throws PathfinderException {

        JnxTraceroute jnxTraceroute = null;
        String source = null;
        String pathSrc = null;
        List<String> hops = null;

        jnxTraceroute = new JnxTraceroute();
        try {
            pathSrc = jnxTraceroute.traceroute(src, dest);
        } catch (IOException ex) {
            throw new PathfinderException(ex.getMessage());
        }
        hops = jnxTraceroute.getHops();
        // prepend source to path
        hops.add(0, pathSrc);
        return hops;
    }
}
