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

        
        
        PathElem localIngressPE = null;
        PathElem localEgressPE = null; 
        
        List<PathElem> localSegment = this.extractLocalSegment(requestedPath);
        if (localSegment != null && localSegment.size() >= 2) {
            localIngressPE = localSegment.get(0);
            localEgressPE = localSegment.get(localSegment.size() -1);
        }
        

        boolean pathTerminatesLocally = this.pathTerminatesLocally(requestedPath);

        // Case 1: We HAVE received a set of hops in the requested path
        if (this.isPathSpecified(requestedPath)) {
            Link localEgressLink = null;
            // if we have a set of hops, our ingress must always be specified
            Link localIngressLink = this.firstLocalLink(requestedPath);

            // if the reservation terminates here, we know our "egress"
            if (pathTerminatesLocally) {
                if (localSegment.size() < 2) {
                    throw new PathfinderException("Local segment too short");
                }
                localEgressLink = this.lastLocalLink(requestedPath);
            } else {
                // otherwise, find the path to the next hop
                String firstAfterLocal = this.firstHopAfterLocal(requestedPath);
                localEgressLink = this.findEgressTo(localIngressLink.getFQTI(), firstAfterLocal, resv);
                this.log.debug("Local egress is: "+localEgressLink.getFQTI());
            }

            PathElem nextDomainIngressPE = null;
            if (!pathTerminatesLocally) {
                // We should always have the ingress of the next domain in our DB
                nextDomainIngressPE = new PathElem();
                nextDomainIngressPE.setLink(localEgressLink.getRemoteLink());
                nextDomainIngressPE.setUrn(localEgressLink.getRemoteLink().getFQTI());
                this.log.debug("Next domain ingress will be: "+localEgressLink.getRemoteLink().getFQTI());
            }

            boolean addedLocalPes = false;

            this.log.debug("Reconciling requested path");
            for (PathElem pe : requestedPath.getPathElems()) {
                this.log.debug("Examining: "+pe.getUrn());
                PathElem pecopy = null;
                // Non-local hops get copied
                if (pe.getLink() == null || !pe.getLink().getPort().getNode().getDomain().isLocal()) {
                    if (nextDomainIngressPE == null || !pe.getUrn().equals(nextDomainIngressPE.getUrn())) {
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
                    // just do this once, after that ignore all local hops
                    if (!addedLocalPes) {
                        PathElem newIngressPE = null;
                        PathElem newEgressPE = null;
                        try {
                            newIngressPE = PathElem.copyPathElem(localIngressPE);
                            newEgressPE = PathElem.copyPathElem(localEgressPE);
                        } catch (BSSException e) {
                            log.error(e);
                            throw new PathfinderException(e.getMessage());
                        }
                        this.log.debug("Added local ingress: "+newIngressPE.getLink().getFQTI());
                        this.log.debug("Added local egress: "+newEgressPE.getLink().getFQTI());

                        interdomainPath.addPathElem(newIngressPE);
                        interdomainPath.addPathElem(newEgressPE);
                        
                        if (!pathTerminatesLocally) {
                            interdomainPath.addPathElem(nextDomainIngressPE);
                            interdomainPath.setNextDomain(nextDomainIngressPE.getLink().getPort().getNode().getDomain());
                            this.log.debug("Added next domain ingress: "+nextDomainIngressPE.getLink().getFQTI());
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
                Link localIngressLink = domDAO.getFullyQualifiedLink(this.findEndpoints(requestedPath).get("src"));

                Link localEgressLink = this.findEgressTo(this.findEndpoints(requestedPath).get("src"),
                                                this.findEndpoints(requestedPath).get("dest"), resv);

                PathElem newIngressPE = new PathElem();
                newIngressPE.setLink(localIngressLink);
                newIngressPE.setUrn(localIngressLink.getFQTI());
                PathElem newEgressPE = new PathElem();
                newEgressPE.setLink(localEgressLink);
                newEgressPE.setUrn(localEgressLink.getFQTI());
                
                try {
                    PathElem.copyPathElemParams(newIngressPE, requestedPath.getPathElems().get(0), null);
                    PathElem.copyPathElemParams(newEgressPE, requestedPath.getPathElems().get(requestedPath.getPathElems().size() -1), null);
                } catch (BSSException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                interdomainPath.addPathElem(newIngressPE);
                interdomainPath.addPathElem(newEgressPE);

                if (!pathTerminatesLocally) {
                    // We should always have the ingress of the next domain in our DB
                    if (localEgressLink.getRemoteLink() == null) {
                        throw new PathfinderException("Cannot locate next domain ingress!");
                    }
                    PathElem nextDomainIngress = new PathElem();
                    nextDomainIngress.setLink(localEgressLink.getRemoteLink());
                    nextDomainIngress.setUrn(localEgressLink.getRemoteLink().getFQTI());
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

            Link possiblelocalEgressLink = link.getRemoteLink();
            if (possiblelocalEgressLink != null) {
                if (possiblelocalEgressLink.getPort().getNode().getDomain().isLocal()) {
                    return possiblelocalEgressLink;
                }
            }
        }
        return null;
    }


}
