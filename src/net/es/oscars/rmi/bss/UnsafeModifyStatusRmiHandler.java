package net.es.oscars.rmi.bss;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.bss.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.ws.*;

public class UnsafeModifyStatusRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public UnsafeModifyStatusRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object> modifyStatus(HashMap<String, Object> params, String userName)
        throws IOException {
        this.log.debug("overrideStatus.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "OverrideStatus";

        UserManager userMgr =  new UserManager("aaa");
        Reservation resv = null;
        result.put("method", methodName);

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);

        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to override reservation status");
            this.log.debug("overrideStatus failed: permission denied");
            return result;
        }
        String gri = (String) params.get("gri");

        String status = (String) params.get("forcedStatus");

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
                result.put("error", errMessage);
                bss.getTransaction().rollback();
                this.log.debug("overrideStatusfailed: " + errMessage);
                return result;
            }
        }
        result.put("gri", resv.getGlobalReservationId());
        result.put("status", "Overrode status for reservation with GRI " + resv.getGlobalReservationId());
        /* REMOVE THIS LINE FOR TESTING */
        //result.put("status", "Not implemented yet");
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.debug("overrideStatus.end");
        return result;
    }
}
