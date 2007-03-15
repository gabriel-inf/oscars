package net.es.oscars.pathfinder;

import java.util.*;

import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

import net.es.oscars.LogWrapper;
import net.es.oscars.bss.BSSException;

/**
 * This class contains methods for finding the next domain.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class DomainManager {
    private LogWrapper log;

    public DomainManager() {
        this.log = new LogWrapper(this.getClass());
    }

     /**
     * Finds next domain by looking up first hop in peerIpaddr table
     *
     * @param nextHop string with IP address of next hop
     * @return Domain an instance associated with the next domain, if any
     * @throws BSSException
     */
    public Domain getNextDomain(String nextHop) throws BSSException {

        PeerIpaddrDAO peerDAO = new PeerIpaddrDAO();
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        peerDAO.setSession(session);
        Domain nextDomain = null;

        this.log.info("getNextDomain.nextHop", nextHop);
       
        if (nextHop != null) {
            nextDomain = peerDAO.getDomain(nextHop);
            if (nextDomain != null) {
                this.log.info("getNextDomain.nextDomain",
                              nextDomain.getAsNum()+"");
            }
        }
        return nextDomain;
    }
}
