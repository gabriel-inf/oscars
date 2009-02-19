package net.es.oscars.rmi.bss;

/**
 * Interface between rmi cancelReservation and ReservationManager.cancelReservation
 *
 * @author David Robertson, Mary Thompson
 */
import java.io.*;
import java.rmi.RemoteException;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;

/**
 * CancelResRmiHandler - rmi interface to ReservationManager.cancelReservation
 *
*/

public class CancelResRmiHandler {
    private OSCARSCore core;
    private Logger log;
    
    
    public CancelResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * cancelReservation
     * 
     * @param params contains the gri of the reservation at key "gri"
     * @param userName String name of user making request
     *
     * @return HashMap - contains gri and success or error status
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public void cancelReservation(String gri, String userName)
        throws RemoteException {
        this.log.debug("cancel.start");
        String methodName = "CancelReservation";

        ReservationManager rm = core.getReservationManager();

        EventProducer eventProducer = new EventProducer();
        String institution = null;
        String loginConstraint = null;
        Reservation reservation = null;

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);

        AuthValue authVal = rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            //result.put("error", "no permission to cancel Reservations");
            this.log.debug("cancelReservation failed: permission denied");
            throw new RemoteException("CancelReservation: no permission to cancel Reservation");
        }
        if (authVal.equals(AuthValue.MYSITE)){
            institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = userName;
        }
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        RemoteException remEx = null;
        try {
            reservation =
                rm.getConstrainedResv(gri, loginConstraint, institution, null);
            rm.submitCancel(reservation, loginConstraint, userName, institution);
        } catch (BSSException e) {
            errMessage = e.getMessage();
            remEx= new RemoteException(errMessage,e);
            this.log.debug(methodName + " failed: " + errMessage);
        } catch (Exception e) {
            errMessage = "caught Exception " + e.toString();
            remEx= new RemoteException(errMessage,e);
            this.log.error(methodName + " failed: " +errMessage,e);
        } 
        if (errMessage != null) {
            if (reservation != null){
                eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, userName, "oscars-core", reservation, "", errMessage);
            }
            bss.getTransaction().rollback();
            throw  remEx;
        }
        this.log.debug("cancel.end");
    }
}
