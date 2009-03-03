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

    public Ipaddr getValidIpaddr(String ip) {
        String sql = "select * from ipaddrs ip " +
                     "where ip.ip = ? and ip.valid = 1";
        Ipaddr ipaddr = (Ipaddr) this.getSession().createSQLQuery(sql)
                                        .addEntity(Ipaddr.class)
                                        .setString(0, ip)
                                        .setMaxResults(1)
                                        .uniqueResult();
        return ipaddr;
    }
}
