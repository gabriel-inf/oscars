package net.es.oscars.aaa;

import java.util.List;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * SiteDAO is the data access object for the bss.institutions table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class SiteDAO extends GenericHibernateDAO<Site, Integer> {

    public SiteDAO(String dbname) {
        this.setDatabase(dbname);
    }

/**
 * List<String> getInstitutions
 * get the names of all institutions that contain the domain given by topoId\
 * 
 * @param String topoId  topology Identifier of the domain
 * @return List of the institution names that contain this domain
 */
    public List<String> getInstitutions(String topoId) {

        List<String> institutions= null;

        // query for all sites were topologyId=topoId
        String hsql = "from Site" +
        "where topologyId = ? ";
        List<Site> sites = (List<Site>) this.getSession().createQuery(hsql)
        .setString(0, topoId)
        .list();

        for (Site site: sites) {
            institutions.add(site.getInstitution().getName());
        }
        return institutions;
    }
}