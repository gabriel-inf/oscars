package net.es.oscars.bss.topology;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * EdgeInfoDAO is the data access object for the bss.edgeInfos table.
 *
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class EdgeInfoDAO extends GenericHibernateDAO<EdgeInfo,Integer> { 

    public EdgeInfoDAO(String dbname) {
        this.setDatabase(dbname);
    }

     /**
     * Finds next domain by looking up first hop in edgeInfo table
     *
     * @param ip String with IP address of first hop past local domain
     * @return Domain an instance associated with the next domain, if any
     * @throws BSSException
     */
    public Domain getDomain(String ip){
        // TODO:  alternative field in edgeInfos for lookup
        String sql = "SELECT * FROM edgeInfos WHERE externalIP = ?";
        EdgeInfo edgeAddress =
               (EdgeInfo) this.getSession().createSQLQuery(sql)
                                             .addEntity(EdgeInfo.class)
                                             .setString(0, ip)
                                             .setMaxResults(1)
                                             .uniqueResult();
        //
        // if the lookup fails, return null, else return the IP
        if (edgeAddress == null) { return null; }
        else { return edgeAddress.getDomain(); }
    }
}
