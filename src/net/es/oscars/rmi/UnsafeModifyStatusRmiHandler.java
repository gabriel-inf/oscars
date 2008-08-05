package net.es.oscars.rmi;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.oscars.*;

public class UnsafeModifyStatusRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public UnsafeModifyStatusRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object> modifyStatus(HashMap<String, String[]> inputMap, String userName)
        throws IOException {
        this.log.debug("overrideStatus.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "OverrideStatus";

        UserManager userMgr =  new UserManager("aaa");
        Reservation reservation = null;
        result.put("method", methodName);

        // OK not sure what to put here
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        AuthValue authVal = userMgr.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to override reservation status");
            aaa.getTransaction().rollback();
            this.log.debug("overrideStatus failed: permission denied");
            return result;
        }
        aaa.getTransaction().commit();




        String []paramValues = inputMap.get("gri");
        String gri = paramValues[0];

        paramValues = inputMap.get("status");
        String status = paramValues[0];

        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());
            Reservation resv = resvDAO.query(gri);
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
        result.put("gri", reservation.getGlobalReservationId());
        result.put("status", "Overrode status for reservation with GRI " + reservation.getGlobalReservationId());
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.debug("overrideStatus.end");
        return result;
    }

}
