package net.es.oscars.pathfinder;

import java.util.List;

import net.es.oscars.wsdlTypes.PathInfo;


/**
 * PCE is the interface implemented by the path computation element.
 *
 * @author David Robertson, Jason Lee
 */
public interface PCE {

    PathInfo findPath(PathInfo pathInfo)
        throws PathfinderException;
}
