import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.Transaction;

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
        
        SQLQuery query;
        String queryString;
        
        
        System.out.println("1. Doing general sanity checks.");
        
        System.out.println("1.1 Checking orphan nodes...");
        queryString = "SELECT * FROM nodes WHERE domainId NOT IN (SELECT id FROM domains)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Orphan node, id: "+orphan.toString());
        }

        System.out.println("1.2 Checking orphan nodeAddresses...");
        queryString = "SELECT * FROM nodeAddresses WHERE nodeId NOT IN (SELECT id FROM nodes)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Orphan nodeAddress, id: "+orphan.toString());
        }

        System.out.println("1.3 Checking orphan ports...");
        queryString = "SELECT * FROM ports WHERE nodeId NOT IN (SELECT id FROM nodes)";
        query = session.createSQLQuery (queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Orphan port, id: "+orphan.toString());
        }

        System.out.println("1.4 Checking orphan links...");
        queryString = "SELECT * FROM links WHERE portId NOT IN (SELECT id FROM ports)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Orphan link, id: "+orphan.toString());
        }

        System.out.println("1.5 Checking remoteLinks...");
        queryString = "SELECT * FROM links WHERE remoteLinkId NOT IN (SELECT id FROM links)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Invalid remoteLinkId, link id: "+orphan.toString());
        }
        
        System.out.println("1.6 Checking orphan l2SwCaps...");
        queryString = "SELECT * FROM l2SwitchingCapabilityData WHERE linkId NOT IN (SELECT id FROM links)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Orphan l2SwCap, id: "+orphan.toString());
        }

        System.out.println("1.7 Checking links for l2SwCaps...");
        queryString = "SELECT * FROM  links WHERE id NOT IN (SELECT linkId FROM l2SwitchingCapabilityData)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("WARN: Link with no l2SwCap, id: "+orphan.toString());
        }

        System.out.println("1.8 Checking orphan ipaddrs...");
        queryString = "SELECT * FROM ipaddrs WHERE linkId NOT IN (SELECT id FROM links)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Orphan ipaddr, id: "+orphan.toString());
        }

        System.out.println("1.9 Checking paths...");
        queryString = "SELECT * FROM paths WHERE nextDomainId IS NOT NULL AND nextDomainId NOT IN (SELECT id FROM domains)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Path with invalid nextDomainId, id: "+orphan.toString());
        }

        queryString = "SELECT * FROM paths WHERE mplsDataId IS NOT NULL AND mplsDataId NOT IN (SELECT id FROM mplsData)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Path with invalid mplsDataId, id: "+orphan.toString());
        }

        queryString = "SELECT * FROM paths WHERE layer2DataId IS NOT NULL AND layer2DataId NOT IN (SELECT id FROM layer2Data)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Path with invalid layer2DataId, id: "+orphan.toString());
        }
        
        queryString = "SELECT * FROM paths WHERE layer3DataId IS NOT NULL AND layer3DataId NOT IN (SELECT id FROM layer3Data)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Path with invalid layer3DataId, id: "+orphan.toString());
        }
        
        queryString = "SELECT * FROM paths WHERE pathElemId IS NOT NULL AND pathElemId NOT IN (SELECT id FROM pathElems)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Path with invalid pathElemId, id: "+orphan.toString());
        }
        
        queryString = "SELECT * FROM paths WHERE layer2DataId IS NULL AND layer3DataId IS NULL ";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Path with null layer3DataId AND layer2DataId, id: "+orphan.toString());
        }

        System.out.println("1.10 Checking pathElems...");
        queryString = "SELECT * FROM pathElems WHERE linkId NOT IN (SELECT id FROM links)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: PathElem with invalid linkId, id: "+orphan.toString());
        }
        
        queryString = "SELECT * FROM pathElems WHERE nextId IS NOT NULL AND nextId NOT IN (SELECT id FROM pathElems)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: PathElem with invalid nextId, id: "+orphan.toString());
        }
        
        queryString = "SELECT * FROM pathElems WHERE nextId IS NULL AND description != \"egress\" ";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: PathElem with null nextId but is not egress, id: "+orphan.toString());
        }

        queryString = "SELECT * FROM pathElems WHERE nextId IS NOT NULL AND description = \"egress\" ";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: PathElem with not-null nextId but is egress, id: "+orphan.toString());
        }

        queryString = "SELECT * FROM pathElems WHERE id NOT IN (SELECT nextId FROM pathElems) AND description != \"inress\" ";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: PathElem is not a nextId but is not ingress, id: "+orphan.toString());
        }

        queryString = "SELECT * FROM pathElems WHERE id IN (SELECT nextId FROM pathElems) AND description = \"inress\" ";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: PathElem is a nextId but is ingress, id: "+orphan.toString());
        }

        System.out.println("1.11 Checking reservations...");
        queryString = "SELECT * FROM reservations WHERE pathId NOT IN (SELECT id FROM paths)";
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Reservation with invalid pathId, id: "+orphan.toString());
        }

        System.out.println("1.12 Checking for duplicate gris...");
        queryString = "SELECT * FROM reservations GROUP BY globalReservationId HAVING COUNT(*) > 1 ";
        query = session.createSQLQuery(queryString).addScalar("globalReservationId", Hibernate.STRING);
        for (Object orphan : query.list()) {
        	System.out.println("ERROR: Multiple reservations with globalReservationId : "+orphan.toString());
        }
        
        session.getTransaction().commit();
    }
    public static void checkDomain(Session session, int domainId) {
        SQLQuery query;
        String queryString;
        queryString = "SELECT * FROM nodes WHERE domainId = "+domainId;
        query = session.createSQLQuery(queryString).addScalar("id", Hibernate.INTEGER).addScalar("valid", Hibernate.BOOLEAN);
        boolean foundValid = false;
        for (Object node : query.list()) {
        	System.out.println(node.toString());
        }
        
    }
    
    
    		
}
