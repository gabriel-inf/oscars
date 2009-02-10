package net.es.oscars.pathfinder;

import java.util.*;
import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;

/**
 * This class contains methods for handling PCE's (path computation elements)
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PCEManager {
    private Logger log;
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
     * @param resv parameters used to find the local path
     * @return local path used for resource scheduling
     * @throws PathfinderException
     */
    public List<Path> findLocalPath(Reservation resv) throws PathfinderException {
        this.log.info("PCEManager.findLocalPath.start");

        List<String> pathMethods = this.getPathMethods(PathType.LOCAL);
        List<Path> results = null;
        
        if (pathMethods != null) {
            for( String method : pathMethods ) {
                try {
                    this.log.info("PCEManager.findLocalPath."+method+".start");
                    LocalPCE pathfinder = new PathfinderFactory().getLocalPCE(method, this.dbname);
                    results = pathfinder.findLocalPath(resv);
                    this.log.info("PCEManager.findLocalPath."+method+".end");
                 } catch (Exception ex) {
                    this.log.error("Exception caught finding local path using method "+method+": "+ex.getMessage());
                    ex.printStackTrace();
                 }

                 if (results != null && !results.isEmpty())
                     break;
            }
        }
        
        this.log.info("PCEManager.findLocalPath.end");
        return results;
    }
    
    /**
     * Finds path from source to destination, taking into account information,
     * if specified by user.  Sets information, such
     * as local hops in path.
     *
     * @param resv parameters used to find the local path
     * @return local path used for resource scheduling
     * @throws PathfinderException
     */
    public List<Path> findInterdomainPath(Reservation resv) throws PathfinderException {
        this.log.info("PCEManager.findLocalPath.start");

        List<String> pathMethods = this.getPathMethods(PathType.INTERDOMAIN);
        List<Path> results = null;
        
        if (pathMethods != null) {
            for( String method : pathMethods ) {
                try {
                    this.log.info("PCEManager.findInterdomainPath."+method+".start");
                    InterdomainPCE pathfinder = new PathfinderFactory().getInterdomainPCE(method, this.dbname);
                    results = pathfinder.findInterdomainPath(resv);
                    this.log.info("PCEManager.findInterdomainPath."+method+".end");
                 } catch (Exception ex) {
                    this.log.error("Exception caught finding interdomain path using method "+method+": "+ex.getMessage());
                    ex.printStackTrace();
                 }

                 if (results != null && !results.isEmpty())
                     break;
            }
        }
        
        this.log.info("PCEManager.findInterdomainPath.end");
        return results;
    }

    /**
     * Does error checking to make sure method of pathfinding is set,
     * and that it is set correctly.
     *
     * @return Ordered list of strings containing which pathfinding components to try.
     * @throws PathfinderException
     */
    public List<String> getPathMethods(String pfType) throws PathfinderException {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("pathfinder", true);

        /* Determine whether to perform path calculation.  Path calculation
         * may not be desired if testing other components. */
        String findPath = props.getProperty("findPath");
        if (findPath == null || findPath.equals("0")) {
            return null;
        }

        String pathMethod = props.getProperty("pathMethod." + pfType);
        if(pathMethod == null){
            pathMethod = props.getProperty("pathMethod");
        }
        if (pathMethod == null) {
            throw new PathfinderException(
                "No path computation method specified in oscars.properties.");
        }

        String [] methods = pathMethod.split(",");

        ArrayList <String> retMethods = new ArrayList<String>();

        for( String method : methods) {
            String newMethod = method.trim();
            if (!newMethod.equals(PCEMethod.DATABASE) &&
                !newMethod.equals(PCEMethod.STATIC) &&
                !newMethod.equals(PCEMethod.PERFSONAR)) {
                throw new PathfinderException(
                    "Path computation method specified in oscars.properties " +
                    "must be one of: static, database or perfsonar.");
            }
            retMethods.add(newMethod);
        }

        return retMethods;
    }
}
