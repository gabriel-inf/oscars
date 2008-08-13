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

	PathInfo intraPath = null;

        this.log.info("PCEManager.findPath.start");

        List<String> pathMethods = this.getPathMethods();

        if (pathMethods != null) {
            for( String method : pathMethods ) {
                try {
                    this.log.info("PCEManager.findPath."+method+".start");
                    this.pathfinder = new PathfinderFactory().createPathfinder(method, this.dbname);
                    intraPath = this.pathfinder.findPath(pathInfo, reservation);
                    this.log.info("PCEManager.findPath."+method+".end");
                 } catch (Exception ex) {
                    this.log.error("Exception caught finding path using method "+method+": "+ex.getMessage());
                 }

                 if (intraPath != null)
                     break;
            }
        }

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

	String ingress = null;

        this.log.info("PCEManager.findIngress.start");

        List<String> pathMethods = this.getPathMethods();

        if (pathMethods != null) {
            for( String method : pathMethods ) {
                try {
                    this.log.info("PCEManager.findIngress."+method+".start");
                    this.pathfinder = new PathfinderFactory().createPathfinder(method, this.dbname);
                    ingress = this.pathfinder.findIngress(pathInfo);
                    this.log.info("PCEManager.findIngress."+method+".end");
                    break;
                 } catch (Exception ex) {
                    this.log.error("Exception caught finding ingress point using method "+method+": "+ex.getMessage());
                 }

                 if (ingress != null)
                     break;
            }
        }

        this.log.info("PCEManager.findIngress.end");

        return ingress;
    }

    /**
     * Does error checking to make sure method of pathfinding is set,
     * and that it is set correctly.
     *
     * @return Ordered list of strings containing which pathfinding components to try.
     * @throws PathfinderException
     */
    public List<String> getPathMethods() throws PathfinderException {
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

	String [] methods = pathMethod.split(",");

	ArrayList <String> retMethods = new ArrayList<String>();

	for( String method : methods) {
	    String newMethod = method.trim();

            if (!newMethod.equals("traceroute") && !newMethod.equals("terce") && !newMethod.equals("database") && !newMethod.equals("perfsonar"))
                throw new PathfinderException(
                    "Path computation method specified in oscars.properties " +
                    "must be either traceroute, terce, database or perfsonar.");

            retMethods.add(newMethod);
        }

        return retMethods;
    }
}
