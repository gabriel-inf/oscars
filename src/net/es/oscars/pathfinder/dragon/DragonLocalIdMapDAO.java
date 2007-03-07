package net.es.oscars.pathfinder.dragon;

import java.util.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * DragonLocalIdMap is the data access object for the bss.dragonLocalIdMap table.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class DragonLocalIdMapDAO extends GenericHibernateDAO<DragonLocalIdMap,Integer> { 

    /**
     * Inserts a row into the DragonLocalIdMap table.
     *
     * @param localId a local id instance to be persisted
     */
    public void create(DragonLocalIdMap localId) {
        this.makePersistent(localId);
    }

    /**
     * List all dragon local ids.
     *
     * @return a list of dragon local ids
     */
    public List<DragonLocalIdMap> list() {
        List<DragonLocalIdMap> localIds = null;

        String hsql = "from DragonLocalIdMap";
        localIds = this.getSession().createQuery(hsql).list();
        return localIds;
    }

    /**
     * Deletes a row from the DragonLocalIdMap table.
     *
     * @param localId an DragonLocalIdMap instance to remove from the database
     */
    public void remove(DragonLocalIdMap localId) {
        this.makeTransient(localId);
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
