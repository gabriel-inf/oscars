package net.es.oscars.pathfinder;

import java.util.*;

import org.apache.log4j.*;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import net.es.oscars.bss.topology.*;

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
     * Convenience method for finding ingress. Subclass may call this by 
     * converting the source to a fully-qualified link URN and converting
     * all the hops to URNs (if they aren't already) then passing them to
     * this method.
     *
     * @param src the fully-qualified link-id of the request's source
     * @param path the path to analyze. if null then src is returned.
     * @return the fully-qualified link-id of the ingress link
     * @throws
     */
    protected String findIngress(String src, CtrlPlanePathContent path) 
                                    throws PathfinderException{
         this.log.debug("findIngress.start");
        /* If no path given then return the source. In such a case this must
           be the first domain so the source must be the ingress */
        if(path == null && src == null){
            throw new PathfinderException("Ingress cannot be found. " + 
                                          "Pathfinder module did not " + 
                                          "pass src or a path");
        }else if(path == null){
            return src;
        }
        
        /* Search for explicitly defined link in local domain */
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        CtrlPlaneHopContent[] hops = path.getHop();
        ArrayList<String> links = new ArrayList<String>();
        for(CtrlPlaneHopContent hop : hops){
            String linkId = hop.getLinkIdRef();
            /* if encounter domain-id, etc then stop because past the point 
            where previous domains expanded */
            if(linkId == null){
                break;
            }
            links.add(linkId);
            
            //parse link id and check if in local domain
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(linkId);
            String domainId = parseResults.get("domainId");
            if(domainId == null){
                throw new PathfinderException("Invalid link-id in path: No domain ID");
            }else if(domainDAO.isLocal(domainId)){
                return linkId;
            }
        }
        
        /* If path given and local hop not explicitly provided then check to 
           see if any of the links are the remote side of a local link. It is 
           a best common practice to explicitly specify the ingress so the 
           checks above this should capture 90% of the cases. */
        for(String link : links){
            Link dbLink = domainDAO.getFullyQualifiedLink(link);
            if(dbLink == null){
                continue;
            }
            
            Link remoteLink = dbLink.getRemoteLink();
            if(remoteLink == null){
                continue;
            }
            
            //Hibernate does not allow Port->Node->Domain to be null
            Domain domain = remoteLink.getPort().getNode().getDomain();
            if(domain.isLocal()){
                return link;
            }
        }
        
        this.log.debug("findIngress.end");
        throw new PathfinderException("Failed to find an ingress after " +
                                      "analyzing the path");
    }
}
