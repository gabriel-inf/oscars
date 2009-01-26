package net.es.oscars.rmi.bss;

/**
 * Interface between rmi queryReservation and ReservationManager.queryReservation
 *
 * @author Mary Thompson, David Robertson
 */

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.BSSException;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiQueryResRequest;
import net.es.oscars.rmi.bss.xface.RmiQueryResReply;

/**
 * QueryResRmiHandler - interfaces between servlet and ReservationManager
 */
public class QueryResRmiHandler {
    private OSCARSCore core;
    private Logger log = Logger.getLogger(QueryResRmiHandler.class);


    public QueryResRmiHandler() {
        this.core = OSCARSCore.getInstance();
    }

    /**
     * Finds reservation based on information passed from servlet.
     *
     * @param request RmiQueryResRequest contains the gri of the reservation
     * @param userName String - name of user  making request
     * @return RmiQueryResReply contains: gri, status, user, description
     *   start, end and create times, bandwidth, vlan tag, and path information.
     * @throws IOException
     */
    public RmiQueryResReply
          queryReservation(RmiQueryResRequest request, String userName)
            throws  RemoteException {

        this.log.debug("query.start");
        String methodName = "QueryReservation";
        RmiQueryResReply result = new RmiQueryResReply();
        ReservationManager rm = core.getReservationManager();
        boolean internalIntradomainHops = false;
        Reservation reservation = null;
        String institution = null;
        String loginConstraint = null;

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        // check to see if user is allowed to query at all, and if they can
        // only look at reservations they have made
        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "query");
        if (authVal == AuthValue.DENIED) {
            this.log.debug("query failed: no permission to query Reservations");
            throw new RemoteException("no permission to query Reservations");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = userName;
        }

        // check to see if may look at internal intradomain path elements
        // if user can specify hops on create, he can look at them
        AuthValue authValHops =
            rmiClient.checkModResAccess(userName, "Reservations", "create", 0,
                                        0, true, false );
        if  (authValHops != AuthValue.DENIED ) {
            result.setInternalPathAuthorized(true);
        } else {
            result.setInternalPathAuthorized(false);
        }
        // AAA section end

        // BSS section start
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String gri = request.getGlobalReservationId();
        try {
            reservation = rm.query(gri, loginConstraint, institution);
            if (reservation == null) {
                throw new BSSException("Reservation with gri: "+gri+" does not exist");
            }
            BssRmiUtils.initialize(reservation);
            result.setReservation(reservation);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.debug(e.getMessage());
            throw new RemoteException(e.getMessage());
        }
        bss.getTransaction().commit();
        // BSS section end
        this.log.debug("query.end");
        return result;
    }
}
