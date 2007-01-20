package net.es.oscars.bss.topology;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * IpaddrDAO is the data access object for the bss.ipaddrs table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class IpaddrDAO extends GenericHibernateDAO<Ipaddr,Integer> { 

    /**
     * Inserts a row into the ipaddrs table.
     *
     * @param ipaddr an Ipaddr instance to be persisted
     */
    public void create(Ipaddr ipaddr) {
        this.makePersistent(ipaddr);
    }

    /**
     * List all interfaces.
     *
     * @return a list of interfaces
     */
    public List<Ipaddr> list() {
        List<Ipaddr> ipaddrs = null;

        String hsql = "from Ipaddr";
        ipaddrs = this.getSession().createQuery(hsql).list();
        return ipaddrs;
    }

    /**
     * Deletes a row from the ipaddrs table.
     *
     * @param ipaddr an Ipaddr instance to remove from the database
     */
    public void remove(Ipaddr ipaddr) {
        this.makeTransient(ipaddr);
    }

    /**
     * Returns ipaddr instance given SQL query with routerName and addressType
     *     as named parameters.
     * @param sql string containing a SQL query
     * @param routerName  string with the name of associated router
     * @param addressType string with address type ("ingress" or "egress")
     * @return ipaddrObj an ipaddr instance, if any, obtained from the query
     */
    public Ipaddr getAddress(String sql, String routerName,
                             String addressType) {

        Ipaddr ipaddrObj = (Ipaddr) this.getSession().createSQLQuery(sql)
                                .addEntity(Ipaddr.class)
                                .setString(0, routerName)
                                .setString(1, addressType)
                                .setMaxResults(1)
                                .uniqueResult();
        return ipaddrObj;
    }

    /**
     * Gets trace or loopback IP address, if any, associated with router, given
     *     routerName. 
     * @param routerName string containing name of router
     * @param addressType string containing type of address to look for
     * @return string with the IP address of the desired type, if any
     */
    public String getIpType(String routerName, String addressType) {

        // given router name, get address if any
        String sql = "select * from ipaddrs ip " +
            "inner join interfaces i on i.id = ip.interfaceId " +
            "inner join routers r on r.id = i.routerId " +
            "where r.name = ? and ip.description = ?";

        Ipaddr ipaddrObj = this.getAddress(sql, routerName, addressType);
        //
        // if the lookup fails, return null, else return the IP
        if (ipaddrObj == null){ return "";}
        else
            return ipaddrObj.getIp();
    }
}
