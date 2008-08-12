package net.es.oscars.bss.topology;

import java.util.*;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.database.GenericHibernateDAO;
import net.es.oscars.bss.BSSException;


/**
 * DomainDAO is the data access object for the bss.domains table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class DomainDAO extends GenericHibernateDAO<Domain, Integer> {
    private Logger log;
    private String dbname;

    public DomainDAO(String dbname) {
        this.setDatabase(dbname);
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Retrieves local domain (domain running reservation manager).
     *
     * @return a domain instance
     */
    public Domain getLocalDomain() {
        String hsql = "from Domain where local=1";
        return (Domain) this.getSession().createQuery(hsql)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }

    /**
     * Retrieves domain given a topology identifier
     *
     * @param topologyIdent the topology identifier of a domain
     * @return a domain instance
     */
    public Domain fromTopologyIdent(String topologyIdent) {
        String hsql = "from Domain where topologyIdent=?";
        if (topologyIdent == null) {
            this.log.error("Null topology ident!");
            return null;
        }


        topologyIdent = topologyIdent.replaceAll("domain=", "").trim();

        if (topologyIdent == "") {
            this.log.error("Empty topology ident!");
            return null;
        }

        Domain dom = null;

        dom = (Domain) this.getSession().createQuery(hsql)
                .setString(0, topologyIdent)
                .setMaxResults(1)
                .uniqueResult();

        if (dom == null) {
            this.log.error("Could not find domain for topology ident ["+topologyIdent+"]");
        }

        return dom;

    }

     /**
     * Finds next domain by looking up first hop in edgeInfo table
     *
     * @param nextHop CtrlPlaneHopContent with next hop past local domain
     * @return Domain an instance associated with the next domain, if any
     * @throws BSSException
     */
    public Domain getNextDomain(CtrlPlaneHopContent nextHop)
            throws BSSException {

        EdgeInfoDAO edgeInfoDAO = new EdgeInfoDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Domain nextDomain = null;

        this.log.info("getNextDomain.start: " + nextHop.getLinkIdRef());
        String[] componentList = nextHop.getLinkIdRef().split(":");

        if (componentList.length != 7) {
            throw new BSSException("Hop " + nextHop.getLinkIdRef() + " must" +
                " be in format urn:ogf:network:domainId:nodeId:portId:linkId");
        }

        // check if topologyIdent maps to known domain
        nextDomain = this.fromTopologyIdent(componentList[3]);
        if (nextDomain != null) {
            return nextDomain;
        }

        if (!componentList[3].equals("other")) {
            return null;
        }
        // IP address for lookup is last item
        String ip = componentList[6];
        nextDomain = edgeInfoDAO.getDomain(ip);
        if (nextDomain != null) {
            this.log.info("getNextDomain.nextDomain: " +
                          nextDomain.getTopologyIdent());
        }
        return nextDomain;
    }

    /**
     * Given a domain topology id, checks to see if it is the local domain.
     *
     * @param topologyIdent a string with the topology id
     * @return boolean indicating whether this is a local domain
     */
    public boolean isLocal(String topologyIdent) {
        String domainTopoId = topologyIdent.replaceAll("domain=", "");
        Domain domain = this.queryByParam("topologyIdent", domainTopoId);
        if (domain == null) {
            return false;
        }
        return domain.isLocal();
    }

    /**
     * Converts topology identifier to IP address.
     *
     * @param topologyIdent String with topology identifier
     * @return string with IP address
     */
     public String convertTopologyIdentifier(String topologyIdent) {
         String ip = null;
         String[] componentList = topologyIdent.split(":");
         // TODO:  error checking
         Link link = this.getFullyQualifiedLink(componentList);
         Ipaddr ipaddr = link.getValidIpaddr();
         return ipaddr.getIP();
     }

     /**
      * Given a fully qualified portId, find the
      * corresponding port in the database.
      *
      * @param portFullTopoId string with the fully qualified port id
      * @return Port unique port instance
      */
    public Port getFullyQualifiedPort(String portFullTopoId) {
        Port port = null;
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(portFullTopoId);
        String type = parseResults.get("type");
        String domainId = parseResults.get("domainId");
        String nodeId = parseResults.get("nodeId");
        String portId = parseResults.get("portId");
        if (type.equals("port")) {
            port = this.getFullyQualifiedPort(domainId, nodeId, portId);
        }
        return port;
    }

    /**
     * Given domain, node and port locally scoped topology identifiers,
     * find the corresponding port in the database.
     *
     * @param domainTopoId the domain topology identifier
     * @param nodeTopoId the node topology identifier
     * @param portTopoId the port topology identifier
     * @return Port unique Port instance
     */
    public Port getFullyQualifiedPort(String domainTopoId, String nodeTopoId, String portTopoId) {
        String sql = "select * from ports p " +
        "inner join nodes n on n.id = p.nodeId " +
        "inner join domains d on d.id = n.domainId " +
        "where p.topologyIdent = ? " +
        "and n.topologyIdent = ? and d.topologyIdent = ? ";
        Port port = null;
        port = (Port) this.getSession().createSQLQuery(sql)
        .addEntity(Port.class)
        .setString(0, portTopoId)
        .setString(1, nodeTopoId)
        .setString(2, domainTopoId)
        .setMaxResults(1)
        .uniqueResult();
        return port;
    }



    /**
     * Given a String[] containing urn:ogf:network:domainId:nodeId:portId:linkId, find the
     * corresponding link in the database.
     *
     * @param componentList string with all elements of name
     * @return Link unique Link instance
     */
    public Link getFullyQualifiedLink(String[] componentList) {
        Link link = null;
        if(componentList.length == 7) {
            String linkTopoId = componentList[6].replaceAll("link=", "");
            String portTopoId = componentList[5].replaceAll("port=", "");
            String nodeTopoId = componentList[4].replaceAll("node=", "");
            String domainTopoId = componentList[3].replaceAll("domain=", "");
            link = this.getFullyQualifiedLink(domainTopoId, nodeTopoId, portTopoId, linkTopoId);
        }

        return link;
    }
     /**
      * Given a fully qualified linkId, find the
      * corresponding link in the database.
      *
      * @param linkFullTopoId string with the fully qualified link id
      * @return Link unique Link instance
      */
    public Link getFullyQualifiedLink(String linkFullTopoId) {
        Link link = null;
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(linkFullTopoId);
        String type = parseResults.get("type");
        String domainId = parseResults.get("domainId");
        String nodeId = parseResults.get("nodeId");
        String portId = parseResults.get("portId");
        String linkId = parseResults.get("linkId");
        if (type != null && type.equals("link")) {
            link = this.getFullyQualifiedLink(domainId, nodeId, portId, linkId);
        }
        return link;
    }
    /**
     * Given domain, node, port and link locally scoped topology identifiers,
     * find the corresponding link in the database.
     *
     * @param domainTopoId the domain topology identifier
     * @param nodeTopoId the node topology identifier
     * @param portTopoId the port topology identifier
     * @param linkTopoId the link topology identifier
     * @return Link unique Link instance
     */
    public Link getFullyQualifiedLink(String domainTopoId, String nodeTopoId, String portTopoId, String linkTopoId) {
        String sql = "select * from links l " +
        "inner join ports p on p.id = l.portId " +
        "inner join nodes n on n.id = p.nodeId " +
        "inner join domains d on d.id = n.domainId " +
        "where l.topologyIdent = ? and p.topologyIdent = ? " +
        "and n.topologyIdent = ? and d.topologyIdent = ? ";
        Link link = null;
        link = (Link) this.getSession().createSQLQuery(sql)
        .addEntity(Link.class)
        .setString(0, linkTopoId)
        .setString(1, portTopoId)
        .setString(2, nodeTopoId)
        .setString(3, domainTopoId)
        .setMaxResults(1)
        .uniqueResult();
        return link;
    }

    /**
     * Given a domain identifier and an IP address, generate a full
     * topology identifier.
     *
     * @param domainIdent string with domain identifier
     * @param hop string with IP address
     * @return fqn full topology identifier (domain:node:port:link)
     */
    public String setFullyQualifiedLink(String domainIdent, String hop) {
        String fqn = null;

        if (domainIdent.equals("other")) {
            // TODO:  better solution
            fqn = "urn:ogf:network:other:other:other:" + hop;
            return fqn;
        }
        String sql = "select * from links l " +
            "inner join ipaddrs ip on l.id = ip.linkId " +
            "where ip.IP = ? AND ip.valid = 1";
        Link link = (Link) this.getSession().createSQLQuery(sql)
                      .addEntity(Link.class)
                      .setString(0, hop)
                      .setMaxResults(1)
                      .uniqueResult();
        Port port = link.getPort();
        Node node = port.getNode();
        Domain domain = node.getDomain();
        fqn = "urn:ogf:network:" + domain.getTopologyIdent() + ":" +
                     node.getTopologyIdent() + ":" +
                     port.getTopologyIdent() + ":" +
                     link.getTopologyIdent();
        return fqn;
    }
    
    /**
     * Returns a list of the local domain's direct neighbors
     *
     * @return a list of the local domain's direct neighbors
     */
    public List<Domain> getNeighbors(){
         String sql = "SELECT DISTINCT rd.* FROM domains AS d INNER JOIN " +
                      "nodes AS n ON d.id=n.domainId INNER JOIN ports AS p " +
                      "ON n.id=p.nodeId INNER JOIN links AS l ON " +
                      "p.id=l.portId INNER JOIN links AS rl ON " +
                      "l.id=rl.remoteLinkId INNER JOIN ports AS rp ON " +
                      "rp.id=rl.portId INNER JOIN nodes AS rn ON " +
                      "rn.id=rp.nodeID INNER JOIN domains AS rd ON " +
                      "rd.id=rn.domainId WHERE d.local=1 AND rd.local!=1";
          return (List<Domain>) this.getSession().createSQLQuery(sql).addEntity(Domain.class).list();           
    }
}
