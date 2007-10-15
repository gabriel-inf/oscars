package net.es.oscars.pathfinder.dragon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.rmi.RemoteException;

import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.DomainDAO;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import org.apache.log4j.*;
 
import edu.internet2.hopi.dragon.terce.ws.types.rce.*;
import edu.internet2.hopi.dragon.terce.ws.service.*;

/**
 * TERCEPathfinder that uses TERCE to calculate path
 *
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class TERCEPathfinder extends Pathfinder implements PCE {
    private Properties props;
    private Logger log;
    
    /**
     * Constructor that initializes TERCE properties from oscars.properties file
     *
     */
    public TERCEPathfinder(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("terce", true);
        super.setDatabase(dbname);
    }
    
    /**
     * Finds a path given just source and destination or by expanding
     * a path the user explicitly sets
     *
     * @param pathInfo PathInfo instance containing hops of entire path
     * @throws PathfinderException
     */
    public boolean findPath(PathInfo pathInfo) throws PathfinderException{
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        if(layer2Info == null){
            throw new PathfinderException("Layer 2 path information must be" +
                "provided for TERCE");
        }
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        CtrlPlanePathContent localPathForOSCARSDatabase;
        CtrlPlanePathContent pathToForwardToNextDomain;
        String src = layer2Info.getSrcEndpoint();
        String dest = layer2Info.getDestEndpoint();
        
        if(ctrlPlanePath == null){
            /* Calculate path that contains strict local hops and 
            loose interdomain hops */
            FindPathResponseContent terceResult = this.terce(src, dest);
            CtrlPlanePathContent path = terceResult.getPath();
            
            pathInfo.setPath(path);
        }else{
            /* The path stored in the database will be the local hops including 
            some internal hops we might not share with anyone else */
            this.expandLocalPath(pathInfo);   
        }
        
        return false;  // just for compatibility with interface
    }

    /**
     * Expands loose hops and finds hops that are in the local domain, given a
     * path containing a list of addresses.
     *
     * @param pathInfo PathInfo instance containing hops of entire path
     * @throws PathfinderException
     */
    public void expandLocalPath(PathInfo pathInfo) 
            throws PathfinderException{

        Layer2Info layer2Info = pathInfo.getLayer2Info();
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();  
        CtrlPlanePathContent localPath = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        CtrlPlaneHopContent prevHop = null;
        String prevLinkUrn = null;
        boolean hopFound = false;
        
        /* Expand local hops */
        for (int i = 0; i < hops.length; i++) {
            CtrlPlaneHopContent hop = hops[i];
            String linkUrn = hop.getLinkIdRef();
            if(linkUrn == null){
                throw new PathfinderException("Unable to parse layer 2 path. " + 
                "This implementation only supports paths containing links as " + 
                "hops");
            }
            
            if (this.isLocalLink(linkUrn)) {
            
                this.log.info("localHop: " + linkUrn);
                hopFound = true;
    
                if (prevHop != null) {       
                    CtrlPlanePathContent expandedPath = null;
                    FindPathResponseContent terceResult = 
                            this.terce(prevLinkUrn, linkUrn);
                        
                    expandedPath = terceResult.getPath();
                    
                    /* Append expanded path to local path */
                    CtrlPlaneHopContent[] expandedHops = expandedPath.getHop();
                    for (int j = 0; j < expandedHops.length; j++) {
                        if(j != 0 || localPath.getHop() == null){
                            localPath.addHop(expandedHops[j]);
                        }
                    }
                }
                prevHop = hop;
                prevLinkUrn = linkUrn;
            }else if (hopFound) {
                this.log.info("Adding interdomain hop: " + linkUrn);
                localPath.addHop(hop);
                //break;
            }
        }
        
        /* Return error if no local hops found */
        if (localPath.getHop() == null) {
            throw new PathfinderException(
                "No local hops found in path");
        }
        
        pathInfo.setPath(localPath);
    }  
    

    /**
     * Retrieves path calculation from TERCE
     *
     * @param src string with IP address of source host
     * @param dest string with IP address of destination host
     * @return responseContent list of hops in path
     * @throws PathfinderException
     */
    public FindPathResponseContent terce(String src, String dest)
            throws PathfinderException {

        String terceURL = this.props.getProperty("url");
        FindPath fp = new FindPath();
        FindPathContent request = new FindPathContent();
        FindPathResponse response = null;
        FindPathResponseContent responseContent= null;
        CtrlPlanePathContent path = null;
        CtrlPlaneHopContent[] hops = null;
        TERCEStub terce= null;
        
        /* Calculate path */
        try {
            this.log.info("terce.start");
            terce = new TERCEStub(terceURL);
                   
            /* Format Request */
            request.setSrcEndpoint(src);
            request.setDestEndpoint(dest);
            request.setVtag("any");
            /* setPreferred(true) and setStrict(true) tell the TERCE to return
               strict hops for the local domain and loose hops for all other
               domains. */
            request.setPreferred(true);
            request.setStrict(true);
            request.setAllvtags(true);
            
            /* Send request and get response*/
            fp.setFindPath(request);
            response = terce.findPath(fp);
            responseContent = response.getFindPathResponse();
            path = responseContent.getPath();
            hops = path.getHop();
            
            log.info("terce.path.start");
            for(int i = 0; i < hops.length; i++){
                log.info("terce.path.hop=" + hops[i].getLinkIdRef());
            }
            log.info("terce.path.end");
            
            this.log.info("terce.end");
        } catch (RemoteException e) {
			throw new PathfinderException(e.getMessage());
		}catch (RCEFaultMessage e) {
			throw new PathfinderException(e.getFaultMessage().getMsg());
		}
        
        return responseContent;
    }
    
    /**
     * Subroutine to determin if a given fully-qualified link ID is local
     *
     * @param linkId fully-qualified identifier of link to be tested
     * @return true if local, false otherwise
     *
     */
    private boolean isLocalLink(String linkId){
        String[] componentList = linkId.split(":");
        if(componentList.length != 7){
            return false;
        }
        DomainDAO domainDAO = new DomainDAO("bss");
        return domainDAO.isLocal(componentList[3]);
        
    }
}
