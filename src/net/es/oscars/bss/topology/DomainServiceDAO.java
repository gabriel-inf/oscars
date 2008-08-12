package net.es.oscars.bss.topology;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * DomainServiceDAO is the data access object for the bss.domainServices table.
 *
 */
public class DomainServiceDAO extends GenericHibernateDAO<Node, Integer> {

    public DomainServiceDAO(String dbname) {
        this.setDatabase(dbname);
    }

    /**
     * Returns a URL for the service belonging to the given domain and 
     * having the given type
     *
     * @param domain Domain of the service to lookup
     * @param type the type of service to lookup
     * @return A Node instance.
     */
    public String getUrl(Domain domain, String type){
        String sql = "SELECT ds.* FROM domainServices AS ds INNER JOIN domains " +
                     "AS d ON ds.domainId=d.id WHERE d.topologyIdent=? AND " +
                     "ds.type=?";
        String url = null;
        DomainService service = (DomainService) this.getSession().createSQLQuery(sql)
                                        .addEntity(DomainService.class)
                                        .setString(0, domain.getTopologyIdent())
                                        .setString(1, type)
                                        .setMaxResults(1)
                                        .uniqueResult();
        if(service != null){
            url = service.getUrl();
        }
        return url;
    }
}
