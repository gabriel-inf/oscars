package net.es.oscars.rmi.bss;

/**
 * Interface between rmi cancelReservation and ReservationManager.cancelReservation
 *
 * @author David Robertson, Mary Thompson
 */
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.notify.*;
import net.es.oscars.database.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;

public class CancelResRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public CancelResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * CancelResRmiHandler - interfaces between servlet and ReservationManager
     *
     * @param params contains the gri of the reservation at key "gri"
     * @param userName String name of user making request
     *
     * @return HashMap - contains gri and success or error status
     *
     * @throws IOException
     */
    public HashMap<String, Object> cancelReservation(HashMap<String, Object> params, String userName)
        throws IOException {
        this.log.debug("cancel.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CancelReservation";

        ReservationManager rm = core.getReservationManager();

        EventProducer eventProducer = new EventProducer();
        String institution = null;
        String loginConstraint = null;
        Reservation reservation = null;

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);

        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to cancel Reservations");
            this.log.debug("cancelReservation failed: permission denied");
            return result;
        }
        if (authVal.equals(AuthValue.MYSITE)){
            institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = userName;
        }

        String caller = (String) params.get("caller");
        if (caller.equals("WBUI") || caller.equals("AAR")) {
            result.put("method", methodName);
            String gri = (String) params.get("gri");
            Session bss = core.getBssSession();
            bss.beginTransaction();
            String errMessage = null;
            try {
                reservation = rm.getConstrainedResv(gri, loginConstraint, institution);
                rm.submitCancel(reservation, loginConstraint, userName, institution);
            } catch (BSSException e) {
                errMessage = e.getMessage();
            } catch (Exception e) {
                errMessage = e.getMessage();
            } finally {
                if (errMessage != null) {
                    result.put("error", errMessage);
                    bss.getTransaction().rollback();
                    if (reservation != null){
                        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, userName, caller, reservation, "", errMessage);
                    }
                    this.log.debug("cancelReservation failed: " + errMessage);
                    return result;
                }
            }
            result.put("gri", reservation.getGlobalReservationId());
            result.put("status", "Cancelled reservation with GRI " + reservation.getGlobalReservationId());
            result.put("method", methodName);
            result.put("success", Boolean.TRUE);
            bss.getTransaction().commit();
        } else {
            this.log.error("INTERNAL ERROR");
            throw new IOException("Internal error");
        }
        this.log.debug("cancel.end");
        return result;
    }
}
