package net.es.oscars.pathfinder;

import java.util.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.bss.topology.*;
import net.es.oscars.pss.SNMP;
import net.es.oscars.pss.PSSException;

/**
 * @author David Robertson (dwrobertson@lbl.gov)
 *
 * This class contains utility methods for router and IP queries.
 * TODO:  due to exception handling, currently no good place to handle this.
 * Used in ReservationManager, TraceroutePathfinder and pss.  Too many
 * interpackage dependencies.
 */
public class Utils {
    private String dbname;
    private Logger log;

    public Utils(String dbname) {
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Finds the loopback IP associated with a router.  If description is
     * not null, router must be of that type to get the loopback.
     *
     * @param ip string with node IP
     * @return string with node's loopback, if any
     */
    public String getLoopback(String ip, String description)
            throws PathfinderException {

        String loopbackIP = null;

        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = nodeDAO.fromIp(ip);
        if (node == null) {
            return null;
        }
        try {
            String nodeName = node.getTopologyIdent();
            if (description != null) {
                if (!this.isRouterType(nodeName, description)) {
                    return null;
                }
                // temporary kludge
                if (description.equals("Juniper")) {
                    nodeName += "-oscars";
                }
            }
            loopbackIP = this.getIP(nodeName);
            if (loopbackIP == null) {
                throw new PathfinderException(
                    "Unable to look up IP for host " + nodeName);
            }
        } catch (IOException e) {
            throw new PathfinderException(e.getMessage());
        } catch (PSSException e) {
            throw new PathfinderException(e.getMessage());
        }
        return loopbackIP;
    }

    /**
     * Checks to see whether router is of a specified type using an SNMP query.
     *
     * @param ip string with router name
     * @param description string that sysDescr must contain
     * @throws IOException
     * @throws PSSException
     * @return boolean indicating whether the router is of the specified type
     */
    public boolean isRouterType(String routerName, String description)
            throws IOException, PSSException {
        SNMP snmp = new SNMP();
        snmp.initializeSession(routerName);
        String sysDescr = snmp.queryRouterType();
        snmp.closeSession();
        return sysDescr.contains(description);
    }

    /**
     * Checks name for validity, and returns an IP address if not already
     * in that format.
     *
     * @param hopId string with potential DNS name, or IPv4 or IPv6 address
     * @return string with IP address.
     */
    public String getIP(String hopId) {
        InetAddress inetAddr = null;
        // could do some very intricate checks for valid IPv4 or IPv6
        // address to avoid getByName, but doing this instead
        try {
            inetAddr = InetAddress.getByName(hopId);
        } catch (UnknownHostException e) {
            return null;
        }
        return inetAddr.getHostAddress();
    }
}
