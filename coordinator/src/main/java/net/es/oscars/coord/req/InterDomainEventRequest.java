package net.es.oscars.coord.req;


import java.util.HashMap;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.coord.actions.*;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.workers.NotifyWorker;
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
        String errorSrc = PathTools.getLocalDomainId(); // Will be modified for FAILURE events
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
            LOG.info(netLogger.start(method, "type is " + eventType));

            // get local version of resDetails in case the one in eventContent has incomplete local path elements
            /* The local resDetails does not have the remoteLinkIds
            resDetails = getResDetails(gri);
            if (resDetails == null) {
                LOG.error(netLogger.getMsg(method,"no reservation found for " + gri));
                ErrorReport errRep = new ErrorReport(ErrorCodes.RESV_NOT_FOUND,
                                                      "InterDomainEventContent contained invalid GRI",
                                                       ErrorReport.SYSTEM,
                                                       gri,
                                                       this.getMessageProperties().getGlobalTransactionId(),
                                                       System.currentTimeMillis()/1000L,
                                                       ModuleName.COORD,
                                                       eventContent.getErrorSource());
                OSCARSServiceException oEx = new OSCARSServiceException ( errRep );
                throw oEx;
            }
            */

            resDetails = eventContent.getResDetails();
            
            /* Set login to reservation owner Note that its not the user that sent the event.
             * This is because we do not use this attribute in cases where we care about the
             * event sender. If this changes we may need to make separate attributes.
             */
            this.setAttribute(CoordRequest.LOGIN_ATTRIBUTE, resDetails.getLogin());
            this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE, resDetails.getDescription());
            
            if (eventType.equals(NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED)) {

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

                CreateResvCompletedAction action = new CreateResvCompletedAction(this.getName() + "-CreateResvCompletedAction", this, resDetails);
                this.add (action);
                action.execute();
                if (action.getState() == CoordAction.State.FAILED){
                    throw (new OSCARSServiceException (ErrorCodes.RESV_COMPLETE_FAILED,
                                                           method + " " + action.getException().getMessage(),
                                                           ErrorReport.SYSTEM));
                }
            } else if (eventType.equals(NotifyRequestTypes.RESV_MODIFY_COMPLETED)) {

                ModifyResvCompletedAction action = new ModifyResvCompletedAction(this.getName() + "-ModifyResvCompletedAction", this, resDetails);
                this.add (action);
                action.execute();
                if (action.getState() == CoordAction.State.FAILED){
                    throw (new OSCARSServiceException (ErrorCodes.RESV_MOD_COMPLETE_FAILED,
                                                           method + " " + action.getException().getMessage(),
                                                           ErrorReport.SYSTEM));
                }
            } else if (eventType.equals(NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED)) {

                CommittedEventAction action = new CommittedEventAction(this.getName() + "-ModifyCommittedEventAction",
                                                                       this.getCoordRequest(),
                                                                       NotifyRequestTypes.RESV_MODIFY_COMMIT_CONFIRMED,
                                                                       resDetails);
                this.add (action);
                action.execute();
                if (action.getState() == CoordAction.State.FAILED){
                    throw (new OSCARSServiceException (ErrorCodes.PCE_MODIFY_COMMIT_FAILED,
                                                           method + " " + action.getException().getMessage(),
                                                           ErrorReport.SYSTEM));
                }
            } else if ((eventType.equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_CONFIRMED)) ||
                       (eventType.equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_CONFIRMED))) {

                PathRequest request = PathRequest.getPathRequest(PathRequest.PSS_CREATE_PATH + "-" +gri,
                                                                 this.getMessageProperties(),
                                                                 resDetails);
                request.execute(); // may already have been executed, but should be ok
                request.processEvent(eventType);
                if (request.getState() == CoordAction.State.FAILED){
                    throw request.getException();
                }

            } else if ((eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_CONFIRMED)) ||
                       (eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_CONFIRMED))) {

                PathRequest request = PathRequest.getPathRequest(PathRequest.PSS_TEARDOWN_PATH + "-" +gri,
                                                                 this.getMessageProperties(),
                                                                 resDetails);
                request.execute(); // may already have been executed, but should be ok
                request.processEvent (eventType);
                if (request.getState() == CoordAction.State.FAILED){
                    throw request.getException();
                }
            } else  if (eventType.equals(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_FAILED) ||
                eventType.equals(NotifyRequestTypes.PATH_SETUP_UPSTREAM_FAILED)   ) {

                errorSrc = eventContent.getErrorSource();
                PathRequest request = PathRequest.getPathRequest("CreatePath-" + gri,
                                                                  this.getMessageProperties(),
                                                                  resDetails);

                request.checkIfExecuted();  // keeps anything else from executing it
                request.processErrorEvent(eventType, eventContent);
            } else if (eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_FAILED) ||
                       eventType.equals(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_FAILED)) {

                errorSrc = eventContent.getErrorSource();
                PathRequest request = PathRequest.getPathRequest("TeardownPath-" + gri,
                                                                  this.getMessageProperties(),
                                                                  resDetails);
                request.checkIfExecuted(); // keeps anything else from executing it
                request.processErrorEvent(eventType, eventContent);

            } else if (eventType.equals(NotifyRequestTypes.RESV_CANCEL_CONFIRMED) ||
                       eventType.equals(NotifyRequestTypes.RESV_CANCEL_COMPLETED) ||
                       eventType.equals(NotifyRequestTypes.RESV_CANCEL_FAILED)) {

                CancelRequest cancelRequest = (CancelRequest) CoordRequest.getCoordRequestByAlias("CancelReservation-" + gri);
                if (cancelRequest == null ) {
                   throw (new OSCARSServiceException(ErrorCodes.RESV_CANCEL_FAILED,
                                                          method + " no CancelResvRequest associated with this event",
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
                    errorSrc = eventContent.getErrorSource();
                    remoteErrorReport = new ErrorReport(eventType,
                                                        eventContent.getErrorMessage(),
                                                        ErrorReport.UNKNOWN,
                                                        gri,
                                                        cancelRequest.getTransactionId(),
                                                        System.currentTimeMillis()/1000L,
                                                        ModuleName.COORD,
                                                        eventContent.getErrorSource());

                    OSCARSServiceException ex = new OSCARSServiceException(remoteErrorReport);
                    cancelRequest.failed(ex);
                }

            }  else {
                this.fail (new OSCARSServiceException ("InterDomainEvent FAILED INVALID TYPE " + eventType));
                LOG.fatal(netLogger.getMsg("InterDomainEvent FAILED", "INVALID TYPE " + eventType));

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
     * Gets the ResDetails for this reservation incase they are needed to create
     * a new PathRequest in getPathRequest. There are resDetails in the IDC but the
     * path elements maY not be complete for this domain.
     * @param gri  of the reservation to which this IDC pertains
     * @return    the local resDetails for this reservation
     */
    ResDetails getResDetails (String gri){

        ResDetails resDet = null;
        QueryResContent queryReq = new QueryResContent();
        queryReq.setMessageProperties(this.getMessageProperties());
        queryReq.setGlobalReservationId(gri);
        /* AuthConditions sent to RM are null
         */
        QueryReservationRequest qRequest= new QueryReservationRequest("QueryReservation-" + gri,
                                                                       null,
                                                                       queryReq);
        qRequest.execute();
        if (qRequest.getState() != CoordAction.State.FAILED){
            QueryResReply qReply = qRequest.getResultData();
            resDet = qReply.getReservationDetails();
        }
        return resDet;
    }

    /**
     * Process an internal error (the local IDC failed to process a query: an internal error
     * is when something went wrong within the IDC itself.
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
        
        //if it was a COMPLETED message that failed we need to decide if we actually 
        // need to fail the reservation or if this was a duplicate
        HashMap<String, String> validNotifyStates = new HashMap<String, String>();
        validNotifyStates.put(NotifyRequestTypes.RESV_CREATE_COMPLETED, StateEngineValues.INCOMMIT);
        validNotifyStates.put(NotifyRequestTypes.RESV_CREATE_COMMIT_CONFIRMED, StateEngineValues.INCOMMIT);
        validNotifyStates.put(NotifyRequestTypes.PATH_SETUP_DOWNSTREAM_CONFIRMED, StateEngineValues.INSETUP);
        validNotifyStates.put(NotifyRequestTypes.PATH_SETUP_UPSTREAM_CONFIRMED, StateEngineValues.INSETUP);
        validNotifyStates.put(NotifyRequestTypes.PATH_TEARDOWN_DOWNSTREAM_CONFIRMED, StateEngineValues.INTEARDOWN);
        validNotifyStates.put(NotifyRequestTypes.PATH_TEARDOWN_UPSTREAM_CONFIRMED, StateEngineValues.INTEARDOWN);
        
        
        LOG.info(method + ": eventType=" + eventType + ", " + validNotifyStates.containsKey(eventType));
        if(validNotifyStates.containsKey(eventType) && !this.checkStatus(validNotifyStates.get(eventType))){
            LOG.warn(netLogger.error(method,ErrSev.MAJOR,"Ignoring because received " + eventType + " and was not in " + validNotifyStates.get(eventType)));
            return;
        }
        
        
        // if we failed while processing an event (other than MODIFY)
        // try to fail the reservation now
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

        super.failed(e);
    }
    
    /**
     * Method to check status of reservation. Used to see if we should fail the reservation when a 
     * store action fails. This protects against duplicate notifications (like those from 0.5) 
     * failing the reservation.
     * 
     * @param targetStatus the status the reservation should have
     * @return true if status matches the given target, false otherwise
     */
    private boolean checkStatus(String targetStatus) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.info(netLogger.start("CreateResvCompletedAction.checkStatus"));
        QueryResContent queryReq = new QueryResContent();
        queryReq.setMessageProperties(this.getMessageProperties());
        queryReq.setGlobalReservationId(this.getGRI());
        QueryReservationRequest qRequest= new QueryReservationRequest("QueryReservation-" + this.getGRI(),
                                                                       null,
                                                                       queryReq);
        qRequest.execute();
        if (qRequest.getState() == CoordAction.State.FAILED){
            //can't determine state
            LOG.info(netLogger.end("CreateResvCompletedAction.checkStatus - false1"));
            return false;
        }
        
        QueryResReply qReply = qRequest.getResultData();
        if(qReply.getReservationDetails() != null && 
                targetStatus.equals(qReply.getReservationDetails().getStatus())){
            LOG.info(netLogger.end("CreateResvCompletedAction.checkStatus -true"));
            return true;
        }
        LOG.info(netLogger.end("CreateResvCompletedAction.checkStatus - false2"));
        return false;
    }
}
