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

        List<PathElem> localSegment = this.extractLocalSegment(requestedPath);
        Link localIngress = null;
        Link localEgress = null;

        boolean pathTerminatesLocally = this.pathTerminatesLocally(requestedPath);

        // Case 1: We HAVE received a set of hops in the requested path
        if (this.isPathSpecified(requestedPath)) {

            // if we have a set of hops, our ingress must always be specified
            localIngress = this.firstLocalLink(requestedPath);

            // if the reservation terminates here, we know our "egress"
            if (pathTerminatesLocally) {
                if (localSegment.size() < 2) {
                    throw new PathfinderException("Local segment too short");
                }
                localEgress = this.lastLocalLink(requestedPath);
            } else {
                // otherwise, find the path to the next hop
                String firstAfterLocal = this.firstHopAfterLocal(requestedPath);
                localEgress = this.findEgressTo(localIngress.getFQTI(), firstAfterLocal, resv);
                this.log.debug("Local egress is: "+localEgress.getFQTI());
            }

            PathElem nextDomainIngress = null;
            if (!pathTerminatesLocally) {
                // We should always have the ingress of the next domain in our DB
                nextDomainIngress = new PathElem();
                nextDomainIngress.setLink(localEgress.getRemoteLink());
                nextDomainIngress.setUrn(localEgress.getRemoteLink().getFQTI());
                this.log.debug("Next domain ingress will be: "+localEgress.getRemoteLink().getFQTI());
            }

            boolean addedLocalPes = false;

            this.log.debug("Reconciling requested path");
            for (PathElem pe : requestedPath.getPathElems()) {
                this.log.debug("Examining: "+pe.getUrn());
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
                        this.log.debug("Added non-local: "+pe.getUrn());
                    }

                // All the local hops we got passed are just replaced by our ingress and egress
                } else {
                    if (!addedLocalPes) {
                        PathElem ingress = new PathElem();
                        ingress.setLink(localIngress);
                        ingress.setUrn(localIngress.getFQTI());
                        this.log.debug("Added local ingress: "+localIngress.getFQTI());

                        PathElem egress = new PathElem();
                        egress.setLink(localEgress);
                        egress.setUrn(localEgress.getFQTI());

                        this.log.debug("Added local egress: "+localEgress.getFQTI());

                        interdomainPath.addPathElem(ingress);
                        interdomainPath.addPathElem(egress);
                        if (!pathTerminatesLocally) {
                            interdomainPath.addPathElem(nextDomainIngress);
                            interdomainPath.setNextDomain(nextDomainIngress.getLink().getPort().getNode().getDomain());
                            this.log.debug("Added next domain ingress: "+nextDomainIngress.getLink().getFQTI());
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

                localEgress = this.findEgressTo(this.findEndpoints(requestedPath).get("src"),
                                                this.findEndpoints(requestedPath).get("dest"), resv);

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

        paths.add(interdomainPath);            // If no explicit path for layer 2, we must fill this in

        for (PathElem pe : interdomainPath.getPathElems()) {
            this.log.debug("Interdomain hop:"+pe.getUrn());
        }

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
     * @param src the source endpoint identifier
     * @param dst the destination endpoint identifier
     * @param redv the reservation
     * @return the local egress link
     * @throws PathfinderException
     */
    protected Link findEgressTo(String src, String dst, Reservation resv) throws PathfinderException {
        DomainDAO domDAO = new DomainDAO(dbname);
        Link link = domDAO.getFullyQualifiedLink(dst);


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
