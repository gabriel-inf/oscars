import java.util.*;
import java.io.*;
import java.lang.Throwable;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.Transaction;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

/**
 * HealthCheck checks for database consistency
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */

public class DeleteDomain {

    // time in seconds to look into the future for reservations
    public static void main (String[] args) {
        String usage = "usage: deleteDomain.sh <domainId> \nDeletes a domain and all its children entities";
        String domainId = null;

        Boolean repair = false;

        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-help") ||
                args[i].equals("-h")) {
                System.out.println(usage);
                System.exit(0);
            } else {
                domainId = args[i];
            }
        }
        if (domainId == null) {
            System.out.println(usage);
            System.exit(0);
        }

        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        session.beginTransaction();

        SQLQuery query;
        String queryString;

        DomainDAO domDao = new DomainDAO("bss");
        Domain dom = domDao.fromTopologyIdent(domainId);
        domDao.remove(dom);



        session.getTransaction().commit();
    }

}
