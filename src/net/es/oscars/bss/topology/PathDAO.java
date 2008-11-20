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
     * Retrieves a list of paths starting with the given link.
     *
     * @param link starting link in path
     * @return list of paths starting with that link
     */
    public List<Path> getPaths(Link link) {
        String sql = "select * from paths p " +
                     "inner join pathElems pe on p.id = pe.pathId " +
                     "inner join links l on pe.linkId = l.id " +
                     "where l.id = ?";
        List<Path> paths =
               (List<Path>) this.getSession().createSQLQuery(sql)
                                             .addEntity(Path.class)
                                             .setInteger(0, link.getId())
                                             .list();
        return paths;
    }
}
