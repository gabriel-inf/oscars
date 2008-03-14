package net.es.oscars.pathfinder.db;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.rmi.RemoteException;

import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.db.util.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import org.hibernate.SessionFactory;
import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import org.apache.log4j.*;

/**
 * DBPathfinder that uses the local database to calculate path
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class DBPathfinder extends Pathfinder implements PCE {
    private Properties props;
    private Logger log;
    private DomainDAO domDAO;
    private DBGraphAdapter dbga;
    private PSLookupClient lookupClient;

    public DBPathfinder(String dbname) {
        super(dbname);
        this.lookupClient = new PSLookupClient();
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("dbpath", true);

        this.dbga = new DBGraphAdapter(dbname);

        List<String> dbnames = new ArrayList<String>();
        dbnames.add(dbname);

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
        sf.getCurrentSession().beginTransaction();

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
        if (pathInfo.getLayer2Info() != null) {
           this.handleLayer2ERO(pathInfo, reservation);
        } else if (pathInfo.getLayer3Info() != null) {
            throw new PathfinderException("DB pathfinder does not handle layer 3 yet");
        } else {
            throw new PathfinderException(
                "An ERO must have associated layer 2 or layer 3 info");
        }


        this.log.debug("findPath.End");
        return pathInfo;
    }




    private void handleLayer2ERO(PathInfo pathInfo, Reservation reservation)
            throws PathfinderException {
        int numHops = 0;
        this.log.debug("handleLayer2ERO.start");
        Layer2Info layer2Info = pathInfo.getLayer2Info();

        Hashtable<String, String> parseResults;
        String fqti = null;

        String srcEndpoint = layer2Info.getSrcEndpoint();
        String destEndpoint = layer2Info.getDestEndpoint();

        this.log.debug(srcEndpoint);
        this.log.debug(destEndpoint);

        this.log.debug("handleLayer2ERO.endpoints");

        parseResults = TopologyUtil.parseTopoIdent(srcEndpoint);
        if (parseResults != null) {
            fqti = parseResults.get("fqti");
        } else {
            fqti = null;
        }

        if (fqti == null) {
            if (this.lookupClient == null) {
                throw new PathfinderException("Could not resolve "+srcEndpoint);
            } else {
                try {
                    fqti = this.lookupClient.lookup(srcEndpoint);
                } catch (BSSException ex) {
                    throw new PathfinderException("Could not resolve "+srcEndpoint+" . Error was: "+ex.getMessage());
                }
            }
        }
        srcEndpoint = fqti;

        fqti = null;

        parseResults = TopologyUtil.parseTopoIdent(destEndpoint);
        if (parseResults != null) {
            fqti = parseResults.get("fqti");
        } else {
            fqti = null;
        }
        if (fqti == null) {
            if (this.lookupClient == null) {
                throw new PathfinderException("Could not resolve "+destEndpoint);
            } else {
                try {
                    destEndpoint = this.lookupClient.lookup(destEndpoint);
                } catch (BSSException ex) {
                    throw new PathfinderException("Could not resolve "+destEndpoint+" . Error was: "+ex.getMessage());
                }
            }
        }
        destEndpoint = fqti;

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
                throw new PathfinderException("ERO must include both source and destination!");
            } else if (hops.length >= 2) {
                CtrlPlaneHopContent firstHop = hops[0];
                CtrlPlaneHopContent lastHop = hops[hops.length - -1];
                String firstLinkId = firstHop.getLinkIdRef();
                parseResults = TopologyUtil.parseTopoIdent(firstLinkId);
                firstLinkId = parseResults.get("fqti");
                String lastLinkId = lastHop.getLinkIdRef();
                parseResults = TopologyUtil.parseTopoIdent(lastLinkId);
                lastLinkId = parseResults.get("fqti");
                if ( (! firstLinkId.equals(srcEndpoint)) || (! lastLinkId.equals(destEndpoint)) ) {
                    throw new PathfinderException("ERO must include both source and destination!");
                }
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

            parseResults = TopologyUtil.parseTopoIdent(hopId);
            fqti = parseResults.get("fqti");
            String domainId = parseResults.get("domainId");

            if (domDAO.isLocal(domainId)) {
                if (!foundLocalSegment) {
                    firstLocalHopIndex = i;
                    foundLocalSegment = true;
                }
                this.log.debug("Local link: "+fqti);
                localLinkIds.add(localLinkIndex, fqti);
                localLinkIndex++;
                lastLocalHopIndex = i;
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
            Path segmentPath = findPathBetween(src, dst, bandwidth, startTime, endTime);
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
            Path segmentPath = findPathBetween(lastLocalLink, nextHop, bandwidth, startTime, endTime);
            localPath = joinPaths(localPath, segmentPath);
            this.log.debug("handleEro.foundToNext");
        }




        // inject local path into previous:
        int totalHops = 0;
        CtrlPlanePathContent newPath = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] tmpHops = new CtrlPlaneHopContent[100]; // should be enough..
        for (int i = 0; i < firstLocalHopIndex; i++) {
            totalHops++;
            tmpHops[i] = hops[i];
            this.log.debug("Injecting previous "+hops[i].getLinkIdRef());
        }
        this.log.debug("handleEro.injectedPrevious");

        int j = firstLocalHopIndex;
        PathElem pathElem = localPath.getPathElem();
        while (pathElem != null) {
            totalHops++;
            CtrlPlaneHopContent newHop = new CtrlPlaneHopContent();
            newHop.setLinkIdRef(pathElem.getLink().getFQTI());
            tmpHops[j] = newHop;
            j++;
            this.log.debug("Injecting local "+newHop.getLinkIdRef());
            pathElem = pathElem.getNextElem();
        }

        this.log.debug("handleEro.injectedLocal");

        for (int i = lastLocalHopIndex + 1; i < hops.length; i++) {
            totalHops++;
            tmpHops[j] = hops[i];
            this.log.debug("Injecting next "+hops[i].getLinkIdRef());
            j++;
        }
        this.log.debug("handleEro.injectedNext");

        CtrlPlaneHopContent[] resultHops = new CtrlPlaneHopContent[totalHops];
        for (int i = 0; i < totalHops; i++) {
            resultHops[i] = tmpHops[i];
        }


        newPath.setHop(resultHops);

        pathInfo.setPath(newPath);


        this.log.debug("handleExplicitRouteObject.finish");
    }

    private Path joinPaths(Path onePath, Path otherPath) {
        Path result = new Path();
        if (onePath.getPathElem() != null) {
            PathElem firstOne = onePath.getPathElem();
            if (otherPath.getPathElem() != null) {
                PathElem lastOne = onePath.getPathElem();
                while (lastOne.getNextElem() != null) {
                    lastOne = lastOne.getNextElem();
                }
                lastOne.setNextElem(otherPath.getPathElem());
            }
            result.setPathElem(firstOne);
            while (firstOne.getNextElem() != null) {
                firstOne.setPath(result);
                firstOne = firstOne.getNextElem();
            }
        } else {
            result = otherPath;
        }
        return result;
    }




    public Path findPathBetween(Link src, Link dst, Long bandwidth, Long startTime, Long endTime)
            throws PathfinderException {
        String srcStr = src.getFQTI();
        String dstStr = dst.getFQTI();
        return this.findPathBetween(srcStr, dstStr, bandwidth, startTime, endTime);
    }

    public Path findPathBetween(String src, String dst, Long bandwidth, Long startTime, Long endTime)
            throws PathfinderException {
        this.log.debug("findPathBetween.start");

        Path path = new Path();

        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph(bandwidth, startTime, endTime);
        src = TopologyUtil.parseTopoIdent(src).get("fqti");
        dst = TopologyUtil.parseTopoIdent(dst).get("fqti");

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
            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(topoId);
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

    private Path removeLoops(Path path) {
        Path newPath = path;
        return newPath;
    }


}
