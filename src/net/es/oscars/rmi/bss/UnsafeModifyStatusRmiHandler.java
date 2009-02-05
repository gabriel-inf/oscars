package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiModifyStatusRequest;

public class UnsafeModifyStatusRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public UnsafeModifyStatusRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public String unsafeModifyStatus(RmiModifyStatusRequest params,
                                     String userName)
            throws IOException {

        this.log.debug("unsafeModifyStatus.start");
        String result = "success";  // unused for now
        String methodName = "UnsafeModifyStatus";

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
            rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            this.log.debug("permission denied to override reservation status");
            throw new RemoteException(
                    "no permission to override reservation status");
        }

        Reservation resv = null;
        String gri = params.getGlobalReservationId();
        String status = params.getStatus();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());
            resv = resvDAO.query(gri);
            resv.setStatus(status);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                bss.getTransaction().rollback();
                throw new RemoteException(errMessage);
            }
        }
        bss.getTransaction().commit();
        this.log.debug("unsafeModifyStatus.end");
        return result;
    }
}
