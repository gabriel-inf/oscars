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
     * and egress nodes if specified by user.  Sets information, such
     * as local hops in path, and ingress and egress nodes if not given.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressNodeIP string with address of ingress node, if any
     * @param egressNodeIP string with address of egress node, if any
     * @param path CommonPath instance to fill in
     * @throws PathfinderException
     */
    public void findPath(String srcHost, String destHost,
                         String ingressNodeIP, String egressNodeIP,
                         CommonPath path)
            throws PathfinderException {

        List<CommonPathElem> pathElems = null;

        this.log.info("PCEManager.findPath.start");
        if (path.getElems() != null) {
             this.log.debug("findPath, explicit path given: " +
                             path.toString());
        }
        String pathMethod = this.getPathMethod();
        this.log.info("pathfinder method is " + pathMethod);
        if (pathMethod == null) { return; }
        this.pathfinder = 
            new PathfinderFactory().createPathfinder(pathMethod, this.dbname);
        // find complete path
        if (path.getElems() == null) {
            pathElems = this.pathfinder.findPath(srcHost, destHost,
                                             ingressNodeIP, egressNodeIP);
            path.setElems(pathElems);
        } else {
            // change given path elements in place
            this.pathfinder.findPath(path);
        }
        this.log.info("PCEManager.findPath.end");
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
            throw new PathfinderException(
                "No path computation method specified in oscars.properties.");
        }
        if (!pathMethod.equals("traceroute") && !pathMethod.equals("narb")) {
            throw new PathfinderException(
                "Path computation method specified in oscars.properties " +
                "must be either traceroute or narb.");
        }
        return pathMethod;
    }
}
