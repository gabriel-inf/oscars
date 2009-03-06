package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.pss.PSSException;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiPathRequest;

public class UnsafeTeardownPathRmiHandler {
    private OSCARSCore core;
    private Logger log = Logger.getLogger(UnsafeTeardownPathRmiHandler.class);


    public UnsafeTeardownPathRmiHandler() {
        this.core = OSCARSCore.getInstance();
    }

    public
        String unsafeTeardownPath(RmiPathRequest params, String userName)
            throws RemoteException {

        this.log.debug("unsafeTeardownPath.start");
        String methodName = "UnsafeTeardownPath";
        EventProducer eventProducer = new EventProducer();
        Reservation resv = null;

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            throw new RemoteException(
                    "permission denied to tear down path manually");
        }

        String gri = params.getGlobalReservationId();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());
            resv = resvDAO.query(gri);
            this.core.getPathSetupManager().teardown(resv, resv.getStatus());
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_COMPLETED, userName, "WBUI", resv);
        } catch (PSSException e) {
            errMessage = e.getMessage();
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                bss.getTransaction().rollback();
                if (resv != null){
                    eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, userName, "WBUI", resv, "", errMessage);
                }
                throw new RemoteException(errMessage);
            }
        }
        bss.getTransaction().commit();
        String statusMessage =
            "Manually tearing down path for reservation with GRI " + gri;
        this.log.debug("unsafeTeardownPath.end");
        return statusMessage;
    }
}
