package net.es.oscars.pathfinder;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.bss.Reservation;

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
     * @return local path used for resource scheduling
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation) throws PathfinderException {

        this.log.info("PCEManager.findPath.start");
        String pathMethod = this.getPathMethod();
        this.log.info("pathfinder method is " + pathMethod);
        if (pathMethod == null) {
            return null;
        }
        this.pathfinder =
            new PathfinderFactory().createPathfinder(pathMethod, this.dbname);
        PathInfo intraPath = this.pathfinder.findPath(pathInfo, reservation);
        this.log.info("PCEManager.findPath.end");
        return intraPath;
    }
    
    /**
     * Finds the local ingress of a path
     *
     * @param pathInfo instance containing layer 2 or layer 3 information
     * @return local ingress link id
     * @throws PathfinderException
     */
    public String findIngress(PathInfo pathInfo) throws PathfinderException {

        this.log.info("PCEManager.findIngress.start");
        String pathMethod = this.getPathMethod();
        this.log.info("pathfinder method is " + pathMethod);
        if (pathMethod == null) {
            return null;
        }
        this.pathfinder =
            new PathfinderFactory().createPathfinder(pathMethod, this.dbname);
        String ingress = this.pathfinder.findIngress(pathInfo);
        this.log.info("PCEManager.findIngress.end");
        return ingress;
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
        if (!pathMethod.equals("terce") && !pathMethod.equals("database")) {
            throw new PathfinderException(
                "Path computation method specified in oscars.properties " +
                "must be either terce or database.");
        }
        return pathMethod;
    }
}
