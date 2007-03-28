package net.es.oscars.pathfinder;

import java.util.*;
import java.net.UnknownHostException;
import java.net.InetAddress;

import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

import net.es.oscars.LogWrapper;
import net.es.oscars.bss.topology.*;

/**
 * This class is intended to be subclassed by NARBPathfinder and TraceroutePathfinder.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class Pathfinder {
    private LogWrapper log;

    public Pathfinder() {
        this.log = new LogWrapper(this.getClass());
    }

    public Path checkPath(List<String> hops,
                        String ingressIP, String egressRouterIP) 
            throws PathfinderException {

        List<Ipaddr> ipaddrs = new ArrayList<Ipaddr>();
        Path path = null;
        Ipaddr currentIpaddr = null;
        Ipaddr ipaddr = null;
        boolean samePath = false;
        int ctr = -1;

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        PathDAO pathDAO = new PathDAO();
        pathDAO.setSession(session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);

        for (String hop: hops) {
            ipaddrs.add(ipaddrDAO.queryByParam("ip", hop));
        }

        // check to make sure path is not already in the database
        Path currentPath = (Path)
                     pathDAO.queryByParam("ipaddrId", ipaddrs.get(0).getId());
        if (currentPath != null) {
            ctr = ipaddrs.size();
            samePath = true;
            path = currentPath;
            for (int i=0; i < ctr; i++) {
                currentIpaddr = ipaddrs.get(i);
                ipaddr = path.getIpaddr();
                if (currentIpaddr != ipaddr) {
                    samePath = false;
                    break;
                }
                path = path.getNextPath();
                // takes care of case where current path is shorter
                if ((path == null) && (i != (ctr-1))) {
                    samePath = false;
                    break;
                }
            }
            // takes care of case where new path is shorter
            if (path != null) { samePath = false; }
        }
        if (!samePath) {
            path = pathDAO.create(ipaddrs, ingressIP, egressRouterIP);
        } else { 
            path = currentPath; 
        }

        return path;
    }

    /**
     * Gets last interface within this domain.
     *
     * @param hops list of IP addresses
     * @return string containing last interface address, if any
     * @throws PathfinderException
     */
    public String lastInterface(List<String> hops) throws PathfinderException {

        Ipaddr ipaddr = null;
        String ingressIP = "";
        Interface xface = null;
        
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops) {
            // get interface associated with that address
            ipaddr = ipaddrDAO.queryByParam("ip", hop);
            if (ipaddr == null) continue;
            xface = ipaddr.getInterface();
            if (xface != null) { ingressIP = hop; }
        }
        if (ingressIP.equals("")) { 
            throw new PathfinderException(
                "No ingress interface found by reverse traceroute");
        }
        return ingressIP;
    }

    /* If the ingress router is given, make sure it is in the database,
           and then return as is. */
    public String checkIngressLoopback(String ingressRouterIP)
            throws PathfinderException {

        Router router = null;
        String ingressIP = null;

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);

        router = routerDAO.fromIp(ingressRouterIP);
        if ((router != null) && (router.getName() != null)) {
            ingressIP = ipaddrDAO.getIpType(router.getName(), "loopback");
        }
        if (ingressIP.equals("")) {
            throw new PathfinderException(
                "No loopback for specified ingress router" +
                 ingressRouterIP);
        }
        return ingressIP;
    }

    /**
     * Gets last OSCARS interface within this domain.
     *
     * @param hops list of IP addresses
     * @return string containing last loopback address, if any
     * @throws PathfinderException
     */
    public String lastLoopback(List<String> hops) throws PathfinderException {

        Ipaddr ipaddr;
        String ingressLoopbackIp = "";

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops)  {
            this.log.info("hop: ", hop);
            String loopbackFound = "";
            Router router = routerDAO.fromIp(hop);
            if ((router != null) && (router.getName() != null)) {
                loopbackFound = ipaddrDAO.getIpType(router.getName(), "loopback");
            }

            if (!loopbackFound.equals("")) { ingressLoopbackIp = hop; }
        }
        if (ingressLoopbackIp.equals("")) { 
            throw new PathfinderException(
                "No ingress loopback found by reverse traceroute");
        }
        return ingressLoopbackIp;
    }

    /**
     * Gets next hop outside of this domain.
     *
     * @param hops list of IP addresses
     * @return String representation of next hop
     */
    public String nextExternalHop(List<String> hops) {

        Ipaddr ipaddr = null;
        String outsideHop = "";
        boolean ingressFound = false;
        
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        for (String hop: hops) {
            // get IP address
            ipaddr = ipaddrDAO.queryByParam("ip", hop);
            if (ingressFound && ipaddr == null) {
                outsideHop = hop;
                break; 
            } else if(ipaddr != null) {
                ingressFound = true;
            }
        }
        return outsideHop;
    }
}
