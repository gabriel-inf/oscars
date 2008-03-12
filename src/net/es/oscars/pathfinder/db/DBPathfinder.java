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

    public DBPathfinder(String dbname) {
        super(dbname);
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

        System.out.println("findPath.begin");
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            DomainDAO domainDAO = new DomainDAO(this.dbname);
            CtrlPlaneHopContent[] ctrlPlaneHops = path.getHop();
            // if an ERO, not just ingress and/or egress
           this.log.info("handling explicit route object");
           if (pathInfo.getLayer2Info() != null) {
               this.handleLayer2ERO(pathInfo, reservation);
            } else if (pathInfo.getLayer3Info() != null) {
                throw new PathfinderException("DB pathfinder does not hdle layer 3 yet");
            } else {
                throw new PathfinderException(
                    "An ERO must have associated layer 2 or layer 3 info");
            }
        }
        System.out.println("findPath.End");
        return pathInfo; //return same path to conform to interface
    }




    private void handleLayer2ERO(PathInfo pathInfo, Reservation reservation)
            throws PathfinderException {
        int numHops = 0;
        System.out.println("handleLayer2ERO.start");

        Domain domain = domDAO.getLocalDomain();
        Long bandwidth = reservation.getBandwidth();

        CtrlPlanePathContent ero = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ero.getHop();

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

            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(hopId);
            String fqti = parseResults.get("fqti");
            String domainId = parseResults.get("domainId");

            if (domDAO.isLocal(domainId)) {
                if (!foundLocalSegment) {
                    firstLocalHopIndex = i;
                    foundLocalSegment = true;
                }
                System.out.println("Local link: "+fqti);
                localLinkIds.add(localLinkIndex, fqti);
                localLinkIndex++;
                lastLocalHopIndex = i;
            }

            // reset id ref to local topology identifier
            ctrlPlaneHop.setLinkIdRef(fqti);
            // make schema validator happy
            ctrlPlaneHop.setId(fqti);
        }
        System.out.println("first local hop is: "+firstLocalHopIndex);
        System.out.println("last local hop is: "+lastLocalHopIndex);

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
            System.out.println("Finding path between: ["+src+"] ["+dst+"] bw: "+bandwidth);
            Path segmentPath = findPathBetween(src, dst, bandwidth);
            if (segmentPath == null) {
                throw new PathfinderException("Could not find path between ["+src+"] and ["+dst+"]");
            } else {
                localPath = joinPaths(localPath, segmentPath);
            }
        }
        System.out.println("handleEro.foundLocalPath!");


        if (lastLocalHopIndex < hops.length - 1) {
            String lastLocalLink = localLinkIds.get(localLinkIndex -1);
            String nextHop = hops[lastLocalHopIndex + 1].getLinkIdRef();
            Path segmentPath = findPathBetween(lastLocalLink, nextHop, bandwidth);
            localPath = joinPaths(localPath, segmentPath);
            System.out.println("handleEro.foundToNext");
        }




        // inject local path into previous:
        int totalHops = 0;
        CtrlPlanePathContent newPath = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] tmpHops = new CtrlPlaneHopContent[100]; // should be enough..
        for (int i = 0; i < firstLocalHopIndex; i++) {
            totalHops++;
            tmpHops[i] = hops[i];
            System.out.println("Injecting previous "+hops[i].getLinkIdRef());
        }
        System.out.println("handleEro.injectedPrevious");

        int j = firstLocalHopIndex;
        PathElem pathElem = localPath.getPathElem();
        while (pathElem != null) {
            totalHops++;
            CtrlPlaneHopContent newHop = new CtrlPlaneHopContent();
            newHop.setLinkIdRef(pathElem.getLink().getFQTI());
            tmpHops[j] = newHop;
            j++;
            System.out.println("Injecting local "+newHop.getLinkIdRef());
            pathElem = pathElem.getNextElem();
        }

        System.out.println("handleEro.injectedLocal");

        for (int i = lastLocalHopIndex; i < hops.length; i++) {
            totalHops++;
            tmpHops[j] = hops[i];
            System.out.println("Injecting next "+hops[i].getLinkIdRef());
            j++;
        }
        System.out.println("handleEro.injectedNext");

        CtrlPlaneHopContent[] resultHops = new CtrlPlaneHopContent[totalHops];
        for (int i = 0; i < totalHops; i++) {
            resultHops[i] = tmpHops[i];
        }


        newPath.setHop(resultHops);

        pathInfo.setPath(newPath);


        System.out.println("handleExplicitRouteObject.finish");
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




    public Path findPathBetween(Link src, Link dst, Long bandwidth)
            throws PathfinderException {
        String srcStr = src.getFQTI();
        String dstStr = dst.getFQTI();
        return this.findPathBetween(srcStr, dstStr, bandwidth);
    }

    public Path findPathBetween(String src, String dst, Long bandwidth)
            throws PathfinderException {
        System.out.println("findPathBetween.start");

        Path path = new Path();

        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph(bandwidth);
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
        boolean firstLink = true;

        while (peIt.hasNext()) {
            DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();

            String[] cols = edge.toString().split("\\s\\:\\s");

            // Double weight = graph.getEdgeWeight(edge);
            // System.out.println(edge);

            String topoId = cols[0].substring(1);
            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(topoId);
            String type = parseResults.get("type");
            if (type.equals("link")) {
                System.out.println("Adding "+topoId+" edge");
                Link link = domDAO.getFullyQualifiedLink(topoId);
                pathElem = new PathElem();
                pathElem.setLink(link);
                if (firstLink) {
                    firstLink = false;
                    path.setPathElem(pathElem);
                } else {
                    prvPathElem.setNextElem(pathElem);
                }
                prvPathElem = pathElem;
            }
        }
        System.out.println("findPathBetween.end");
        return path;

    }


}
