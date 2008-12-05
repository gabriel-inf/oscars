package net.es.oscars.pathfinder.staticroute;

import java.util.*;
import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import org.apache.log4j.*;

/**
 * XMLFileLocalPathfinder that uses TERCE to calculate path
 *
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class XMLFileLocalPathfinder extends Pathfinder implements LocalPCE {
    private Properties props;
    private Logger log;
    private StaticRouteXMLParser routeParser;
    
    /**
     * Constructor that initializes TERCE properties from oscars.properties file
     *
     */
    public XMLFileLocalPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("staticxml", true);
        this.routeParser = new StaticRouteXMLParser(this.props.getProperty("file"));
    }
    
    public List<Path> findLocalPath(Reservation resv) throws PathfinderException{
        List<Path> results;
        try{
            results = this.localFromInter(resv);
        }catch(Exception e){
            e.printStackTrace();
            throw new PathfinderException(e.getMessage());
        }
        return results;
    }
    
    public List<Path> localFromInter(Reservation resv) throws PathfinderException, BSSException{
        ArrayList<Path> results = new ArrayList<Path>();
        Path interPath = resv.getPath(PathType.INTERDOMAIN);
        Path localPath = new Path();
        List<PathElem> localHops = this.extractLocalSegment(interPath);
        
        int seqNumber = 0;
        for(int i = 0; i < (localHops.size() - 1); i++){
            
            Link srcLink = localHops.get(i).getLink();
            Link destLink = localHops.get(i+1).getLink();
            String srcURN = null;
            String destURN = null;
            
            /* Get the URN of each end of segment */
            if(srcLink != null){
                srcURN = localHops.get(i).getLink().getFQTI();
            }else{
                srcURN = localHops.get(i).getUrn();
                srcLink = TopologyUtil.getLink(srcURN,this.dbname);
            }
            if(destLink != null){
                destURN = localHops.get(i+1).getLink().getFQTI();
            }else{
                destURN = localHops.get(i+1).getUrn();
                destLink = TopologyUtil.getLink(destURN,this.dbname);
            }
            
            /* Create new PathElems */
            PathElem srcPE = PathElem.copyPathElem(localHops.get(i));
            srcPE.setLink(srcLink);
            srcPE.setUrn(srcURN);
            PathElem destPE = PathElem.copyPathElem(localHops.get(i));
            destPE.setLink(destLink);
            destPE.setUrn(destURN);
            
            /* Build Path */
            Link srcRemoteLink = srcLink.getRemoteLink();
            Node srcNode = srcLink.getPort().getNode();
            Node destNode = destLink.getPort().getNode();
            if(seqNumber == 0){
                srcPE.setSeqNumber(++seqNumber);
                localPath.addPathElem(srcPE);
                this.log.debug("Hop: " + srcPE.getUrn());
            }
            if(srcNode.equals(destNode) || 
                (srcRemoteLink != null && srcRemoteLink.equals(destLink))){
                destPE.setSeqNumber(++seqNumber);
                localPath.addPathElem(destPE);
                this.log.debug("Hop: " + destPE.getUrn());
            }else{
                List<PathElem> staticPath = this.routeParser.findPath(srcURN, destURN);
                for(int j = 1; j < staticPath.size(); j++){
                    PathElem pe = staticPath.get(j);
                    pe.setLink(TopologyUtil.getLink(pe.getUrn(),this.dbname));
                    pe.setUserName(srcPE.getUserName());
                    pe.setSeqNumber(++seqNumber);
                    localPath.addPathElem(pe);
                    this.log.debug("Hop: " + pe.getUrn());
                }
            }
        }
        
        /* Finalize and store path */
        try {
            localPath.setPathType(PathType.LOCAL);
        } catch (BSSException e) {
            throw new PathfinderException(e.getMessage());
        }
        localPath.setExplicit(false);//only req paths explicit?
        localPath.setPathSetupMode(interPath.getPathSetupMode());
        localPath.setDirection(PathDirection.BIDIRECTIONAL);
        results.add(localPath);
        
        return results;
    }


    
}
