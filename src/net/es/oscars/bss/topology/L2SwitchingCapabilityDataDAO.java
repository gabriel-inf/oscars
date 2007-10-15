package net.es.oscars.bss.topology;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * L2SwitchingCapabilityDataDAO is the data access object for the
 * bss.l2SwitchingCapabilityData table.
 *
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class L2SwitchingCapabilityDataDAO extends
    GenericHibernateDAO<L2SwitchingCapabilityData,Integer> { 

    public L2SwitchingCapabilityDataDAO(String dbname) {
        this.setDatabase(dbname);
    }
    
    /**
     * Select table entry given a link
     *
     * @param link the parent link of the requested entry
     * @return entry that corresponds to link
     */
    public L2SwitchingCapabilityData getFromLink(Link link){
         String hsql = "from L2SwitchingCapabilityData "+
            "where link = ?";
        
         return (L2SwitchingCapabilityData) this.getSession().createQuery(hsql)
                                         .setEntity(0, link)
                                         .setMaxResults(1)
                                         .uniqueResult();
    }
}
