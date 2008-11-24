package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.bss.*;
import net.es.oscars.notify.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.oscars.*;
import net.es.oscars.pss.PSSException;

public class UnsafeCreatePathRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public UnsafeCreatePathRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public
        HashMap<String, Object> createPath(HashMap<String, String[]> inputMap,
                                           String userName)
            throws IOException {

         this.log.debug("createPath.start");
         HashMap<String, Object> result = new HashMap<String, Object>();
         String methodName = "CreatePath";
         UserManager userMgr =  new UserManager("aaa");
         EventProducer eventProducer = new EventProducer();
         Reservation resv = null;
         result.put("method", methodName);

         Session aaa = core.getAaaSession();
         aaa.beginTransaction();
         AuthValue authVal = userMgr.checkAccess(userName, "Reservations", "modify");
         if (authVal == AuthValue.DENIED) {
             result.put("error", "no permission to force path creation");
             aaa.getTransaction().rollback();
             this.log.debug("createPath failed: permission denied");
             return result;
         }
         aaa.getTransaction().commit();

         String[] paramValues = inputMap.get("gri");
         String gri = paramValues[0];
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
                 result.put("error", errMessage);
                 bss.getTransaction().rollback();
                 if (resv != null){
                     eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, userName, "WBUI", resv, "", errMessage);
                 }
                 this.log.debug("createPath failed: " + errMessage);
                 return result;
             }
         }
         result.put("gri", resv.getGlobalReservationId());
         result.put("status", "Manually set up path for GRI " + resv.getGlobalReservationId());
         /* REMOVE THIS LINE FOR TESTING */
         //result.put("status", "Not implemented yet");
         result.put("method", methodName);
         result.put("success", Boolean.TRUE);

         bss.getTransaction().commit();
         this.log.debug("createPath.end");
         return result;
    }
}
