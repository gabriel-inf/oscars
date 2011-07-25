package net.es.oscars.coord.req;

import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.coord.actions.RMGetStatusAction;
import net.es.oscars.coord.actions.RMUpdateFailureStatus;
import net.es.oscars.coord.workers.NotifyWorker;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.ModifyResReply;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.actions.RMUpdateStatusAction;
import net.es.oscars.coord.runtimepce.PCEData;
import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.utils.soap.OSCARSServiceException;

import java.beans.MethodDescriptor;

/**
 * A coordRequest to handle ModifyReservation requests. Input parameters are placed in
 * setRequestData of type ModifyResContent. SetResultData of type ModifyResReply is
 * not actually used.
 *
 */
public class ModifyReservationRequest extends CoordRequest <ModifyResContent,ModifyResReply >{
    
    private static final long       serialVersionUID  = 1L;
    private static final Logger LOG = Logger.getLogger(ModifyReservationRequest.class.getName());
    private OSCARSNetLogger netLogger = null;
    
    //private GlobalReservationId GRI;
    // Seems like this should be in the parent CoordRequest except there seems to be some magic with GRIs there
    // that I don't understand -mrt


    public ModifyReservationRequest(String name, 
                                    String gri,
                                    String loginName, 
                                    AuthConditions authConds, 
                                    ModifyResContent modifyResvReq) {
        super (name, modifyResvReq.getMessageProperties().getGlobalTransactionId(), gri, authConds);
        this.setRequestData(loginName, modifyResvReq);
        this.setCoordRequest(this);
        this.setMessageProperties(modifyResvReq.getMessageProperties() );
        this.setLog();
    }

    public void setRequestData (String loginName, ModifyResContent modifyResvReq) {
        // Set input parameter using base class method
        super.setRequestData (modifyResvReq);
        // Add the reservation description to the attribute list of the request.
        this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE, modifyResvReq.getDescription());
        // Add login attribute
        this.setAttribute(CoordRequest.LOGIN_ATTRIBUTE, loginName);
    }
 
    /**
     * Called by CoordImpl to start the execution of a ModifyReservation request. 
     * Synchronous parts of the processing are done and a PCERuntime action is
     * created to start the path calculation. If it returns without throwing an exception
     * the new GRI has been placed in this CoordRequest object
     * @params were set by the constructor or by setRequestData
     * @return sets the new GRI into this request object
     * @throws OSCARSServiceException - nothing is expected, but could get runtimeError
     */
    public void execute()  {

        ModifyResContent  request = this.getRequestData();
        ModifyResReply reply = new ModifyResReply();
        String method = "ModifyReservationRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method));

        ResDetails resDetails = null;
        try {
            /* Call resource Manager to do its part of modifyReservation and return the resDetails
             * for the reservation. It will set the new state to INMODIFY
             * the authConditions for modify are passed through the resourceManager which will
             * check for permitted domains and permitted login conditions and
             * only return the reservation details if any such conditions are met.
             */
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
            Object [] req = new Object[]{this.getAuthConditions(),request};
            Object[] res = rmClient.invoke("modifyReservation",req);

            if ((res == null) || (res[0] == null)) {
                this.notifyError("No response from ResourceManager", this.getGRI());
                throw new OSCARSServiceException ("No response from ResourceManager");
            }
            ModifyResReply response = (ModifyResReply) res [0];
            resDetails = response.getReservation();
            String state = resDetails.getStatus();
            if (request.getDescription().equals("")) {
                this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE,resDetails.getDescription());
            } else {
                this.setAttribute(CoordRequest.DESCRIPTION_ATTRIBUTE,request.getDescription());
            }
            // this is the previous state, not INMODIFY
            this.setAttribute(CoordRequest.STATE_ATTRIBUTE, state);
            this.setResultData(response);
            LOG.debug(netLogger.getMsg(method,"received cancel for reservation in state " + state));

            PCERuntimeAction pceRuntimeAction = new PCERuntimeAction (this.getName() + "-Modify-PCERuntimeAction",
                                                                      this,
                                                                      null,
                                                                      this.getTransactionId(),
                                                                      PCERequestTypes.PCE_MODIFY);

            PCEData pceData = new PCEData(request.getUserRequestConstraint(),
                                          request.getReservedConstraint(),
                                          request.getOptionalConstraint(),
                                          null);

            pceRuntimeAction.setRequestData(pceData);

            this.add(pceRuntimeAction);
            this.executed();

        } catch (OSCARSServiceException ex ) {
            LOG.debug(netLogger.error(method, ErrSev.MINOR, "caught OSCARSServiceException "+ ex.getMessage()));
            if (resDetails != null) {
                this.notifyError (method + "caught Exception: " +ex.getMessage(),resDetails);
            } else {
                this.notifyError (method + "caught Exception: " +ex.getMessage(),this.getGRI());
            }
            this.fail(new OSCARSServiceException(method + "caught Exception: " +ex.getMessage(), "user"));
        } catch (Exception ex ) {
            String message = ex.getMessage();
            if (message == null ) {
                message = ex.toString();
            }
            LOG.debug(netLogger.error(method, ErrSev.MINOR, "caught Exception "+ message));
            if (resDetails != null) {
                this.notifyError (method + "caught Exception: " +ex.getMessage(),resDetails);
            } else {
                this.notifyError (method + "caught Exception: " +ex.getMessage(),this.getGRI());
            }
            this.fail (new OSCARSServiceException(method + "caught Exception: " +message, "system"));
        }
        LOG.debug(netLogger.end(method));
        return;
    }
    

    private void setLog() {
        this.netLogger = OSCARSNetLogger.getTlogger();
        this.netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        this.netLogger.setGRI(this.getGRI());
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
        NotifyWorker.getInstance().sendError(this.getCoordRequest(),
                                             NotifyRequestTypes.RESV_MODIFY_FAILED,
                                             errorMsg,
                                             source,
                                             resDetails);
    }

    public void failed (Exception exception) {
        String method = "ModifyReservationRequest.execute";
        LOG.error(netLogger.error(method, ErrSev.FATAL, " ModifyReservationRequest failed with " + exception.getMessage()));
        ErrorReport errorRep = this.getCoordRequest().getErrorReport(method,
                                                                     ErrorCodes.RESV_MODIFY_FAILED,
                                                                     exception);

        // Get current status of the reservation
        RMGetStatusAction getStatusAction = new RMGetStatusAction (this.getName() + "-RMGetStatusAction",
                                                                   this,
                                                                   this.getGRI());
        getStatusAction.execute();

        if (getStatusAction.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmGetStatusStatus failed with exception " +
                                      getStatusAction.getException().getMessage()));
        } else {
            String curState = getStatusAction.getResultData().getStatus();
            if ( ! curState.equals (StateEngineValues.FAILED)) {
                 // update reservation status to UNKNOWN since  part of the modify may have succeeded
                 RMUpdateFailureStatus action = new RMUpdateFailureStatus (this.getName() + "-RMStoreAction",
                                                                           this,
                                                                           this.getGRI(),
                                                                           StateEngineValues.UNKNOWN,
                                                                           errorRep);
                action.execute();

                if (action.getState() == CoordAction.State.FAILED) {
                    LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                              action.getException().getMessage()));

                }
            }
        }
        // send notification of modifyReservation failure
        this.notifyError (errorRep.getErrorCode() + ":" + errorRep.getErrorMsg(),
                          this.getGRI());

        // TODO check that we have the Mutex
        PCERuntimeAction.releaseMutex(this.getGRI());
        super.failed(exception);
    }

}
