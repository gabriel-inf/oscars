package net.es.oscars.pathfinder.dragon;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * DragonLocalIdMap is the data access object for the bss.dragonLocalIdMap table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class DragonLocalIdMapDAO extends GenericHibernateDAO<DragonLocalIdMap,Integer> { 
    
    
    public DragonLocalIdMapDAO() {
        this.setDatabase("bss");
    }
    
    /**
     * Gets dragon local id where vslrIP and host ip match
     *
     * @param hostIP ip address of host
     * @param vlsrIP ip address of vlsr
     */
    public DragonLocalIdMap getFromIPs(String hostIP, String vlsrIP) {
        String sql = "SELECT * FROM dragonLocalIdMap AS d INNER JOIN ipaddrs AS i WHERE d.vlsrIpId = i.id && d.ip= ? && i.ip= ?";
        DragonLocalIdMap localIDObj = (DragonLocalIdMap) this.getSession().createSQLQuery(sql)
                               .addEntity(DragonLocalIdMap.class)
                               .setString(0, hostIP)
                               .setString(1, vlsrIP)
                               .setMaxResults(1)
                               .uniqueResult();
        return localIDObj;
    }
  
}
