package net.es.oscars.pathfinder;

import net.es.oscars.pathfinder.dragon.NARBPathfinder;
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
    public PCE createPathfinder(String pathMethod) {

        // only two choices at the moment
        if (pathMethod.equals("traceroute")) {
            return new TraceroutePathfinder();
        } else if (pathMethod.equals("narb")) {
            return new NARBPathfinder();
        }
        return null;
    }
}
