package net.es.oscars.pathfinder.interdomain;

import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

/**
 * This is a generic Interdomain pathfinder that can handle
 * simple interdomain paths.
 *
 * Other Interdomain pathfinders should generally subclass this
 * and override findEgressTo(String urn)
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 *
 */

public class GenericInterdomainPathfinder extends Pathfinder implements InterdomainPCE {
    private Logger log = Logger.getLogger(GenericInterdomainPathfinder.class);

    public GenericInterdomainPathfinder(String dbname) {
        super(dbname);
    }

    public List<Path> findInterdomainPath(Reservation resv) throws PathfinderException {
        ArrayList<Path> paths = new ArrayList<Path>();

        Path requestedPath = null;
        Path interdomainPath = new Path();

        // Get path information:
        try {
            interdomainPath.setPathType(PathType.INTERDOMAIN);
            requestedPath = resv.getPath(PathType.REQUESTED);
            if (requestedPath == null) {
                throw new PathfinderException("Requested path not found!");
            }
        } catch (BSSException ex) {
            throw new PathfinderException(ex.getMessage());
        }


        Link localIngress = null;
        Link localEgress = null;

        boolean pathTerminatesLocally = this.pathTerminatesLocally(requestedPath);

        // Case 1: We HAVE received a set of hops in the requested path
        if (this.isPathSpecified(requestedPath)) {

            // if we have a set of hops, our ingress must always be specified
            localIngress = this.firstLocalLink(requestedPath);

            // if the reservation terminates here, we know our "egress"
            if (pathTerminatesLocally) {
                localEgress = this.lastLocalLink(requestedPath);
            } else {
                // otherwise, find the path to the next hop
                String firstAfterLocal = this.firstHopAfterLocal(requestedPath);
                localEgress = this.findEgressTo(firstAfterLocal);
            }

            PathElem nextDomainIngress = null;
            if (!pathTerminatesLocally) {
                // We should always have the ingress of the next domain in our DB
                nextDomainIngress = new PathElem();
                nextDomainIngress.setLink(localEgress.getRemoteLink());
                nextDomainIngress.setUrn(localEgress.getRemoteLink().getFQTI());
            }

            boolean addedLocalPes = false;

            for (PathElem pe : requestedPath.getPathElems()) {
                PathElem pecopy = null;
                // Non-local hops get copied
                if (!pathTerminatesLocally && (pe.getLink() == null || !pe.getLink().getPort().getNode().getDomain().isLocal())) {
                    if (!pe.getUrn().equals(nextDomainIngress.getUrn())) {
                        try {
                            pecopy = PathElem.copyPathElem(pe);
                        } catch (BSSException e) {
                            throw new PathfinderException(e.getMessage());
                        }
                        interdomainPath.addPathElem(pecopy);
                    }

                // All the local hops we got passed are just replaced by our ingress and egress
                } else {
                    if (!addedLocalPes) {
                        PathElem ingress = new PathElem();
                        ingress.setLink(localIngress);
                        ingress.setUrn(localIngress.getFQTI());
                        PathElem egress = new PathElem();
                        egress.setLink(localEgress);
                        egress.setUrn(localEgress.getFQTI());

                        interdomainPath.addPathElem(ingress);
                        interdomainPath.addPathElem(egress);
                        if (!pathTerminatesLocally) {
                            interdomainPath.addPathElem(nextDomainIngress);
                            interdomainPath.setNextDomain(nextDomainIngress.getLink().getPort().getNode().getDomain());
                        }

                        addedLocalPes = true;
                    }
                }
            }

        // Case 2: No path hops were specified and we only have the endpoints to work from
        // This will be the usual case when a user requestss reservation
        } else {
            // Note: no need to check other case because an exception will be thrown
            if (this.pathOriginatesLocally(requestedPath)) {
                DomainDAO domDAO = new DomainDAO(dbname);

                // FIXME: This currently only works for L2 identifiers
                localIngress = domDAO.getFullyQualifiedLink(this.findEndpoints(requestedPath).get("src"));

                localEgress = this.findEgressTo(this.findEndpoints(requestedPath).get("dest"));

                PathElem ingress = new PathElem();
                ingress.setLink(localIngress);
                ingress.setUrn(localIngress.getFQTI());

                PathElem egress = new PathElem();
                egress.setLink(localEgress);
                egress.setUrn(localEgress.getFQTI());

                interdomainPath.addPathElem(ingress);
                interdomainPath.addPathElem(egress);

                if (!pathTerminatesLocally) {
                    // We should always have the ingress of the next domain in our DB
                    if (localEgress.getRemoteLink() == null) {
                        throw new PathfinderException("Cannot locate next domain ingress!");
                    }
                    PathElem nextDomainIngress = new PathElem();
                    nextDomainIngress.setLink(localEgress.getRemoteLink());
                    nextDomainIngress.setUrn(localEgress.getRemoteLink().getFQTI());
                    interdomainPath.addPathElem(nextDomainIngress);

                    PathElem destination = new PathElem();
                    destination.setLink(null);
                    destination.setUrn(this.findEndpoints(requestedPath).get("dest"));

                    interdomainPath.setNextDomain(nextDomainIngress.getLink().getPort().getNode().getDomain());
                }

            }
        }

        paths.add(interdomainPath);

        return paths;
    }



    /**
     * Trivial egress finder
     * Locates the identifier in the urn parameter as a link in the local
     * topology DB.
     *
     * If found, and is a local link, that local link is our egress.
     *
     * If found, and  NOT a local link, but is connected to one,
     * then THAT local link is our egress.
     *
     * Should probably be overwritten by more sophisticated egress finders
     *
     * @param urn the endpoint identifier
     * @return the local egress link
     * @throws PathfinderException
     */
    protected Link findEgressTo(String urn) throws PathfinderException {
        DomainDAO domDAO = new DomainDAO(dbname);
        Link link = domDAO.getFullyQualifiedLink(urn);


        if (link != null) {
            if (link.getPort().getNode().getDomain().isLocal()) {
                return link;
            }

            Link possibleLocalEgress = link.getRemoteLink();
            if (possibleLocalEgress != null) {
                if (possibleLocalEgress.getPort().getNode().getDomain().isLocal()) {
                    return possibleLocalEgress;
                }
            }
        }
        return null;
    }


}
