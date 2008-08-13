package net.es.oscars.pathfinder;

import net.es.oscars.pathfinder.dragon.TERCEPathfinder;
import net.es.oscars.pathfinder.db.DBPathfinder;
import net.es.oscars.pathfinder.perfsonar.PSPathfinder;

/**
 * This class contains a factory method to create a Pathfinder instance
 * implementing the PCE interface.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathfinderFactory {

    /**
     * Factory method.
     *
     * @return pathfinder An instance of a class implementing the PCE interface.
     */
    public PCE createPathfinder(String pathMethod, String dbname) {

        // only two choices at the moment
        if (pathMethod.equals("terce")) {
            return new TERCEPathfinder(dbname);
        } else if (pathMethod.equals("database")) {
            return new DBPathfinder(dbname);
        } else if (pathMethod.equals("perfsonar")) {
            return new PSPathfinder(dbname);
        }
        return null;
    }
}
