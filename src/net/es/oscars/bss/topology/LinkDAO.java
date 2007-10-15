package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * LinkDAO is the data access object for the bss.links table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class LinkDAO extends GenericHibernateDAO<Link, Integer> {

    public LinkDAO(String dbname) {
        this.setDatabase(dbname);
    }
    
    /**
     * Returns a link given a topology identifier and the parent port.
     *
     * @param topologyIdent the topology identifier (NOT fully quialified)
     * @param port the parent port of the link
     * @param a Link instance iwth the given topologyIdent and parent port
     */
    public Link fromTopologyIdent(String topologyIdent, Port port){
        String hsql = "from Link "+
            "where port = ? AND topologyIdent = ?";
        
         return (Link) this.getSession().createQuery(hsql)
                                         .setEntity(0, port)
                                         .setString(1, topologyIdent)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }
}
