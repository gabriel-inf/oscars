package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * PortDAO is the data access object for the bss.ports table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PortDAO extends GenericHibernateDAO<Port, Integer> {

    public PortDAO(String dbname) {
        this.setDatabase(dbname);
    }
    
    /**
     * Returns a port given a topology identifier and the parent domain.
     *
     * @param topologyIdent the topology identifier (NOT fully quialified)
     * @param node the parent node of the port
     * @param a Port instance iwth the given topologyIdent and parent node
     */
    public Port fromTopologyIdent(String topologyIdent, Node node){
        String hsql = "from Port "+
            "where node = ? AND topologyIdent = ?";
        
         return (Port) this.getSession().createQuery(hsql)
                                         .setEntity(0, node)
                                         .setString(1, topologyIdent)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }
}
