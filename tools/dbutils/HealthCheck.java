import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.hibernate.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.bss.*;

/**
 * HealthCheck checks for database consistency
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */

public class HealthCheck {

    // time in seconds to look into the future for reservations
    public static void main (String[] args) {
    	String usage = "usage: checkHealth.sh [-repair] \nUse -repair to repair errors (delete orphan entries)";

        Boolean repair = false;

        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-help") ||
                args[i].equals("-h")) {
                System.out.println(usage);
                System.exit(0);
            } else if (args[i].equals("-repair")) {
            	repair = true;
            }
        }

        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        session.beginTransaction();
        
        String domQS = "SELECT * FROM domains";
        SQLQuery domQ = session.createSQLQuery (domQS).addScalar("id", Hibernate.INTEGER);
        
        for (Object domRes : domQ.list()) {
        	Integer domId = (Integer) domRes;
        	/*
        	String nodQS = "SELECT * FROM nodes WHERE domainId = "+domId;
            SQLQuery nodQ = session.createSQLQuery (nodQS).addScalar("id", Hibernate.INTEGER);
            for (Object nodRes : nodQ.list()) {
            	Integer nodId = (Integer) nodRes;
            }
            */
        }

        SQLQuery orphanQ;
        String orphanQS;

        orphanQS = "SELECT * FROM nodes WHERE domainId NOT IN (SELECT id FROM domains)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Orphan node, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM nodeAddresses WHERE nodeId NOT IN (SELECT id FROM nodes)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Orphan nodeAddress, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM ports WHERE nodeId NOT IN (SELECT id FROM nodes)";
        orphanQ = session.createSQLQuery (orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Orphan port, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM links WHERE portId NOT IN (SELECT id FROM ports)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Orphan link, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM links WHERE remoteLinkId NOT IN (SELECT id FROM links)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Invalid remoteLinkId, link id: "+orphan.toString());
        }
        
        orphanQS = "SELECT * FROM l2SwitchingCapabilityData WHERE linkId NOT IN (SELECT id FROM links)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Orphan l2SwCap, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM ipaddrs WHERE linkId NOT IN (SELECT id FROM links)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Orphan ipaddr, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM paths WHERE nextDomainId IS NOT NULL AND nextDomainId NOT IN (SELECT id FROM domains)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Path with invalid nextDomainId, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM paths WHERE mplsDataId IS NOT NULL AND mplsDataId NOT IN (SELECT id FROM mplsData)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Path with invalid mplsDataId, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM paths WHERE layer2DataId IS NOT NULL AND layer2DataId NOT IN (SELECT id FROM layer2Data)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Path with invalid layer2DataId, id: "+orphan.toString());
        }
        
        orphanQS = "SELECT * FROM paths WHERE layer3DataId IS NOT NULL AND layer3DataId NOT IN (SELECT id FROM layer3Data)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Path with invalid layer3DataId, id: "+orphan.toString());
        }
        
        orphanQS = "SELECT * FROM paths WHERE pathElemId IS NOT NULL AND pathElemId NOT IN (SELECT id FROM pathElems)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Path with invalid pathElemId, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM pathElems WHERE linkId NOT IN (SELECT id FROM links)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: PathElem with invalid linkId, id: "+orphan.toString());
        }
        
        orphanQS = "SELECT * FROM pathElems WHERE nextId IS NOT NULL AND nextId NOT IN (SELECT id FROM pathElems)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: PathElem with invalid nextId, id: "+orphan.toString());
        }
        orphanQS = "SELECT * FROM pathElems WHERE nextId IS NOT NULL AND nextId NOT IN (SELECT id FROM pathElems)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: PathElem with invalid nextId, id: "+orphan.toString());
        }

        orphanQS = "SELECT * FROM reservations WHERE pathId NOT IN (SELECT id FROM paths)";
        orphanQ = session.createSQLQuery(orphanQS).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : orphanQ.list()) {
        	System.out.println("ERROR: Reservation with invalid pathId, id: "+orphan.toString());
        }

        session.getTransaction().commit();
    }
    public static String join(List objs, String sep) {
    	String out = "";
    	int i = 0;
    	for (Object o : objs) {
    		out += o.toString();
    		if (i < objs.size() -1) {
    			out += sep;
    		}
    		i++;
    	}
    	return out;
    }
    		
}
