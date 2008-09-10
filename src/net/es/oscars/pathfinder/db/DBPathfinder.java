package net.es.oscars.pathfinder.db;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.rmi.RemoteException;

import net.es.oscars.*;
import net.es.oscars.lookup.*;
import net.es.oscars.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.traceroute.*;
import net.es.oscars.pathfinder.db.util.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import org.hibernate.SessionFactory;
import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import org.apache.log4j.*;

/**
 * DBPathfinder that uses the local database to calculate path
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class DBPathfinder extends Pathfinder implements PCE {
    private Logger log;
    private DomainDAO domDAO;
    private DBGraphAdapter dbga;
    private HashMap<String, Double> objectsToReweigh;

    public DBPathfinder(String dbname) {
        super(dbname);

        this.log = Logger.getLogger(this.getClass());

        this.dbga = new DBGraphAdapter(dbname);
        this.objectsToReweigh = new HashMap<String, Double>();
        /*

        List<String> dbnames = new ArrayList<String>();
        dbnames.add(dbname);

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
        sf.getCurrentSession().beginTransaction();
        */

        domDAO = new DomainDAO(dbname);
    }

    /**
     * Finds a path given just source and destination or by expanding
     * a path the user explicitly sets
     *
     * @param pathInfo PathInfo instance containing hops of entire path
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation)
            throws PathfinderException {

        this.log.debug("findPath.begin");
        // if an ERO, not just ingress and/or egress

        this.log.debug("handling explicit route object");
        //Converts mixture of IDRefs and objects to all IDRefs
        TypeConverter tc = OSCARSCore.getInstance().getTypeConverter();
        PathInfo refPathInfo = tc.createRefPath(pathInfo);
        if (pathInfo.getLayer2Info() != null) {
           this.handleLayer2ERO(refPathInfo, reservation);
        } else if (pathInfo.getLayer3Info() != null) {
            // Falling through to traceroute for L3
            this.handleLayer3ERO(refPathInfo, reservation);
        } else {
            throw new PathfinderException("An ERO must have associated layer 2 or layer 3 info");
        }
        //Restores any link objects in original path that were replaced by IDRefs
        try{
            tc.mergePathInfo(pathInfo, refPathInfo, false);
        }catch(BSSException e){
            throw new PathfinderException(e.getMessage());
        }

        this.log.debug("findPath.End");
        return pathInfo;
    }

    /**
     * Returns the ingress given a path. Converts hops in path to URNs
     * then passes to superclass
     *
     * @param pathInfo the path from which to extract the needed info
     * @return the ingress linkId of the path
     */
    public String findIngress(PathInfo pathInfo) throws PathfinderException {
        Layer2Info l2Info = pathInfo.getLayer2Info();

        //need to convert to URN
        // if layer2 request then already a URN so pass to super...
        if (l2Info != null) {
            String src = l2Info.getSrcEndpoint();
            String srcURN = this.resolveToFQTI(src);
            if (pathInfo.getPath() != null) {
                CtrlPlaneHopContent[] hops = pathInfo.getPath().getHop();
                for (int i = 0; i < hops.length; i++) {
                    hops[i].setLinkIdRef(this.resolveToFQTI(hops[i].getLinkIdRef()));
                }
                pathInfo.getPath().setHop(hops);
            }
            return super.findIngress(srcURN, pathInfo.getPath());
        }

        //...if layer 3 request then pass to TraceroutePathfinder.findIngress
        TraceroutePathfinder tracePF = new TraceroutePathfinder(this.dbname);
        return tracePF.findIngress(pathInfo);
    }

    private void handleLayer3ERO(PathInfo pathInfo, Reservation reservation)
            throws PathfinderException {
        this.log.debug("handleLayer3ERO.start");
        Domain localDomain = domDAO.getLocalDomain();
        Hashtable<String, String> parseResults;
        String fqti = null;

        TraceroutePathfinder tracePF = new TraceroutePathfinder(this.dbname);
        PathInfo tracePathInfo = tracePF.findPath(pathInfo, reservation);
        CtrlPlaneHopContent[] traceHops = tracePathInfo.getPath().getHop();


        boolean foundIngress = false;
        String ingress = "";
        String egress = "";
        for (CtrlPlaneHopContent traceHop : traceHops) {
            parseResults = URNParser.parseTopoIdent(traceHop.getId());
            String domainId = parseResults.get("domainId");
            fqti = parseResults.get("fqti");
            if (domainId != null && domainId.equals(localDomain.getTopologyIdent())) {
                egress = fqti;
                if (!foundIngress) {
                    ingress = fqti;
                    foundIngress = true;
                }
            }
        }
        if (!foundIngress) {
            throw new PathfinderException("Could not find any local hops!");
        }


        Long bandwidth = reservation.getBandwidth();
        Long startTime = reservation.getStartTime();
        Long endTime = reservation.getEndTime();
        CtrlPlanePathContent newPathContent = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] tmpHops = new CtrlPlaneHopContent[100]; // should be enough..

        Path foundPath = findPathBetween(ingress, egress, bandwidth, startTime, endTime, reservation, "L3");
        if (foundPath == null) {
            throw new PathfinderException("Could not find path!");
        }

        PathElem pathElem = foundPath.getPathElem();
        if (pathElem == null) {
            throw new PathfinderException("Could not find path!");
        }
        int totalHops = 0;
        while (pathElem.getNextElem() != null) {
            tmpHops[totalHops] = new CtrlPlaneHopContent();
            tmpHops[totalHops].setLinkIdRef(pathElem.getLink().getFQTI());
            totalHops++;
            pathElem = pathElem.getNextElem();
        }
        tmpHops[totalHops] = new CtrlPlaneHopContent();
        tmpHops[totalHops].setLinkIdRef(pathElem.getLink().getFQTI());

        CtrlPlaneHopContent[] hops = new CtrlPlaneHopContent[totalHops];

        for (int i = 0; i < totalHops; i++) {
            hops[i] = tmpHops[i];
        }

        newPathContent.setHop(hops);
        pathInfo.setPath(newPathContent);
        reservation.getPath().setExplicit(true);


        this.log.debug("handleLayer3ERO.end");
    }


    private void handleLayer2ERO(PathInfo pathInfo, Reservation reservation)
            throws PathfinderException {
        Layer2Info layer2Info = pathInfo.getLayer2Info();

        Hashtable<String, String> parseResults;
        String fqti = null;

        String srcEndpoint = layer2Info.getSrcEndpoint();
        String destEndpoint = layer2Info.getDestEndpoint();

        this.log.debug(srcEndpoint);
        this.log.debug(destEndpoint);

        this.log.debug("handleLayer2ERO.endpoints");

        srcEndpoint = this.resolveToFQTI(srcEndpoint);
        destEndpoint = this.resolveToFQTI(destEndpoint);

        this.log.debug("handleLayer2ERO.resolved");


        Domain domain = domDAO.getLocalDomain();

        Long bandwidth = reservation.getBandwidth();
        Long startTime = reservation.getStartTime();
        Long endTime = reservation.getEndTime();

        CtrlPlanePathContent ero = pathInfo.getPath();
        CtrlPlaneHopContent[] hops;
        if (ero != null) {
            hops = ero.getHop();
        } else {
            hops = null;
        }

        this.log.debug("handleLayer2ERO.initialized");


        if (hops != null) {
            if (hops.length == 0) {
                hops = new CtrlPlaneHopContent[2];
                hops[0] = new CtrlPlaneHopContent();
                hops[0].setLinkIdRef(srcEndpoint);
                hops[1] = new CtrlPlaneHopContent();
                hops[1].setLinkIdRef(destEndpoint);
            } else if (hops.length == 1) {
                throw new PathfinderException("ERO if nonempty must at least include both source and destination!");
            }
        } else {
            hops = new CtrlPlaneHopContent[2];
            hops[0] = new CtrlPlaneHopContent();
            hops[0].setLinkIdRef(srcEndpoint);
            hops[1] = new CtrlPlaneHopContent();
            hops[1].setLinkIdRef(destEndpoint);
        }

        this.log.debug("handleLayer2ERO.normalized");

        // an array to hold the local portion of the path
        ArrayList<String> localLinkIds = new ArrayList<String>();
        int localLinkIndex = 0;

        // store where the local stuff begins and ends
        int firstLocalHopIndex = 0;
        int lastLocalHopIndex = 0;

        String localIngress = "";
        String localEgress = "";

        boolean foundLocalSegment = false;

        for (int i=0; i < hops.length; i++) {
            CtrlPlaneHopContent ctrlPlaneHop = hops[i];
            String hopId = ctrlPlaneHop.getLinkIdRef();
            if (!TopologyUtil.isTopologyIdentifier(hopId)) {
                throw new PathfinderException(
                    "layer 2 ERO must currently be made up of " +
                    "LINK topology identifiers");
            }
            this.log.debug("hop id (original):["+hopId+"]");

            parseResults = URNParser.parseTopoIdent(hopId);
            fqti = parseResults.get("fqti");
            String domainId = parseResults.get("domainId");

            if (domDAO.isLocal(domainId)) {
                if (!foundLocalSegment) {
                    firstLocalHopIndex = i;
                    foundLocalSegment = true;
                    localIngress = fqti;
                }
                this.log.debug("Local link: "+fqti);
                localLinkIds.add(localLinkIndex, fqti);
                localLinkIndex++;
                lastLocalHopIndex = i;
                localEgress = fqti;
            }

            // reset id ref to local topology identifier
            ctrlPlaneHop.setLinkIdRef(fqti);
            // make schema validator happy
            ctrlPlaneHop.setId(fqti);
        }
        this.log.debug("first local hop is: "+firstLocalHopIndex);
        this.log.debug("last local hop is: "+lastLocalHopIndex);



        // Calculate path
        // case 1: we get passed NO local links
        // we need to know our ingress at the very least,
        // so throw an exception
        if (!foundLocalSegment) {
            throw new PathfinderException("Path must contain at least the ingress link to local domain ["+domain.getTopologyIdent()+"]");
        }


        // case 2: We get passed ONE or MORE local links
        // The very first one should be our ingress
        // we find the paths between each two until we reach the last one
        // then we find a path from the last one to the next domain.
        Path localPath = new Path();
        for (int i = 0; i < localLinkIndex -1; i++) {
            String src = localLinkIds.get(i);
            String dst = localLinkIds.get(i+1);
            this.log.debug("Finding path between: ["+src+"] ["+dst+"] bw: "+bandwidth);
            Path segmentPath = findPathBetween(src, dst, bandwidth, startTime, endTime, reservation, "L2");
            if (segmentPath == null) {
                throw new PathfinderException("Could not find path between ["+src+"] and ["+dst+"]");
            } else {
                localPath = joinPaths(localPath, segmentPath);
            }
        }
        this.log.debug("handleEro.foundLocalPath");


        if (lastLocalHopIndex < hops.length - 1) {
            String lastLocalLink = localLinkIds.get(localLinkIndex -1);
            String nextHop = hops[lastLocalHopIndex + 1].getLinkIdRef();
            Path segmentPath = findPathBetween(lastLocalLink, nextHop, bandwidth, startTime, endTime, reservation, "L2");
            localPath = joinPaths(localPath, segmentPath);
            this.log.debug("handleEro.foundToNext");
        }

        // inject local path into the local path that was passed to us:
        int totalHops = 0;
        CtrlPlanePathContent newPath = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] tmpHops = new CtrlPlaneHopContent[100]; // should be enough..
        for (int i = 0; i < firstLocalHopIndex; i++) {
            totalHops++;
            tmpHops[i] = hops[i];
            this.log.debug("Injecting previous "+hops[i].getLinkIdRef());
        }
        this.log.debug("handleEro.injectedPrevious");

        // inject local bits of the calculated path to the reservation localPath
        int j = firstLocalHopIndex;
        PathElem pathElem = localPath.getPathElem();
        while (pathElem != null) {
            String linkId = pathElem.getLink().getFQTI();

            parseResults = URNParser.parseTopoIdent(linkId);
            fqti = parseResults.get("fqti");
            String domainId = parseResults.get("domainId");

            if (domDAO.isLocal(domainId)) {
                if (!foundLocalSegment) {
                    localIngress = fqti;
                }
                totalHops++;
                CtrlPlaneHopContent newHop = new CtrlPlaneHopContent();
                newHop.setLinkIdRef(fqti);
                tmpHops[j] = newHop;
                j++;
                this.log.debug("Injecting local "+newHop.getLinkIdRef());
                localEgress = fqti;
            }
            pathElem = pathElem.getNextElem();
        }
        this.log.debug("handleEro.injectedIntoLocal");
        
        
