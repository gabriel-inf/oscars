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
import net.es.oscars.interdomain.*;
import net.es.oscars.pss.PSSException;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiPathRequest;

public class UnsafeCreatePathRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public UnsafeCreatePathRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public String unsafeCreatePath(RmiPathRequest params, String userName)
            throws IOException {

         this.log.debug("unsafeCreatePath.start");
         String methodName = "UnsafeCreatePath";

         AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
         AuthValue authVal =
             rmiClient.checkAccess(userName, "Reservations", "modify");
         if (authVal == AuthValue.DENIED) {
             this.log.debug("createPath failed: permission denied");
             throw new RemoteException("no permission to force path creation");
         }

         EventProducer eventProducer = new EventProducer();
         Reservation resv = null;
         String gri = params.getGlobalReservationId();
         Session bss = core.getBssSession();
         bss.beginTransaction();
         String errMessage = null;
         try {
             ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());
             resv = resvDAO.query(gri);
             this.core.getPathSetupManager().create(resv, false);

             eventProducer.addEvent(OSCARSEvent.PATH_SETUP_COMPLETED, userName, "WBUI", resv);
         } catch (PSSException e) {
             errMessage = e.getMessage();
         } catch (BSSException e) {
             errMessage = e.getMessage();
         } catch (InterdomainException e) {
             errMessage = e.getMessage();
         } finally {
             if (errMessage != null) {
                 bss.getTransaction().rollback();
                 if (resv != null){
                     eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, userName, "WBUI", resv, "", errMessage);
                 }
                 throw new RemoteException(errMessage);
             }
         }
         bss.getTransaction().commit();
         String statusMessage =
             "Manually setting up path for reservation with GRI " + gri;
         this.log.debug("unsafeCreatePath.end");
         return statusMessage;
    }
}
