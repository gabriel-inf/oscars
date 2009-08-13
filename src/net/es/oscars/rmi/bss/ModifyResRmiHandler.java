package net.es.oscars.rmi.bss;

/**
 * rmi handler for modifyReservation. Interfaces to ReservationManager.modifyReservation
 *
 * @author Evangelos Chaniotakis, David Robertson
 */


import java.rmi.RemoteException;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;


public class ModifyResRmiHandler {
    private OSCARSCore core;
    private Logger log;

    public ModifyResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * RMI handler for modifying reservation; interfaces between war, aar and
     * core ReservationManager.
     *
     * @param userName String name of user  making request
     * @param resv Reservation containing start and end times, bandwidth,
     *          description, and (TODO) path information
     * @return persistentResv Reservation from db matching GRI
     * @throws RemoteException
     */
    public Reservation
        modifyReservation(Reservation resv, String userName)
            throws RemoteException {

        this.log.debug("modify.start");
        String methodName = "ModifyReservation";

        String institution = null;
        String loginConstraint = null;
        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
            rmiClient.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            this.log.info("modify failed: no permission");
            throw new RemoteException("no permission to modify reservation");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)) {
            loginConstraint = userName;
        }

        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        Session bss = core.getBssSession();
        Reservation persistentResv = null;
        bss.beginTransaction();
        try {
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_RECEIVED, userName,
                                   "core", resv);
            persistentResv =
                rm.submitModify(resv, loginConstraint, userName, institution);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_ACCEPTED, userName,
                                   "core", resv);
            BssRmiUtils.initialize(persistentResv);
            bss.getTransaction().commit();
        } catch (Exception e) {
            try{
                String errMessage = "caught Exception " + e.toString();
                this.log.debug("Modify  failed: " + errMessage);
                eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, loginConstraint,
                        "RMI", resv, "", errMessage);
            }catch(Exception E){
                //do nothing, just pass to finally so Hibernate closed
            }finally{
                bss.getTransaction().rollback();
            }
            throw new RemoteException(e.getMessage());
        }
        this.log.debug("modify.end - success");
        return persistentResv;
    }
}
