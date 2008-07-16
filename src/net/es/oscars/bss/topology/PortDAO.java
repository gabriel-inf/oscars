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
     * @param topologyIdent the topology identifier (NOT fully qualified)
     * @param node the parent node of the port
     * @return a Port instance with the given topologyIdent and parent node
     */
    public Port fromTopologyIdent(String topologyIdent, Node node){
        String sql = "select * from ports "+
            "where nodeId = ? AND topologyIdent = ?";
        
         return (Port) this.getSession().createSQLQuery(sql)
                                         .addEntity(Port.class)
                                         .setInteger(0, node.getId())
                                         .setString(1, topologyIdent)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }
}
