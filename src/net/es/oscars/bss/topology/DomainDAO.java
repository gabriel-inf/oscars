package net.es.oscars.bss.topology;

import java.util.*;

import org.apache.log4j.*;

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
     * Finds next domain by looking up first hop in edgeInfo table
     *
     * @param nextHop string with IP address of next hop
     * @return Domain an instance associated with the next domain, if any
     * @throws BSSException
     */
    public Domain getNextDomain(String nextHop) throws BSSException {

        EdgeInfoDAO edgeInfoDAO = new EdgeInfoDAO(this.dbname);
        Domain nextDomain = null;

        this.log.info("getNextDomain.nextHop: " + nextHop);
       
        if (nextHop != null) {
            nextDomain = edgeInfoDAO.getDomain(nextHop);
            if (nextDomain != null) {
                this.log.info("getNextDomain.nextDomain: " +
                              nextDomain.getAsNum()+"");
            }
        }
        return nextDomain;
    }
}
