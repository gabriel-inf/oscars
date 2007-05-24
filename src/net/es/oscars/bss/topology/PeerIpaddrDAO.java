package net.es.oscars.bss.topology;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * PeerIpaddrDAO is the data access object for the bss.peerIpaddrs table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class PeerIpaddrDAO extends GenericHibernateDAO<PeerIpaddr,Integer> { 

    public PeerIpaddrDAO(String dbname) {
        this.setDatabase(dbname);
    }

    public Domain getDomain(String ip){
        String sql = "SELECT * FROM peerIpaddrs WHERE ip = ?";
        PeerIpaddr peerAddress =
               (PeerIpaddr) this.getSession().createSQLQuery(sql)
                                             .addEntity(PeerIpaddr.class)
                                             .setString(0, ip)
                                             .setMaxResults(1)
                                             .uniqueResult();
        //
        // if the lookup fails, return null, else return the IP
        if (peerAddress == null) { return null; }
        else { return peerAddress.getDomain(); }
    }
}
