package net.es.oscars.pathfinder;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;


/**
 * This class contains methods for handling PCE's (path computation elements)
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PCEManager {
    private Logger log;
    private PCE pathfinder;
    private String dbname;

    public PCEManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     * Finds path from source to destination, taking into account ingress
     * and egress routers if specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @param reqPath CommonPath instance, may be null
     * @return path a CommonPath instance
     * @throws PathfinderException
     */
    public CommonPath findPath(String srcHost, String destHost,
                         String ingressRouterIP, String egressRouterIP,
                         CommonPath reqPath)
            throws PathfinderException {

        CommonPath path = null;
        List<CommonPathElem> elems = null;

        this.log.info("findPath.start");
        if (reqPath != null) {
             this.log.debug("findPath-reqPath not null: " + reqPath.toString());
        }
        String pathMethod = this.getPathMethod();
        this.log.info("pathfinder method is " + pathMethod);
        if (pathMethod == null) { return null; }
        this.pathfinder = 
            new PathfinderFactory().createPathfinder(pathMethod, this.dbname);
        if (reqPath == null) {
            elems = this.pathfinder.findPath(srcHost, destHost,
                                        ingressRouterIP, egressRouterIP);
            path = new CommonPath();
            path.setElems(elems);
        } else {
            elems = this.pathfinder.findPath(reqPath.getElems());
            path = new CommonPath();
            path.setVlanId(reqPath.getVlanId());
            path.setElems(elems);
        }
        return path;
    }

    /**
     * Does error checking to make sure method of pathfinding is set,
     * and that it is set correctly.
     *
     * @return string with name of pathfinding component to use.
     * @throws PathfinderException
     */
    public String getPathMethod() throws PathfinderException {
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
            throw new PathfinderException("No path computation method specified in oscars.properties.");
        }
        if (!pathMethod.equals("traceroute") && !pathMethod.equals("narb")) {
            throw new PathfinderException("Path computation method specified in oscars.properties must be either traceroute or narb.");
        }
        return pathMethod;
    }

    /**
     * Gets next hop past this domain.
     * Called only by bss.ReservationManager in its create method.
     * @return string with next hop
     */
    public String nextExternalHop() {
        return this.pathfinder.nextExternalHop();
    }

     /**
     * Returns path that includes both local and interdomain hops
     * Called by bss.ReservationManager.getCompletePath.
     *
     * @return path that includes both local and interdomain hops
     */
    public List<CommonPathElem> getCompletePath(){
        return this.pathfinder.getCompletePath();
    }
}
