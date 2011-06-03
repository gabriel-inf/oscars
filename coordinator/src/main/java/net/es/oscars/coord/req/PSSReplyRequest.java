package net.es.oscars.coord.req;

import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.sharedConstants.PSSConstants;

import net.es.oscars.coord.actions.RMUpdateStatusAction;
import net.es.oscars.coord.actions.RMGetStatusAction;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.soap.gen.PSSReplyContent;

/** class to handle a reply message from PSS setupPath, teardownPath, modifyPath or status
 *  Currently only setup and teardown are implemented
 * @author mrt
 *
 */
public class PSSReplyRequest extends CoordRequest <PSSReplyContent,Object >{

    private static final long       serialVersionUID  = 1L;
    private static String           requestType = null;         // is reply from setupPath or teardownPath

    private static final Logger     LOG = Logger.getLogger(PSSReplyRequest.class.getName());

    public PSSReplyRequest(String name, String gri, PSSReplyContent pssReply) {
        super (name, pssReply.getTransactionId(), gri);
        this.requestType= pssReply.getReplyType();
        this.setCoordRequest(this);
    }

    public void setRequestData (PSSReplyContent params) {
        // Set input parameter using base class method
        super.setRequestData (params);
    }

    /**
     * calls ResourceManger to update the state 
     */
    public void execute()  {

        String method = "PSSReplyRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method, " for " +this.requestType));

        try {
            PSSReplyContent pssReply = this.getRequestData();
            if (pssReply == null) {
                this.fail (new OSCARSServiceException ("Null PSSReplyContent"));
                throw new OSCARSServiceException (method + " Null PSSReplyContent", ErrorReport.SYSTEM);
            }
            String gri = pssReply.getGlobalReservationId();
            CoordRequest request = null;
            CancelRequest cancelRequest = null;
            if (pssReply.getReplyType().equals(PSSConstants.PSS_SETUP)) {
                request = CoordRequest.getCoordRequestByAlias(PathRequest.PSS_CREATE_PATH + "-"
                        + gri);
            } else if (pssReply.getReplyType().equals(PSSConstants.PSS_TEARDOWN)) {
                // Look for CancelRequest first since it has a unique key
                //LOG.debug("PSSReplyRequest.execute looking for " + "cancelReservation-" + gri + "-" + this.getTransactionId());
                cancelRequest = (CancelRequest) CoordRequest.getCoordRequestByAlias("CancelReservation-" + gri);
 
                if (cancelRequest == null) { 
                    request = CoordRequest.getCoordRequestByAlias(PathRequest.PSS_TEARDOWN_PATH  +"-" + gri);
                }
            }
            if (request == null && cancelRequest == null) {
                this.fail (new OSCARSServiceException(method + " no CreatePathRequest,TearDownPathRequest or CancelReservation associated with this PSSReply"));
                LOG.warn(netLogger.error(method, ErrSev.FATAL," no CreatePathRequest,TearDownPathRequest or CancelReservation associated with this PSSReply"));               
            }
            // Notify the CreatePathRequest, TearDownPathRequest or CancelReservation request
            if (request != null) {
               ((PathRequest)request).setPSSReplyResult(pssReply);
            } else {
                cancelRequest.setPSSReplyResult(pssReply);
            }

            this.executed();
            
        } catch (Exception ex){
            this.fail (ex);
            LOG.warn(netLogger.error(method, ErrSev.MINOR," failed with exception " + ex.getMessage()));
        }
        LOG.debug(netLogger.end(method));
    }


    public void failed (Exception exception) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId());
        netLogger.setGRI(this.getGRI());
        String method = "PSSReplyRequest.execute";
        LOG.error(netLogger.error(method, ErrSev.FATAL, " PSSReplyRequest failed with " + exception.getMessage()));

        // Get up to date state of the GRI
       // update status
        RMGetStatusAction getStatusAction = new RMGetStatusAction (this.getName() + "-RMGetStatusAction",
                                                                   this,
                                                                   this.getGRI());
        getStatusAction.execute();

        if (getStatusAction.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmGetStatusStatus failed with exception " +
                                      getStatusAction.getException().getMessage()));
            this.notifyError ("rmGetStatusStatus failed with exception - cannot process failed properly" +
                              getStatusAction.getException().getMessage(), this.getGRI());
        }

        String curState = getStatusAction.getResultData().getStatus();
        String newState = curState;

        if ( ! curState.equals (StateEngineValues.FAILED)) {
            newState = StateEngineValues.RESERVED;
        }

        // update status
        RMUpdateStatusAction action = new RMUpdateStatusAction (this.getName() + "-RMStoreAction",
                                                                this,
                                                                this.getGRI(),
                                                                newState);
        action.execute();

        if (action.getState() == CoordAction.State.FAILED) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR,"rmUpdateStatus failed with exception " +
                                      action.getException().getMessage()));
            this.notifyError ("rmUpdateStatus failed with exception " +
                              action.getException().getMessage(), this.getGRI());
        }
        super.failed(exception);
    }
}
