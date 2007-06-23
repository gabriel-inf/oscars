package net.es.oscars.pathfinder;

import java.util.List;

/**
 * PCE is the interface implemented by the path computation element.
 *
 * @author David Robertson, Jason Lee
 */
public interface PCE {

    List<CommonPathElem> findPath(String srcHost, String destHost,
                        String ingressNodeIP,
                        String egressNodeIP)
        throws PathfinderException;

    // changes elements in place
    void findPath(CommonPath path)
        throws PathfinderException;
}
