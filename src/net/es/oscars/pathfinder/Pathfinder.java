package net.es.oscars.pathfinder;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.lookup.*;

/**
 * This class is intended to be subclassed by the other xxxPathfinder classes
 *
 * @author David Robertson (dwrobertson@lbl.gov), Andrew Lake (alake@internet2.edu)
 * @author Evangelos Chaniotakis (haniotak@es.net)
 *
 */
public class Pathfinder {
    private Logger log = Logger.getLogger(Pathfinder.class);
    protected String dbname;
    private DomainDAO domDAO;

    public Pathfinder(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        domDAO = new DomainDAO(dbname);

    }

    /**
     * Determines if the path elements have been specified
     *
     * @param requestedPath the path to examine
     * @return true if the path has been explicitly specified
     */
    public boolean isPathSpecified(Path requestedPath) {
        List<PathElem> pathElems = requestedPath.getPathElems();
        return !(pathElems == null || pathElems.isEmpty());
    }


    /**
     * Utility function to decide whether the requested path terminates
     * in this domain
     *
     * @param requestedPath a requested path with all local URNs already resolved to Links
     * @return whether the path terminates locally
     * @throws PathfinderException
     */
    public boolean pathTerminatesLocally(Path requestedPath) throws PathfinderException {
        List<PathElem> pathElems = requestedPath.getPathElems();
        if (this.isPathSpecified(requestedPath)) {
            // If the path is specified and the last hop has not already been
            // resolved to a Link, it is not local
            if (pathElems.get(pathElems.size()-1).getLink() == null ){
                return false;
            }
            // if it is in our DB, return the locality
            return pathElems.get(pathElems.size()-1).getLink().getPort().getNode().getDomain().isLocal();
        } else {
            // if the path is NOT specified, try to locate the destination link
            String destEndpoint = this.findEndpoints(requestedPath).get("dest");

            // FIXME: this ONLY works with L2 identifiers
            Link dstLink = domDAO.getFullyQualifiedLink(destEndpoint);
            if (dstLink != null) {
                return dstLink.getPort().getNode().getDomain().isLocal();
            }
            return false;
        }
    }

    /**
     * Utility function to decide whether the requested path originates
     * from this domain
     *
     * @param requestedPath a requested path with all local URNs already resolved to Links
     * @return if the path originates locally
     * @throws PathfinderException
     */
    public boolean pathOriginatesLocally(Path requestedPath) throws PathfinderException {
        List<PathElem> pathElems = requestedPath.getPathElems();
        if (this.isPathSpecified(requestedPath)) {
            // If the path is specified and the first hop has not already been
            // resolved to a Link, it is not a local hop
            if (pathElems.get(0).getLink() == null ){
                return false;
            }
            // if it is in our DB, return the locality
            return pathElems.get(0).getLink().getPort().getNode().getDomain().isLocal();
        } else {
            // If the path is NOT specified, that means this is a user request,
            // and we are the 1st domain on the path,
            // Try to locate the source link from L2 / L3 data.
            String srcEndpoint = this.findEndpoints(requestedPath).get("src");

            // FIXME: this ONLY works with L2 identifiers
            Link srcLink = domDAO.getFullyQualifiedLink(srcEndpoint);
            if (srcLink == null) {
                throw new PathfinderException("No path specified and source link not found");
            }
            return srcLink.getPort().getNode().getDomain().isLocal();
        }
    }



    protected Link firstLocalLink(Path requestedPath) throws PathfinderException {
        List<PathElem> localPathElems = this.extractLocalSegment(requestedPath);
        return localPathElems.get(0).getLink();
    }

    protected Link lastLocalLink(Path requestedPath) throws PathfinderException {
        List<PathElem> localPathElems = this.extractLocalSegment(requestedPath);
        return localPathElems.get(localPathElems.size()-1).getLink();
    }


    protected String firstHopAfterLocal(Path requestedPath) throws PathfinderException {
        List<PathElem> pathElems = requestedPath.getPathElems();
        int i = this.firstHopAfterLocalIndex(requestedPath);
        PathElem pe = pathElems.get(i);
        if (pe.getLink() != null) {
            return pe.getLink().getFQTI();
        } else {
            return pe.getUrn();
        }
    }

