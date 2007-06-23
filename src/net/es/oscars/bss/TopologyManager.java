package net.es.oscars.bss;

import java.util.*;
import java.util.ArrayList;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.CommonPath;
import net.es.oscars.pathfinder.CommonPathElem;

/**
 * This class contains methods to update the topology database, given
 * possibly new topology information.  To do so, it must
 *
 * 1) Mark all existing nodes, ports, and ipaddrs as invalid.  Trying
 *    to determine the equivalency of node, port, and ipaddr information
 *    in the current and new topology would be too time-consuming at this
 *    point.
 *
 * 2) Save the new topology information in the database.  New nodes that were
 *    marked invalid as part of generating the node list are not saved.  In
 *    that case, all information associated with the current node is marked
 *    as valid again.
 *
 * 3) Recalculate the paths for all pending reservations.  If a path then
 *    violates policy by oversubscription or other means, the reservation is
 *    marked invalid, and the old path remains associated with the reservation.
 *
 * 4) Recalculate the paths for all active reservations.  If the path
 *    violates policy or is not the same after recalculation, the reservation
 *    is marked invalid, and the old path remains associated with the
 *    reservation.
 *
 * 5) Remove all invalidated node and port rows.  Remove all invalidated
 *    ipaddrs that are not part of any reservation's path.  Remove all paths
 *    that are no longer associated with any reservation.
 *
 * NOTE:  How this will work with VLAN-associated tables is not determined yet.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class TopologyManager {
    private Logger log;
    private SessionFactory sf;
    private String dbname;
    private ReservationManager rm;
    private Utils utils;

    public TopologyManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        Initializer initializer = new Initializer();
        this.dbname = dbname;
        List<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.rm = new ReservationManager(this.dbname);
        this.utils = new Utils(this.dbname);
    }

    public void updateDb(List<Node> newNodes) {

        this.log.info("updateDb.start");
        this.sf.getCurrentSession().beginTransaction();

        try {
            // step 1
            this.log.info("invalidating current topology information");
            this.invalidateCurrentInfo();
            this.log.info("finished invalidating current topology");

            // step 2
            this.log.info("saving new topology information");
            this.save(newNodes);
            this.log.info("finished saving new topology information");

            // step 3
            this.log.info("recalculating pending paths");
            this.recalculatePaths("PENDING");
            this.log.info("recalculated pending paths");

            // step 4
            this.log.info("recalculating active paths");
            this.recalculatePaths("ACTIVE");
            this.log.info("recalculated active paths");

            // step 5
            this.clean();

        } catch (BSSException e) {
            this.sf.getCurrentSession().getTransaction().rollback();
            this.log.error("updateDb: " + e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            this.sf.getCurrentSession().getTransaction().rollback();
            this.log.error("updateDb exception: " + e.getMessage());
            System.exit(-1);
        }
        this.sf.getCurrentSession().getTransaction().commit();
        this.log.info("updateDb.finish");
    }

    /** Reads existing topology information from the database, and marks
     *  each current node, port, and ipaddr as invalid. 
     */
    private void invalidateCurrentInfo() {
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        nodeDAO.invalidateAll();
        PortDAO portDAO = new PortDAO(this.dbname);
        portDAO.invalidateAll();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        ipaddrDAO.invalidateAll();
    }

    /**
     * Saves node list to database. Saving the nodes also saves the
     * ports and ipaddrs via associations.
     *
     * @param nodes list of nodes to save
     */
    private void save(List<Node> newNodes) {

        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        for (Node n: newNodes) {
            if (n.isValid()) {
                nodeDAO.create(n);
            } else {
                // mark the current node valid again, if one corresponds
                // to the new node
                nodeDAO.validate(n.getName());
            }
        }
    }

    /**
     * Recalculates the paths for reservations with the given status.
     * If the new path violates policy by oversubscription or other means, the
     * reservation is marked invalid, and the old path remains associated with
     * the reservation.  If the reservation is active and the new path differs
     * in any way, the reservation is marked invalid.
     *
     * @param status string with status of reservations to check
     *
     * @throws BSSException
     */
    private void recalculatePaths(String status) throws BSSException {

        String ingressNodeIP = null;
        String egressNodeIP = null;
        CommonPath reqPath = null;
        Path path = null;

        ReservationDAO dao = new ReservationDAO(this.dbname);
        List<Reservation> reservations = dao.statusReservations(status);
        for (Reservation r: reservations) {
            Path oldPath = r.getPath();
            if (oldPath.isExplicit()) {
                reqPath = this.buildExplicitPath(oldPath);
            }
            // find old ingress and egress IP's
            PathElem pathElem = oldPath.getPathElem();
            while (pathElem != null) {
                if (pathElem.getDescription() != null) {
                    if (pathElem.getDescription().equals("ingress")) {
                        ingressNodeIP = pathElem.getIpaddr().getIP();
                    } else if (pathElem.getDescription().equals("egress")) {
                        egressNodeIP = pathElem.getIpaddr().getIP();
                    }
                }
                pathElem = pathElem.getNextElem();
            }
            // This should never happen.  However, the semantics of
            // using these are different than in the original
            // reservation creation, where they can be null.
            if ((ingressNodeIP == null) || (egressNodeIP == null)) {
                r.setStatus("INVALIDATED");
                dao.update(r);
                continue;
            }
            try {
                // finds path and checks for oversubscription
                path = this.rm.getPath(r, ingressNodeIP,
                                       egressNodeIP, reqPath);
            } catch (BSSException e) {
                r.setStatus("INVALIDATED");
                dao.update(r);
                continue;
            }
            if (status.equals("PENDING")) {
                r.setPath(path);
                dao.update(r);
            } else if (status.equals("ACTIVE")) {
                if (!this.utils.isDuplicate(oldPath, path)) {
                    r.setStatus("INVALIDATED");
                    dao.update(r);
                }
            }
        }
    }

    /**
     * Removes invalidated topology information, except for ipaddrs associated
     * with non-pending and non-active paths.  Removes paths that are no longer
     * associated with any reservation.
     */
    private void clean() {
        this.log.info("clean.start");
        this.log.info("removing invalidated nodes");
        // remove nodes and associated ports
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        nodeDAO.removeAllInvalid();
        this.log.info("finished removing invalidated nodes");

        // remove any path that is not part of a reservation
        this.log.info("removing paths that are no longer in use");
        PathDAO pathDAO = new PathDAO(this.dbname);
        List<Path> paths = pathDAO.list();
        for (Path path: paths) {
            Set reservations = path.getReservations();
            if (reservations.isEmpty()) {
                pathDAO.remove(path);
            }
        }
        this.log.info("finished removing paths that are no longer in use");

        // remove all invalid ipaddrs that are not part of any reservation
        // (ipaddrs associated with pending and active reservations are
        // guaranteed to be valid because of path recalculation)
        this.log.info("removing invalid ipaddrs that are no longer in use");
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = ipaddrDAO.list();
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        List<PathElem> pathElems = pathElemDAO.list();
        Set<Ipaddr> ipset = new HashSet<Ipaddr>();
        for (PathElem pathElem: pathElems) {
            ipset.add(pathElem.getIpaddr());
        }
        for (Ipaddr ipaddr: ipaddrs) {
            if (!ipaddr.isValid()) {
                // remove an address if it is not part of any paths
                // or if there is already an invalid copy of it
                if (!ipset.contains(ipaddr)) {
                    ipaddrDAO.remove(ipaddr);
                }
            }
        }
        this.log.info("finished removing invalid ipaddrs");
        this.log.info("clean.finish");
    }

    /**
     * Converts from format retrieved from reservation to common format.
     *
     * @param path a Path (db bean) instance
     * @return reqPath a CommonPath (non-db bean) instance
     */
    private CommonPath buildExplicitPath(Path path) {

        CommonPath reqPath = new CommonPath();
        List<CommonPathElem> cpathElems = new ArrayList<CommonPathElem>();
        CommonPathElem cpathElem = null;
        PathElem pathElem = null;

        reqPath.setExplicit(true);
        pathElem = path.getPathElem();
        while (pathElem != null) {
            cpathElem = new CommonPathElem();
            cpathElem.setLoose(pathElem.isLoose());
            cpathElem.setDescription(pathElem.getDescription());
            cpathElem.setIP(pathElem.getIpaddr().getIP());
            cpathElems.add(cpathElem);
            pathElem = pathElem.getNextElem();
        }
        reqPath.setElems(cpathElems);
        return reqPath;
    }
}
