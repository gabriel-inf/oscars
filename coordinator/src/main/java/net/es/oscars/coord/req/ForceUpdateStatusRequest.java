package net.es.oscars.coord.req;

import net.es.oscars.coord.actions.CoordAction;
import org.apache.log4j.Logger;

import net.es.oscars.resourceManager.soap.gen.UpdateFailureStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusRespContent;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.coord.workers.RMWorker;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.soap.OSCARSServiceException;

import java.util.FormatterClosedException;

public class ForceUpdateStatusRequest extends CoordRequest <UpdateFailureStatusReqContent, UpdateStatusRespContent> {
    
    private static final long       serialVersionUID  = 1L;
    //private String     GRI     = null;
    private static final Logger LOG = Logger.getLogger(ForceUpdateStatusRequest.class.getName());
    
    /**
     * 
     * @param name - name for this instance of CoordRequest
     * @param authConds  results of the checkAuthorization call for this user and action
     * @param updateReq contains the  GlobalReservationId of the reservation to be queried
     */
    public ForceUpdateStatusRequest(String name, AuthConditions authConds, UpdateFailureStatusReqContent updateReq) {
        super(name,updateReq.getTransactionId(),
                updateReq.getGlobalReservationId(),authConds);
        //this.GRI = updateReq.getGlobalReservationId();
        this.setRequestData(updateReq);
    }

    /**
     * Called from CoordImpl.ForceUpdateStatus which is called from the WBUI.
     * It needs to check access, unlike actions.RMUPdateFailureStatus which is only called
     * internal to the coordinator
     *
     * sends a synchronous updateFailureStatusReservation message to the ResourceManager
     * @params were set in the constructor: authDecision, QueryResContent
     * @return UpdateStatusResponse set in this.ResultData.
     */
    public void execute(){
        String method = "forceUpdateStatusRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method));

        try {
            // QueryReservation to check the conditions that apply to MODIFY
            QueryResContent queryResCon = new QueryResContent();
            UpdateFailureStatusReqContent updateStatusReq = this.getRequestData();
            AuthConditions authConditions = this.getAuthConditions();
            queryResCon.setMessageProperties(this.getMessageProperties());
            queryResCon.setGlobalReservationId(updateStatusReq.getGlobalReservationId());
            QueryReservationRequest queryReq = new QueryReservationRequest(method,
                                                                          authConditions,
                                                                          queryResCon);
            queryReq.execute();
            if (queryReq.getState() == CoordAction.State.FAILED) {
                throw queryReq.getException();
            }
            // Call the ResourceManager to change the status
            UpdateStatusRespContent reply = null;
            Object [] res =  null;
            RMWorker rmWorker = RMWorker.getInstance();
            RMClient rmClient = rmWorker.getRMClient();
            Object [] req = new Object[]{this.getRequestData()};
            res = rmClient.invoke("updateFailureStatus",req);
            reply = (UpdateStatusRespContent)res[0];
            this.setResultData(reply);
            this.executed();
        } catch (OSCARSServiceException ex ){
            this.fail(ex);
            LOG.debug(netLogger.error(method, ErrSev.MINOR, " catching OSCARSServiceException setting fail  " + 
                                       ex.getMessage()));
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message == null){
                message = ex.toString();
            }
            this.fail(new OSCARSServiceException(message, "system"));
            LOG.debug(netLogger.error(method, ErrSev.MINOR, "catching exception setting fail  "  +
                                      ex.getMessage()));
            ex.printStackTrace();
        }
        LOG.debug(netLogger.end(method));
    }
}
