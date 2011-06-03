package net.es.oscars.coord.runtimepce;

import net.es.oscars.logging.ModuleName;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.InterDomainEventContent;
import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.workers.InternalAPIWorker;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.topology.PathTools;


/**
 * ProxyAction is the base class for PCEProxyAction and AggProxyAction.
 * It stores the shared parameters of the classes. The execute, processReply and 
 * processErrorReply methods must be overridden by the extended classes.
 * 
 * @author lomax
 *
 */
public class ProxyAction extends CoordAction <PCEData, PCEData> {

    private static final long serialVersionUID = 1439115737928915954L;

    public static enum Role {
        AGGREGATOR,
        PCE
    }
    
    private ProxyAction      parentPce     = null;  // parent pce in a chain of PCE, null for root PCE
    private String           pceEndpoint   = null;  // address of pce to call
    private Role             pceRole       = null;  // PCE | AGGREGATOR
    private String           proxyEndpoint = null;  // callback address to pass to PCE
    private String           transactionId = null;  // Unique Id for each IDC request
    private String           pathTag       = null;
    private String           requestType   = null;  // pceCreate, pceCreateCommit, pceModifyCommit, pceModify, pceCancel, aggregatorCreate
    private PCERuntimeAction pceRuntime    = null;

    private static final Logger LOG = Logger.getLogger(ProxyAction.class.getName());
    private OSCARSNetLogger netLogger = null;
    /**
     * Constructor - registers this action onto the PCERuntimeAction
     * 
     * @param parentPce ProxyAction for any parentPCE
     * @param coordRequest 
     * @param name String name of PCE
     * @param pathTag
     * @param role - "AGGREGATOR" or "PCE"
     * @param proxyEndpoint callback address to which PCE service sends PCEReply messages
     * @param pceEndpoint  address of PCE service to be called
     * @param requestType  pceCreate, pceCreateCommit, pceModify, pceModify, pceCancel, aggregatorCreate
     */
    @SuppressWarnings("unchecked")
    public ProxyAction (ProxyAction       parentPce,
                        CoordRequest      coordRequest,
                        PCERuntimeAction  pceRuntime,
                        String            name,
                        String            pathTag,
                        Role              role,
                        String            proxyEndpoint,
                        String            pceEndpoint,
                        String            transactionId,
                        String            requestType) {
        
        // Note that in the input data is not set when creating the object but is dynamically
        // stored in CoordAction.requestData at execution time.
        super (name, coordRequest, null);

        this.parentPce     = parentPce;
        this.pceEndpoint   = pceEndpoint;
        this.pceRole       = role;
        this.proxyEndpoint = proxyEndpoint;
        this.transactionId = transactionId;
        this.pathTag       = pathTag;
        this.requestType   = requestType;
        this.pceRuntime    = pceRuntime;
        // Register this action onto the PCERuntimeAction
        PCERuntimeAction.setProxyAction (this.getCoordRequest().getGRI(), this.getName(), this.requestType, this);
    }
    
    public Role getRole() {
        return this.pceRole;
    }
    
    public String getPathTag() {
        return this.pathTag;
    }
 
    public String getProxyEndpoint() {
        return this.proxyEndpoint;
    }
    
    public String getPceEndpoint() {
        return this.pceEndpoint;
    }

    public String getTransactionId() {
        return this.transactionId;
    }
    
    public void execute()  {
        throw new RuntimeException ("execute called on base class ProxyAction");
    }  
    
    public void executed() {
        super.executed();
    }
    
    public ProxyAction getParentPce () {
        return this.parentPce;
    }
    
    public String getRequestType () {
        return this.requestType;
    }
    
    public PCERuntimeAction getPCERuntimeAction () {
        return this.pceRuntime;
    }
    
    /**
     * Retrieve the closest aggregator (up in the tree).  
     * @return AggProxyAction 
     */
    protected AggProxyAction getAggregator() {
        ProxyAction pce = this.getParentPce();
        while (pce != null) {
            if (pce.getRole() == Role.AGGREGATOR) {
                // Found the closes aggregator
                return (AggProxyAction) pce;
            }
            pce = pce.getParentPce();
        }
        return null;
    }
    
    public void processReply (PCEData pceData) {
        throw new RuntimeException ("processReply called on base class ProxyAction");
    }

