package net.es.oscars.pathfinder.dragon;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;


import net.es.oscars.pathfinder.Domain;

/**
 * PeerIpaddrDAO is the data access object for the bss.peerIpaddrs table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class PeerIpaddrDAO extends GenericHibernateDAO<PeerIpaddr,Integer> { 

    /**
     * Inserts a row into the ipaddrs table.
     *
     * @param ipaddr an Ipaddr instance to be persisted
     */
    public void create(PeerIpaddr peerIpaddr) {
        this.makePersistent(peerIpaddr);
    }

    /**
     * List all interfaces.
     *
     * @return a list of interfaces
     */
    public List<PeerIpaddr> list() {
        List<PeerIpaddr> ipaddrs = null;

        String hsql = "from PeerIpaddr";
        ipaddrs = this.getSession().createQuery(hsql).list();
        return ipaddrs;
    }

    /**
     * Deletes a row from the ipaddrs table.
     *
     * @param ipaddr an Ipaddr instance to remove from the database
     */
    public void remove(PeerIpaddr ipaddr) {
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
    public PeerIpaddr getAddress(String sql, String ip) {

        PeerIpaddr ipaddrObj = (PeerIpaddr) this.getSession().createSQLQuery(sql)
                                .addEntity(PeerIpaddr.class)
                                .setString(0, ip)
                                .setMaxResults(1)
                                .uniqueResult();
        return ipaddrObj;
    }
    
    public Domain getDomain(String ip){
    	String sql = "SELECT * FROM peerIpaddrs WHERE ip = ?";
    	
    	PeerIpaddr ipaddrObj = this.getAddress(sql, ip);
        //
        // if the lookup fails, return null, else return the IP
        if (ipaddrObj == null){ return null;}
        else
            return ipaddrObj.getDomain();
    }
}
