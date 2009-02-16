package net.es.oscars.rmi.bss;

/**
 * rmi handler for modifyReservation. Interfaces to ReservationManager.modifyReservation
 *
 * @author Evangelos Chaniotakis, David Robertson
 */

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.PropHandler;
import net.es.oscars.database.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.ws.*;

public class ModifyResRmiHandler {
    private OSCARSCore core;
    private Logger log;

    public ModifyResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * RMI handler for modifying reservation; interfaces between client and
     * ReservationManager.
     *
     * @param userName String name of user  making request
     * @param resv Reservation containing start and end times, bandwidth,
     *          description, and (TODO) path information
     * @return persistentResv Reservation from db matching GRI
     */
    public Reservation
        modifyReservation(Reservation resv, String userName)
            throws IOException {

        this.log.debug("modify.start");
        String methodName = "ModifyReservation";

        String institution = null;
        String loginConstraint = null;
        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
            rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            this.log.info("modify failed: no permission");
            throw new RemoteException("no permission to modify reservation");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)) {
            loginConstraint = userName;
        }

        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        Reservation persistentResv = null;
        try {
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_RECEIVED, userName,
                                   "core", resv);
            persistentResv =
                rm.submitModify(resv, loginConstraint, userName, institution);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_ACCEPTED, userName,
                                   "core", resv);
        } catch (Exception e) {
            String errMessage = "caught Exception " + e.toString();
            this.log.debug("Modify  failed: " + errMessage);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, loginConstraint,
                "RMI", resv, "", errMessage);
            bss.getTransaction().rollback();
            throw new RemoteException(e.getMessage());
        }
        BssRmiUtils.initialize(persistentResv);
        bss.getTransaction().commit();
        this.log.debug("modify.end - success");
        return persistentResv;
    }
}
