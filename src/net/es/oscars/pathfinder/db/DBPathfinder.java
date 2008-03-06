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

    public Path findPathBetween(Link src, Link dst, Long bandwidth) {
        String srcStr = src.getFQTI();
        String dstStr = dst.getFQTI();
        return this.findPathBetween(srcStr, dstStr, bandwidth);
    }

    public Path findPathBetween(String src, String dst, Long bandwidth) {

        Path path = new Path();

        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph(bandwidth);
        src = TopologyUtil.parseTopoIdent(src).get("fqti");
        dst = TopologyUtil.parseTopoIdent(dst).get("fqti");

        DijkstraShortestPath sp;
        Iterator peIt;

        sp = new DijkstraShortestPath(graph, src, dst);

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
            String topoId = cols[0].substring(1);
            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(topoId);
            String type = parseResults.get("type");
            if (type.equals("link")) {
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
        return path;


    }

    /**
     * Finds a path given just source and destination or by expanding
     * a path the user explicitly sets
     *
     * @param pathInfo PathInfo instance containing hops of entire path
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo) throws PathfinderException{
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        CtrlPlanePathContent localPathForOSCARSDatabase;
        CtrlPlanePathContent pathToForwardToNextDomain;

        if(ctrlPlanePath == null){
            /* Calculate path that contains strict local hops and
            loose interdomain hops */
            CtrlPlanePathContent path = null;

            pathInfo.setPath(path);
        } else {

        }

        return pathInfo;  // just for compatibility with interface
    }

}
