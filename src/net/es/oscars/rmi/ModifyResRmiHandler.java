package net.es.oscars.rmi;

/**
 * rmi handler for modifyReservation. Interfaces to ReservationManager.modifyReservation
 * 
 * @author Evangelos Chaniotakis, David Robertson
 */

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.notify.*;
import net.es.oscars.PropHandler;
import net.es.oscars.database.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.oscars.*;
import net.es.oscars.wsdlTypes.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

public class ModifyResRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public ModifyResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * modifyReservation rmi handler; interfaces between servlet and ReservationManager.
     * 
     * @param userName String - name of user  making request
     * @param inputMap HashMap - contains start and end times, bandwidth, description,
     *          productionType, pathinfo
     * @return HashMap - contains gri and sucess or error status
     */
    public HashMap<String, Object> modifyReservation(HashMap<String, String[]> inputMap, String userName) 
        throws IOException {
        this.log.debug("modify.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        HashMap<String,String> simpleInputMap = new HashMap<String, String>();
        String methodName = "ModifyReservation";
        String institution = null;
        String loginConstraint = null;
 
        TypeConverter tc = core.getTypeConverter();
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        
        Session aaa = core.getAaaSession();  
        aaa.beginTransaction();
        UserManager userMgr = core.getUserManager();

        AuthValue authVal = userMgr.checkAccess(userName, "Reservations",
                "modify");
        if (authVal == AuthValue.DENIED) {
                this.log.info("modify failed: no permission");
                result.put("error", "modifyReservation: permission denied");
                aaa.getTransaction().rollback();
                return result;
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = userMgr.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)) {
            loginConstraint = userName;
        }
        aaa.getTransaction().commit();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        Set <String> keys = inputMap.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext() ) {
            String paramName = (String)it.next();
            String [] paramValues = inputMap.get(paramName);
            simpleInputMap.put(paramName, paramValues[0]);
        }

        Reservation resv = this.toReservation(simpleInputMap);
        try {   
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_RECEIVED, userName, "RMI", resv);
            Reservation persistentResv = rm.submitModify(resv, loginConstraint, institution);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_ACCEPTED, userName, "RMI", resv);
        } catch (Exception e) {
            String errMessage = e.getMessage();
            this.log.debug("Modify  failed: " + errMessage);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, loginConstraint, 
                "RMI", resv, "", errMessage);
            result.put("error", errMessage);
            bss.getTransaction().rollback();
            return result;
        }
 
        result.put("status", "modified reservation with GRI " + resv.getGlobalReservationId());
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.debug("modify.end - success");
        return result;
    }

    public Reservation toReservation(HashMap<String, String> request) {
        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();

        strParam = request.get("gri");
        resv.setGlobalReservationId(strParam);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.get("modifyStartSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setStartTime(seconds);
        strParam = request.get("modifyEndSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setEndTime(seconds);

        // currently hidden form fields; not modifiable
        strParam = request.get("modifyBandwidth");
        bandwidth = ((strParam != null) && !strParam.trim().equals(""))
            ? (Long.valueOf(strParam.trim()) * 1000000L) : 0L;
        resv.setBandwidth(bandwidth);
        String description = request.get("modifyDescription");
        resv.setDescription(description);
        return resv;
    }
}