/*
        for (int i = lastLocalHopIndex + 1; i < hops.length; i++) {
            totalHops++;
            tmpHops[j] = hops[i];
            this.log.debug("Injecting next "+hops[i].getLinkIdRef());
            j++;
        }
        this.log.debug("handleEro.injectedNext");
*/
        String finalPath = "Path is:\n";

        CtrlPlaneHopContent[] resultHops = new CtrlPlaneHopContent[totalHops];
        for (int i = 0; i < totalHops; i++) {
            resultHops[i] = tmpHops[i];
            finalPath += resultHops[i].getLinkIdRef()+"\n";
        }
        this.log.debug(finalPath);


        newPath.setHop(resultHops);

        pathInfo.setPath(newPath);

        String altPathStr = "Local alternate path\n";
        Path altPath = this.findLocalAltPath(localIngress, localEgress, bandwidth, startTime, endTime, reservation, "L2", localPath);
        if (altPath != null) {
            PathElem ape = altPath.getPathElem();
            while (ape != null) {
                String altFqti = ape.getLink().getFQTI();
                altPathStr += altFqti+"\n";
                ape = ape.getNextElem();
            }
        } else {
            this.log.info("No alt path:\n\n");
        }
        this.log.debug(altPathStr);


        this.log.debug("handleExplicitRouteObject.finish");
    }

    private Path joinPaths(Path pathA, Path pathB) {

        Path result = new Path();
        if (pathA.getPathElem() != null) {
            // very first element of the first path
            PathElem firstOfA = pathA.getPathElem();
            if (pathB.getPathElem() != null) {
                // find very last element of the first path
                PathElem lastOfA = pathA.getPathElem();
                while (lastOfA.getNextElem() != null) {
                    lastOfA = lastOfA.getNextElem();
                }
                Link lastLinkOfA = lastOfA.getLink();

                // first path element of the second path
                PathElem firstOfB = pathB.getPathElem();
                Link firstLinkOfB = firstOfB.getLink();
                while (lastLinkOfA.equalsTopoId(firstLinkOfB)) {
                    firstOfB = firstOfB.getNextElem();
                    firstLinkOfB = firstOfB.getLink();
                }

                lastOfA.setNextElem(firstOfB);
            } else {
                result = pathA;
            }

            // ok, joined the two, now setPath() for all the pathElems
            result.setPathElem(firstOfA);
            while (firstOfA.getNextElem() != null) {
                firstOfA.setPath(result);
                firstOfA = firstOfA.getNextElem();
            }
        } else {
            result = pathB;
        }
        return result;
    }




    public Path directPath(Link src, Link dst) throws PathfinderException {

        this.log.debug("checking if "+src.getFQTI()+" and "+dst.getFQTI()+" are directly connected");
        boolean linked = false;
        if (!src.isValid()) {
            throw new PathfinderException("Link with id: "+src.getFQTI()+" is no longer valid");
        } else if (!dst.isValid()) {
            throw new PathfinderException("Link with id: "+dst.getFQTI()+" is no longer valid");
        }

        // Special case: if links are in same node
        if (src.getPort().getNode().equals(dst.getPort().getNode())) {
            linked = true;
            this.log.debug(" yes, same node");
        } else if (src.getRemoteLink() != null && src.getRemoteLink().equals(dst)) {
            // TODO: check if there is available bandwidth!
            this.log.debug(" yes, are linked");
            linked = true;
        }
        if (linked) {
            Path newPath = new Path();
            PathElem pathElemSrc = new PathElem();
            pathElemSrc.setLink(src);

            PathElem pathElemDst = new PathElem();
            pathElemDst.setLink(dst);
            pathElemSrc.setNextElem(pathElemDst);

            newPath.setPathElem(pathElemSrc);

            return(newPath);
        } else {
            this.log.debug(" no, are not connected");
            return null;
        }
    }



    public Path findPathBetween(String src, String dst, Long bandwidth,
                                Long startTime, Long endTime, Reservation reservation, String layer)
            throws PathfinderException {
        this.log.debug("findPathBetween.start");


        DomainDAO domDAO = new DomainDAO(this.dbname);

        if (layer.equals("L2")) {
            Link srcLink = domDAO.getFullyQualifiedLink(src);
            Link dstLink = domDAO.getFullyQualifiedLink(dst);
            if (srcLink == null) {
                throw new PathfinderException("Could not locate link in DB for string: "+src);
            } else if (dstLink == null) {
                throw new PathfinderException("Could not locate link in DB for string: "+dst);
            }

            Path directPath = this.directPath(srcLink, dstLink);
            if (directPath != null) {
                return directPath;
            }
        }

        Path path = new Path();

        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph =
            dbga.dbToGraph(bandwidth, startTime, endTime, reservation, this.objectsToReweigh, null);
        src = URNParser.parseTopoIdent(src).get("fqti");
        dst = URNParser.parseTopoIdent(dst).get("fqti");

        DijkstraShortestPath sp;
        Iterator peIt;
        try {
            sp = new DijkstraShortestPath(graph, src, dst);
        } catch (Exception ex) {
            throw new PathfinderException(ex.getMessage());
        }

        if ((sp == null) || (sp.getPathEdgeList() == null)) {
            return null;
        }


        peIt = sp.getPathEdgeList().iterator();

        PathElem pathElem = null;
        PathElem prvPathElem = null;
        boolean firstEdge = true;
        boolean lastEdge = false;

        while (peIt.hasNext()) {
            DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();
            if (!peIt.hasNext()) {
                lastEdge = true;
            }

            String[] cols = edge.toString().split("\\s\\:\\s");

            Double weight = graph.getEdgeWeight(edge);
            this.log.debug(edge);

            String topoId = cols[0].substring(1);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(topoId);
            String type = parseResults.get("type");
            if (type.equals("link")) {
                this.log.debug("Adding "+topoId+" edge "+weight);
                Link link = domDAO.getFullyQualifiedLink(topoId);
                pathElem = new PathElem();
                pathElem.setLink(link);
                if (firstEdge) {
                    firstEdge = false;
                    path.setPathElem(pathElem);
                } else {
                    prvPathElem.setNextElem(pathElem);
                }
                prvPathElem = pathElem;
            }
        }

        Link lastLink = domDAO.getFullyQualifiedLink(dst);
        PathElem lastPathElem = new PathElem();
        lastPathElem.setLink(lastLink);
        prvPathElem.setNextElem(lastPathElem);


        this.log.debug("findPathBetween.end");
        return path;

    }


    public Path findLocalAltPath(String src, String dst, Long bandwidth,
            Long startTime, Long endTime, Reservation reservation, String layer, Path primaryPath)
                throws PathfinderException {
        this.log.debug("findLocalAltPath.start");


        DomainDAO domDAO = new DomainDAO(this.dbname);

        if (layer.equals("L2")) {
            Link srcLink = domDAO.getFullyQualifiedLink(src);
            Link dstLink = domDAO.getFullyQualifiedLink(dst);
            if (srcLink == null) {
                throw new PathfinderException("Could not locate link in DB for string: "+src);
            } else if (dstLink == null) {
                throw new PathfinderException("Could not locate link in DB for string: "+dst);
            }
        }

        HashMap<String, Long> alreadyReserved = new HashMap<String, Long>();
        PathElem ppe = primaryPath.getPathElem();

        while (ppe != null) {
            Link link = ppe.getLink();
            Port port = link.getPort();
            Node node = port.getNode();
            String linkFQTI = link.getFQTI();
            String portFQTI = port.getFQTI();
            String nodeFQTI = node.getFQTI();
            this.objectsToReweigh.put(nodeFQTI, 20d);
//            this.objectsToReweigh.put(portFQTI, 20d);
//            this.objectsToReweigh.put(linkFQTI, 20d);
            if (linkFQTI.equals(src) || linkFQTI.equals(dst)) {
            } else {
                alreadyReserved.put(portFQTI, bandwidth);
            }
            ppe = ppe.getNextElem();
        }

        Path path = new Path();


        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph =
                dbga.dbToGraph(bandwidth, startTime, endTime, reservation, this.objectsToReweigh, alreadyReserved);
        src = URNParser.parseTopoIdent(src).get("fqti");
        dst = URNParser.parseTopoIdent(dst).get("fqti");

        DijkstraShortestPath sp;
        Iterator peIt;
        try {
            sp = new DijkstraShortestPath(graph, src, dst);
        } catch (Exception ex) {
            this.log.error(ex);
            throw new PathfinderException(ex.getMessage());
        }

        if ((sp == null) || (sp.getPathEdgeList() == null)) {
            this.log.info("No alternate path found");
            return null;
        }


        peIt = sp.getPathEdgeList().iterator();

        PathElem pathElem = null;
        PathElem prvPathElem = null;
        boolean firstEdge = true;
        boolean lastEdge = false;

        while (peIt.hasNext()) {
            DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();
            if (!peIt.hasNext()) {
                lastEdge = true;
            }

            String[] cols = edge.toString().split("\\s\\:\\s");

            Double weight = graph.getEdgeWeight(edge);
            this.log.debug(edge);

            String topoId = cols[0].substring(1);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(topoId);
            String type = parseResults.get("type");
            if (type.equals("link")) {
                this.log.debug("Adding "+topoId+" edge "+weight);
                Link link = domDAO.getFullyQualifiedLink(topoId);
                pathElem = new PathElem();
                pathElem.setLink(link);
                if (firstEdge) {
                    firstEdge = false;
                    path.setPathElem(pathElem);
                } else {
                    prvPathElem.setNextElem(pathElem);
                }
                prvPathElem = pathElem;
            }
        }

        Link lastLink = domDAO.getFullyQualifiedLink(dst);
        PathElem lastPathElem = new PathElem();
        lastPathElem.setLink(lastLink);
        prvPathElem.setNextElem(lastPathElem);


        this.log.debug("findLocalAltPath.end");
        return path;
    }

    private Path removeLoops(Path path) {
        Path newPath = path;
        return newPath;
    }

    /**
     * @return the objectsToReweigh
     */
    public HashMap<String, Double> getObjectsToReweigh() {
        return objectsToReweigh;
    }

    /**
     * @param objectsToReweigh the objectsToReweigh to set
     */
    public void setObjectsToReweigh(HashMap<String, Double> objectsToReweigh) {
        this.objectsToReweigh = objectsToReweigh;
    }


}
