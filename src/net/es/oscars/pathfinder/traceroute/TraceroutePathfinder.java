package net.es.oscars.pathfinder.traceroute;

import java.util.*;
import java.io.IOException;

import net.es.oscars.LogWrapper;
import net.es.oscars.pathfinder.*;
import net.es.oscars.wsdlTypes.ExplicitPath;


/**
 * TraceroutePathfinder performs traceroutes to find path from source to destination
 * within the local domain.
 */
public class TraceroutePathfinder extends Pathfinder implements PCE {
    private LogWrapper log;
    private String nextHop;

    /** Constructor. */
    public TraceroutePathfinder() {
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
     * @return hops A list of strings containing the hops
     * @throws PathfinderException
     */
    public Path findPath(String srcHost, String destHost,
                         String ingressRouterIP, String egressRouterIP, ExplicitPath reqPath)
            throws PathfinderException {

        List<String> hops = null;
        List<String> reverseHops = null;
        String ingressIP = null;

        /* If the ingress router is given, make sure it is in the database,
           and then return as is. */
        if (ingressRouterIP != null) {
            ingressIP = this.checkIngressLoopback(ingressRouterIP);
        } else {
            reverseHops = this.reversePath(egressRouterIP, srcHost);
            ingressIP = this.lastLoopback(reverseHops);
        }
        this.log.info("createReservation.ingressIP", ingressIP);

        // find internal path
        hops = this.forwardPath(destHost, ingressIP, egressRouterIP);

        // set next hop for static lookup - do this now because path not yet
        // reduced to local path
        this.nextHop = this.nextExternalHop(hops);

        // recalculate path to be *inside*
        String lastIface = this.lastInterface(hops);
        hops = this.traceroute(ingressIP, lastIface);
        return this.checkPath(hops, ingressIP, egressRouterIP);
    }
    
    public String getNextHop() {
        return this.nextHop;
    }

    /**
     * Performs traceroute from ingress to destination.
     *
     * @param destHost string with IP address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    public List<String> forwardPath(String destHost,
                             String ingressRouterIP, String egressRouterIP)
            throws PathfinderException {

        List<String> hops = null;
        List<String> unused = null;

        // If egress specified by the admin
        if (egressRouterIP != null) {
            // Do a forward trace to get the local path
            unused =
                this.traceroute(ingressRouterIP, egressRouterIP);

            /* Do a forward trace from the egress to the destination to get 
               the next hop */
            hops = this.traceroute(egressRouterIP, destHost);
        /* Else, trace from ingress to reservation destination, and get
           next hop if any */
        } else {
            hops = this.traceroute(ingressRouterIP, destHost);
        }
        this.log.info("forwardPath", hops.toString());
        return hops;
    }

    /**
     * Performs reverse traceroute to source.
     *
     * @param egressRouterIP string with egress router, if any
     * @param srcHost string with IP address of source host
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    public List<String> reversePath(String egressRouterIP, String srcHost)
            throws PathfinderException {

        String src = null;
        List<String> hops = null;

        /* If the egress router is given, use it as the source of 
           the traceroute.*/
        if (egressRouterIP != null) {
            src = egressRouterIP;
        }else { /* Otherwise use default router as source */
            src = "default";
        }
        // Run the reverse traceroute to the reservation source.
        hops = traceroute(src, srcHost);
        this.log.info("reversePath", hops.toString());
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
    public List<String> traceroute(String src, String dest)
        throws PathfinderException {

        JnxTraceroute jnxTraceroute = null;
        String source = null;
        String pathSrc = null;
        List<String> hops = null;

        jnxTraceroute = new JnxTraceroute();
        try {
            pathSrc = jnxTraceroute.traceroute(src, dest);
        } catch (IOException e) {
            throw new PathfinderException(e.getMessage());
        }
        hops = jnxTraceroute.getHops();
        // prepend source to path
        hops.add(0, pathSrc);

        // if we didn't hop much, maybe the same router (is this needed?)
        if (hops.size() == 0) {
            throw new PathfinderException("same router?");
        }
        return hops;
    }
}
