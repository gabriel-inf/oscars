package net.es.oscars.coord.req;


import java.util.HashMap;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.coord.actions.*;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.resourceManager.soap.gen.GetStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.GetStatusRespContent;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.StateEngineValues;


public class InterDomainEventRequest extends CoordRequest <InterDomainEventContent,Object >{

    private static final long       serialVersionUID  = 1L;
    private static final Logger     LOG = Logger.getLogger(InterDomainEventRequest.class.getName());
    private CoordRequest            origRequest = null;
    public InterDomainEventRequest(String name, String transId, String gri ) {
        super (name, transId, gri);

        this.setCoordRequest(this);
    }

    public void setRequestData (InterDomainEventContent params) {
        // Set input parameter using base class method
        super.setRequestData(params);
    }

    public void execute()  {

        String method = "InterDomainEventRequest.execute";
        String transId = this.getTransactionId();
        String gri = this.getGRI();
        ErrorReport remoteErrorReport = null;
        ResDetails resDetails;
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,transId);
        netLogger.setGRI(this.getGRI());

        try {
            InterDomainEventContent eventContent = this.getRequestData();
            if (eventContent == null) {
                LOG.error(netLogger.getMsg(method,"Null InterDomainEventContent"));
                throw new OSCARSServiceException (method + " Null InterDomainEventContent", "system");
            }
            String eventType = eventContent.getType();
            LOG.info(netLogger.start(method, "received " + eventType));

            /* need to merge the local version of resDetails which does not have the remote links
             * with the one in eventContent which might have incomplete local path elements
             * Get the local resDetails from the CreateReservationRequest
             *
             * CreateReservationRequest  request = (CreateReservationRequest)
             *          CoordRequest.getCoordRequestByAlias("createReservation-" + gri);
             *
             * ResDetails localResDetails = pceRuntimeAction.pceData2ResDetails(request.getRequestData());
             *
             * ResDetails remResDetails  = eventContent.getResDetails();
             * now merge the two
             */

            resDetails = eventContent.getResDetails();
            
            /* Set login to reservation owner Note that its not the user that sent the event.
             * This is because we do not use this attribute in cases where we care about the
             * event sender. If this changes we may need to make separate attributes.
             */
            this.setAttribute(CoordRequest.LOGIN_ATTRIBUTE, resDetails.getLogin());
            this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE, resDetails.getDescription());
            
