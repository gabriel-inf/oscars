package net.es.oscars.coord.req;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.coord.actions.*;
import net.es.oscars.utils.sharedConstants.*;
import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.coord.common.Coordinator;
import net.es.oscars.coord.runtimepce.PCEData;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.soap.gen.PSSReplyContent;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pss.soap.gen.TeardownReqContent;
import net.es.oscars.resourceManager.soap.gen.RMCancelRespContent;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


public class CancelRequest extends CoordRequest <CancelResContent, CancelResReply>{

    private static final long       serialVersionUID  = 1L;
    private static final Logger     LOG = Logger.getLogger(CancelRequest.class.getName());
    private boolean                 firstDomain = true;
    private boolean                 lastDomain = true;
    private boolean                 isLocalOnly = false;
    private boolean                 isActive = false;
    private boolean                 PCEFinished = false;
    private boolean                 receivedCONFIRMED = false;
    private boolean                 sentCONFIRMED = false;
    private boolean                 receivedCOMPLETED = false;
    private boolean                 sentCOMPLETED = false;
    private CtrlPlanePathContent    reservedPath = null;
    private ResDetails              resDetails = null;
    private String                  nextDomain = null;
    private String                  previousDomain = null;
    private String                  localDomain = null;
    private OSCARSNetLogger         netLogger = null;
    
    public CancelRequest(String name,
                         AuthConditions authConditions,
                         CancelResContent cancelResReq) {
        
        super (name,
               cancelResReq.getMessageProperties().getGlobalTransactionId(),
               cancelResReq.getGlobalReservationId(),
               authConditions);
        this.setRequestData(cancelResReq);
        this.setMessageProperties(cancelResReq.getMessageProperties());
        this.setCoordRequest(this);
        this.registerAlias("CancelReservation-" + this.getGRI());
    }



