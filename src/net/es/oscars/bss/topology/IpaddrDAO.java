package net.es.oscars.bss.topology;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * IpaddrDAO is the data access object for the bss.ipaddrs table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class IpaddrDAO extends GenericHibernateDAO<Ipaddr,Integer> { 

    public IpaddrDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /**
     * Gets valid Ipaddr instance associated with given link.
     *
     * @param link Link instance associated with a path
     * @return ipaddrObj Ipaddr instance
     */
    public Ipaddr fromLink(Link link) {

        String sql = "select * from ipaddrs ip " +
            "inner join links l on l.id = ip.linkId " +
            "where l.id = ? and ip.valid = true";

        Ipaddr ipaddrObj = (Ipaddr) this.getSession().createSQLQuery(sql)
                                .addEntity(Ipaddr.class)
                                .setInteger(0, link.getId())
                                .setMaxResults(1)
                                .uniqueResult();
        return ipaddrObj;
    }
}
