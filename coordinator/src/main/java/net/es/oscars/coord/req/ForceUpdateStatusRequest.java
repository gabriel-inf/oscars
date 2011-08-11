package net.es.oscars.coord.req;

import net.es.oscars.resourceManager.soap.gen.UpdateFailureStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusRespContent;
import org.apache.log4j.Logger;

import net.es.oscars.resourceManager.soap.gen.UpdateFailureStatusReqContent;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusRespContent;
import net.es.oscars.common.soap.gen.AuthConditions;
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
     * sends a synchronous updateFailureStatusReservation message to the ResourceManager
     * were set in the constructor: authDecision, QueryResContent
     * @return UpdateStatusResponse set in this.ResultData.
     */
    public void execute(){
        String method = "forceUpdateStatusRequest.execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(CoordRequest.moduleName,this.getTransactionId()); 
        netLogger.setGRI(this.getGRI());
        LOG.debug(netLogger.start(method));

        // Call the ResourceManager to change the status
        UpdateStatusRespContent reply = null;
        Object [] res =  null;
        try {
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
        }
        LOG.debug(netLogger.end(method));
    }
}
