package net.es.oscars.pathfinder.terce;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import edu.internet2.hopi.dragon.terce.ws.service.RCEFaultMessage;
import edu.internet2.hopi.dragon.terce.ws.service.TERCEStub;
import edu.internet2.hopi.dragon.terce.ws.types.rce.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathDirection;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.bss.topology.TopologyUtil;
import net.es.oscars.pathfinder.LocalPCE;
import net.es.oscars.pathfinder.Pathfinder;
import net.es.oscars.pathfinder.PathfinderException;

public class TERCEPathfinder extends Pathfinder implements LocalPCE{
    
    private Logger log;
    private Properties props;
    
    /**
     * Constructor that initializes TERCE properties from oscars.properties file
     *
     */
    public TERCEPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("terce", true);
    }
    
    /**
     * Finds a local path using the TERCE web service
     * 
     * @param resv the reservation with the path to find
     * @return a single local path returned by the TERCE
     * @throws PathfinderException
     */
    public List<Path> findLocalPath(Reservation resv) throws PathfinderException{
        List<Path> results = new ArrayList<Path>();
        
        //Get the inter-domain path already calculated
        Path interPath = null;
        try {
            interPath = resv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException e) {
           throw new PathfinderException("Unable to get interdomain path: " + 
                   e.getMessage());
        }
        List<PathElem> interElems = interPath.getPathElems();
        
        //Find the local path
        Path intraPath = new Path();
        try {
            intraPath.setPathType(PathType.LOCAL);
            intraPath.setDirection(PathDirection.BIDIRECTIONAL);
        } catch (BSSException e) {
            throw new PathfinderException(e.getMessage());
        }
        
        boolean firstHop = true;
        //iterate through all hops until second-to-last is reached since we do an i+1
        for(int i = 0; i < (interElems.size() - 1); i++){
            String src = interElems.get(i).getUrn();
            String dest = interElems.get(i+1).getUrn();
            Link srcLink = null;
            Link destLink = null;
            try{
                srcLink = TopologyUtil.getLink(src, this.dbname);
                destLink = TopologyUtil.getLink(dest, this.dbname);
            }catch(BSSException e){
                //not in database to continue
                continue;
            }
            
            //if source or destination are not in local domain then continue
            if(!srcLink.getPort().getNode().getDomain().isLocal()){
                continue;
            }
            if(!destLink.getPort().getNode().getDomain().isLocal()){
                continue;
            }
            
            Link srcRemoteLink = srcLink.getRemoteLink();
            Node srcNode = srcLink.getPort().getNode();
            Node destNode = destLink.getPort().getNode();
            
            PathElem currElem = null;
            PathElem nextElem = null;
            try{
                currElem = PathElem.copyPathElem(interElems.get(i));
                nextElem = PathElem.copyPathElem(interElems.get(i+1));
            }catch(BSSException e){
                throw new PathfinderException(e.getMessage());
            }
            if(firstHop){
                this.log.debug("Added first hop " + currElem.getUrn());
                intraPath.addPathElem(currElem);
            }
            if(srcNode.equals(destNode) || 
                (srcRemoteLink != null && srcRemoteLink.equals(destLink))){
                this.log.debug("Added next hop " + nextElem.getUrn());
                intraPath.addPathElem(nextElem);
            }else{
                CtrlPlanePathContent tercePath = this.terce(src, dest);
                CtrlPlaneHopContent[] terceHops = tercePath.getHop();
                for(int j = 1; j < terceHops.length; j++){
                    //if statement maintains given objects
                    if(terceHops[j].getLinkIdRef().equals(src)){
                        intraPath.addPathElem(currElem);
                    }else if(terceHops[j].getLinkIdRef().equals(dest)){
                        intraPath.addPathElem(nextElem);
                    }else{
                        PathElem elem = new PathElem();
                        elem.setUrn(terceHops[j].getLinkIdRef());
                        try{ 
                            elem.setLink(TopologyUtil.getLink(elem.getUrn(), 
                                    this.dbname));
                        }catch(BSSException e){
                            throw new PathfinderException("TERCE returned hop " +
                            		"not in database: " + elem.getUrn());
                        }
                        intraPath.addPathElem(elem);
                    }
                }
            }
            firstHop = false;
        }
        
        //Empty path means no local hops in interdomain path
        if(intraPath.getPathElems() == null || 
                intraPath.getPathElems().isEmpty()){
            throw new PathfinderException("TERCE did not find any local hops " +
                    "in interdomain path");
        }
        
        //Add path to results
        results.add(intraPath);
        
        return results;
    }

    /**
     * Retrieves path calculation from TERCE
     *
     * @param src string with IP address of source host
     * @param dest string with IP address of destination host
     * @return responseContent list of hops in path
     * @throws PathfinderException
     */
    public CtrlPlanePathContent terce(String src, String dest)
            throws PathfinderException {

        String terceURL = this.props.getProperty("url");
        FindPath fp = new FindPath();
        FindPathContent request = new FindPathContent();
        FindPathResponse response = null;
        FindPathResponseContent responseContent= null;
        CtrlPlanePathContent path = null;
        CtrlPlaneHopContent[] hops = null;
        TERCEStub terce= null;
        ConfigurationContext configContext = null;
        String errMessage = "";
        String repo = System.getenv("CATALINA_HOME");
        String axis2Config = "";
        
        this.log.debug("terce.start");
        this.log.debug("src=" + src);
        this.log.debug("dest=" + dest);
        repo += (repo.endsWith("/") ? "" :"/");
        repo += "shared/classes/terce.conf/repo/";
        axis2Config = repo + "axis2.xml";
        
        /* Calculate path */
        try {
            configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repo, axis2Config);
            terce = new TERCEStub(configContext, terceURL);

            /* Format Request */
            request.setSrcEndpoint(src);
            request.setDestEndpoint(dest);
            request.setVtag("any");
            request.setPreferred(true);
            request.setStrict(true);
            request.setAllvtags(true);

            /* Send request and get response*/
            fp.setFindPath(request);
            response = terce.findPath(fp);
            responseContent = response.getFindPathResponse();
            path = responseContent.getPath();
            hops = path.getHop();

            log.debug("terce.path.start");
            for(int i = 0; i < hops.length; i++){
                log.info("terce.path.hop=" + hops[i].getLinkIdRef());
            }
            log.debug("terce.path.end");
            this.log.debug("terce.end");
        }catch (RemoteException e) {
            errMessage = e.getMessage();
        }catch (RCEFaultMessage e) {
            errMessage = e.getFaultMessage().getMsg();
        }catch (Exception e) {
            errMessage = e.getMessage();
        }finally{
            //must terminate configContext to prevent memory leak
            errMessage += this.cleanUp(configContext);
            
            //throw exception
            if(!errMessage.equals("")){
                throw new PathfinderException(errMessage);
            }
        }

        return responseContent.getPath();
    }
    
    /**
     * Prevents memory leak in axis2 client
     *
     * @param configContext an Axis2 ConfigurationContext to delete
     * @return an error message if one occurred. An empty string otherwise.
     */
    private String cleanUp(ConfigurationContext configContext){
        if(configContext == null){
            return "";
        }
        
        try{
            configContext.terminate();
        }catch(Exception e){
            return "Unable to disconnect from TERCE: " + e.getMessage();
        }
        
        return "";
    }
}
