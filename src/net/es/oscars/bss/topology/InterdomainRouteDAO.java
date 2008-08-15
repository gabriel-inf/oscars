package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;
import net.es.oscars.bss.BSSException;
import org.hibernate.SQLQuery;
import net.es.oscars.database.GenericHibernateDAO;

/**
 * InterdomainRouteDAO the data access object for bss.interdomainRoutes
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class InterdomainRouteDAO extends GenericHibernateDAO<InterdomainRoute, Integer> {
    private Logger log;
    private String dbname;
    
    /** 
     * Default constructor that sets the database and initialise the logger
     *
     * @param dbname the name of the database to use
     */
    public InterdomainRouteDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }
    /**
     * Looks-up routes that match the given source and destination. If no
     * routes match then the default route or null is returned. It orders
     * routes my source, destination and preference. Matches that are more 
     * specific are ordered higher (i.e. a matching link is higher than a 
     * matching port, etc).
     * 
     * @param srcLink a Link object representing the source of the path
     * @param destURN the domain, node, port or link URN of the destination
     * @return a list of matching routes. null or default route if no matches.
     * @throws BSSException
     */
    public List<InterdomainRoute> lookupRoute(Link srcLink, String destURN)
        throws BSSException{
        
        Port srcPort = srcLink.getPort();
        Node srcNode = srcPort.getNode();
        SQLQuery query = this.createQuery(TopologyUtil.LINK_URN, destURN);
        return (List<InterdomainRoute>) query.setInteger(0, srcNode.getId())
                                             .setInteger(1, srcPort.getId())
                                             .setInteger(2, srcLink.getId())
                                             .list();
    }
    
    /**
     * Looks-up routes that match the given source and destination. If no
     * routes match then the default route or null is returned. It orders
     * routes my source, destination and preference. Matches that are more 
     * specific are ordered higher (i.e. a matching link is higher than a 
     * matching port, etc).
     * 
     * @param srcPort a Port object representing the source of the path
     * @param destURN the domain, node, port or link URN of the destination
     * @return a list of matching routes. null or default route if no matches.
     * @throws BSSException
     */
    public List<InterdomainRoute> lookupRoute(Port srcPort, String destURN)
        throws BSSException{
        
        Node srcNode = srcPort.getNode();
        SQLQuery query = this.createQuery(TopologyUtil.PORT_URN, destURN);
        return (List<InterdomainRoute>) query.setInteger(0, srcNode.getId())
                                             .setInteger(1, srcPort.getId())
                                             .list();
    }
    
    /**
     * Looks-up routes that match the given source and destination. If no
     * routes match then the default route or null is returned. It orders
     * routes my source, destination and preference. Matches that are more 
     * specific are ordered higher (i.e. a matching link is higher than a 
     * matching port, etc).
     * 
     * @param srcNode a Node object representing the source of the path
     * @param destURN the domain, node, port or link URN of the destination
     * @return a list of matching routes. null or default route if no matches.
     * @throws BSSException
     */
    public List<InterdomainRoute> lookupRoute(Node srcNode, String destURN)
        throws BSSException{
        
        SQLQuery query = this.createQuery(TopologyUtil.NODE_URN, destURN);
        return (List<InterdomainRoute>) query.setInteger(0, srcNode.getId()).list();
    }
    
    /**
     * Convenience function for creating route lookup queries.
     *
     * @param srcURNType the type of URN. see TopologyUtil constants.
     * @param destURN the domain, node, port or link URN of the destination
     * @return the generated query with the src* entities not set
     * @throws BSSException
     */
    private SQLQuery createQuery(int srcURNType, String destURN) 
        throws BSSException{
        
        String[] componentList = destURN.split(":");
        Domain destDomain = null;
        Node destNode = null;
        Port destPort = null;
        Link destLink = null;
        SQLQuery query = null;
        int offset = srcURNType - 4;
        String sql = "SELECT * FROM interdomainRoutes WHERE defaultRoute=1 ";
        String orderByClause = " order by srcLinkId DESC, srcPortId DESC, " +
            "srcNodeId DESC, destLinkId DESC, destPortId DESC, destNodeId DESC, " +
            "destDomainId DESC, preference ASC, defaultRoute ASC";
        String whereClause = "OR (";
        
        /* Match against given source */   
        if(srcURNType >= TopologyUtil.NODE_URN){
            whereClause += "(srcNodeId=? OR srcNodeId IS NULL)";
        } 
        if(srcURNType >= TopologyUtil.PORT_URN){
            whereClause += " OR (srcPortId=? OR srcPortId IS NULL)";
        }
        if(srcURNType >= TopologyUtil.LINK_URN){
            whereClause += " OR (srcLinkId=? OR srcLinkId IS NULL)";   
        } 
         
         whereClause += ") AND ";
        /* Match against given destination */
        if(componentList.length == 7){
            try{
                destLink = TopologyUtil.getLink(destURN, dbname);
                destPort = destLink.getPort();
                destNode = destPort.getNode();
                destDomain = destNode.getDomain();
                whereClause += "(destDomainId=? OR destNodeId=? OR destPortId=? OR " +
                                "destLinkId=?)";
                sql += (whereClause + orderByClause);
                query = this.getSession().createSQLQuery(sql)
                               .addEntity(InterdomainRoute.class);
                query.setInteger(offset, destDomain.getId());
                query.setInteger(offset + 1, destNode.getId());
                query.setInteger(offset + 2, destPort.getId());
                query.setInteger(offset + 3, destLink.getId());
            }catch(BSSException e){}
        }
        
        if(componentList.length == 6 || (componentList.length > 6 && 
            destPort == null)){
            
            String urn = "urn:ogf:network:" + componentList[3] + ":" + 
                componentList[4] + ":" + componentList[5];
            try{
                destPort = TopologyUtil.getPort(urn, dbname);
                destNode = destPort.getNode();
                destDomain = destNode.getDomain();
                whereClause += "(destDomainId=? OR destNodeId=? OR destPortId=?)";
                sql += (whereClause + orderByClause);
                query = this.getSession().createSQLQuery(sql)
                            .addEntity(InterdomainRoute.class);
                query.setInteger(offset, destDomain.getId());
                query.setInteger(offset + 1, destNode.getId());
                query.setInteger(offset + 2, destPort.getId());
             }catch(BSSException e){}
        }
        
        if(componentList.length == 5 || (componentList.length > 5 && 
            destNode == null)){
            
            String urn = "urn:ogf:network:" + componentList[3] + ":" + 
                componentList[4];
            try{
                destNode = TopologyUtil.getNode(urn, dbname);
                destDomain = destNode.getDomain();
                whereClause += "(destDomainId=? OR destNodeId=?)";
                sql += (whereClause + orderByClause);
                query = this.getSession().createSQLQuery(sql)
                            .addEntity(InterdomainRoute.class);
                query.setInteger(offset, destDomain.getId());
                query.setInteger(offset + 1, destNode.getId());
            }catch(BSSException e){}
        }
        
        if(componentList.length == 4 || (componentList.length > 4 && 
            destDomain == null)){
            
            String urn = "urn:ogf:network:" + componentList[3];
            destDomain = TopologyUtil.getDomain(urn, dbname);
            whereClause += "destDomainId=?";
            sql += (whereClause + orderByClause);
            query = this.getSession().createSQLQuery(sql)
                        .addEntity(InterdomainRoute.class);
            query.setInteger(offset, destDomain.getId());
        }
        
        /* Not sure if will ever reach this, but if give funny urn this will 
           catch it */
        if(destDomain == null){
            throw new BSSException("Unable to identify destination " 
                + destURN);
        }
        this.log.info(sql);
        
        return query;
    }
    
    /**
     * Lists all the entries in the interdomainRoutes table
     *
     * @return list of interdomain routes
     */
    public List<InterdomainRoute> list(){
        String sql = "SELECT * FROM interdomainRoutes";
        return this.getSession().createSQLQuery(sql)
                   .addEntity(InterdomainRoute.class).list();
    }
}
