package net.es.oscars.pathfinder.traceroute;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import net.es.oscars.*;
import net.es.oscars.bss.BSSException;


/**
 * Pathfinder performs traceroutes to find path from source to destination
 * within the local domain.
 */
public class Pathfinder {
    private LogWrapper log;
    private JnxSNMP jnxSnmp;

    /** Constructor. */
    public Pathfinder()
            throws BSSException {
        this.log = new LogWrapper(this.getClass());
        try {
            this.jnxSnmp = new JnxSNMP();
        } catch (IOException e) {
            throw new BSSException(e.getMessage());
        }
    }

    /**
     * Performs traceroute from ingress to destination.
     *
     * @param destHost string with IP address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @return hops list of strings with addresses in path
     * @throws BSSException
     */
    public ArrayList<String> forwardPath(String destHost,
                             String ingressRouterIP, String egressRouterIP)
            throws BSSException {

        ArrayList<String> hops = null;
        ArrayList<String> unused = null;

        // If egress specified by the admin
        if (egressRouterIP != null) {
            // Do a forward trace to get the local path
            unused =
                this.forwardPath(ingressRouterIP, egressRouterIP);

            /* Do a forward trace from the egress to the destination to get 
               the next hop */
            hops = this.forwardPath(egressRouterIP, destHost);
        /* Else, trace from ingress to reservation destination, and get
           next hop if any */
        } else {
            hops = this.forwardPath(ingressRouterIP, destHost);
        }
        return hops;
    }

    /**
     * Performs reverse traceroute to source.
     *
     * @param egressRouterIP string with egress router, if any
     * @param srcHost string with IP address of source host
     * @return hops list of strings with addresses in path
     * @throws BSSException
     */
    public ArrayList<String>
        reversePath(String egressRouterIP, String srcHost)
            throws BSSException {

        String src = null;
        ArrayList<String> hops = null;

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
     * Performs forward traceroute from given source and destination
     *     addresses.
     *
     * @param src source string
     * @param dest destination string
     * @return hops list of strings with addresses in path
     * @throws BSSException
     */
    public ArrayList<String> forwardPath(String src, String dest) 
        throws BSSException {

        /* Traceroute is performed whether the egress router is specified or 
           not, in order to get the path within the local domain, and 
           the autonomous system number of the next domain. */
        ArrayList<String> hops = traceroute(src, dest);
        this.log.info("forwardPath", hops.toString());
        return hops;
    }

    /**
     * Performs traceroute.
     *
     * @param src source string
     * @param dest destination string
     * @return hops list of strings with addresses in path
     * @throws BSSException
     */
    public ArrayList<String> traceroute(String src, String dest)
        throws BSSException {

        JnxTraceroute jnxTraceroute = null;
        String source = null;
        String pathSrc = null;
        ArrayList<String> hops = null;

        jnxTraceroute = new JnxTraceroute();
        try {
            pathSrc = jnxTraceroute.traceroute(src, dest);
        } catch (IOException e) {
            throw new BSSException(e.getMessage());
        }
        hops = jnxTraceroute.getHops();
        // prepend source to path
        hops.add(0, pathSrc);

        // if we didn't hop much, maybe the same router (is this needed?)
        if (hops.size() == 0) {
            throw new BSSException("same router?");
        }
        return hops;
    }

    /**
     * Gets the autonomous service number associated with an IP address by 
     * performing an SNMP query against the egress router.
     *
     * @param routerName a string containing the router's name
     * @param nextHop a string containing the IP address of the next hop
     * @return nextAsNumber a string containing autonomous service number
     * @throws BSSException
     */
    public String findNextDomain(String routerName, String nextHop)
            throws BSSException {

        String nextAsNumber = null;

        this.jnxSnmp.initializeSession(routerName);
        // TODO:  fix error handling
        String errorMsg = this.jnxSnmp.getError();
        if ((errorMsg != null) && (!errorMsg.equals(""))) {
            throw new BSSException("Unable to initialize SNMP session:" + errorMsg);
        }

        try {
            nextAsNumber = this.jnxSnmp.queryAsNumber(nextHop);
        } catch (IOException e) {
            throw new BSSException(e.getMessage());
        }

        errorMsg = this.jnxSnmp.getError();
        if (errorMsg != null) {
            this.log.info("findNextDomain", errorMsg);
            return "noSuchInstance";
        }
        if ((nextAsNumber == null) || (nextAsNumber.equals("noSuchInstance"))) {
            return "noSuchInstance";
        }
        return nextAsNumber;
    }
}
