package net.es.oscars.bss;

import net.es.oscars.bss.topology.*;
import net.es.oscars.database.GenericHibernateDAO;

import org.apache.log4j.*;

import org.hibernate.*;

import java.util.*;


/**
 * HistoryDAO is the data access object for
 * the bss.history table.
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class HistoryDAO extends GenericHibernateDAO<History, Integer> {
    private Logger log;
    private String dbname;
    private List<History> histories;

    public HistoryDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
        this.histories = new ArrayList<History>();
    }
}