    /**
     * cancelReservation  cancels a reservation
     *   All the PCEs will be notified that the reservation is canceled, in case they need to
     *   release co-scheduled resources.
     *   Then if the reservation is currently active, a teardown will be initiated.
     */
    public void execute()  {
        String method = "CancelReservationRequest.execute";
        netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getMessageProperties().getGlobalTransactionId());
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method));

        try {
            // call resourceManager to do its part of cancel and get resDetails for the reservation
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
 
            CancelResContent cancelResReq = this.getRequestData();
            Object[] req = {this.getAuthConditions(), cancelResReq};
            Object[] res = rmClient.invoke("cancelReservation",req);
     
            if ((res == null) || (res[0] == null)) {
                throw new OSCARSServiceException (ErrorCodes.RESV_CANCEL_FAILED ,
                                                  "No response from ResourceManager",
                                                   ErrorReport.SYSTEM);
            }
            RMCancelRespContent response = (RMCancelRespContent) res [0];
            this.resDetails = response.getReservation();
            String state = this.resDetails.getStatus();
            if (state.equals(StateEngineValues.CANCELLED)) { // nothing to do, should we forward to the next domain?
                return;
            }
            if (state.equals(StateEngineValues.ACTIVE) ) {
                isActive = true;
            }
            reservedPath = this.resDetails.getReservedConstraint().getPathInfo().getPath();
            if (reservedPath != null) {
                isLocalOnly = PathTools.isPathLocalOnly(reservedPath);
                localDomain = PathTools.getLocalDomainId();
                String domain = PathTools.getLastDomain(reservedPath);
                lastDomain = localDomain.equals(domain);
                domain = PathTools.getFirstDomain(reservedPath);
                firstDomain = localDomain.equals(domain);
                nextDomain = PathTools.getNextDomain (reservedPath,localDomain);
                previousDomain = PathTools.getPreviousDomain(reservedPath, localDomain);
            }

            // inform all PCEs of the cancel action , done first because it also sets state in RM to INCANCEL
            PCERuntimeAction pceRuntimeAction = new PCERuntimeAction (this.getName() + "-Cancel-PCERuntimeAction",
                                                                      this,
                                                                      null,
                                                                      this.getTransactionId(),
                                                                      PCERequestTypes.PCE_CANCEL);

            PCEData pceData = new PCEData(this.resDetails.getUserRequestConstraint(),
                                          this.resDetails.getReservedConstraint(),
                                          this.resDetails.getOptionalConstraint(),
                                          null);

            pceRuntimeAction.setRequestData(pceData);
            this.add(pceRuntimeAction);
            
            this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE,resDetails.getDescription());
            this.setAttribute(CoordRequest.STATE_ATTRIBUTE, state);
            LOG.debug(netLogger.getMsg(method,"received cancel for reservation in state " + state));
            

            this.executed();
            
        } catch (OSCARSServiceException ex) {
            LOG.warn(netLogger.error(method, ErrSev.MINOR, "caught OSCARSServiceException " + ex.getMessage()));
            ErrorReport errReport = this.getCoordRequest().getErrorReport(method,
                                                                          ErrorCodes.RESV_CANCEL_FAILED,
                                                                          ex);
            this.fail(new OSCARSServiceException(errReport));
        }
        LOG.debug(netLogger.end(method));
    }
    /**
     * Called by PCERuntimeAction when all the PCE actions for the cancel are done
     */
    public void setPCEResultData() {
        String method = "CancelRequest.setPCEData";
        LOG.debug(netLogger.start(method));
        PCEFinished = true;
        // when pce is finished, forward the message if necessary
        if (! isLocalOnly && ! lastDomain) {
                // Forward CANCEL_RESERVATION  to the next IDC

                CancelResContent cancelContent = this.getRequestData();
                CancelResvForwarder forwarder = new CancelResvForwarder (this.getName() + "-CancelResvForwarder",
                                                                         this.getCoordRequest(),
                                                                         nextDomain,
                                                                         cancelContent);
                forwarder.execute();

                if (forwarder.getState() == CoordAction.State.FAILED) {
                    LOG.error(netLogger.error(method,ErrSev.MAJOR,
                                              "forwardRequest failed in execute " +
                                              forwarder.getException().getMessage()));

                    NotifyWorker.getInstance().sendInfo(this.getCoordRequest(),
                                                        NotifyRequestTypes.RESV_CANCEL_FAILED,
                                                        this.resDetails);
                    this.fail(forwarder.getException());
                    return;
                }
        }
        setReservationState("CancelRequest.setPCEData");
    }
    /**
     * Called by PSSRequestReply when it gets the PSSReply associated with this coordRequest
     * Notifications are sent for either success or failure.
     * @param pssReply
     */
    public void setPSSReplyResult(PSSReplyContent pssReply){
        String method = "CancelRequest.setResultsData";
        String status = pssReply.getStatus();
        
        if (status.equals(PSSConstants.FAIL)) {
            LOG.debug(netLogger.error(method, ErrSev.MINOR, " PSS failed"));
            if (pssReply.getErrorReport() != null ) {
                this.fail(new OSCARSServiceException(ErrorReport.fault2report(pssReply.getErrorReport())));
            } else {
                this.fail(new OSCARSServiceException(ErrorCodes.PATH_TEARDOWN_FAILED,
                                                     method + " PSS failed",
                                                     ErrorReport.SYSTEM));
            }

        } else if (status.equals(PSSConstants.SUCCESS)) {
            isActive = false;
            setReservationState(method);
        } else {
         // This is a safety net test
            LOG.debug(netLogger.error(method, ErrSev.MINOR, "PSS called Coordinator with " + pssReply.getStatus()));
        }
    }
    /**
     *  Called by InterDomainEventRequest when it gets a RESV_CANCEL_CONFIRMED event
     */
    public void cancelConfirmedReceived() {
        receivedCONFIRMED = true;
        setReservationState("CancelRequest.confirmedReceived");
    };
    
    /**
     *  Called by InterDomainEventRequest when it gets a RESV_CANCEL_COMPLETED event
     */
    public void cancelCompletedReceived() {
        receivedCOMPLETED = true;
        setReservationState("CancelRequest.completedReceived");
    }

    /**
     *  Updates the various state variables depending on the method
     * @param method The method that has been called.
     */
    private void setReservationState(String method) {

        // should we do a teardown
        if (PCEFinished && isActive &&
                (isLocalOnly || lastDomain || receivedCONFIRMED)) {
            this.startTeardown(method, this.resDetails);
            return;
        }

        // is it time to send a CANCEL_CONFIRMED
        if ( PCEFinished && ! isActive && ! isLocalOnly &&
                !firstDomain && !sentCONFIRMED &&
                (lastDomain || receivedCONFIRMED)) {
            // send a cancel_confirmed message to previous IDC confirming the cancel
            sendInterDomainEvent(previousDomain, NotifyRequestTypes.RESV_CANCEL_CONFIRMED);
            sentCONFIRMED = true;
            if (lastDomain){
                receivedCONFIRMED = true; // simplify next test
            }
            return;
        }
        // is everything completed? 
        if ( PCEFinished && ! isActive && 
                ( isLocalOnly || (receivedCONFIRMED && (firstDomain || receivedCOMPLETED)))) {
            // firstDomain, receivedCONFIRMED will pass that test
            updateStatus (method, StateEngineValues.CANCELLED);
            if ( ! isLocalOnly  && ! lastDomain ) {
                if (sentCOMPLETED == true) { // shouldn't happen
                    LOG.error(netLogger.error(method, ErrSev.MINOR, 
                                             "Trying to do a second send RESV_CANCEL_COMPLETED"));
                } else {
                    sendInterDomainEvent(nextDomain, NotifyRequestTypes.RESV_CANCEL_COMPLETED);
                    sentCOMPLETED = true;
                }
            }
            PCERuntimeAction.releaseMutex(this.getGRI());
            NotifyWorker.getInstance().sendInfo(this.getCoordRequest(),
                    NotifyRequestTypes.RESV_CANCEL_COMPLETED,
                    this.resDetails);
        }
    }

    private void updateStatus(String method, String state) {
        RMUpdateStatusAction rmUpdateStatusAction = null;

        rmUpdateStatusAction = new RMUpdateStatusAction(this.getName() + "-RMUpdateStatusAction",
                                                        this.getCoordRequest(),
                                                        this.getCoordRequest().getGRI(), 
                                                        state);
        rmUpdateStatusAction.execute();
        
        if (rmUpdateStatusAction.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MINOR,
                                      "rmUpdateStatus failed in PCERuntimeAction.setResultData with exception " +
                                      rmUpdateStatusAction.getException().getMessage()));
            this.fail (rmUpdateStatusAction.getException());
            this.notifyError ("rmUpdateStatus failed in PCERuntimeAction.setResultData with exception " +
                              rmUpdateStatusAction.getException().getMessage(), this.resDetails);
            return;
        } 
    }

    private void startTeardown(String method, ResDetails resDetails){
        try {
            // Create and execute a PSSTeaddownPathAction
            TeardownReqContent pssReq = new TeardownReqContent();
            Coordinator coordinator = Coordinator.getInstance();
            pssReq.setTransactionId(this.getMessageProperties().getGlobalTransactionId());
            pssReq.setCallbackEndpoint(coordinator.getCallbackEndpoint());
            pssReq.setReservation(resDetails);
            PSSTeardownPathAction pathTeardownAction = new PSSTeardownPathAction (this.getName(),
                                                                                  null,
                                                                                  pssReq);
            pathTeardownAction.execute();
            if (pathTeardownAction.getState() == CoordAction.State.FAILED) {
                throw pathTeardownAction.getException();
            }
         } catch (Exception e) {
             LOG.error(netLogger.error(method,ErrSev.MAJOR,
                                       "PSS teardown failed in PCERuntimeAction.setCancelResultData with exception " +
                                       e.getMessage()));
             this.fail(e);
             this.notifyError ("PSS teardown failed in PCERuntimeAction.setCancelResultData with exception " +
                                e.getMessage(), this.resDetails);
             return;
         }
    }
    
    private void sendInterDomainEvent(String targetDomain, String event) {
        
        try {
            InternalAPIWorker apiWorker = InternalAPIWorker.getInstance ();
            apiWorker.sendEventContent(this.getCoordRequest(),
                                       this.resDetails,
                                       event,
                                       targetDomain);
        } catch (OSCARSServiceException e) {
            LOG.error(netLogger.error("CancelReservationRequest",ErrSev.MAJOR,
                                      "IDCeventSend failed in PCERuntimeAction.setResultData with exception " +
                                      e.getMessage()));
            this.fail(e);
            this.notifyError ("IDCeventSend failed in PCERuntimeAction.setResultData with exception " +
                              e.getMessage(), this.resDetails);
            return;
        } 
    }

    private void sendFailureEvent (String targetDomain,ErrorReport errorReport) {
        try {
            InternalAPIWorker apiWorker = InternalAPIWorker.getInstance ();
            apiWorker.sendErrorEvent(this.getCoordRequest(),
                                     this.resDetails,
                                     NotifyRequestTypes.RESV_CANCEL_FAILED,
                                     errorReport.getErrorMsg(),
                                     errorReport.getDomainId(),
                                     targetDomain);
        } catch (OSCARSServiceException oEx) {

        }
    }

     /**
     * Process an internal error (the local IDC failed to process a query: an internal error
     * is when something went wrong within the IDC itself.
     * CoordRequest implementations are expected to implement it.
     * @param errorMsg
     * @param resDetails
     */
    public void notifyError (String errorMsg, ResDetails resDetails) {
        String source = PathTools.getLocalDomainId();
        NotifyWorker.getInstance().sendError(this.getCoordRequest(),
                                             NotifyRequestTypes.RESV_CANCEL_FAILED,
                                             errorMsg,
                                             source,
                                             resDetails);
    }

    /**
     * Handle any errors that occur during a CancelRequest.
     * Called from the fail method of this request or a child action such as PSSTeardown or RMUdateStatus
     * Also colled when a RESV_CANCEL_FAIL  is received.
     * @param exception
     */
    public void failed (Exception exception) {
        String method = "CancelRequest.failed";
        LOG.error(netLogger.error(method, ErrSev.FATAL, " CancelRequest failed with " + exception.getMessage()));
        ErrorReport errorRep = this.getCoordRequest().getErrorReport(method,
                                                                     ErrorCodes.RESV_CANCEL_FAILED,
                                                                     exception);

         RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                   this,
                                                                   this.getGRI(),
                                                                   StateEngineValues.FAILED,
                                                                   errorRep);
        action.execute();

        if (action.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                      action.getException().getMessage()));
        }

        if (!isLocalOnly) { // pass the event to the next
            if (errorRep.getDomainId().equals(localDomain)) { //error happened in this domain
                if (!lastDomain){
                    sendFailureEvent(nextDomain,errorRep);
                }
                if (!firstDomain){
                    sendFailureEvent(previousDomain,errorRep);
                }
            }
            else { // error was passed from another doamain
                if (errorRep.getDomainId().equals(previousDomain) && !lastDomain) {
                    sendFailureEvent(nextDomain, errorRep);
                } if (errorRep.getDomainId().equals(nextDomain) && !firstDomain) {
                    sendFailureEvent(previousDomain,errorRep);
                }
            }
        }

        // TODO check if we hold mutex
        PCERuntimeAction.releaseMutex(this.getGRI());
                // send notification of cancelReservation failure
        this.notifyError (errorRep.getErrorCode() + ":" + errorRep.getErrorMsg(),
                          this.getGRI());

        super.failed(exception);
    }

}
