package net.es.oscars.pathfinder.generic;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.InterdomainRouteDAO;
import net.es.oscars.bss.topology.InterdomainRoute;
import net.es.oscars.bss.topology.RouteElem;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.TopologyUtil;
import net.es.oscars.pathfinder.*;
import net.es.oscars.wsdlTypes.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import java.util.List;
import org.apache.log4j.*;

/**
 * InterdomainPathfinder finds paths in the interdomainRoutes table of the
 * database. It does so by matching a given path (or the source and destination
 * if nor hops are given) to an entry in the database. It can match such requests
 * at the domain, node, port, or link level. It can also accept paths that contain
 * not only link-id but also domain-ids, node-ids, and port-ids.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class InterdomainPathfinder extends Pathfinder{
    private Logger log;

    /**
     * Constructor
     *
     * @param dbname the name of the database to use
     */
    public InterdomainPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Finds an interdomain path given path information from a
     * createReservation request.
     *
     * @param pathInfo PathInfo instance containing current set of interdomain hops
     * @return a path containing the ingress and egress of the local domain
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation) throws PathfinderException{

        CtrlPlanePathContent interPath = pathInfo.getPath();
        CtrlPlanePathContent intraPath = null;
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        String src = null;
        String dest = null;
        PathInfo intraPathInfo = new PathInfo();

        if(layer2Info != null){
            src = layer2Info.getSrcEndpoint();
            dest = layer2Info.getDestEndpoint();
        }else{
           this.reportError("Layer 2 path information must be provided for " +
            "this IDC.");
        }

        if(interPath == null || interPath.getHop() == null){
            /* Create path with only src and dest in it */
            interPath = new CtrlPlanePathContent();
            interPath.setId("path-" + System.currentTimeMillis());
            CtrlPlaneHopContent srcHop = new CtrlPlaneHopContent();
            CtrlPlaneHopContent destHop = new CtrlPlaneHopContent();

            srcHop.setId("src");
            destHop.setId("dest");
            srcHop.setLinkIdRef(src);
            destHop.setLinkIdRef(dest);
            interPath.addHop(srcHop);
            interPath.addHop(destHop);
            pathInfo.setPathType("loose");
            pathInfo.setPath(interPath);
        }else{
            /* verify the given LIDP is valid */
            this.verifyPath(src, dest, interPath);
        }

        /* build new LIDP from existing LIDP */
        try{
            intraPath = this.buildNewPath(pathInfo);
            intraPathInfo.setPath(intraPath);
        }catch(BSSException e){
            this.reportError(e.getMessage());
        }

        this.log.info("Path Type: " + pathInfo.getPathType());
        for(int i = 0; i < pathInfo.getPath().getHop().length; i++){
            this.log.info(pathInfo.getPath().getHop()[i].getLinkIdRef());
        }
        
        /* Remove strict pathType for backward compatibility */
        String interPathType = pathInfo.getPathType();
        if(interPathType != null && interPathType.equals("strict")){
            pathInfo.setPathType(null);
        }
        
        return intraPathInfo;
    }

    /**
     * Builds an interdomain path and stores it in the interdomain request
     *
     * @param pathInfo the PathInfo element from a createReservation request
     * @return a path containing the ingress and egress for this domain
     */
    private CtrlPlanePathContent buildNewPath(PathInfo pathInfo)
        throws PathfinderException, BSSException{

        CtrlPlanePathContent currPath = pathInfo.getPath();
        CtrlPlanePathContent newPath = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] currHops = currPath.getHop();
        CtrlPlaneHopContent ingressHop = new CtrlPlaneHopContent();
        CtrlPlaneHopContent egressHop = new CtrlPlaneHopContent();
        CtrlPlanePathContent intraPath = new CtrlPlanePathContent();
        int ingressIndex = -1;
        int currHopIndex = -1;
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        boolean ingressFound = false;
        boolean isLocal = false;
        String ingressURN = null;
        String egressURN = null;
        String currHopURN = null;
        String pathType = pathInfo.getPathType();
        Link ingressLink = null;
        boolean oneLocalHop = false;
        boolean onlyLocal = true;

        /* Copy each hop until reach egress or the last hop (destination) */
        for(int i = 0; i < currHops.length; i++){
            CtrlPlaneHopContent currHop = currHops[i];
            CtrlPlaneHopContent newHop = new CtrlPlaneHopContent();
            String domainIdURN = currHop.getDomainIdRef();
            String nodeIdURN = currHop.getNodeIdRef();
            String portIdURN = currHop.getPortIdRef();
            String linkIdURN = currHop.getLinkIdRef();
            String[] componentList = null;
            currHopIndex = i;

            if(linkIdURN != null){
                componentList = this.splitURN(linkIdURN, TopologyUtil.LINK_URN);
                newHop.setLinkIdRef(linkIdURN);
                currHopURN = linkIdURN;
            }else if(portIdURN != null){
                componentList = this.splitURN(portIdURN, TopologyUtil.PORT_URN);
                newHop.setPortIdRef(portIdURN);
                currHopURN = portIdURN;
            }else if(nodeIdURN != null){
                componentList = this.splitURN(nodeIdURN, TopologyUtil.NODE_URN);
                newHop.setNodeIdRef(nodeIdURN);
                currHopURN = nodeIdURN;
            }else if(domainIdURN != null){
                componentList = this.splitURN(domainIdURN, TopologyUtil.DOMAIN_URN);
                newHop.setDomainIdRef(domainIdURN);
                currHopURN = domainIdURN;
            }else{
                this.reportError("Empty hop in provided path");
            }

            isLocal = domainDAO.isLocal(componentList[3]);
            if(isLocal && ingressFound){
                egressURN = currHopURN;
                oneLocalHop = false;
                continue;
            }else if(isLocal){
                ingressURN = currHopURN;
                ingressIndex = i;
                egressURN = currHopURN; //egress only null if no local hops
                oneLocalHop = true;
                ingressFound = true;
                continue;
            }else if(ingressFound){
                onlyLocal = false;
                break;
            }else{
                onlyLocal = false;
            }

            newHop.setId("hop");
            newPath.addHop(newHop);
        }

        /* If strict or local path return local ingress and egress  */
        if(onlyLocal){
            pathInfo.setPathType("strict");
            return pathInfo.getPath();
        }else if(pathType == null || pathType.equals("strict")){
            ingressHop.setLinkIdRef(ingressURN);
            egressHop.setLinkIdRef(egressURN);
            intraPath.addHop(ingressHop);
            intraPath.addHop(egressHop);
            return intraPath;
        }

        /* If loose path given find ingress... */
        if(ingressURN == null){
            ingressLink = this.findIngressFromPrevEgress(currHopURN, domainDAO);
            ingressURN = this.urnFromLink(ingressLink);
        }else if(ingressIndex > 0){
            String prevEgressURN = currHops[ingressIndex - 1].getLinkIdRef();
            ingressLink = this.findIngressFromPrevEgress(prevEgressURN,
                domainDAO);
            ingressURN = this.matchURNToLink(ingressURN, ingressLink);
        }else{
            /* ingress and the source are the same */
            ingressLink = domainDAO.getFullyQualifiedLink(ingressURN);
        }
        ingressHop.setLinkIdRef(ingressURN);
        newPath.addHop(ingressHop);
        intraPath.addHop(ingressHop);

        /* ...then find egress */
        if(egressURN == null || (!egressURN.equals(currHopURN))){
            RouteElem route = this.lookupRoute(ingressLink, egressURN,
                                               currHopURN, oneLocalHop);
            newPath = this.addNewRoute(pathInfo, currHopIndex, currHopURN,
                                        route, newPath);
            intraPath.addHop(newPath.getHop()[ingressIndex + 1]);
        }else{
            /* sets egress if same as destination */
            egressHop.setId("dest");
            egressHop.setLinkIdRef(egressURN);
            newPath.addHop(egressHop);
            intraPath.addHop(egressHop);
        }

        /* Save new interdomain path */
        newPath.setId("path-" + System.currentTimeMillis());
        pathInfo.setPath(newPath);

        return intraPath;
    }

    /**
     * Returns an ingress link given the URN of an egress link. It does so by
     * looking-up the remoteLinkId of the egress link in the database.
     *
     * @param prevEgressURN the urn of the previous domain's egress
     * @param domainDAO the data access object to use with the database
     * @return the ingress link to the domain
     * @throws PathfinderException
     */
    private Link findIngressFromPrevEgress(String prevEgressURN,
        DomainDAO domainDAO) throws PathfinderException{

        Link ingressLink = null;
        Link prevDomainEgress = domainDAO.getFullyQualifiedLink(prevEgressURN);
        if(prevDomainEgress == null){
            this.reportError("Path contains hops from previous domain " +
                "that are unknown to this domain or are not fully " +
                "qualified. Please replace the hop containing " +
                prevEgressURN + " with a known fully-qualified link ID.");
        }

        ingressLink = prevDomainEgress.getRemoteLink();
        if(ingressLink == null){
            this.reportError("Could not locate the remote link of " +
                prevEgressURN + ". Unable to find and verify local" +
                " ingress.");
        }

        return ingressLink;
    }

    /**
     * Looks-up a route in the database given the ingress, egress, and next hop
     *
     * @param ingressLink the ingress of the route to look-up
     * @param egressURN the domain, node, port or link URN of the egress
     * @param nextHopURN the urn of the first hop past the egress
     * @param oneLocalHop boolean indicating whether only one hop given in path
     * @return RouteElem representing the chosen route
     * @throws PathfinderException
     * @throws BSSException
     */
    private RouteElem lookupRoute(Link ingressLink, String egressURN,
        String nextHopURN, boolean oneLocalHop)
        throws PathfinderException, BSSException{

        RouteElem route = null;
        Link egressLink = null;
        InterdomainRouteDAO routeDAO = new InterdomainRouteDAO(this.dbname);
        List<InterdomainRoute> routes = null;
        int egressURNType = TopologyUtil.getURNType(egressURN);

        if(egressURN == null || oneLocalHop ||
           egressURNType == TopologyUtil.DOMAIN_URN){
            routes = routeDAO.lookupRoute(ingressLink, nextHopURN);
        }else if(egressURNType == TopologyUtil.LINK_URN){
            egressLink = TopologyUtil.getLink(egressURN, this.dbname);
            route = new RouteElem();
            route.setLink(egressLink);
        }else if(egressURNType == TopologyUtil.PORT_URN){
            Port egressPort = TopologyUtil.getPort(egressURN, this.dbname);
            routes = routeDAO.lookupRoute(egressPort, nextHopURN);
        }else if(egressURNType == TopologyUtil.NODE_URN){
            Node egressNode = TopologyUtil.getNode(egressURN, this.dbname);
            routes = routeDAO.lookupRoute(egressNode, nextHopURN);
        }else{
            this.reportError("Invalid egress URN provided " + egressURN);
        }

        if((routes != null && routes.size() > 0)){
            route = routes.get(0).getRouteElem();
            egressLink = route.getLink();
        }else if(egressLink == null){
            //TODO: Calculate strict path to destination
            this.reportError("Unable to find route to " + nextHopURN);
        }

        /* Check that first hop is a link-id */
        if(egressLink == null){
            this.reportError("Error with routing tables. " +
                "Domain egress to " + nextHopURN + " is not a link id. " +
                "Please contact the IDC administrator.");
        }

        return route;
    }

    /**
     * Adds given route to the interdomain path and sets whether the new path
     * is strict or loose.
     *
     * @param pathInfo PathInfo element of request
     * @param hopIndex index of the lst hop in given path parsed
     * @param nextHopURN the URN of the first hop past the egress
     * @param route the route to be added to the path
     * @param newPath the new interdomain path being constructed
     * @return the new path with the given route added
     * @throws PathfinderException
     * @throws BSSException
     */
    private CtrlPlanePathContent addNewRoute(PathInfo pathInfo, int hopIndex,
        String nextHopURN, RouteElem route, CtrlPlanePathContent newPath)
        throws PathfinderException, BSSException{

        CtrlPlaneHopContent[] currHops = pathInfo.getPath().getHop();
        Link egressLink = route.getLink();
        Link nextHopLink = null;
        int hopCount = 0;
        String lastDomain = null;
        String nextHopDomain = TopologyUtil.getURNDomainId(nextHopURN);
        int nextHopType = TopologyUtil.getURNType(nextHopURN);
        int lastHopType = 0;
        String dest = currHops[currHops.length - 1].getLinkIdRef();
        String routeType = route.isStrict() ? "strict" : "loose";

        /* Add all the hops to the path */
        while(route != null){
            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
            Domain domain = route.getDomain();
            Node node = route.getNode();
            Port port = route.getPort();
            Link link = route.getLink();
            String urn = "";

            if(link != null){
                urn = this.urnFromLink(link);
                hop.setLinkIdRef(urn);
            }else if(port != null){
                urn = this.urnFromPort(port);
                hop.setPortIdRef(urn);
            }else if(node != null){
                urn = this.urnFromNode(node);
                hop.setNodeIdRef(urn);
            }else if(domain != null){
                urn = this.urnFromDomain(domain);
                hop.setDomainIdRef(urn);
            }else{
                this.reportError("Invalid hop in route. Please ask the IDC " +
                    "administrator to check their routing tables");
            }
            lastDomain = TopologyUtil.getURNDomainId(urn);
            lastHopType = TopologyUtil.getURNType(urn);

            hop.setId("hop");
            newPath.addHop(hop);
            route = route.getNextHop();
            hopCount++;
        }

        /* add remoteLink if one hop and remoteLink exists */
        nextHopLink = egressLink.getRemoteLink();
        if(hopCount == 1 && nextHopLink != null){
            String remoteLinkURN = this.urnFromLink(nextHopLink);
            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
            if(!remoteLinkURN.equals(dest)){
                hop.setId("hop");
                hop.setLinkIdRef(remoteLinkURN);
                newPath.addHop(hop);
            }
            lastDomain = nextHopLink.getPort().getNode()
                                    .getDomain().getTopologyIdent();
        }

        /* add next hop if its the destination or a different domain than last
           hop in path looked up */
        if(dest.equals(nextHopURN)){
            newPath.addHop(currHops[hopIndex]);
            pathInfo.setPathType(routeType);
        }else if(!lastDomain.equals(nextHopDomain)){
            newPath.addHop(currHops[hopIndex]);
            pathInfo.setPathType("loose");
        }else if(nextHopType > lastHopType){
            /* if hop already in path is more acurate than table's */
            CtrlPlaneHopContent[] newHops = newPath.getHop();
            newHops[newHops.length - 1] = currHops[hopIndex];
        }

        /* fill in remaining hops */
        for(int i = (hopIndex + 1); i < currHops.length; i++){
            newPath.addHop(currHops[i]);
        }

        return newPath;
    }

    /**
     * Verifies a given path is valid
     *
     * @param src a createReservation request's given source URN
     * @param dest a createReservation request's given destination URN
     * @param interPath a createReservation request's given path
     * @throws PathfinderException
     */
    private void verifyPath(String src, String dest,
        CtrlPlanePathContent interPath) throws PathfinderException{

        CtrlPlaneHopContent[] hops = interPath.getHop();
        String firstHop = hops[0].getLinkIdRef();
        String lastHop = hops[hops.length - 1].getLinkIdRef();

        if(firstHop == null || lastHop == null){
            this.reportError("The first and last hop of the given path must " +
                "be a link ID reference.");
        }else if(!firstHop.equals(src)){
            this.reportError("The first hop of the path must be the same as " +
            "the source. The source given was " + src + " and the first hop " +
            "of the provided path is " + firstHop);
        }else if(!lastHop.equals(dest)){
            this.reportError("The last hop of the path must be the same as " +
            "the destination. The destination given was " + dest + " and the" +
            "last hop of the provided path is " + lastHop);
        }

    }

    /**
     * Splits a given URN into String array after verifying its of the
     * expected type
     *
     * @param urn the urn to split
     * @param partCount the type of URN expected
     * @return a String array of the URN's components
     * @throws PathfinderException
     */
    private String[] splitURN(String urn, int partCount)
        throws PathfinderException{

        String[] componentList = urn.split(":");
        String refType = "link";

        if(partCount == TopologyUtil.DOMAIN_URN){
            refType = "domain";
        }else if(partCount == TopologyUtil.NODE_URN){
            refType = "node";
        }else if(partCount == TopologyUtil.PORT_URN){
            refType = "port";
        }

        if(componentList.length != partCount){
            this.reportError("Invalid " + refType + " ID reference given in " +
                "provided path. The URN that caused the error is " + urn);
        }

        return componentList;
    }

    /**
     * Generates a URN from a given Link
     *
     * @param link the Link from which to generate the URN
     * @return the URN of the given Link
     */
    private String urnFromLink(Link link){
        Port port = link.getPort();
        String urn = this.urnFromPort(port);
        urn += ":link=" + link.getTopologyIdent();
        return urn;
    }

    /**
     * Generates a URN from a given Port
     *
     * @param port the Port from which to generate the URN
     * @return the URN of the given Port
     */
    private String urnFromPort(Port port){
        Node node = port.getNode();
        String urn = this.urnFromNode(node);
        urn += ":port=" + port.getTopologyIdent();
        return urn;
    }

    /**
     * Generates a URN from a given Node
     *
     * @param node the Node from which to generate the URN
     * @return the URN of the given Node
     */
    private String urnFromNode(Node node){
        Domain domain = node.getDomain();
        String urn = this.urnFromDomain(domain);
        urn += ":node=" + node.getTopologyIdent();
        return urn;
    }

    /**
     * Generates a URN from a given Domain
     *
     * @param domain the Domain from which to generate the URN
     * @return the URN of the given Domain
     */
    private String urnFromDomain(Domain domain){
        String urn = "urn:ogf:network:";
        urn += "domain=" + domain.getTopologyIdent();
        return urn;
    }

    /**
     * Matches a URN to a given link and returns the link's URN. If URN is a
     * domain,node, or port ID then it is only matched to the level of the
     * given URN.
     *
     * @param urn the URN to match to the link
     * @param link the Link to be matched
     * @return the URN of the given link
     * @throws PathfinderException
     *
     */
    private String matchURNToLink(String urn, Link link)
        throws PathfinderException{

        String linkURN = this.urnFromLink(link);
        String[] urnCompList = urn.split(":");
        String[] linkCompList = urn.split(":");

        for(int i = 0; i < urnCompList.length; i++){
            if(!linkCompList[i].equals(urnCompList[i])){
                this.reportError("The given ingress URN " + urn +
                    " does not match the remote link of the previous " +
                    "domain's egress hop. The hop should match " + linkURN);
            }
        }

        return linkURN;
    }

    /**
     * Reports an error
     *
     * @param msq the message to report
     * @throws PathfinderException
     */
    private void reportError(String msg) throws PathfinderException{
        //this.log.error(msg);
        throw new PathfinderException(msg);
    }
}