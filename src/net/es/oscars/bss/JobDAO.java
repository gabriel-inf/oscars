package net.es.oscars.bss;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.database.GenericHibernateDAO;

/**
 * JobDAO is the data access object for
 * the bss.jobs table.
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class JobDAO extends GenericHibernateDAO<Job, Integer> {
    private Logger log;
    private String dbname;
    private List<Job> jobs;

    public JobDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    @SuppressWarnings("unchecked")
    public List<Job> list(Long startTime, Long endTime) throws BSSException {
    	this.log.info("list.start");

    	ArrayList<Job> jobList = new ArrayList<Job>();
    	String hsql = "from Job j where "+"" +
    			"(j.scheduledTime >= :startTime) and"+
    			"(j.scheduledTime <= :endTime)";
    			
        Query query = this.getSession().createQuery(hsql);
        query.setLong("startTime", startTime);
        query.setLong("endTime", endTime);
    	
        
        this.log.debug("HSQL is: ["+hsql+"]");
        

        jobList = (ArrayList<Job>) query.list();

    	this.log.info("list.finish");
    	return jobList;
    }

}
