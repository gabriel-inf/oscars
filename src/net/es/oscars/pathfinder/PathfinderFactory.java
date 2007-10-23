package net.es.oscars.pathfinder;

import net.es.oscars.pathfinder.dragon.TERCEPathfinder;
import net.es.oscars.pathfinder.overlay.OverlayPathfinder;
import net.es.oscars.pathfinder.traceroute.TraceroutePathfinder;

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

        // only three choices at the moment
        if (pathMethod.equals("traceroute")) {
            return new TraceroutePathfinder(dbname);
        } else if (pathMethod.equals("terce")) {
            return new TERCEPathfinder(dbname);
        } else if (pathMethod.equals("overlay")) {
            return new OverlayPathfinder(dbname);
        }
        return null;
    }
}
