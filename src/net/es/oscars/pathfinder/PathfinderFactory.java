package net.es.oscars.pathfinder;

import net.es.oscars.pathfinder.db.DBPathfinder;
import net.es.oscars.pathfinder.perfsonar.PSPathfinder;
import net.es.oscars.pathfinder.staticroute.XMLFileLocalPathfinder;


/**
 * This class contains a factory method to create a Pathfinder instance
 * implementing the PCE interface.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathfinderFactory {

    /**
     * DEPRECATED
     * Factory method.
     *
     * @return pathfinder An instance of a class implementing the PCE interface.
     */
    public PCE createPathfinder(String pathMethod, String dbname) {

        // only three choices at the moment
        if (pathMethod.equals("terce")) {
            //return new XMLFileLocalPathfinder(dbname);
        } else if (pathMethod.equals("database")) {
            // return new DBPathfinder(dbname);
        } else if (pathMethod.equals("perfsonar")) {
            return new PSPathfinder(dbname);
        }
        return null;
    }


    /**
     * Factory method.
     *
     * @return interdomain pathfinder An instance of a class implementing the InterdomainPCE interface.
     */
    public InterdomainPCE getInterdomainPCE(String method, String dbname) {
        if (method.equals(PCEMethod.DATABASE)) {
            return new DBPathfinder(dbname);
        } else if (method.equals("another")) {
        }
        return null;
    }

    /**
     * Factory method.
     *
     * @return local pathfinder An instance of a class implementing the LocalPCE interface.
     */
    public LocalPCE getLocalPCE(String method, String dbname) {
        if (method.equals(PCEMethod.DATABASE)) {
            return new DBPathfinder(dbname);
        } else if (method.equals("another")) {
        }
        return null;
    }

}