    protected int firstHopAfterLocalIndex(Path requestedPath) throws PathfinderException {
        List<PathElem> pathElems = requestedPath.getPathElems();
        boolean foundLocal = false;
        int i = 0;
        for (PathElem pe : pathElems) {
            if (pe.getLink() != null) {
                if (pe.getLink().getPort().getNode().getDomain().isLocal()) {
                    foundLocal = true;
                } else {
                    if (foundLocal) {
                        return i;
                    }
                }
            } else {
                if (foundLocal) {
                    return i;
                }
            }
            i++;
        }
        throw new PathfinderException("No local segment found!");
    }


    /**
     * Extracts only the local segment of a path
     *
     * @param path a path to examine
     * @return an  list of the local pathElems in the order in which they were found
     * @throws PathfinderException
     */
    protected List<PathElem> extractLocalSegment(Path path) throws PathfinderException {
        DomainDAO domDAO = new DomainDAO(this.dbname);
        List<PathElem> localSegment = new ArrayList<PathElem>();
        if (!isPathSpecified(path)) {
            return null;
        }

        boolean localSegmentStarted = false;
        boolean localSegmentFinished = false;
        for (PathElem pe : path.getPathElems()) {
            if (pe.getLink() != null) {
                if (pe.getLink().getPort().getNode().getDomain().isLocal()) {
                    if (localSegmentFinished) {
                        throw new PathfinderException("Two separate local segments detected");
                    }
                    localSegmentStarted = true;
                    PathElem copy = null;
                    try {
                        copy = PathElem.copyPathElem(pe);
                    } catch (BSSException e) {
                        throw new PathfinderException(e.getMessage());
                    }
                    localSegment.add(copy);
                }
            } else if (pe.getUrn() != null) {
                String fqti = this.resolveToFQTI(pe.getUrn());
                Link link = domDAO.getFullyQualifiedLink(fqti);
                if (link != null && link.getPort().getNode().getDomain().isLocal()) {
                    localSegmentStarted = true;
                    if (localSegmentFinished) {
                        throw new PathfinderException("Two separate local segments detected");
                    }
                    PathElem copy = null;
                    try {
                        copy = PathElem.copyPathElem(pe);
                    } catch (BSSException e) {
                        throw new PathfinderException(e.getMessage());
                    }
                    copy.setLink(link);
                    localSegment.add(copy);
                }
            } else if (localSegmentStarted) {
                localSegmentFinished = true;
            }
        }

        if (localSegment != null && localSegment.size() < 2) {
            throw new PathfinderException("Local segment in requested path too short");
        }

        return localSegment;
    }


    protected HashMap<String, String> findEndpoints(Path path) throws PathfinderException {
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        String srcEndpoint = null;
        String destEndpoint= null;
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            if (path.isLayer2()) {
                srcEndpoint = path.getLayer2Data().getSrcEndpoint();
                destEndpoint = path.getLayer2Data().getDestEndpoint();
            } else if (path.isLayer3()) {
                srcEndpoint = path.getLayer3Data().getSrcHost();
                destEndpoint = path.getLayer3Data().getDestHost();
            }
        } catch (BSSException ex) {
            throw new PathfinderException(ex.getMessage());
        }
        result.put("src", srcEndpoint);
        result.put("dest", destEndpoint);
        return result;
    }


    /**
     * Utility function to resolve to FTQIs
     *
     * This functionality should probably be moved into whatever
     * preprocesses the path before it is passed to the pathfinder
     *
     * @deprecated
     * @param urnOrName
     * @return
     * @throws PathfinderException
     */
    protected String resolveToFQTI(String urnOrName) throws PathfinderException {
        LookupFactory lookupFactory = new LookupFactory();
        PSLookupClient lookupClient = lookupFactory.getPSLookupClient();
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(urnOrName);
        String fqti;
        if (parseResults != null) {
            fqti = parseResults.get("fqti");
        } else {
            fqti = null;
        }

        if (fqti == null) {
            if (lookupClient == null) {
                throw new PathfinderException("Could not resolve "+urnOrName);
            } else {
                try {
                    fqti = lookupClient.lookup(urnOrName);
                } catch (LookupException ex) {
                    throw new PathfinderException("Could not resolve "+urnOrName+" . Error was: "+ex.getMessage());
                }
            }
        }
        if (fqti == null) {
            throw new PathfinderException("Could not resolve "+urnOrName);
        }

        return fqti;
    }


}
