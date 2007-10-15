package net.es.oscars.bss;

import java.util.*;
import org.apache.log4j.*;

import org.hibernate.*;

import net.es.oscars.database.GenericHibernateDAO;


/**
 * IdSequenceDAO is the data access object for
 * the bss.idSequence table.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Andrew Lake (alake@internet2.edu)
 */
public class IdSequenceDAO
    extends GenericHibernateDAO<IdSequence, Integer> {

    private Logger log;
    private String dbname;

    public IdSequenceDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    /**
     * Creates a new entries in the table and returns an ID. This will create 
     * a lot of entries but makes locking issues easy.
     *
     * @return the new id as an int
     * @throws BSSException
     */
    public int getNewId() throws BSSException {
        IdSequence id = new IdSequence();
        this.getSession().save(id);
        int newId = id.getId().intValue();
        
        return newId;
    }
}
