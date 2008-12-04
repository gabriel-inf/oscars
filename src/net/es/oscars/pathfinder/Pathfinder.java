package net.es.oscars.pathfinder;

import java.util.*;

import org.apache.log4j.*;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.bss.topology.*;
import net.es.oscars.lookup.*;

/**
 * This class is intended to be subclassed by TERCEPathfinder and
 * TraceroutePathfinder.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Andrew Lake (alake@internet2.edu)
 */
public class Pathfinder {
    private Logger log;
    protected String dbname;

    public Pathfinder(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     * Determines if the path elements have been specified 
     * 
     * @param requestedPath the path to examine
     * @return true if the path has been explicitly specified
     */
    protected boolean isPathSpecified(Path requestedPath) {
    	List<PathElem> pathElems = requestedPath.getPathElems();
    	return pathElems.isEmpty();
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
    				PathElem copy = PathElem.copyPathElem(pe);
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
					PathElem copy = PathElem.copyPathElem(pe);
					copy.setLink(link);
					localSegment.add(copy);
    			}
    		} else if (localSegmentStarted) {
    			localSegmentFinished = true;
    		}
    	}
    	
    	return localSegment;
    }
    
    
    
    
    
    
    /**
     * Convenience method for finding ingress. Subclass may call this by
     * converting the source to a fully-qualified link URN and converting
     * all the hops to URNs (if they aren't already) then passing them to
     * this method.
     *
     * @param src the fully-qualified link-id of the request's source
     * @param path the path to analyze. if null then src is returned.
     * @return the fully-qualified link-id of the ingress link
     * @throws PathfinderException
     */
    protected String findIngress(String src, CtrlPlanePathContent path)
                                    throws PathfinderException {
         this.log.debug("findIngress.start");
         DomainDAO domainDAO = new DomainDAO(this.dbname);


        /* If no path given then return the source. In such a case this must
           be the first domain so the so   
    protected boolean isPathSpecified(Path requestedPath) {
    	List<PathElem> pathElems = requestedPath.getPathElems();
    	return pathElems.isEmpty();
    }
    
    protected List<PathElem> findLocalSegment(Path path) {
    	DomainDAO domDAO = new DomainDAO(this.dbname);
    	List<PathElem> localSegment = ne   
    protected boolean isPathSpecified(Path requestedPath) {
    	List<PathElem> pathElems = requestedPath.getPathElems();
    	return pathElems.isEmpty();
    }
    
    protected List<PathElem> findLocalSegment(Path path) {
    	DomainDAO domDAO = new DomainDAO(this.dbname);
    	List<PathElem> localSegment = new ArrayList<PathElem>()
    	if (!isPathSpecified(path)) {
    		return null;
    	}
    	for (PathElem pe : path.getPathElems()) {
    		if (pe.getLink() != null) {
    			if (pe.getLink().getPort().getNode().getDomain().isLocal()) {
    				PathElem copy = this.clonePathElem(pe);
    				localSegment.add(copy);
    			}
    		} else if (pe.getUrn() != null) {
    			String fqti = this.resolveToFQTI(pe.getUrn());
    			Link link = domDAO.getFullyQualifiedLink(fqti);
    			if (link != null && link.getPort().getNode().getDomain().isLocal()) {
					PathElem copy = this.clonePathElem(pe);
					localSegment.add(copy);
    			}
    		}
    	}
    	
    	return localSegment;
    }
    
    protected PathElem clonePathElem(PathElem pe) {
		PathElem copy = new PathElem();
		copy.setDescription(pe.getDescription());
		copy.setLink(pe.getLink());
		copy.setUrn(pe.getUrn());
		copy.setUserName(pe.getUserName());
		copy.setLinkDescr(pe.getLinkDescr());
		return copy;
    }w ArrayList<PathElem>()
    	if (!isPathSpecified(path)) {
    		return null;
    	}
    	for (PathElem pe : path.getPathElems()) {
    		if (pe.getLink() != null) {
    			if (pe.getLink().getPort().getNode().getDomain().isLocal()) {
    				PathElem copy = this.clonePathElem(pe);
    				localSegment.add(copy);
    			}
    		} else if (pe.getUrn() != null) {
    			String fqti = this.resolveToFQTI(pe.getUrn());
    			Link link = domDAO.getFullyQualifiedLink(fqti);
    			if (link != null && link.getPort().getNode().getDomain().isLocal()) {
					PathElem copy = this.clonePathElem(pe);
					localSegment.add(copy);
    			}
    		}
    	}
    	
    	return localSegment;
    }
    
    protected PathElem clonePathElem(PathElem pe) {
		PathElem copy = new PathElem();
		copy.setDescription(pe.getDescription());
		copy.setLink(pe.getLink());
		copy.setUrn(pe.getUrn());
		copy.setUserName(pe.getUserName());
		copy.setLinkDescr(pe.getLinkDescr());
		return copy;
    }urce must be the ingress */
        if (path == null && src == null) {
            throw new PathfinderException("Could not determine ingress; no path or source link given.");
        } else if (path == null) {
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(src);
            String domainId = parseResults.get("domainId");
            String srcType = parseResults.get("type");
            if (!srcType.equals("link")) {
                throw new PathfinderException("Could not determine ingress; no path given and source is not a link.");
            } else if (!domainDAO.isLocal(domainId)) {
                throw new PathfinderException("Could not determine ingress; no path given and source link is not local (" + domainId + ")");
            } else {
                String fqti = parseResults.get("fqti");
                Link ingressLink = domainDAO.getFullyQualifiedLink(fqti);
                if (ingressLink == null) {
                    throw new PathfinderException("Could not locate ingress link in DB");
                } else if (!ingressLink.isValid()) {
                    throw new PathfinderException("Ingress link is not valid");
                }

                return fqti;
            }
        }

        /* Search for explicitly defined link in local domain */
        CtrlPlaneHopContent[] hops = path.getHop();
        ArrayList<String> links = new ArrayList<String>();
        for(CtrlPlaneHopContent hop : hops){
            String linkId = hop.getLinkIdRef();
            /* if encounter domain-id, etc then stop because past the point
            where previous domains expanded */

            if (linkId == null) {
                break;
            } else {
                links.add(linkId);
            }

            this.log.debug("looking at: " +linkId);
            //parse link id and check if in local domain
            linkId = this.resolveToFQTI(linkId);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(linkId);

            String domainId = parseResults.get("domainId");
            if (domainDAO.isLocal(domainId)) {
                this.log.debug("first local is: " +linkId);
                return linkId;
            }
        }

        /* If path given and local hop not explicitly provided then check to
           see if any of the links are the remote side of a local link. It is
           a best common practice to explicitly specify the ingress so the
           checks above this should capture 90% of the cases. */
        for (String link : links) {
            Link dbLink = domainDAO.getFullyQualifiedLink(link);
            if (dbLink == null) {
                continue;
            }

            Link remoteLink = dbLink.getRemoteLink();
            if (remoteLink == null) {
                continue;
            }

            //Hibernate does not allow Port->Node->Domain to be null
            Domain domain = remoteLink.getPort().getNode().getDomain();
            if (domain.isLocal()) {
                return link;
            }
        }

        this.log.debug("findIngress.end");
        throw new PathfinderException("Failed to find an ingress after analyzing the path");
    }




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
