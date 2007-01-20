package net.es.oscars.bss;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * DomainDAO is the data access object for the oscars.domains table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class DomainDAO extends GenericHibernateDAO<Domain, Integer> { 

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
}
