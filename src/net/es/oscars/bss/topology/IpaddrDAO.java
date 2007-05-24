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
     * Gets ipaddr instance, given IP address and what type of validity.
     * @param ip string containing IP address
     * @param valid boolean indicating whether to look for valid IP
     * @return ipaddr valid Ipaddr instance
     */
    public Ipaddr getIpaddr(String ip, boolean valid) {

        String sql = "select * from ipaddrs ip " +
            "where ip.IP = ? and ip.valid = ?";

        Ipaddr ipaddrObj = (Ipaddr) this.getSession().createSQLQuery(sql)
                                .addEntity(Ipaddr.class)
                                .setString(0, ip)
                                .setBoolean(1, valid)
                                .setMaxResults(1)
                                .uniqueResult();
        return ipaddrObj;
    }

    /**
     * Gets trace or loopback IP address, if any, associated with router, given
     *     routerName. 
     * @param routerName string containing name of router
     * @param description string containing type of address to look for
     * @return string with the IP address of the desired type, if any
     */
    public String getIpType(String routerName, String description) {

        // given router name, get address if any
        String sql = "select * from ipaddrs ip " +
            "inner join interfaces i on i.id = ip.interfaceId " +
            "inner join routers r on r.id = i.routerId " +
            "where r.name = ? and ip.description = ? " +
            "and ip.valid = true";

        Ipaddr ipaddrObj = (Ipaddr) this.getSession().createSQLQuery(sql)
                                .addEntity(Ipaddr.class)
                                .setString(0, routerName)
                                .setString(1, description)
                                .setMaxResults(1)
                                .uniqueResult();
        //
        // if the lookup fails, return null, else return the IP
        if (ipaddrObj == null) { return null; }
        else
            return ipaddrObj.getIP();
    }

    public void invalidateAll() {
        List<Ipaddr> ipaddrs = this.list();
        for (Ipaddr ipaddr: ipaddrs) {
            ipaddr.setValid(false);
            this.update(ipaddr);
        }
    }
}
