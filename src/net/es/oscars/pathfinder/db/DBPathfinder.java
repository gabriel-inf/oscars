package net.es.oscars.pathfinder.db;

import java.util.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.interdomain.*;
import net.es.oscars.pathfinder.db.util.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import org.jgrapht.alg.*;
import org.jgrapht.graph.*;

import org.apache.log4j.*;

/**
 * DBPathfinder that uses the local database to calculate path
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class DBPathfinder extends GenericInterdomainPathfinder implements LocalPCE, InterdomainPCE {
    private Logger log = Logger.getLogger(DBPathfinder.class);
    private DomainDAO domDAO;
    private DBGraphAdapter dbga;
    private HashMap<String, Double> objectsToReweigh;

    public DBPathfinder(String dbname) {
        super(dbname);

        this.log = Logger.getLogger(this.getClass());

        this.dbga = new DBGraphAdapter(dbname);
        this.objectsToReweigh = new HashMap<String, Double>();
        domDAO = new DomainDAO(dbname);
    }

    public List<Path> findLocalPath(Reservation resv) throws PathfinderException {
        List<Path> paths = new ArrayList<Path>();

        Path requestedPath = null;
        try {
            requestedPath = resv.getPath(PathType.REQUESTED);
        } catch (BSSException ex) {
            this.log.error(ex.getMessage());
            throw new PathfinderException(ex.getMessage());
        }

        Layer2Data l2data = requestedPath.getLayer2Data();
        Layer3Data l3data = requestedPath.getLayer3Data();

        if (l2data == null && l3data == null) {
            throw new PathfinderException("No L2 data or L3 data");
        } else if (l2data != null && l3data != null) {
            throw new PathfinderException("Both L2 data and L3 data were specified");
        } else if (l2data != null) {
            paths = this.findL2LocalPath(resv, requestedPath);
        } else if (l3data != null) {
            paths = this.findL3LocalPath(resv, requestedPath);
        }
        return paths;
    }

    public List<Path> findL2LocalPath(Reservation resv, Path requestedPath) throws PathfinderException {
        ArrayList<Path> paths = new ArrayList<Path>();
        Path path = null;

        List<PathElem> localSegment = null;
        try {
            List<PathElem> requestedLocalSegment = this.extractLocalSegment(requestedPath);
            List<PathElem> interdomainLocalSegment = this.extractLocalSegment(resv.getPath(PathType.INTERDOMAIN));

            if (requestedLocalSegment == null || requestedLocalSegment.size() < 2) {
                if (interdomainLocalSegment == null || interdomainLocalSegment.size() < 2) {
                }
                localSegment = interdomainLocalSegment;

            } else {
                localSegment = requestedLocalSegment;
            }
        } catch (BSSException ex) {
            throw new PathfinderException(ex.getMessage());
        }


        for (int i = 0; i < localSegment.size() -1; i++) {
            String src = localSegment.get(i).getLink().getFQTI();
            String dst = localSegment.get(i+1).getLink().getFQTI();
            this.log.debug("Finding path between: ["+src+"] ["+dst+"] ");
            Path partialPath = findPathBetween(src, dst, resv, "L2");
            if (partialPath == null) {
                throw new PathfinderException("Could not find path between ["+src+"] and ["+dst+"]");
            } else {
                path = joinPaths(path, partialPath);
            }
        }

        Layer2Data l2data = requestedPath.getLayer2Data().copy();
        path.setLayer2Data(l2data);

        try {
            path.setPathType(PathType.LOCAL);
        } catch (BSSException ex) {
            this.log.error(ex.getMessage());
            throw new PathfinderException(ex.getMessage());
        }

        for (int i = 0; i < path.getPathElems().size(); i++) {
            PathElem pe = path.getPathElems().get(i);
            for (PathElem rpe : localSegment) {
                if (pe.getLink().getFQTI().equals(rpe.getLink().getFQTI())) {
                    try {
                       this.log.debug("Copying path parameters for "+pe.getUrn());
                       path.getPathElems().set(i, PathElem.copyPathElem(rpe));
                   } catch (BSSException ex) {
                       throw new PathfinderException(ex.getMessage());
                   }
                }
            }
        }
        for (PathElem pe : path.getPathElems()) {
            this.log.debug("L2 local hop:"+pe.getUrn());
        }


        paths.add(path);
        return paths;
    }



    public List<Path> findL3LocalPath(Reservation resv, Path requestedPath) throws PathfinderException {
        ArrayList<Path> paths = new ArrayList<Path>();

        String src = null;
        String dst = null;
        boolean gotValidExplicitPath = false;
        // if we have received an explicit path, use that:
        if (requestedPath.getPathElems() != null &&
            !requestedPath.getPathElems().isEmpty()) {
            List<PathElem> localSegment = this.extractLocalSegment(requestedPath);
            if (localSegment != null && !localSegment.isEmpty()) {
                if (localSegment.size() == 1) {
                    throw new PathfinderException("Local segment of explicit path only 1 hop long");
                } else {
                    gotValidExplicitPath = true;
                    src = localSegment.get(0).getUrn();
                    dst = localSegment.get(localSegment.size()-1).getUrn();
                }
            }
        }

        if (!gotValidExplicitPath) {
            TracerouteHelper trcHelper = new TracerouteHelper(this.dbname);
            TracerouteResult trcResult = trcHelper.findEdgeLinks(requestedPath);
            if (trcResult == null) {
                throw new PathfinderException("Could not perform traceroute");
            } else if (trcResult.srcLink == null) {
                throw new PathfinderException("Could not determine source link");
            } else if (trcResult.dstLink == null) {
                throw new PathfinderException("Could not determine destination link");
            }

            src = trcResult.srcLink.getFQTI();
            dst = trcResult.dstLink.getFQTI();
        }

        Path path = this.findPathBetween(src, dst, resv, "L3");
        try {
            path.setPathType(PathType.LOCAL);
        } catch (BSSException ex) {
            this.log.error(ex.getMessage());
            throw new PathfinderException(ex.getMessage());
        }

        for (PathElem pe : path.getPathElems()) {
            this.log.debug("L3 local hop:"+pe.getUrn());
        }

        paths.add(path);

        return paths;
    }

    private Path joinPaths(Path pathA, Path pathB) {
        this.log.debug("joinPaths.start");

        Path result = new Path();
        if (pathA != null && pathA.getPathElems() != null && !pathA.getPathElems().isEmpty()) {
            // if need to join
            if (pathB != null && pathB.getPathElems() != null && !pathB.getPathElems().isEmpty()) {
                // probably safer than the shorter version with setPathElems
                List<PathElem> pathAElems = pathA.getPathElems();
                for (PathElem pathAElem: pathAElems) {
                    result.addPathElem(pathAElem);
                }
                List<PathElem> pathBElems = pathB.getPathElems();
                int pathLength = pathBElems.size();
                // don't duplicate first element
                for (int ctr = 1; ctr < pathLength; ctr++) {
                    PathElem pathBElem = pathBElems.get(ctr);
                    result.addPathElem(pathBElem);
                }
                // FIXME: check for sane path
            } else {
                result = pathA;
            }
        } else {
            result = pathB;
        }
        this.log.debug("joinPaths.end");
        return result;
    }

    private Path directPath(Link src, Link dst) throws PathfinderException {
        String srcUrn = src.getFQTI();
        String dstUrn = dst.getFQTI();
        this.log.debug("checking if "+srcUrn+" and "+dstUrn+" are directly connected");
        boolean linked = false;
        if (!src.isValid()) {
            throw new PathfinderException("Link with id: "+srcUrn+" is no longer valid");
        } else if (!dst.isValid()) {
            throw new PathfinderException("Link with id: "+dstUrn+" is no longer valid");
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
            pathElemSrc.setUrn(srcUrn);
            newPath.addPathElem(pathElemSrc);

            PathElem pathElemDst = new PathElem();
            pathElemDst.setLink(dst);
            pathElemDst.setUrn(dstUrn);
            newPath.addPathElem(pathElemDst);

            return(newPath);
        }
        this.log.debug(" no, are not connected");
        return null;
    }

    protected Link findEgressTo(String src, String dst, Reservation resv) throws PathfinderException {
        Link result = super.findEgressTo(src, dst, resv);
        if (result != null) {
            this.log.debug("Egress to next domain: "+result.getFQTI());
            return result;
        }

        Path pathToOther = this.findPathBetween(src, dst, resv, "L2");
        result = this.lastLocalLink(pathToOther);


        this.log.debug("Egress to next domain: "+result.getFQTI());

        return result;
    }



    public Path findPathBetween(String src, String dst, Reservation reservation, String layer)
            throws PathfinderException{
        Long bandwidth = reservation.getBandwidth();
        Long startTime = reservation.getStartTime();
        Long endTime = reservation.getEndTime();
        return this.findPathBetween(src, dst, bandwidth, startTime, endTime, reservation, layer);
    }


    public Path findPathBetween(String src, String dst, Long bandwidth,
                                Long startTime, Long endTime,
                                Reservation reservation, String layer)
            throws PathfinderException {

        this.log.debug("findPathBetween.start");
        DomainDAO domDAO = new DomainDAO(this.dbname);
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
        while (peIt.hasNext()) {
            DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();
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
                pathElem.setUrn(link.getFQTI());
                path.addPathElem(pathElem);
            }
        }
        Link lastLink = domDAO.getFullyQualifiedLink(dst);
        PathElem lastPathElem = new PathElem();
        lastPathElem.setLink(lastLink);
        lastPathElem.setUrn(lastLink.getFQTI());
        path.addPathElem(lastPathElem);
        this.log.debug("findPathBetween.end");
        return path;
    }


}
