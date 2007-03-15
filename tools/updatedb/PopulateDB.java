import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import net.es.oscars.oscars.*;
import net.es.oscars.client.Client;
import net.es.oscars.bss.topology.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.Path;
import org.hibernate.*;

public class PopulateDB {

    private static String dName = null;
    private static Properties props;
    private static Session session;
    private static boolean dryrun = false;
    private static boolean verbosity;

    public static void main(String[] argv) 
    {

        int optind;
        String usage = "updatedb [-v] [-d] -f /path/to/files";

        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-d")) {
                dryrun = true;
            } else
                if (argv[optind].equals("-f")) {
                    dName = argv[++optind];
                } else
                    if (argv[optind].equals("-v")) {
                        verbosity = true;
                        } else
                            if (argv[optind].startsWith("-")) {
                                System.out.println("" + usage);
                                System.out.println("-- Invalid option " + argv[optind]);
                                System.exit(1);
                            } else {
                                break;
                            }
        }

        if (dName == null) {
            System.out.println(usage);
            System.exit(1);
        }

        PropHandler propHandler = new PropHandler("oscars.properties");

        Initializer initializer = new Initializer();
        initializer.initDatabase();

        List<Router> db_rlist = null;
        List<Router> file_rlist = null;
        List<Path> db_plist;
        List<Path> file_plist;

        // setup the classes 
        TopologyFiles topo_files = new TopologyFiles();
        TopologyDB topo_db = new TopologyDB();

        System.out.println("Getting db entries...");
        db_rlist = topo_db.topologyFromDB();
        db_plist = topo_db.getPaths();

        System.out.println("Got all lists from db");
        System.out.println("Loading DB done.");
     
        System.out.println("Getting entires from files.");
        // create the local topology
        try {
            file_rlist = topo_files.constructTopology(dName);
            System.out.println("Got all lists from files.");
        } catch (Exception e) {
            System.out.println("Unknown exception: " + e.getMessage());
            e.printStackTrace();
        }

        if (verbosity)
            System.out.println("Loading files done.");

        if (verbosity)
            System.out.println("Starting to map");

        /* remap the paths file */
        for (Path p : db_plist ) {

            Ipaddr ipad = p.getIpaddr();
            Integer old_ipId = ipad.getId();
            String ipString = ipad.getIp();
            Integer ipId = topo_db.findIp(ipString); 

            /* need a p.setValid() call here!!) */
            if ( -1 == ipId ) {
                System.out.println("*** BAD PATH *** IP: " + ipString);
                continue;
            }
            if (ipId != old_ipId) {
                Ipaddr new_ipaddr = topo_files.getIpAddr(ipString);
                p.setIpaddr(new_ipaddr);
                if (verbosity) {
                    System.out.println("Mapping " + ipString + " " + ipad + " to " + new_ipaddr.toString() );
                }
            }
        }
        System.out.println("Finished updating paths.");

        if (dryrun) {
            System.out.println("Finished dryrun mode, exiting...");
            System.exit(1);
        }

        topo_db.removeOldRouters();

        System.out.println("Finished removal of old routers.");

        // Have to close then reopen the session
        // to force the flush to the db...
        topo_db.commitSession();

        System.out.println("Starting a new session.");
        // restart the sesssion
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();

        session.beginTransaction();
        // let updater know the we have a new session
        topo_db.setSession(session);

        topo_db.commitNewRouters(file_rlist);
        System.out.println("Finished adding in routers.");

        // commit all actions to the database
        topo_db.commitSession();

        System.out.println("End program.");
    }

    public static void printPath( List<Path> dbPath) {
        System.out.println("Id     IP");
        for (Path p : dbPath ) {
            Ipaddr ip = p.getIpaddr();
            if (ip != null) {
                System.out.println(ip.getId()+"     "+ ip.getIp());
            }
        }
    }
}
