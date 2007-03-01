package net.es.oscars.pathfinder;

import java.util.*;

import net.es.oscars.bss.topology.Path;

/**
 * PCE is the interface implemented by pathfinding classes.
 *
 * @author David Robertson, Jason Lee
 */
public interface PCE {

    String getNextHop(Path path);

    Path getPath(String src, String dst);
}
