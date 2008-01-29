package net.es.oscars.pathfinder;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.PathInfo;


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
     * Finds path from source to destination, taking into account information,
     * if specified by user.  Sets information, such
     * as local hops in path.
     *
     * @param pathInfo instance containing layer 2 or layer 3 information
     * @throws PathfinderException
     */
    public boolean findPath(PathInfo pathInfo) throws PathfinderException {

        this.log.info("PCEManager.findPath.start");
        String pathMethod = this.getPathMethod();
        // TODO:  better method; override traceroute method for now if
        // ERO is given
        if (pathMethod.equals("traceroute") &&
            (pathInfo.getPath() != null)) {
            pathMethod = "overlay";
        }
        this.log.info("pathfinder method is " + pathMethod);
        if (pathMethod == null) { return false; }
        this.pathfinder = 
            new PathfinderFactory().createPathfinder(pathMethod, this.dbname);
        boolean isExplicit = this.pathfinder.findPath(pathInfo);
        this.log.info("PCEManager.findPath.end");
        return isExplicit;
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
        if (!pathMethod.equals("traceroute") && !pathMethod.equals("terce")) {
            throw new PathfinderException(
                "Path computation method specified in oscars.properties " +
                "must be either traceroute or terce.");
        }
        return pathMethod;
    }
}