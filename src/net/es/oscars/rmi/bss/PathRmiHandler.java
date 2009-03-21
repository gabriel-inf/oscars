package net.es.oscars.rmi.bss;

import java.rmi.RemoteException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.interdomain.*;
import net.es.oscars.pss.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.bss.xface.RmiPathRequest;

public class PathRmiHandler {
    private OSCARSCore core;
    private Logger log;
    private PathSetupManager pm;
    private ReservationManager rm;
    
    public PathRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.rm = this.core.getReservationManager();
        this.pm = this.core.getPathSetupManager();
    }

    /**
     * Sets up a path in response to a CreatePath request. Forwards request
     * first, and sets-up path is reply successful. If there is an error during
     * local path setup a teardownPath message is issued.
     *
     * @param params RmiPathRequest request parameters
     * @param userName name of user making request
     * @return the result containing the status.
     * @throws RemoteException
     */
    public String createPath(RmiPathRequest params, String userName)
            throws RemoteException {

        this.log.debug("createPath.start");
        String methodName = "CreatePath";
        String loginConstraint = null;
        String institution = null;

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
             rmiClient.checkAccess(userName, "Reservations", "signal");
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new RemoteException("createPath: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
           institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
           loginConstraint = userName;
        }

        String gri = params.getGlobalReservationId();
        String tokenValue = params.getToken();
        Long currTime = System.currentTimeMillis()/1000;
        EventProducer eventProducer = new EventProducer();
        Forwarder forwarder = new Forwarder();
        Reservation resv = null;
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try {
            resv = this.rm.getConstrainedResv(gri, loginConstraint,
                                              institution, tokenValue);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, userName, 
                                   "core", "", e.getMessage());
            throw new RemoteException(e.getMessage());
        }
        eventProducer.addEvent(OSCARSEvent.PATH_SETUP_RECEIVED, userName,
                               "core", resv);
        String errMessage = null;
        /* Check reservation parameters to make sure it can be created */
        try {
            if (resv.getPath(PathType.LOCAL) == null) {
                errMessage = "Reservation has no LOCAL path";
            } else if (resv.getPath(PathType.LOCAL).getPathSetupMode() == null) {
                errMessage = "Path setup mode is null";
            } else if (!resv.getPath(PathType.LOCAL).getPathSetupMode().equals("signal-xml")) {
                errMessage = "Path setup mode is not signal-xml";
            } else if(currTime.compareTo(resv.getStartTime()) < 0){
                errMessage = "Path cannot be created. Reservation " +
                "start time not yet reached.";
            } else if(currTime.compareTo(resv.getEndTime()) > 0){
                errMessage = "Path cannot be created. Reservation " +
                "end time has been reached.";
            }
        } catch (BSSException ex) {
            errMessage = ex.getMessage();
        } finally {
            if (errMessage != null) {
                bss.getTransaction().rollback();
                this.log.error(errMessage);
                throw new RemoteException(errMessage);
            }
        }
        
        /* Forward */
        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INSETUP);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FWD_STARTED, userName,
                                   "core", resv);
            boolean replyPresent = forwarder.createPath(resv);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FWD_ACCEPTED,
                                   userName, "core", resv);
        } catch (InterdomainException e) {
            forwarder.cleanUp();
            errMessage = e.getMessage();
        } catch (Exception e) {
            forwarder.cleanUp();
            errMessage = "caught Exception " + e.toString();
        } finally {
            if (errMessage != null) {
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, userName,
                                       "core", resv, "", errMessage);
                bss.getTransaction().rollback();
                this.log.error(errMessage);
                throw new RemoteException(errMessage);
            }
        }
        
        String status = null;
        try {
            /* Schedule path setup */
            status = this.pm.create(resv, true);
        } catch (InterdomainException e) {
            errMessage = e.getMessage();
        } catch (PSSException e) {
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                bss.getTransaction().rollback();
                this.log.error(errMessage);
                throw new RemoteException(errMessage);
            }
        }
        eventProducer.addEvent(OSCARSEvent.PATH_SETUP_ACCEPTED, userName,
                               "core", resv);
        bss.getTransaction().commit();
        this.log.info("createPath.end");
        return status;
    }

    /**
     * Verifies a path in response to a refreshPath request. Checks local path
     * first and then forward request. If local path is fine it forwards the
     * refreshPath request. If the local path has failed it forwards a teardown
     * message. If the forwardResponse indicates an downstream error the local
     * path is removed and the exception passed upstream.
     *
     * @param params RmiPathRequest request parameters
     * @param userName name of user making request
     * @return the result containing the status.
     * @throws RemoteException
     */
    public String refreshPath(RmiPathRequest params, String userName)
            throws  RemoteException {

        this.log.debug("refreshPath.start");
        String methodName = "RefreshPath";
        String loginConstraint = null;
        String institution = null;

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
             rmiClient.checkAccess(userName, "Reservations", "signal");
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new RemoteException("refreshPath: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
           institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
           loginConstraint = userName;
        }

        String gri = params.getGlobalReservationId();
        String status = null;
        String tokenValue = params.getToken();
        Reservation resv = null;
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            resv = 
                this.rm.getConstrainedResv(gri, loginConstraint, institution,
                        tokenValue);
            /* Check reservation parameters */
            if (resv.getPath(PathType.LOCAL).getPathSetupMode() == null ||
                    (!resv.getPath(PathType.LOCAL).getPathSetupMode().equals("signal-xml")) ){
                errMessage = "No reservations match request";
            } else if (!resv.getStatus().equals("ACTIVE")) {
                errMessage = "Path cannot be refreshed. " +
                "Reservation is not active. Please run createPath first.";
            }
        } catch (BSSException ex) {
        	  errMessage = ex.getMessage();
        } finally {
            if (errMessage != null) {
                bss.getTransaction().rollback();
                this.log.error(errMessage);
                throw new RemoteException(errMessage);
            }
        }
        /* Refresh path */
        try {
            status = this.pm.refresh(resv, true);
        } catch (PSSException e) {
            errMessage = e.getMessage();
        } catch (InterdomainException e) {
            errMessage = e.getMessage();
        } finally {
            // TODO? make sure status gets updated
            if (errMessage != null) {
                bss.getTransaction().rollback();
                this.log.error(errMessage);
                throw new RemoteException(errMessage);
            }
        }
        bss.getTransaction().commit();
        this.log.info("refreshPath.end");
        return status;
    }

    /**
     * Removes a path in response to a teardown request. Removes local path
     * first and then forwards request. If there is a failure in the local path
     * teardown the request is still forwarded. The exception is reported
     * upstream. Returns PENDING status if successful. It is returns PENDING and
     * not complete because it is possible the user could re-build the path
     * later if the reservation is not expired.
     *
     * @param params RmiPathRequest request parameters
     * @param userName name of user making request
     * @return the result containing the status.
     * @throws RemoteException
     */
    public String teardownPath(RmiPathRequest params, String userName)
            throws  RemoteException {

        this.log.debug("teardownPath.start");
        String methodName = "TeardownPath";
        String loginConstraint = null;
        String institution = null;

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
             rmiClient.checkAccess(userName, "Reservations", "signal");
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new RemoteException("teardownPath: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
           institution = rmiClient.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
           loginConstraint = userName;
        }

        EventProducer eventProducer = new EventProducer();
        String gri = params.getGlobalReservationId();
        String tokenValue = params.getToken();
        String status = null;
        Reservation resv = null;
        String errMessage = null;
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try {
            resv =
                this.rm.getConstrainedResv(gri, loginConstraint, institution,
                                           tokenValue);
        } catch (BSSException e) {
            errMessage = "No reservation found matching request";
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, userName,
                                   "core", resv, "", errMessage);
            bss.getTransaction().rollback();
        }
        eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_RECEIVED, userName,
                               "core", resv);
        Forwarder forwarder = new Forwarder();
        try {
            if (resv.getPath(PathType.LOCAL).getPathSetupMode() == null ||
                (!resv.getPath(PathType.LOCAL).getPathSetupMode().equals("signal-xml")) ) {
                throw new RemoteException("No reservations match request");
            }
            String currentStatus = StateEngine.getStatus(resv);
            StateEngine.canModifyStatus(currentStatus, StateEngine.INTEARDOWN);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FWD_STARTED, userName,
                                   "core", resv);
            boolean replyPresent = forwarder.teardownPath(resv);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FWD_ACCEPTED,
                                   userName, "core", resv);
            status = this.pm.teardown(resv, StateEngine.RESERVED);
        } catch (InterdomainException e) {
            forwarder.cleanUp();
            errMessage = e.getMessage();
        } catch (Exception e) {
            forwarder.cleanUp();
            errMessage = "Caught Exception " + e.toString();
        } finally {
            if (errMessage != null) {
                this.log.error(errMessage);
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, userName,
                                   "core", resv, "", errMessage);
                bss.getTransaction().rollback();
                throw new RemoteException(errMessage);
            }
        }
        eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_ACCEPTED, userName,
                               "core", resv);
        bss.getTransaction().commit();
        this.log.info("teardownPath.end");
        return status;
    }
}
