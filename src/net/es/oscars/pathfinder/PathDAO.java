package net.es.oscars.pathfinder;

import java.util.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.Ipaddr;

import net.es.oscars.database.GenericHibernateDAO;

/**
 * PathDAO is the data access object for the bss.paths table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathDAO extends GenericHibernateDAO<Path, Integer> {
    
    /**
     * Builds up Path linked list from list of hops.
     * @param ipaddrs list of Ipaddrs
     * @param ingressRouter string with IP address of ingress router
     * @param egressRouter string with IP address of egress router
     * @return path beginning path instance
     */
    public Path create(List<Ipaddr> ipaddrs,
                       String ingressRouter, String egressRouter) throws BSSException {

        List<Path> paths = new ArrayList<Path>();
        Path path = null;
        String addr = null;
        int ctr = 0;

        // Fill in Path instance information given list of Ipaddr instances
        for (Ipaddr ipaddr: ipaddrs) {
            if ( ipaddr == null ) { 
                throw new BSSException("Unknown router in path: " 
                + ipaddr.getIp());
            }
            addr = ipaddr.getIp();
            path = new Path();
            if (addr.equals(ingressRouter)) {
                path.setAddressType("ingress");
            } else if (addr.equals(egressRouter)) {
                path.setAddressType("egress");
            }
            path.setIpaddr(ipaddr);
            paths.add(path);
        }

        ctr = paths.size() - 1;
        for (int i = 0; i < ctr; i++) {
            path = paths.get(i);
            path.setNextPath(paths.get(i+1));
        }
        path = paths.get(0);
        this.makePersistent(path);

        return path;
    }
    
    /**
     * List all paths.
     *
     * @return a list of all paths
     */
    public List<Path> list() {
        List<Path> paths = null;

        String hsql = "from Path";
        paths = this.getSession().createQuery(hsql).list();
        return paths;
    }

    /**
     * Deletes Path, given id.
     * @param id id of the beginning path
     */
    public void remove(int id) {
        Path path = this.queryByParam("id", id);
        this.makeTransient(path);
    }
}
