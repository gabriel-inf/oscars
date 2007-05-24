package net.es.oscars.bss.topology;

import java.util.*;
import org.apache.log4j.*;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * PathDAO is the data access object for the bss.Paths table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathDAO extends GenericHibernateDAO<Path, Integer> {
    private Logger log;
    
    public PathDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
    }

    /**
     * Retrieves a list of paths starting with the given IP address.
     *
     * @param ip String with starting IP address
     * @return list of paths starting with the IP address
     */
    public List<Path> getPaths(String ip) {
        String sql = "select * from paths p " +
                     "inner join pathElems pe on p.pathElemId = pe.id " +
                     "inner join ipaddrs ip on pe.ipaddrId = ip.id " +
                     "where ip.ip = ?";
        List<Path> paths =
               (List<Path>) this.getSession().createSQLQuery(sql)
                                             .addEntity(Path.class)
                                             .setString(0, ip)
                                             .list();
        return paths;
    }
}
