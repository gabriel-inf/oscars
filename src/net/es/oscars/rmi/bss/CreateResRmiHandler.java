package net.es.oscars.rmi.bss;

/**
 * rmi handler for createReservation. Interfaces to ReservationManager.createReservation
 *
 * @author Evangelos Chaniotakis, David Robertson
 */

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.PropHandler;
import net.es.oscars.interdomain.*;
import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.RmiUtils;

public class CreateResRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public CreateResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * CreateReservation rmi handler; interfaces between servlet and ReservationManager.
     *
     * @param resv - partially filled in reservation with requested params
     * @param userName String - name of user  making request
     * @return gri - new global reservation id assigned to reservation
     */
    public String
        createReservation(Reservation resv, String userName)
            throws IOException, RemoteException {

        this.log.debug("create.start");
        String methodName = "CreateReservation";

        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();

        // TODO:  may want some null checks

        // bandwidth limits are stored in megaBits
        int reqBandwidth = (int) (resv.getBandwidth() / 1000000);
        // convert from seconds to minutes
        int reqDuration = (int) (resv.getEndTime() - resv.getStartTime()) / 60;

        boolean specifyPath = false;
        try {
            if (!resv.getPath(PathType.REQUESTED).getPathElems().isEmpty()) {
                specifyPath = true;
            }
        } catch (BSSException e) {
            throw new RemoteException(e.getMessage());
        }
        boolean specifyGRI = (resv.getGlobalReservationId() != null);

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
            rmiClient.checkModResAccess(userName, "Reservations", "create",
                            reqBandwidth, reqDuration, specifyPath, specifyGRI);

        if (authVal == AuthValue.DENIED) {
            this.log.debug("createReservation failed permission denied");
            throw new RemoteException("no permission to create reservation");
        }

        // submit reservation request
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        String gri = null;
        try {
            // url returned, if not null, indicates location of next domain
            // manager
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_RECEIVED, userName, "localhost", resv);
            gri = rm.submitCreate(resv, userName);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_ACCEPTED, userName, "localhost", resv);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, userName, "localhost", resv, "", errMessage);
                this.log.debug("createReservation failed: " + errMessage);
                throw new RemoteException(errMessage);
            }
        }
        bss.getTransaction().commit();
        this.log.debug("create.end - success");
        return gri;
    }
}
