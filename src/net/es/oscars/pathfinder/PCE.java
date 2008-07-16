package net.es.oscars.pathfinder;

import java.util.List;

import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.bss.Reservation;

/**
 * PCE is the interface implemented by the path computation element.
 *
 * @author David Robertson, Jason Lee
 */
public interface PCE {

    PathInfo findPath(PathInfo pathInfo, Reservation reservation)
        throws PathfinderException;
    
    String findIngress(PathInfo pathInfo)
        throws PathfinderException;
}
