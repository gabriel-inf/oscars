package net.es.oscars.pathfinder;

import java.util.List;

import org.hibernate.*;

import net.es.oscars.wsdlTypes.ExplicitPath;

/**
 * PCE is the interface implemented by the path computation element.
 *
 * @author David Robertson, Jason Lee
 */
public interface PCE {

    Path findPath(String srcHost, String destHost,
                  String ingressRouterIP, String egressRouterIP, ExplicitPath reqPath)
        throws PathfinderException;

    String getNextHop();
}
