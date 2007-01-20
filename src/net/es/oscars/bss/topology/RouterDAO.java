package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * RouterDAO is the data access object for the bss.routers table.
 * 
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 *
 */
public class RouterDAO extends GenericHibernateDAO<Router, Integer> {

    /**
     * Inserts a row into the routers table.
     *
     * @param router a Router instance to be persisted
     */
    public void create(Router router) {
        this.makePersistent(router);
    }

    /**
     * Retrieves all routers.  For now just calls the generic findAll.
     *
     * @return routers A list of router instances
     */
    public List<Router> list() {

        List<Router> routers = this.findAll();
        return routers;
    }

    /**
     * Deletes a row from the routers table.
     *
     * @param router a Router instance to remove from the database
     */
    public void remove(Router router) {
        this.makeTransient(router);
    }

    /**
     * Returns the router having an interface with this IP address.
     * @param ipaddr String containing IP address of router.
     * @return A Router instance.
     * @throws BSSException.
     */
    public Router fromIp(String ipaddr) {
        String sql = "select * from routers r " +
            "inner join interfaces i on r.id = i.routerId " +
            "inner join ipaddrs ip on i.id = ip.interfaceId";

        sql += " where ip.ip = ?";
        Router router = (Router) this.getSession().createSQLQuery(sql)
                                        .addEntity(Router.class)
                                        .setString(0, ipaddr)
                                        .setMaxResults(1)
                                        .uniqueResult();
        return router;
    }
}
