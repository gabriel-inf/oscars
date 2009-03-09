package net.es.oscars.pathfinder.staticroute;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.*;

/**
 * StaticDBInterPathfinder finds paths in the interdomainRoutes table of the
 * database. It does so by matching a given path (or the source and destination
 * if no hops are given) to an entry in the database. It can match such requests
 * at the domain, node, port, or link level. It can also accept paths that contain
 * not only link-id but also domain-ids, node-ids, and port-ids.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class StaticDBInterPathfinder extends Pathfinder implements InterdomainPCE{
    private Logger log;
    
    /**
     * Constructor
     *
     * @param dbname the name of the database to use
     */
    public StaticDBInterPathfinder(String dbname) {
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
    public List<Path> findInterdomainPath(Reservation resv) throws PathfinderException{
        ArrayList<Path> results = new ArrayList<Path>();
        try {
            Path reqPath = resv.getPath(PathType.REQUESTED);
            Path interdomainPath = this.buildNewPath(reqPath);
            interdomainPath.setPathType(PathType.INTERDOMAIN);
            results.add(interdomainPath);
        } catch (BSSException e) {
            throw new PathfinderException(e.getMessage());
        }

        /* Print path and set next domain */
        DomainDAO domainDAO = new DomainDAO(OSCARSCore.getInstance().getBssDbName());
        Domain localDomain = domainDAO.getLocalDomain();
        for(Path p : results){
            boolean localFound = false;
            boolean nextFound = false;
            for(PathElem e : p.getPathElems()){
                String vlan = "";
                Hashtable<String,String> urnInfo = URNParser.parseTopoIdent(e.getUrn());
                if((!nextFound) && localDomain.getTopologyIdent().equals(urnInfo.get("domainId"))){
                    localFound = true;
                }else if((!nextFound) && localFound){
                    p.setNextDomain(domainDAO.fromTopologyIdent(urnInfo.get("domainId")));
                    this.log.debug("Found next domain: " + urnInfo.get("domainId"));
                    nextFound = true;
                }
                
                try {
                    PathElemParam peParam = e.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
                    if(peParam != null){ vlan =" --VLAN=" + peParam.getValue();}
                } catch (Exception e1) {}
                this.log.debug("Hop) " + e.getUrn() + vlan);
            }
        }
        
        return results;
    }

    /**
     * Builds an interdomain path from a requested path
     *
     * @param reqPath the Path element with request parameters
     * @return the interdomain path
     */
    private Path buildNewPath(Path reqPath)
        throws PathfinderException, BSSException{

        List<PathElem> reqElems = reqPath.getPathElems();
        Path newPath = new Path();
        PathElem ingressPE = null;
        PathElem egressPE = null;
        int ingressIndex = -1;
        int egressIndex = -1;
        int currHopIndex = -1;
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        String currHopURN = null;
        //TODO: Pull transient value from Path
        boolean strict = false;
        boolean oneLocalHop = false;
        boolean onlyLocal = true;
        boolean isLocal = false;
        boolean ingressFound = false;
        
        //TODO: Check strict
        /* If strict then return */
        /* if(pathType == null || pathType.equals("strict")){
            
        }*/
        
        /* Copy each hop until reach egress or the last hop (destination) */
        for(int i = 0; i < reqElems.size(); i++){
            PathElem reqHop = reqElems.get(i);
            PathElem newHop = PathElem.copyPathElem(reqHop);
            currHopIndex = i;
            currHopURN = reqHop.getUrn();
            if(currHopURN == null){
                currHopURN = reqHop.getLink().getFQTI();
                newHop.setUrn(currHopURN);
            }
            
            String[] componentList = currHopURN.split(":");
            isLocal = domainDAO.isLocal(componentList[3]);
            if(isLocal && ingressFound){
                egressPE = newHop;
                egressIndex = i;
                oneLocalHop = false;
                continue;
            }else if(isLocal){
                ingressPE = newHop;
                ingressIndex = i;
                egressPE= PathElem.copyPathElem(newHop); //egress only null if no local hops
                egressIndex = i;
                oneLocalHop = true;
                ingressFound = true;
                continue;
            }else if(ingressFound){
                onlyLocal = false;
                break;
            }else{
                onlyLocal = false;
            }
            
            newPath.addPathElem(newHop);
        }

        /* If strict or local path return local ingress and egress  */
        if(onlyLocal && strict){
            newPath.addPathElem(ingressPE);
            newPath.addPathElem(egressPE);
            return newPath;
        }

        /* If loose path given find ingress... */
        if(ingressIndex < 0){
            Link ingressLink = this.findIngressFromPrevEgress(currHopURN, domainDAO);
            ingressPE.setLink(ingressLink);
            ingressPE.setUrn(ingressLink.getFQTI());
        }else if(ingressIndex > 0){
            String prevEgressURN = reqElems.get(ingressIndex - 1).getUrn();
            Link ingressLink = this.findIngressFromPrevEgress(prevEgressURN,
                domainDAO);
            ingressPE.setLink(ingressLink);
            ingressPE.setUrn(this.matchURNToLink(ingressPE.getUrn(), ingressLink));
        }else if(ingressPE.getLink() == null){
            /* ingress and the source are the same but Link is not filled in */
            ingressPE.setLink(domainDAO.getFullyQualifiedLink(ingressPE.getUrn()));
        }
        newPath.addPathElem(ingressPE);

        /* ...then find egress */
        if(egressIndex == currHopIndex){
            /* sets egress if same as destination */
            newPath.addPathElem(egressPE);
        }else{
            RouteElem route = this.lookupRoute(ingressPE.getLink(), egressPE,
                    currHopURN, oneLocalHop);
            this.addNewRoute(reqElems, currHopIndex, currHopURN,
                                        route, newPath);
        }

        return newPath;
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
    private RouteElem lookupRoute(Link ingressLink, PathElem egressPE,
        String nextHopURN, boolean oneLocalHop)
        throws PathfinderException, BSSException{
        
        String egressURN = null;
        if(egressPE != null){
            egressURN = egressPE.getUrn();
        }
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
    private void addNewRoute(List<PathElem> currHops, int hopIndex,
        String nextHopURN, RouteElem route, Path newPath)
        throws PathfinderException, BSSException{
        
        Link egressLink = route.getLink();
        Link nextHopLink = null;
        int hopCount = 0;
        String lastDomain = null;
        String nextHopDomain = TopologyUtil.getURNDomainId(nextHopURN);
        int nextHopType = TopologyUtil.getURNType(nextHopURN);
        int lastHopType = 0;
        String dest = currHops.get(currHops.size() - 1).getUrn();
        String routeType = route.isStrict() ? "strict" : "loose";

        /* Add all the hops to the path */
        while(route != null){
            PathElem hop = new PathElem();
            Domain domain = route.getDomain();
            Node node = route.getNode();
            Port port = route.getPort();
            Link link = route.getLink();
            String urn = "";

            if(link != null){
                urn = this.urnFromLink(link);
                hop.setUrn(urn);
            }else if(port != null){
                urn = this.urnFromPort(port);
                hop.setUrn(urn);
            }else if(node != null){
                urn = this.urnFromNode(node);
                hop.setUrn(urn);
            }else if(domain != null){
                urn = this.urnFromDomain(domain);
                hop.setUrn(urn);
            }else{
                this.reportError("Invalid hop in route. Please ask the IDC " +
                    "administrator to check their routing tables");
            }
            lastDomain = TopologyUtil.getURNDomainId(urn);
            lastHopType = TopologyUtil.getURNType(urn);

            newPath.addPathElem(hop);
            route = route.getNextHop();
            hopCount++;
        }

        /* add remoteLink if one hop and remoteLink exists */
        nextHopLink = egressLink.getRemoteLink();
        if(hopCount == 1 && nextHopLink != null){
            String remoteLinkURN = this.urnFromLink(nextHopLink);
            PathElem hop = new PathElem();
            if(!remoteLinkURN.equals(dest)){
                hop.setUrn(remoteLinkURN);
                newPath.addPathElem(hop);
            }
            lastDomain = nextHopLink.getPort().getNode()
                                    .getDomain().getTopologyIdent();
        }

        /* add next hop if its the destination or a different domain than last
           hop in path looked up */
        if(dest.equals(nextHopURN)){
            newPath.addPathElem(currHops.get(hopIndex));
            //TODO: Still needed?
            //pathInfo.setPathType(routeType);
        }else if(!lastDomain.equals(nextHopDomain)){
            newPath.addPathElem(currHops.get(hopIndex));
            //TODO: Still needed?
            //pathInfo.setPathType("loose");
        }else if(nextHopType > lastHopType){
            /* if hop already in path is more accurate than table's */
            newPath.getPathElems()
                   .set(newPath.getPathElems().size() - 1, currHops.get(hopIndex));
        }

        /* fill in remaining hops */
        for(int i = (hopIndex + 1); i < currHops.size(); i++){
            newPath.addPathElem(currHops.get(i));
        }
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

        String linkURN = link.getFQTI();
        String[] urnCompList = urn.split(":");
        String[] linkCompList = linkURN.split(":");

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