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
            "inner join ipaddrs ip on p.id = ip.portId";

        sql += " where ip.ip = ?";
        Node node = (Node) this.getSession().createSQLQuery(sql)
                                        .addEntity(Node.class)
                                        .setString(0, ip)
                                        .setMaxResults(1)
                                        .uniqueResult();
        return node;
    }

    public void invalidateAll() {
        List<Node> nodes = this.list();
        for (Node node: nodes) {
            node.setValid(false);
            this.update(node);
        }
    }

    public void removeAllInvalid() {
        String sql = "select * from nodes where valid = false";
        List<Node> nodes = (List<Node>) this.getSession().createSQLQuery(sql)
                                        .addEntity(Node.class)
                                        .list();
        for (Node node: nodes) {
            this.remove(node);
        }
    }

    public void validate(String nodeName) {
        Node node = this.queryByParam("name", nodeName);
        // do nothing if doesn't exist
        if (node != null) {
            node.setValid(true);
            this.update(node);
        }
    }
}
