package net.es.oscars.pathfinder.db;

import java.util.*;
import net.es.oscars.pathfinder.*;
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
public class DBPathfinder extends Pathfinder implements LocalPCE, InterdomainPCE {
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

        String srcEndpoint = requestedPath.getLayer2Data().getSrcEndpoint();
        String dstEndpoint = requestedPath.getLayer2Data().getDestEndpoint();
        Link srcLink = domDAO.getFullyQualifiedLink(srcEndpoint);
        Link dstLink = domDAO.getFullyQualifiedLink(dstEndpoint);

        if (srcLink == null) {
            throw new PathfinderException("Source link is unknown!");
        }
        if (dstLink == null) {
            throw new PathfinderException("Destination link is unknown!");
        }
        if (!srcLink.getPort().getNode().getDomain().isLocal()) {
            throw new PathfinderException("Source link is not local!");
        }
        if (!dstLink.getPort().getNode().getDomain().isLocal()) {
            throw new PathfinderException("Destination link is not local!");
        }

        List<PathElem> localSegment = this.extractLocalSegment(requestedPath);

        if (localSegment == null) {
            path = this.findPathBetween(srcLink.getFQTI(), dstLink.getFQTI(), resv, "L2");
        } else {
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
        }


        try {
            path.setPathType(PathType.LOCAL);
        } catch (BSSException ex) {
            this.log.error(ex.getMessage());
            throw new PathfinderException(ex.getMessage());
        }

        paths.add(path);
        return paths;
    }



    public List<Path> findL3LocalPath(Reservation resv, Path requestedPath) throws PathfinderException {
        ArrayList<Path> paths = new ArrayList<Path>();

        TracerouteHelper trcHelper = new TracerouteHelper(this.dbname);
        TracerouteResult trcResult = trcHelper.findEdgeLinks(requestedPath);

        String src = trcResult.srcLink.getFQTI();
        String dst = trcResult.dstLink.getFQTI();

        Path path = this.findPathBetween(src, dst, resv, "L3");
        try {
            path.setPathType(PathType.LOCAL);
        } catch (BSSException ex) {
            this.log.error(ex.getMessage());
            throw new PathfinderException(ex.getMessage());
        }
        paths.add(path);

        return paths;
    }


    public List<Path> findInterdomainPath(Reservation resv) throws PathfinderException {
        ArrayList<Path> paths = new ArrayList<Path>();
        return paths;
    }

    private Path joinPaths(Path pathA, Path pathB) {

        Path result = new Path();
        if (!pathA.getPathElems().isEmpty()) {
            // if need to join
            if (!pathB.getPathElems().isEmpty()) {
                // probably safer than the shorter version with setPathElems
                List<PathElem> pathAElems = pathA.getPathElems();
                for (PathElem pathAElem: pathAElems) {
                    result.addPathElem(pathAElem);
                }
                List<PathElem> pathBElems = pathB.getPathElems();
                for (PathElem pathBElem: pathBElems) {
                    result.addPathElem(pathBElem);
                }
                // FIXME: check for sane path
            } else {
                result = pathA;
            }
        } else {
            result = pathB;
        }
        return result;
    }

    private Path directPath(Link src, Link dst) throws PathfinderException {
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
            newPath.addPathElem(pathElemSrc);

            PathElem pathElemDst = new PathElem();
            pathElemDst.setLink(dst);
            newPath.addPathElem(pathElemDst);

            return(newPath);
        }
        this.log.debug(" no, are not connected");
        return null;
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
                path.addPathElem(pathElem);
            }
        }
        Link lastLink = domDAO.getFullyQualifiedLink(dst);
        PathElem lastPathElem = new PathElem();
        lastPathElem.setLink(lastLink);
        path.addPathElem(lastPathElem);
        this.log.debug("findPathBetween.end");
        return path;
    }


}
