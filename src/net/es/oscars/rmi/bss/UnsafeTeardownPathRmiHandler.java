package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.pss.PSSException;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.ws.*;

public class UnsafeTeardownPathRmiHandler {
    private OSCARSCore core;
    private Logger log = Logger.getLogger(UnsafeTeardownPathRmiHandler.class);


    public UnsafeTeardownPathRmiHandler() {
        this.core = OSCARSCore.getInstance();
    }

    public
        HashMap<String, Object> teardownPath(HashMap<String, Object> params,
                                             String userName)
            throws IOException {

        this.log.debug("teardownPath.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "TeardownPath";

        EventProducer eventProducer = new EventProducer();
        Reservation resv = null;
        result.put("method", methodName);

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);

        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to force teardown of path");
            this.log.debug("teardownPath failed: permission denied");
            return result;
        }

        String gri = (String) params.get("gri");

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
                result.put("error", errMessage);
                bss.getTransaction().rollback();
                if (resv != null){
                    eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, userName, "WBUI", resv, "", errMessage);
                }
                this.log.debug("teardownPath failed: " + errMessage);
                return result;
            }
        }
        result.put("gri", resv.getGlobalReservationId());
        result.put("status", "Manually tore down reservation with GRI " + resv.getGlobalReservationId());

        /* REMOVE THIS LINE FOR TESTING */
        //result.put("status", "Not implemented yet");
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.debug("teardownPath.end");
        return result;
    }
}