    /**
     *  processErrorReply called from PCERuntimeSoapHandler when it receives an error reply from a PCE
     *  and from PCEProxyAction and AggProxyAction when they cannot send a message to a PCE
     *  Figures out if other domains need to be informed of the failure and if so sends an interDomainEvent
     *  Sends a notification that the request has failed
     *  Finally fails the proxyAction which should clean up all the local state
     * @param of OSCARSFault contains the error information
     */
    public void processErrorReply (OSCARSFault of){
        String method = this.getClass().getName() + ".processErrorReply";
        netLogger = OSCARSNetLogger.getTlogger();
        LOG.debug(netLogger.start(method, this.getRequestType()));
        boolean localRes = true;
        String localDomain = PathTools.getLocalDomainId();
        String previousDomain =  null;
        String nextDomain = null;
        boolean lastDomain = true;
        boolean firstDomain = true;
        ResDetails resDetails = null;
        int phase = 0;
        String notifyMsgType = "";
        String errorCode = "unknown";
        String moduleName = "";
        ErrorReport errorReport = null;

        PCEData requestData = this.getRequestData();
        if (requestData == null ) {
            LOG.error(netLogger.error(method,ErrSev.MAJOR, "no requestData in ProxyAction, shouldn't happen"));
            throw new RuntimeException("no requestData in ProxyAction");
        }
        CtrlPlanePathContent reservedPath = getPathFromPceData(requestData);
        OSCARSFaultReport faultReport = of.getErrorReport();

        // Set variables for what sort of PCErequest caused the error reply
        if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE)) {
            notifyMsgType = NotifyRequestTypes.RESV_CREATE_FAILED;
            errorCode = ErrorCodes.RESV_CREATE_FAILED;
            phase=1;
        }
        else if (this.getRequestType().equals(PCERequestTypes.PCE_MODIFY)) {
            notifyMsgType = NotifyRequestTypes.RESV_MODIFY_FAILED;
            errorCode = ErrorCodes.RESV_MODIFY_FAILED;
            phase =1;
        }
        else if (this.getRequestType().equals(PCERequestTypes.PCE_CANCEL)) {
            notifyMsgType = NotifyRequestTypes.RESV_CANCEL_FAILED;
            errorCode = ErrorCodes.RESV_CANCEL_FAILED;
            phase=1;
        }
        else if (this.getRequestType().equals(PCERequestTypes.PCE_CREATE_COMMIT)) {
            notifyMsgType = NotifyRequestTypes.RESV_CREATE_FAILED;
            errorCode = ErrorCodes.RESV_CREATE_FAILED;
            phase=2;
        }
        else if (this.getRequestType().equals(PCERequestTypes.PCE_MODIFY_COMMIT)) {
            notifyMsgType = NotifyRequestTypes.RESV_MODIFY_FAILED;
            errorCode = ErrorCodes.RESV_MODIFY_FAILED;
            phase=2;
         }

        // get or create an errorReport
        if  (faultReport != null){
            errorReport = ErrorReport.fault2report(faultReport);
        } else {
            errorReport = new ErrorReport(errorCode, of.getMsg(), ErrorReport.UNKNOWN,
                                          this.getCoordRequest().getGRI(), this.getTransactionId(),
                                          System.currentTimeMillis()/1000L,ModuleName.COORD,
                                          localDomain );
        }

        try {
            localRes = PathTools.isPathLocalOnly(reservedPath);
            String domain = PathTools.getLastDomain(reservedPath);
            lastDomain = localDomain.equals(domain);
            domain = PathTools.getFirstDomain(reservedPath);
            firstDomain = localDomain.equals(domain);
            previousDomain =PathTools.getPreviousDomain(reservedPath,localDomain);
            nextDomain =  PathTools.getNextDomain(reservedPath, localDomain);
        } catch (OSCARSServiceException e) {   // catches null reservedPath
            localRes= true; // set to skip any notifications
        }

	    LOG.info(netLogger.getMsg(method,"Path is :" + reservedPath + " localDomain is " +
                                 localDomain + " prevDomain is " + previousDomain + " errorCode is " + errorCode));
        if ( !localRes) {
             // May need to notify other IDCs of failure
            resDetails = new ResDetails();
            CoordRequest request = this.getCoordRequest();
            resDetails.setCreateTime(request.getReceivedTime());
            resDetails.setGlobalReservationId(request.getGRI());

            if ( !firstDomain ) {
                // notify previous IDC
                sendErrorEvent(notifyMsgType,previousDomain, errorReport, method );

                if (phase == 2 ) {
                    // notify  downstream domain as well which may already have committed
                    sendErrorEvent(notifyMsgType, nextDomain, errorReport, method);
                }
            }
        }

        // fail this proxy action which will in turn fail its CoordRequest and the pceRuntimeAction.
        // coordRequest.F will send the failure notification
        OSCARSServiceException ex;
        if (of.getErrorReport() != null ) {
            if (of.getErrorReport().getErrorCode() == null ) {
                of.getErrorReport().setErrorCode(errorCode);
            }
            LOG.info(netLogger.getMsg(method, "of.errorCode is " + of.getErrorReport().getErrorCode()));
            ex = new OSCARSServiceException(ErrorReport.fault2report( of.getErrorReport()));
        } else {
            LOG.debug(netLogger.error(method,ErrSev.MAJOR,"called with no error Report, should not happen"));
            ex = new OSCARSServiceException(errorCode, "PCEReply for this.getRequestType failed with no errorReport",
                                             ErrorReport.SYSTEM);
        }
	    LOG.info(netLogger.getMsg(method, "errorCode is " + ex.getErrorReport().getErrorCode()));
        this.fail (ex);

        LOG.debug(netLogger.end(method));
    }

    private void sendErrorEvent(String notifyMsgType, String dest, ErrorReport errorReport, String method) {

         try {
            InternalAPIWorker.getInstance().sendErrorEvent(this.getCoordRequest(),
                                                           notifyMsgType,
                                                           errorReport,
                                                           dest);
         } catch (OSCARSServiceException ex) {
                LOG.debug(netLogger.error(method, ErrSev.MINOR, "could not send interdomainEvent"));
         }
    }

    
    public void setResultData (PCEData data, ProxyAction srcPce) {
        super.setResultData(data);
    }

    public void fail (Exception exception, String error) {
        if (this.getPCERuntimeAction() != null) {
            // Need to notify the PCE Runtime for this GRI that a PCE has failed
            this.getPCERuntimeAction().fail (exception);
        }       
        super.fail (exception);
    }
    private static CtrlPlanePathContent getPathFromPceData (PCEData pceData) {

           //get path constraint
           CtrlPlanePathContent path = null;
           if ( (pceData.getReservedConstraint() != null) &&
                (pceData.getReservedConstraint().getPathInfo() != null)) {

               path = pceData.getReservedConstraint().getPathInfo().getPath();

           } else if (pceData.getUserRequestConstraint() != null) {
               path = pceData.getUserRequestConstraint().getPathInfo().getPath();
           }
           return path;
       }

   
}

