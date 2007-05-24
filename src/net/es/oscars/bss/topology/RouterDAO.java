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

    public RouterDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /**
     * Returns the router having an interface with this IP address.
     * @param ip String containing IP address of router.
     * @return A Router instance.
     * @throws BSSException.
     */
    public Router fromIp(String ip) {
        String sql = "select * from routers r " +
            "inner join interfaces i on r.id = i.routerId " +
            "inner join ipaddrs ip on i.id = ip.interfaceId";

        sql += " where ip.ip = ?";
        Router router = (Router) this.getSession().createSQLQuery(sql)
                                        .addEntity(Router.class)
                                        .setString(0, ip)
                                        .setMaxResults(1)
                                        .uniqueResult();
        return router;
    }

    public void invalidateAll() {
        List<Router> routers = this.list();
        for (Router r: routers) {
            r.setValid(false);
            this.update(r);
        }
    }

    public void removeAllInvalid() {
        String sql = "select * from routers where valid = false";
        List<Router> routers = (List<Router>) this.getSession().createSQLQuery(sql)
                                        .addEntity(Router.class)
                                        .list();
        for (Router r: routers) {
            this.remove(r);
        }
    }

    public void validate(String routerName) {
        Router router = this.queryByParam("name", routerName);
        // do nothing if doesn't exist
        if (router != null) {
            router.setValid(true);
            this.update(router);
        }
    }
}
