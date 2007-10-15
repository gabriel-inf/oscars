package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * NodeDAO is the data access object for the bss.nodes table.
 * 
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 *
 */
public class NodeDAO extends GenericHibernateDAO<Node, Integer> {

    public NodeDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /**
     * Returns the node having an port with this IP address.
     * @param ip String containing IP address of node.
     * @return A Node instance.
     * @throws BSSException.
     */
    public Node fromIp(String ip) {
        String sql = "select * from nodes n " +
            "inner join ports p on n.id = p.nodeId " +
            "inner join links l on p.id = l.portId " +
            "inner join ipaddrs ip on l.id = ip.linkId";

        sql += " where ip.ip = ?";
        Node node = (Node) this.getSession().createSQLQuery(sql)
                                        .addEntity(Node.class)
                                        .setString(0, ip)
                                        .setMaxResults(1)
                                        .uniqueResult();
        return node;
    }
    
    /**
     * Returns a node given a topology identifier and the parent domain.
     *
     * @param topologyIdent the topology identifier (NOT fully quialified)
     * @param domain the parent domain of the node
     * @param a Node instance iwth the given topologyIdent and parent domain
     */
    public Node fromTopologyIdent(String topologyIdent, Domain domain){
        String hsql = "from Node "+
            "where domain = ? AND topologyIdent = ?";
        
         return (Node) this.getSession().createQuery(hsql)
                                         .setEntity(0, domain)
                                         .setString(1, topologyIdent)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }
}
