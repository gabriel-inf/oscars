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
     * and egress routers if specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @return hops A list of elements containing the hops
     * @throws PathfinderException
     */
    public List<CommonPathElem> findPath(String srcHost, String destHost,
                                String ingressRouterIP, String egressRouterIP)
            throws PathfinderException {

        List<CommonPathElem> path = null;
        List<String> hops = null;
        List<String> localHops = null;
        List<String> reverseHops = null;
        List<String> forwardHops = new ArrayList<String>();
        String finalIngressIP = null;

        this.log.debug("findPath.start");
        if (srcHost == null) {
            throw new PathfinderException( "no source for path given");
        }
        if (destHost == null) {
            throw new PathfinderException( "no destination for path given");
        }
        if (ingressRouterIP != null) {
            // make sure the given ingress router has an OSCARS loopback
            finalIngressIP = this.getOSCARSLoopback(ingressRouterIP);
            if (finalIngressIP == null) {
                throw new PathfinderException(
                    "given ingress router " + ingressRouterIP +
                    " does not have an OSCARS loopback");
            }

        // else if the ingress is not given, find it by doing a reverse path
        } else {
            reverseHops = this.reversePath(egressRouterIP, srcHost);
            // go in forward direction to get Juniper ingress
            for (int i=reverseHops.size()-1; i >= 0; i--) {
                forwardHops.add(reverseHops.get(i));
            }
            finalIngressIP = this.getIngressLoopback(forwardHops);
            if (finalIngressIP == null) {
                throw new PathfinderException("path between src and host " +
                    "does not contain an OSCARS loopback");
            }

        }
        // if no explicit egress, just find path from ingress to destination
        if (egressRouterIP == null) {
            hops = this.traceroute(finalIngressIP, destHost);
            // get local hops from this last path, and next external hop
            // as a side effect
            localHops = this.getLocalHops(hops);
            if (localHops.size() == 1) {
                throw new PathfinderException("no hops past local path: ");
            }
            egressRouterIP = localHops.get(localHops.size()-1);
        } else {
            // make sure the given egress router is in the database
            if (!this.isLocalHop(egressRouterIP)) {
                throw new PathfinderException(
                    "given egress router is not in the database");
            }
            // get hops from egress to destHost (to get next hop)
            List<String> laterHops = this.traceroute(egressRouterIP,
                                                     destHost);
            if (laterHops.size() == 1) {
                throw new PathfinderException("no hops past egress router: " +
                                              egressRouterIP);
            }
            this.nextHop = laterHops.get(1);
            // get local path
            localHops = this.traceroute(finalIngressIP, egressRouterIP);
        }
        path = this.pathFromHops(localHops, finalIngressIP, egressRouterIP);
        return path;
    }

    /**
     * Returns list of hops that are in the local domain, given a list.
     * Note that at this point it must be a complete path, and all hops must be
     * strict.  (The dragon component allows loose hops.)  Also, sets nextHop
     * to the first hop past the local domain.
     *
     * @param reqPath list of CommonPathElem containing hops of entire path
     * @return list of elements representing only hops within the domain
     * @throws PathfinderException
     */
    public List<CommonPathElem> findPath(List<CommonPathElem> reqPath)
            throws PathfinderException {

        this.log.debug("findPath.explicitPath.start");
        // check for errors
        if (reqPath ==  null) {
            throw new PathfinderException(
                    "null explicit path provided to traceroute component");
        }
        List<CommonPathElem> localPath = this.getLocalPath(reqPath);
        for (int i=0; i < localPath.size(); i++) {
            if (localPath.get(i).isLoose()) {
                throw new PathfinderException("traceroute component " +
                    "does not currently accept loose hops");
            }
        }
        this.log.debug("findPath.explicitPath.finish");
        return localPath;
    }

    /**
     * Performs reverse traceroute to source.
     *
     * @param egressRouterIP string with egress router, if any
     * @param srcHost string with IP address of source host
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    private List<String> reversePath(String egressRouterIP, String srcHost)
            throws PathfinderException {

        String src = null;
        List<String> hops = null;

        // use egress router, if given, as the source of the traceroute
        if (egressRouterIP != null) {
            src = egressRouterIP;

        } else { // otherwise use default router as source
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
    
    /**
     * Returns path that includes both local and interdomain hops.
     * Used mainly in forwarding requests. Since passing a traceroute path
     * would mean passing all strict hops essentially, just return null for
     * now.  NOTE that this means getCompletePath cannot be called
     * by ReservationAdapter.  TODO:  FIX
     *
     * @return list of elements representing only hops within the domain
     */
    public List<CommonPathElem> getCompletePath(){
        return null;
    }
}
