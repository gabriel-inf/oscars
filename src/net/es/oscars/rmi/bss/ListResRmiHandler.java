package net.es.oscars.rmi.bss;

/**
 * Interface between rmi server listReservations call and
 * reservationManager.listReservations
 *
 * @author Mary Thompson, David Robertson
 */

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiListResRequest;
import net.es.oscars.rmi.bss.xface.RmiListResReply;

public class ListResRmiHandler {
    private OSCARSCore core;
    private Logger log;

    /**
     *ListReservations rmi handler; interfaces between war,aar and core ReservationManager.
     *
     * @param request a RmiListRequest with the input parameters for a list request
     * @param userName String - name of user  making request
     * @return RmiListReply containing details for all the reservations that matched the request
     * @throws RemoteException
     */
    public ListResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws RemoteException {

        this.log.debug("listReservations.start");

        RmiListResReply result = new RmiListResReply();
        String institution = null;
        String methodName = "ListReservations";
        ReservationManager rm = core.getReservationManager();
        int numRowsReq = request.getNumRequested();
        int resOffset = request.getResOffset();
        String description = request.getDescription();
        String loginConstraint = request.getLogin();
        Long startTimeSeconds = request.getStartTime();
        Long endTimeSeconds = request.getEndTime();
        List<String> links = request.getLinkIds();
        List<String> statuses = request.getStatuses();
        List<String> vlans = request.getVlanTags();
        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "list");
        if (authVal == AuthValue.DENIED) {
            this.log.error("no permission to list Reservations");
            throw new RemoteException("user not authorized to list reservations");
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
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        List<Reservation> reservations = null;
        try {
            reservations =
                rm.list(numRowsReq, resOffset, loginConstraint, institution,
                        statuses, description, links, vlans,
                        startTimeSeconds, endTimeSeconds);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = "Caught Exception " + e.toString();
        } finally {
            if (errMessage != null) {
                bss.getTransaction().rollback();
                this.log.error("list failed: " + errMessage);
                throw new RemoteException(errMessage);
            }
        }
        this.log.debug("initialize start");
        for (Reservation reservation: reservations) {
            BssRmiUtils.initialize(reservation);
        }
        this.log.debug("initialize end");
        result.setReservations(reservations);
        DomainDAO domainDAO = new DomainDAO(core.getBssDbName());
        result.setLocalDomain(domainDAO.getLocalDomain().getTopologyIdent());
        bss.getTransaction().commit();
        this.log.debug("listReservations.end");
        return result;
    }
}
