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
     * @return a Link instance with the given topologyIdent and parent port
     */
    public Link fromTopologyIdent(String topologyIdent, Port port){
        String sql = "select * from links "+
            "where portId = ? AND topologyIdent = ?";
        
         return (Link) this.getSession().createSQLQuery(sql)
                                         .addEntity(Link.class)
                                         .setInteger(0, port.getId())
                                         .setString(1, topologyIdent)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }
}
