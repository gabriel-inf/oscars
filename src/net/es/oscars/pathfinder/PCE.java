package net.es.oscars.pathfinder;

import java.util.List;

/**
 * PCE is the interface implemented by the path computation element.
 *
 * @author David Robertson, Jason Lee
 */
public interface PCE {

    List<CommonPathElem> findPath(String srcHost, String destHost,
                        String ingressRouterIP,
                        String egressRouterIP)
        throws PathfinderException;

    List<CommonPathElem> findPath(List<CommonPathElem> reqPath)
        throws PathfinderException;

    String nextExternalHop();
    
    List<CommonPathElem> getCompletePath();
}
