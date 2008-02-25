package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * InterdomainRouteDAO the data access object for bss.interdomainRoutes
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class InterdomainRouteDAO extends GenericHibernateDAO<InterdomainRoute, Integer> {
    private Logger log;
    
    public InterdomainRouteDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }
    
    public Link lookupEgressLink(String urn){
        String[] componentList = urn.split(":");
        
        //match link
        //match port
        //match node
        //match domain
        //how do i order these so that those that the close matches are better
        //don't forget about default
        //can i add path preferences?
        
        String hsql = "from InterdomainRoute "+
            "where destLink=?";
        
         /* return (Link) this.getSession().createQuery(hsql)
                                         .setEntity(0, port)
                                         .setString(1, topologyIdent)
                                         .setMaxResults(1)
                                         .uniqueResult(); */
    
        return null;
    }
}
