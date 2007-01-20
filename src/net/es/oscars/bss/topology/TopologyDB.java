package net.es.oscars.bss.topology;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

import net.es.oscars.LogWrapper;
import net.es.oscars.bss.BSSException;

/**
 * This class contains methods to update the routers, interfaces, and ipaddrs 
 * tables.  To do so, it is given the current network configuration in the
 * ifrefpoll files.
 *
 * These methods are inefficient and use ESnet-specific ifrefpoll files.
 * Everything in determining the network configuration, except for getting the 
 * list of router names, may be replaced by SNMP queries.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class TopologyDB {

    private LogWrapper log;
    private Session session;

    private List<Router> routersList;
    private List<Interface> interfacesList;
    private List<Ipaddr> ipaddrsList;
    private List<Path> pathList;

    private RouterDAO routerDAO;

    public TopologyDB() {
        this.log = new LogWrapper(this.getClass());
    }

    public void setSession(Session session) {
        this.session = session;
        routerDAO.setSession(this.session);
    }

    /**
     * Updates information in router, interface, and ipaddrs tables.
     * It currently uses data from SNMP output files.
     * @param dirName A String containing the log file directory name.
     * @throws FileNotFoundException
     */
    public List topologyFromDB() {

        //System.out.println("Setting up session");

        this.session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();

        routerDAO = new RouterDAO();
        InterfaceDAO interfaceDAO = new InterfaceDAO();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        PathDAO pathDAO = new PathDAO();
        
        routerDAO.setSession(this.session);
        interfaceDAO.setSession(this.session);
        ipaddrDAO.setSession(this.session);
        pathDAO.setSession(this.session);

        this.session.beginTransaction();

        routersList = routerDAO.list();
        interfacesList = interfaceDAO.list();
        ipaddrsList = ipaddrDAO.list();
        pathList = pathDAO.list();

        return routersList;
    }

    /*
     * Finds the IPaddrID of the IP address
     */
    public Integer findIp(String IPstring) {
        for (Ipaddr i : ipaddrsList ) {
            String dbIp = i.getIp();
            if ( IPstring == dbIp ) {
                //System.out.println("found: " + i.getIp());
                return i.getId();
            }
        }
        return -1;
    }

    public List getPaths() 
    {
        return this.pathList;
    }

    public void removeOldRouters() 
    {
        for (Router r : routersList) {
            this.routerDAO.remove(r);
        }
    }

    public void commitNewRouters( List<Router> rlist)
    {
        for (Router r: rlist) {
            //System.out.println("Commiting router: " + r.getName());
            //replaced the call to "this.routerDAO.create(r);"
            //with this call to avoid conflicting keys
            this.session.replicate(r, ReplicationMode.LATEST_VERSION);
        }
    }


    public void commitSession() {
        this.session.getTransaction().commit();
    }

    public void listInterfaces(Router r)
    {
        System.out.println("Router: " + r.getName());

        Set<Interface> ifaces = r.getInterfaces();
        if (ifaces == null)
            System.out.println("****NULL****");

        System.out.println("TopologyDB number of xfaces is "+ifaces.size());

        for (Interface i: ifaces) {
            if ( ! i.isValid() ) { continue; }
            System.out.println("    Interface: " + i.getDescription());
            Set<Ipaddr> iplist = i.getIpaddrs();
            for (Ipaddr ip: iplist ) {
                    System.out.println("        IP: "+ip.getIp());
            }
        }
    }
}
