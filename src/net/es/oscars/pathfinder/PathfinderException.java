package net.es.oscars.pathfinder;

/**
 * PathfinderException is the top-level exception thrown by pathfinder methods.
 */
public class PathfinderException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public PathfinderException(String msg) {
        super(msg);
    }
}
