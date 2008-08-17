package net.es.oscars.pathfinder.traceroute;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.Reservation;

/**
 * TraceroutePathfinder performs traceroutes to find a layer 3 path from
 * source to destination within the local domain.
 */
public class TraceroutePathfinder extends Pathfinder implements PCE {
    private Logger log;
    private Properties props;
    private Utils utils;

    public TraceroutePathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("traceroute", true);
        this.utils = new Utils(dbname);
    }

    /**
     * Using traceroute, finds path from source to destination.
     *
     * @param pathInfo PathInfo instance containing source and destination
     * @return boolean indicating ERO was not used
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation) throws PathfinderException {

        List<String> hops = null;
        List<String> reverseHops = null;
        List<String> previousHops = new ArrayList<String>();
        String loopbackIP = null;
        String ingressNodeIP = null;
        CtrlPlanePathContent ctrlPlanePath = null;

        this.log.debug("findPath.start");
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info == null) {
            throw new PathfinderException(
                    "No layer 3 information provided for traceroute");
        }
        String srcHost = layer3Info.getSrcHost();
        if (srcHost == null) {
            throw new PathfinderException( "no source for path given");
        }
        String destHost = layer3Info.getDestHost();
        if (destHost == null) {
            throw new PathfinderException( "no destination for path given");
        }
        String defaultRouter = this.props.getProperty("jnxSource");
        // run the reverse traceroute to the reservation source
        reverseHops = this.traceroute(defaultRouter, srcHost);
        // go in forward direction to get Juniper ingress
        // don't include egress address in forward path
        for (int i=reverseHops.size()-1; i >= 1; i--) {
            previousHops.add(reverseHops.get(i));
        }
        // find path from default to destination
        hops = this.traceroute(defaultRouter, destHost);
        List<String> completeHops = this.stitch(previousHops, hops);
        ctrlPlanePath = this.pathFromHops(completeHops);
        pathInfo.setPath(ctrlPlanePath);
        this.log.debug("findPath.End");
        return pathInfo; //return same path to conform to interface
    }

    /**
     * Converts list of hops to Axis2 data structure, and
     * marks hops that are local.
     *
     * @param hops list of strings representing hops
     * @return CtrlPlanePath sequence of fully qualified links
     * @throws PathfinderException
     */
    private CtrlPlanePathContent pathFromHops(List<String> hops)
            throws PathfinderException {

        CtrlPlanePathContent ctrlPlanePath = new CtrlPlanePathContent();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        NodeAddressDAO nodeAddrDAO = new NodeAddressDAO(this.dbname);
        String fqn = null;
        boolean hopFound = false;

        Domain domain = domainDAO.getLocalDomain();
        String localTopologyIdent = domain.getTopologyIdent();

        this.log.debug("pathFromHops.start");
        // assign topology identifiers and identify local hops
        for (String hop: hops)  {
            CtrlPlaneHopContent ctrlPlaneHop = new CtrlPlaneHopContent();
            if (ipaddrDAO.queryByParam("IP", hop) != null) {
                fqn = domainDAO.setFullyQualifiedLink(localTopologyIdent, hop);
                hopFound = true;
            } else if (nodeAddrDAO.queryByParam("address", hop) != null) {
                Node node = nodeAddrDAO.queryByParam("address", hop).getNode();
                fqn = node.getFQTI();
                hopFound = true;
            } else {
                fqn = domainDAO.setFullyQualifiedLink("other", hop);
            }
            // this statement is to make the XSD schema happy
            ctrlPlaneHop.setId(fqn);
            ctrlPlaneHop.setLinkIdRef(fqn);
            ctrlPlanePath.addHop(ctrlPlaneHop);
        }
        // throw error if no local path found
        if (!hopFound) {
            throw new PathfinderException("No local hops found in path");
        }
        this.log.debug("pathFromHops.finish");
        return ctrlPlanePath;
    }
    
    /**
     * Finds first IP address and returns the link-id
     *
     * @param pathInfo the path to examine
     * @return the URN of the ingress link
     */
    public String findIngress(PathInfo pathInfo) throws PathfinderException{
        Layer3Info l3Info = pathInfo.getLayer3Info();
        CtrlPlanePathContent path = pathInfo.getPath();
        if(l3Info == null){
            return null;
        }
        String src = l3Info.getSrcHost();
        if(path == null){
            return src;
        }   
        
        CtrlPlaneHopContent[] hops = path.getHop();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        NodeAddressDAO nodeAddrDAO = new NodeAddressDAO(this.dbname);
        Domain domain = domainDAO.getLocalDomain();
        String localTopologyIdent = domain.getTopologyIdent();
        String fqn = null;
        for(CtrlPlaneHopContent hop:hops){
            String linkId = hop.getLinkIdRef();
            if(linkId == null){
                continue;
            }else if (ipaddrDAO.queryByParam("IP", linkId) != null) {
                fqn = domainDAO.setFullyQualifiedLink(localTopologyIdent, linkId);
            } else if (nodeAddrDAO.queryByParam("address", linkId) != null) {
                Node node = nodeAddrDAO.queryByParam("address", linkId).getNode();
                fqn = node.getFQTI();
            } else {
                fqn = domainDAO.setFullyQualifiedLink("other", linkId);
            }
        }
        
        return fqn;
    }

    /**
     * Given sections of a traceroute, eliminate duplicates and build complete
     * list of hops.  Some of the sections may be null.  Note that there are
     * some redundant checks between this and pathFromHops for locality to
     * the domain.  The check requires a database query.  Caching the IP addresses
     * from the database in a data structure would improve performance.
     *
     * @param previousHops list of addresses that are prior to the ingress
     * @param hops list of addresses from the ingress onwards
     * @return completeHops list of addresses from source to destination
     */
    private List<String> stitch(List<String> previousHops, List<String> hops) {

        this.log.debug("stitch.start");
        List<String> completeHops = new ArrayList<String>();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        for (String hop: previousHops) {
/*           No need any more
             if (ipaddrDAO.queryByParam("IP", hop) == null) {
                this.log.debug("previous: " + hop);
                completeHops.add(hop);
            }
*/
            this.log.debug("previous: " + hop);
            completeHops.add(hop);
        }
        for (String hop: hops) {
            if (ipaddrDAO.queryByParam("IP", hop) != null) {
                this.log.debug("local hop: " + hop);
                completeHops.add(hop);
            } else {
                completeHops.add(hop);
                this.log.debug("non-local hop: " + hop);
            }
        }
        this.log.debug("stitch.end");
        return completeHops;
    }

    /**
     * Performs traceroute.
     *
     * @param src source string
     * @param dest destination string
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    private List<String> traceroute(String src, String dest)
            throws PathfinderException {

        JnxTraceroute jnxTraceroute = null;
        String source = null;
        String pathSrc = null;
        List<String> hops = null;

        jnxTraceroute = new JnxTraceroute();
        try {
            pathSrc = jnxTraceroute.traceroute(src, dest);
        } catch (IOException ex) {
            throw new PathfinderException(ex.getMessage());
        }
        hops = jnxTraceroute.getHops();


        // prepend source to path
        hops.add(0, pathSrc);
        return hops;
    }
}