            if (eventType.equals(NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED)) {
                CreateReservationRequest createRequest = (CreateReservationRequest)
                                    CoordRequest.getCoordRequestByAlias("createReservation-" + gri);
                this.origRequest = createRequest;
                createRequest.setCommitPhase(true);

                CommittedEventAction action = new CommittedEventAction(this.getName() + "-CreateCommittedEventAction",
                                                                       this.getCoordRequest(),
                                                                       NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED,
                                                                       resDetails);
                this.add (action);
                action.execute();
                if (action.getState() == CoordAction.State.FAILED){
                   throw (new OSCARSServiceException (ErrorCodes.PCE_COMMIT_FAILED,
                                                           method + " " + action.getException().getMessage(),
                                                           ErrorReport.SYSTEM));
                }
            } else if (eventType.equals(NotifyRequestTypes.RESV_CREATE_COMPLETED)) {
                CreateReservationRequest createRequest = (CreateReservationRequest)
                                    CoordRequest.getCoordRequestByAlias("createReservation-" + gri);
                this.origRequest = createRequest;
                CreateResvCompletedAction action = new CreateResvCompletedAction(this.getName() + "-CreateResvCompletedAction",
                                                                                 this,
                                                                                 resDetails);
                this.add (action);
                action.execute();
                if (action.getState() == CoordAction.State.FAILED){
                    throw (new OSCARSServiceException (ErrorCodes.RESV_COMPLETE_FAILED,
                                                           method + " " + action.getException().getMessage(),
                                                           ErrorReport.SYSTEM));
                }
            } else if (eventType.equals(NotifyRequestTypes.RESV_CREATE_FAILED)) {
                CreateReservationRequest createRequest = (CreateReservationRequest)
                        CoordRequest.getCoordRequestByAlias("createReservation-" + gri);
                if (createRequest == null) {
                    String status = getResvStatus();
                    if (status.equals(StateEngineValues.FAILED) ||
                        status.equals(StateEngineValues.UNKNOWN) ) {
                   // already handled
                        this.executed();
                        return;
                    }
                }
                remoteErrorReport = new ErrorReport(eventType,
                                                        eventContent.getErrorMessage(),
                                                        eventContent.getErrorCode(),
                                                        gri,
                                                        createRequest.getTransactionId(),
                                                        System.currentTimeMillis()/1000L,
                                                        ModuleName.COORD,
                                                        eventContent.getErrorSource());
                if (createRequest != null) {
                    LOG.debug(netLogger.getMsg("Received InterDomainEvent RESV_CREATE_FAILED",
                        "createRequest name is " + createRequest.getName() +
                        " errorCode " + eventContent.getErrorCode()));
                     // use createReservationRequest failed method to fail it
                    OSCARSServiceException ex = new OSCARSServiceException(remoteErrorReport);
                    createRequest.fail(ex);
                }else {
                    // fail it here - code copied from CreateReservationRequest.failed
                    // this code fails to send IDE failure events, but it should not be executed
                    RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                  this,
                                                                  gri,
                                                                  StateEngineValues.FAILED,
                                                                  remoteErrorReport);
                    action.execute();

                    if (action.getState() == CoordAction.State.FAILED) {
                        LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                                  action.getException().getMessage()));
                    }
                    this.notifyError (remoteErrorReport.getErrorCode() + ":" + remoteErrorReport.getErrorMsg(),
                          gri);
                }


            } else if (eventType.equals(NotifyRequestTypes.RESV_MODIFY_COMPLETED)) {
                ModifyReservationRequest modifyRequest = (ModifyReservationRequest)
                                    CoordRequest.getCoordRequestByAlias("modifyReservation-" + gri);
                this.origRequest=modifyRequest;
                this.setAttribute(CoordRequest.STATE_ATTRIBUTE,modifyRequest.getAttribute(CoordRequest.STATE_ATTRIBUTE));
                ModifyResvCompletedAction action = new ModifyResvCompletedAction(this.getName() + "-ModifyResvCompletedAction",
                                                                                 this,
                                                                                 resDetails);
                this.add (action);
                action.execute();
                if (action.getState() == CoordAction.State.FAILED){
                    throw (new OSCARSServiceException (ErrorCodes.RESV_MOD_COMPLETE_FAILED,
                                                           method + " " + action.getException().getMessage(),
                                                           ErrorReport.SYSTEM));
                }
            } else if (eventType.equals(NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED)) {
                ModifyReservationRequest modifyRequest = (ModifyReservationRequest)
                                    CoordRequest.getCoordRequestByAlias("modifyReservation-" + gri);
                modifyRequest.setCommitPhase(true);
                this.origRequest=modifyRequest;
                this.setAttribute(CoordRequest.STATE_ATTRIBUTE,modifyRequest.getAttribute(CoordRequest.STATE_ATTRIBUTE));
                CommittedEventAction action = new CommittedEventAction(this.getName() + "-ModifyCommittedEventAction",
                                                                       this,
                                                                       NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED,
                                                                       resDetails);
                this.add(action);
                action.execute();
                if (action.getState() == CoordAction.State.FAILED){
                    throw (new OSCARSServiceException (ErrorCodes.PCE_MODIFY_COMMIT_FAILED,
                                                           method + " " + action.getException().getMessage(),
                                                           ErrorReport.SYSTEM));
                }
            } else if (eventType.equals (NotifyRequestTypes.RESV_MODIFY_FAILED)) {
               ModifyReservationRequest modifyRequest = (ModifyReservationRequest)
                       CoordRequest.getCoordRequestByAlias("modifyReservation-" + gri);
                if (modifyRequest == null ) {
                    String status = getResvStatus();
                    if (!status.equals(StateEngineValues.INMODIFY) &&
                        !status.equals(StateEngineValues.MODCOMMITTED)) {
                        this.executed();
                        return;
                    }
                }


                remoteErrorReport = new ErrorReport(eventContent.getErrorCode(),
                                                        eventContent.getErrorMessage(),
                                                        eventType,
                                                        gri,
                                                        modifyRequest.getTransactionId(),
                                                        System.currentTimeMillis()/1000L,
                                                        ModuleName.COORD,
                                                        eventContent.getErrorSource());
                if (modifyRequest != null){
                    // use modifyRequest failed method to handle the failure
                    LOG.debug(netLogger.getMsg("Received InterDomainEvent.RESV_MODIFY_FAILED",
                        "modifyRequest name is " + modifyRequest.getName() +
                        " errorCode " + eventContent.getErrorCode()));


                    OSCARSServiceException ex = new OSCARSServiceException(remoteErrorReport);
                    modifyRequest.fail(ex);
                } else {
                    LOG.error(netLogger.error("InterDomainEvent:RESV_MODIFY_FAILED",
                                               ErrSev.MAJOR,
                                               "No ModifyReservationRequest found, setting status to UNKNOWN"));
                    RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                              this,
                                                                              gri,
                                                                              StateEngineValues.UNKNOWN,
                                                                              remoteErrorReport);
                    action.execute();

                    if (action.getState() == CoordAction.State.FAILED) {
                        LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                                  action.getException().getMessage()));
                    }
                }
            } else if ((eventType.equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_CONFIRMED)) ||
                       (eventType.equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_CONFIRMED))) {

                PathRequest request = PathRequest.getPathRequest(PathRequest.PSS_CREATE_PATH + "-" +gri,
                                                                 this.getMessageProperties(),
                                                                 resDetails);
                request.processEvent(eventType);
                if (request.getState() == CoordAction.State.FAILED){
                    throw request.getException();
                }

            } else if ((eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_CONFIRMED)) ||
                       (eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_CONFIRMED))) {

                PathRequest request = PathRequest.getPathRequest(PathRequest.PSS_TEARDOWN_PATH + "-" +gri,
                                                                 this.getMessageProperties(),
                                                                 resDetails);
                request.processEvent (eventType);
                if (request.getState() == CoordAction.State.FAILED){
                    throw request.getException();
                }
            } else  if (eventType.equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_FAILED) ||
                eventType.equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_FAILED)   ) {

                 PathRequest request = PathRequest.getPathRequest(PathRequest.PSS_CREATE_PATH + "-" +gri,
                                                                 this.getMessageProperties(),
                                                                 resDetails);
                request.checkIfExecuted();  // keeps anything else from executing it
                request.processErrorEvent(eventType, eventContent);
            } else if (eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_FAILED) ||
                       eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_FAILED)) {

                PathRequest request = PathRequest.getPathRequest(PathRequest.PSS_TEARDOWN_PATH + "-" +gri,
                                                                 this.getMessageProperties(),
                                                                 resDetails);
                request.checkIfExecuted(); // keeps anything else from executing it
                request.processErrorEvent(eventType, eventContent);

            } else if (eventType.equals(NotifyRequestTypes.RESV_CANCEL_CONFIRMED) ||
                       eventType.equals(NotifyRequestTypes.RESV_CANCEL_COMPLETED) ||
                       eventType.equals(NotifyRequestTypes.RESV_CANCEL_FAILED)) {

                CancelRequest cancelRequest = (CancelRequest) CoordRequest.getCoordRequestByAlias("cancelReservation-" + gri);
                if (cancelRequest == null ) {
                    LOG.error(netLogger.getMsg("InterDomainEvent.CANCEL","No cancelReservation found for gri " + gri));
                    throw (new OSCARSServiceException(ErrorCodes.RESV_CANCEL_FAILED,
                                                          method + " no CancelResvRequest associated with event " + eventType,
                                                          ErrorReport.SYSTEM));
                }
                if (eventType.equals(NotifyRequestTypes.RESV_CANCEL_CONFIRMED)) {
                    LOG.debug(netLogger.getMsg("InterDomainEvent.CANCEL_CONFIRMED",
                                               "CancelRequest name is " + cancelRequest.getName()));
                    cancelRequest.cancelConfirmedReceived();
                    if (cancelRequest.getState() == CoordAction.State.FAILED) {
                         throw (new OSCARSServiceException(ErrorCodes.RESV_CANCEL_FAILED,
                                                          method + cancelRequest.getException().getMessage(),
                                                          ErrorReport.SYSTEM));
                    }
                } else if (eventType.equals(NotifyRequestTypes.RESV_CANCEL_COMPLETED)) {
                    LOG.debug(netLogger.getMsg("InterDomainEvent.CANCEL_COMPLETED","CancelRequest name is " + cancelRequest.getName()));
                    cancelRequest.cancelCompletedReceived();
                    if (cancelRequest.getState() == CoordAction.State.FAILED) {
                         throw (new OSCARSServiceException(ErrorCodes.RESV_CANCEL_FAILED,
                                                          method + cancelRequest.getException().getMessage(),
                                                          ErrorReport.SYSTEM));
                    }
                } else if (eventType.equals(NotifyRequestTypes.RESV_CANCEL_FAILED)) {
                    LOG.debug(netLogger.getMsg("InterDomainEvent.CANCEL_FAILED",
                                               "CancelRequest name is " + cancelRequest.getName()));
                    remoteErrorReport = new ErrorReport(eventType,
                                                        eventContent.getErrorMessage(),
                                                        eventContent.getErrorCode(),
                                                        gri,
                                                        cancelRequest.getTransactionId(),
                                                        System.currentTimeMillis()/1000L,
                                                        ModuleName.COORD,
                                                        eventContent.getErrorSource());

                    OSCARSServiceException ex = new OSCARSServiceException(remoteErrorReport);
                    cancelRequest.fail(ex);
                }

            }  else {
                LOG.fatal(netLogger.getMsg("InterDomainEvent FAILED", "INVALID TYPE " + eventType));
                this.fail (new OSCARSServiceException ("InterDomainEvent FAILED INVALID TYPE " + eventType));
            }
 
            this.executed();
        } catch (Exception ex) {
            LOG.warn(netLogger.error(method, ErrSev.MINOR, " failed with OSCARSServiceException " + ex.getMessage()));
            ErrorReport errorReport = this.getCoordRequest().getErrorReport(method, ErrorCodes.IDE_FAILED, ex);
            this.fail(new OSCARSServiceException(errorReport));
        }
        LOG.debug(netLogger.end(method));
    }

    /**
     * Process an error that occurred when handling the interDomainEvent
     * CoordRequest implementation are expected to implement it.
     * @param errorMsg
     * @param resDetails
     */
     public void notifyError (String errorMsg, ResDetails resDetails) {
         String source = PathTools.getLocalDomainId();
         String notifyType = null;
         InterDomainEventContent eventContent = this.getRequestData();
         if (eventContent == null) {
             // Not much we can do
             return;
         }

         if (       eventContent.getType().equals(NotifyRequestTypes.PATH_SETUP_FAILED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_CONFIRMED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_FAILED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_CONFIRMED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_FAILED)) {
             notifyType = NotifyRequestTypes.PATH_SETUP_FAILED;

         } else if (eventContent.getType().equals(NotifyRequestTypes.PATH_TEARDOWN_FAILED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_CONFIRMED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_FAILED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_CONFIRMED) ||
                    eventContent.getType().equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_FAILED)) {
             notifyType = NotifyRequestTypes.PATH_TEARDOWN_FAILED;

         }  else if (eventContent.getType().equals(NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED) ||
                     eventContent.getType().equals(NotifyRequestTypes.RESV_CREATE_COMPLETED)) {
             notifyType = NotifyRequestTypes.RESV_CREATE_FAILED;

         } else if (eventContent.getType().equals(NotifyRequestTypes.RESV_MODIFY_COMPLETED) ||
                    eventContent.getType().equals(NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED)) {
             notifyType = NotifyRequestTypes.RESV_MODIFY_FAILED;

         } else if (eventContent.getType().equals(NotifyRequestTypes.RESV_CANCEL_CONFIRMED) ||
                    eventContent.getType().equals(NotifyRequestTypes.RESV_CANCEL_COMPLETED) ||
                    eventContent.getType().equals(NotifyRequestTypes.RESV_CANCEL_FAILED)) {
             notifyType = NotifyRequestTypes.RESV_CANCEL_FAILED;
         }  else {
             return;
         }
         NotifyWorker.getInstance().sendError(this.getCoordRequest(),
                                              notifyType,
                                              errorMsg,
                                              source,
                                              resDetails);
     }

    /**
     * We have failed attempting to process an InterDomain Event. We may need to
     * fail the reservation to which the event applied
     * @param e   the exception caught by the IDE
     */
    public void failed (Exception e) {

        String method = "InterDomainEventRequest.failed";
        String transId = this.getTransactionId();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,transId);
        netLogger.setGRI(this.getGRI());

        InterDomainEventContent eventContent = this.getRequestData();
        if (eventContent == null) {
            // Not much we can do. Cannot happen
            return;
        }
        String eventType = eventContent.getType();
        
        //if it was a COMPLETED or CONFIRMED message that failed we need to decide if we actually
        // need to fail the reservation or if this was a duplicate
        HashMap<String, String> validNotifyStates = new HashMap<String, String>();
        validNotifyStates.put(NotifyRequestTypes.RESV_CREATE_COMPLETED, StateEngineValues.INCOMMIT);
        validNotifyStates.put(NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED, StateEngineValues.INCOMMIT);
        validNotifyStates.put(NotifyRequestTypes.RESV_MODIFY_COMPLETED, StateEngineValues.INMODIFY);
        validNotifyStates.put(NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED, StateEngineValues.INMODIFY);
        validNotifyStates.put(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_CONFIRMED, StateEngineValues.INSETUP);
        validNotifyStates.put(NotifyRequestTypes.PATH_SETUP_UPSTREAM_CONFIRMED, StateEngineValues.INSETUP);
        validNotifyStates.put(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_CONFIRMED, StateEngineValues.INTEARDOWN);
        validNotifyStates.put(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_CONFIRMED, StateEngineValues.INTEARDOWN);
        
        
        LOG.info(method + ": eventType=" + eventType + ", " + validNotifyStates.containsKey(eventType));
        if(validNotifyStates.containsKey(eventType) &&
                !this.getResvStatus().equals(validNotifyStates.get(eventType))){
            LOG.warn(netLogger.error(method,ErrSev.MAJOR,"Ignoring because received " +
                                    eventType + " and was not in " + validNotifyStates.get(eventType)));
            return;
        }

        /* if we failed while processing an event try to fail the reservation*/
        if (this.origRequest != null) {
            LOG.debug(netLogger.getMsg(method,"calling fail on origRequest " + this.origRequest.getName()));
            this.origRequest.fail(e);

        }  else {
            LOG.debug(netLogger.getMsg(method, "no origReqest found doing local fail"));

            // if we failed while processing an event (other than MODIFY)
            // if we failed while processing an event (other than MODIFY)
            // try to fail the reservation now  note: the IDE failure events won't be sent
             ErrorReport errRep = this.getCoordRequest().getErrorReport("InterDomainEventRequest.failed",
                                                                         ErrorCodes.IDE_FAILED,
                                                                         e);

            if (! eventType.equals(NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED) &&
                ! eventType.equals(NotifyRequestTypes.RESV_MODIFY_COMPLETED) &&
                ! eventType.equals(NotifyRequestTypes.RESV_MODIFY_CONFIRMED) &&
                ! eventType.equals(NotifyRequestTypes.RESV_MODIFY_FAILED)) {

                // Set state of the reservation
                RMUpdateFailureStatus rmAction = new RMUpdateFailureStatus(this.getName() + "-RMUpdateAction",
                                                                           this,
                                                                           this.getGRI(),
                                                                           StateEngineValues.FAILED,
                                                                           errRep);
                rmAction.execute();
                if (rmAction.getState() ==  CoordAction.State.FAILED) {
                    LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                              rmAction.getException().getMessage()));
                }
            }

            /* This may be a failure in a pceCommit which is started by InterDomainEventRequest for
              interDomain reservations */
            // TODO check if we hold mutex
            PCERuntimeAction.releaseMutex(this.getGRI());
            notifyError(e.getMessage(), eventContent.getResDetails());
        }
        super.failed(e);
    }
}
