package net.es.oscars.oscars;

/**
 * OSCARSSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3-RC2  Built on : Jul 20, 2007 (04:21:49 LKT)
 * NOTE:  Axis2 knows not to write over this file after initial generation.
 *
 * @author Mary Thompson, David Robertson, Jason Lee, Andrew Lake
 */

import java.util.*;
import java.security.Principal;
import java.security.cert.X509Certificate;

import java.rmi.RemoteException;

import org.apache.axis2.context.*;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;

import net.es.oscars.wsdlTypes.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.bss.BSSException;
import net.es.oscars.tss.TSSException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.interdomain.InterdomainException;

import net.es.oscars.rmi.core.CoreRmiInterface;
import net.es.oscars.rmi.RmiUtils;



/**
 * OSCARS Axis2 service
 */
public class OSCARSSkeleton implements OSCARSSkeletonInterface {
    private Logger log = Logger.getLogger(OSCARSSkeleton.class);
    

    /**
     * @param request CreateReservation instance with with request params
     * @return response CreateReservationResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
    */
    public CreateReservationResponse
        createReservation(CreateReservation request)
            throws AAAFaultMessage, BSSFaultMessage {
    	
        CreateReservationResponse response = new CreateReservationResponse();
        
    	String methodName = "createReservation";

    	CoreRmiInterface rmiClient = null;
    	try {
    		rmiClient = RmiUtils.getCoreRmiClient(methodName, log);
    	} catch (RemoteException ex) {
    		throw new BSSFaultMessage(ex.getMessage());
    	}
        String login = this.checkUser(rmiClient);
        
        CreateReply reply = null;
        ResCreateContent params = request.getCreateReservation();
        
        
        /* FIXME: we actually do this check again inside the RMI, is it worth it to do it again here ?*/
        int reqBandwidth = params.getBandwidth();
        // convert from milli-seconds to minutes
        int  reqDuration = (int)(params.getEndTime() - params.getStartTime())/6000;
        boolean specifyPath = TypeConverter.pathSpecified(params.getPathInfo());
        boolean specifyGRI = (params.getGlobalReservationId() != null);
        
        // Check to see if user can create this  reservation
        AuthValue authVal = AuthValue.DENIED;
        try {
        	authVal = rmiClient.checkModResAccess(login, "Reservations", "create", reqBandwidth, reqDuration, specifyPath, specifyGRI);
        } catch (RemoteException ex) {
            throw new AAAFaultMessage(ex.getMessage());
        }

        if (authVal == AuthValue.DENIED ) {
            this.log.info("denied");
            throw new AAAFaultMessage("createReservation: permission denied");
        }
        this.log.debug("AAA complete");
        
        

        ReservationAdapter resAdapter = new ReservationAdapter();

        try {
            reply = resAdapter.create(params, login, rmiClient);
        } catch (BSSException e) {
            this.log.error("createReservation: " + e.getMessage());
            throw new BSSFaultMessage("createReservation " + e.getMessage());
        }
        response.setCreateReservationResponse(reply);
        
        
        return response;
    }

    /**
     * @param request CancelReservation instance with with request params
     * @return response CancelReservationResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public CancelReservationResponse
        cancelReservation(CancelReservation request)
            throws AAAFaultMessage, BSSFaultMessage {
    	String methodName = "cancelReservation";
        String reply = null;
        log.info(methodName+".start");

    	CoreRmiInterface rmiClient = null;
    	try {
    		rmiClient = RmiUtils.getCoreRmiClient(methodName, log);
    	} catch (RemoteException ex) {
    		throw new BSSFaultMessage(ex.getMessage());
    	}
        String username = this.checkUser(rmiClient);
        
        ReservationAdapter resAdapter = new ReservationAdapter();
        
        try {
            reply = resAdapter.cancel(request, username, rmiClient);
        } catch (BSSException e) {
            this.log.error("cancelReservation caught BSSException: " + e.getMessage());
            throw new BSSFaultMessage("cancelReservation: " + e.getMessage());
        } catch (Exception e) {
            this.log.error("cancelReservation caught Exception: " + e.getMessage());
            throw new BSSFaultMessage("cancelReservation: " + e.getMessage());
        }
        CancelReservationResponse response = new CancelReservationResponse();
        response.setCancelReservationResponse(reply);
        log.info(methodName+".end");
        return response;
    }

    /**
     * @param request QueryReservation instance containing request params
     * @return response QueryReservationResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public QueryReservationResponse queryReservation(QueryReservation request)
            throws AAAFaultMessage, BSSFaultMessage {
    	
    	String methodName = "queryReservation";
        log.info(methodName+".start");

    	CoreRmiInterface rmiClient = null;
    	try {
    		rmiClient = RmiUtils.getCoreRmiClient(methodName, log);
    	} catch (RemoteException ex) {
    		throw new BSSFaultMessage(ex.getMessage());
    	}
        String username = this.checkUser(rmiClient);
        
        ResDetails reply = null;
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.query(request, username, rmiClient);
        } catch (BSSException e) {
            this.log.error(e.getMessage());
            throw new BSSFaultMessage(e.getMessage());
        }
        QueryReservationResponse response = new QueryReservationResponse();
        response.setQueryReservationResponse(reply);
        
        log.info(methodName+".end");
        return response;
    }

    /**
     * @param request ModifyReservation instance with with request params
     * @return response ModifyReservationResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
    */

    public ModifyReservationResponse
        modifyReservation(ModifyReservation request)
            throws AAAFaultMessage, BSSFaultMessage {
    	String methodName = "modifyReservation";
        log.info(methodName+".start");

    	CoreRmiInterface rmiClient = null;
    	try {
    		rmiClient = RmiUtils.getCoreRmiClient(methodName, log);
    	} catch (RemoteException ex) {
    		throw new BSSFaultMessage(ex.getMessage());
    	}
        String username = this.checkUser(rmiClient);

        ReservationAdapter resAdapter = new ReservationAdapter();

        ModifyResReply reply = null;
        ModifyReservationResponse response = new ModifyReservationResponse();
        ModifyResContent params = request.getModifyReservation();

        try {
            reply = resAdapter.modify(params, username, rmiClient);
        } catch (BSSException e) {
            this.log.error("modifyReservation caught BSSException: " + e.getMessage());
            throw new BSSFaultMessage("modifyReservation: " + e.getMessage());
        } catch (Exception e) {
            this.log.error("modifyReservation caught Exception: " + e.getMessage());
            throw new BSSFaultMessage("modifyReservation: " + e.getMessage());
        }
        response.setModifyReservationResponse(reply);
        return response;
    }

    /**
     * @param request optionally contains, status of desired reservations
     *     start and/or end time, linkIds, number of reservations to be
     *     returned at one time, the offset of the first reservation to be
     *     returned.
     * @return response ListReservationsResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public ListReservationsResponse
        listReservations(ListReservations request)
            throws AAAFaultMessage, BSSFaultMessage {
        // FIXME: move this to RMI
    	
        ListReply reply = null;
        
        /*
        String loginConstraint = null;
        String institution = null;
        String login = this.checkUser();

        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "list");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("listReservations: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = this.userMgr.getInstitution(login);
        } else if (authVal.equals(AuthValue.SELFONLY)){getSecurityPrincipals
            loginConstraint = login;
        }
        aaa.getTransaction().commit();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        try {
            reply = this.adapter.list(loginConstraint, institution, request.getListReservations());
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("listReservations: " + e.getMessage());
            throw new BSSFaultMessage("listReservations: " + e.getMessage());
        } catch (LookupException e) {
            bss.getTransaction().rollback();
            this.log.error("listReservations: " + e.getMessage());
            throw new BSSFaultMessage("listReservations: " + e.getMessage());
        }
        ListReservationsResponse response = new ListReservationsResponse();
        response.setListReservationsResponse(reply);
        bss.getTransaction().commit();
        */
        
        ListReservationsResponse response = new ListReservationsResponse();
        response.setListReservationsResponse(reply);
        return response;
    }

    /**
     * @param request GetNetworkTopology instance with with request params
     * @return response GetNetworkTopologyResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public GetNetworkTopologyResponse
        getNetworkTopology(GetNetworkTopology request)
            throws BSSFaultMessage,AAAFaultMessage {

        GetTopologyContent requestContent = request.getGetNetworkTopology();
        GetNetworkTopologyResponse response = new GetNetworkTopologyResponse();
        // FIXME: move this to RMI
        
        /*
        GetTopologyResponseContent responseContent = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        AuthValue authVal = this.userMgr.checkAccess(login, "Domains", "query");
        aaa.getTransaction().commit();

       if (authVal.equals(AuthValue.DENIED)) {
           this.log.info("denied");
           throw new AAAFaultMessage("OSCARSSkeleton:getNetworkTopology: permission denied");
       }

        Session bss = core.getBssSession();
        bss.beginTransaction();
        try {
            responseContent = this.topoAdapter.getNetworkTopology(requestContent);
            response.setGetNetworkTopologyResponse(responseContent);
        } catch (TSSException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("getNetworkTopology: " + e.getMessage());
        } catch (Exception e) {
            bss.getTransaction().rollback();
            throw new AAAFaultMessage("getNetworkTopology: " + e.getMessage());
        }
        bss.getTransaction().commit();
        */
        return response;
    }

    /**
     * @param request InitiateTopologyPull instance with with request params
     * @return response InitiateTopologyPullResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public InitiateTopologyPullResponse
        initiateTopologyPull(InitiateTopologyPull request)
            throws BSSFaultMessage,AAAFaultMessage {

        InitiateTopologyPullContent requestContent = request.getInitiateTopologyPull();
        InitiateTopologyPullResponse response = new InitiateTopologyPullResponse();
        // FIXME: move this to RMI
        /*
        InitiateTopologyPullResponseContent responseContent = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        AuthValue authVal = this.userMgr.checkAccess(login, "Domains", "modify");
        aaa.getTransaction().commit();
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new AAAFaultMessage("OSCARSSkeleton:initiateTopologyPull: permission denied");
        }

        Session bss = core.getBssSession();
        bss.beginTransaction();
        try{
            responseContent = this.topoAdapter.initiateTopologyPull(requestContent);
            response.setInitiateTopologyPullResponse(responseContent);
        } catch(TSSException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("initiateTopologyPull: " + e.getMessage());
        } catch(Exception e) {
            bss.getTransaction().rollback();
            throw new AAAFaultMessage("initiateTopologyPull: " + e.getMessage());
        }
        bss.getTransaction().commit();
        
        */

        return response;
    }

    /**
     * @param request CreatePath instance with with request params
     * @return response CreatePathResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public CreatePathResponse
        createPath(CreatePath request)
            throws BSSFaultMessage,AAAFaultMessage {

        CreatePathContent requestContent = request.getCreatePath();
        CreatePathResponse response = new CreatePathResponse();
        // FIXME: move this to RMI

        /*        
        CreatePathResponseContent responseContent = null;
        String loginConstraint = null;
        String institution = null; // not used yet
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "signal");
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new AAAFaultMessage("OSCARSSkeleton:createPath: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
           institution = this.userMgr.getInstitution(login);
        } else if (authVal.equals(AuthValue.SELFONLY)){
           loginConstraint = login;
        }
        aaa.getTransaction().commit();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try {
            responseContent = this.pathSetupAdapter.create(requestContent,
                                        loginConstraint, login, institution);
            response.setCreatePathResponse(responseContent);
        } catch(PSSException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("createPath: " + e.getMessage());
        } catch(InterdomainException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("createPath: " + e.getMessage());
        } catch(Exception e) {
            bss.getTransaction().rollback();
            throw new AAAFaultMessage("createPath: " + e.getMessage());
        }
        bss.getTransaction().commit();
        */

        return response;
    }

    /**
     * @param request RefreshPath instance with with request params
     * @return response RefreshPathResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public RefreshPathResponse
        refreshPath(RefreshPath request)
            throws BSSFaultMessage,AAAFaultMessage {

        RefreshPathContent requestContent = request.getRefreshPath();
        RefreshPathResponse response = new RefreshPathResponse();
        RefreshPathResponseContent responseContent = null;
        // FIXME: move this to RMI
        
        /*

        String loginConstraint = null;
        String institution = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "signal");
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new AAAFaultMessage("OSCARSSkeleton:refreshPath: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = this.userMgr.getInstitution(login);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = login;
        }
        aaa.getTransaction().commit();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try {
            responseContent = this.pathSetupAdapter.refresh(requestContent, loginConstraint, institution);
            response.setRefreshPathResponse(responseContent);
        } catch (PSSException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("refreshPath: " + e.getMessage());
        } catch (InterdomainException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("refreshPath: " + e.getMessage());
        } catch (Exception e) {
            bss.getTransaction().rollback();
            throw new AAAFaultMessage("refreshPath: " + e.getMessage());
        }

        bss.getTransaction().commit();
        */
        return response;
    }

    /**
     * @param request TeardownPath instance with with request params
     * @return response TeardownPathResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public TeardownPathResponse
        teardownPath(TeardownPath request)
            throws BSSFaultMessage,AAAFaultMessage {

        TeardownPathContent requestContent = request.getTeardownPath();
        TeardownPathResponse response = new TeardownPathResponse();
        TeardownPathResponseContent responseContent = null;
        // FIXME: move this to RMI
        
        /*

        String loginConstraint = null;
        String institution = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "signal");
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new AAAFaultMessage("OSCARSSkeleton:teardownPath: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = this.userMgr.getInstitution(login);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = login;
        }
        aaa.getTransaction().commit();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        try{
            responseContent = this.pathSetupAdapter.teardown(requestContent,
                                          loginConstraint, login, institution);
            response.setTeardownPathResponse(responseContent);
        } catch(PSSException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("teardownPath: " + e.getMessage());
        } catch(InterdomainException e) {
            bss.getTransaction().rollback();
            throw new BSSFaultMessage("teardownPath: " + e.getMessage());
        } catch(Exception e) {
            bss.getTransaction().rollback();
            throw new AAAFaultMessage("teardownPath: " + e.getMessage());
        }
        bss.getTransaction().commit();
        */

        return response;
    }

    /**
     * Serves as a dispatcher for requests forwarded from an
     * adjacent domain. The user authentication is performed
     * by the specific operation methods.  The forward message will be signed
     * by the adjacent domain server. At this point we are authorizing access
     * based on that alone. This should be changed to look at the payload
     * sender as well.
     *
     * @param request Forward instance with request params.
     * @return response ForwardResponse encapsulating library reply.
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public ForwardResponse forward(Forward request)
            throws BSSFaultMessage, AAAFaultMessage {

        ForwardReply forwardReply = new ForwardReply();
        ForwardResponse forwardResponse = new ForwardResponse();
        ForwardPayload forwardPayload = request.getPayload();
        String payloadSender = request.getPayloadSender();

        String contentType = forwardPayload.getContentType();
        forwardReply.setContentType(contentType);

        if (contentType.equals("cancelReservation")) {
            CancelReservation message = new CancelReservation();
            GlobalReservationId params = forwardPayload.getCancelReservation();
            message.setCancelReservation(params);
            CancelReservationResponse response = this.cancelReservation(message);
            String reply = response.getCancelReservationResponse();
            forwardReply.setCancelReservation(reply);

        } else if (contentType.equals("createReservation")) {
            CreateReservation message = new CreateReservation();
            ResCreateContent params = forwardPayload.getCreateReservation();
            message.setCreateReservation(params);
            ReservationAdapter.addPayloadSender(params.getGlobalReservationId(), payloadSender);
            CreateReservationResponse response = this.createReservation(message);
            CreateReply reply = response.getCreateReservationResponse();
            forwardReply.setCreateReservation(reply);

        } else if (contentType.equals("modifyReservation")) {
            ModifyReservation message = new ModifyReservation();
            ModifyResContent params = forwardPayload.getModifyReservation();
            message.setModifyReservation(params);
            ModifyReservationResponse response = this.modifyReservation(message);
            ModifyResReply reply = response.getModifyReservationResponse();
            forwardReply.setModifyReservation(reply);

        } else if (contentType.equals("queryReservation")) {
            QueryReservation message = new QueryReservation();
            GlobalReservationId params = forwardPayload.getQueryReservation();
            message.setQueryReservation(params);
            QueryReservationResponse response = this.queryReservation(message);
            ResDetails reply = response.getQueryReservationResponse();
            forwardReply.setQueryReservation(reply);
/*
        } else if (contentType.equals("modifyReservation")) {
            ModifyReservation message = new ModifyReservation();
            ModifyResContent params = forwardPayload.getModifyReservation();
            message.setModifyReservation(params);
            ModifyReservationResponse response = this.modifyReservation(message);
            ModifyResReply reply = response.getModifyReservationResponse();
            forwardReply.setModifyReservation(reply);
*/
        } else if (contentType.equals("listReservations")) {
            ListReservations message = new ListReservations();
            message.setListReservations(forwardPayload.getListReservations());
            ListReservationsResponse response = this.listReservations(message);
            ListReply reply = response.getListReservationsResponse();
            forwardReply.setListReservations(reply);

        } else if (contentType.equals("createPath")) {
            CreatePath message = new CreatePath();
            message.setCreatePath(forwardPayload.getCreatePath());
            CreatePathResponse response = this.createPath(message);
            CreatePathResponseContent reply = response.getCreatePathResponse();
            forwardReply.setCreatePath(reply);

        } else if (contentType.equals("refreshPath")) {
            RefreshPath message = new RefreshPath();
            message.setRefreshPath(forwardPayload.getRefreshPath());
            RefreshPathResponse response = this.refreshPath(message);
            RefreshPathResponseContent reply =
                response.getRefreshPathResponse();
            forwardReply.setRefreshPath(reply);

        } else if (contentType.equals("teardownPath")) {
            TeardownPath message = new TeardownPath();
            message.setTeardownPath(forwardPayload.getTeardownPath());
            TeardownPathResponse response = this.teardownPath(message);
            TeardownPathResponseContent reply = response.getTeardownPathResponse();
            forwardReply.setTeardownPath(reply);

        } else {
            this.log.error("forward.error, unrecognized request type" + contentType);
            throw new BSSFaultMessage("Forward: unrecognized request type" + contentType);
        }
        forwardResponse.setForwardResponse(forwardReply);
        return forwardResponse;
    }

    /**
     * The Notify message is passed from other IDCs to indicate status changes.
     * Used by resource scheduling and signaling.
     *
     * @param request the Notify message
     */
    public void Notify(Notify request){
     	String methodName = "checkSubscriptionId";
        log.info(methodName+".start");

    	CoreRmiInterface rmiClient = null;
    	try {
    		rmiClient = RmiUtils.getCoreRmiClient(methodName, log);
    	} catch (RemoteException e) {
            this.log.error(methodName+" caught RemoteException: " + e.getMessage());
            return;
    	}

    	
        try {
            rmiClient.Notify(request);
        } catch (RemoteException e) {
            this.log.error(methodName+" caught RemoteException: " + e.getMessage());
        } catch (Exception e) {
            this.log.error(methodName+" caught RemoteException: " + e.getMessage());
        }
        log.info(methodName+".end");

    }

    /**
     * Called from the Axis2 framework during initialization of the service.
     *
     * If the service has application scope, this method is called when the
     * system starts up. Otherwise it is called when the first request comes.
     *
     * @param sc
     */
    public void init(ServiceContext sc) {
        this.log.info("OSCARS AAR initialized");
    }

    public void destroy(ServiceContext sc) {
        this.log.info("OSCARS AAR destroyed");
    }

    /**
     * Called from checkUser to get the DN out of the message context.
     *
     * @param opContext includes the MessageContext containing the message
     *                  signer
     */
    private HashMap<String, Principal> getSecurityPrincipals() {

        this.log.debug("getSecurityPrincipals.start");
        HashMap<String, Principal> result = new HashMap<String, Principal>();
        
        try {
            MessageContext inContext = MessageContext.getCurrentMessageContext();
            // opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inContext == null) {
                this.log.debug("getSecurityPrincipals.start: context is NULL");
                return null;
            }
            Vector results = (Vector) inContext.getProperty(WSHandlerConstants.RECV_RESULTS);
            
            for (int i = 0; results != null && i < results.size(); i++) {
                WSHandlerResult hResult = (WSHandlerResult) results.get(i);
                Vector hResults = hResult.getResults();
                for (int j = 0; j < hResults.size(); j++) {
                    WSSecurityEngineResult eResult = (WSSecurityEngineResult) hResults.get(j);
                    // An encryption or timestamp action does not have an
                    // associated principal. Only Signature and UsernameToken
                    // actions return a principal.
                    if ((((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.SIGN) ||
                        (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.UT)) {
	                    this.log.info("getSecurityPrincipals.getSecurityInfo, " +
	                        "Principal's name: " +
	                        ((Principal) eResult.get(
	                            WSSecurityEngineResult.TAG_PRINCIPAL)).getName());
	                    
	                    Principal subjectDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getSubjectDN();
	                    Principal issuerDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getIssuerDN();
	                    result.put("subject", subjectDN);
	                    result.put("issuer", issuerDN);
	                    return result;
	                } else if (((java.lang.Integer) eResult.get(
	                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.ENCR) {
	                    // Encryption action returns what ?
	                	return null;
	                } else if (((java.lang.Integer) eResult.get(
	                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.TS) {
	                    // Timestamp action returns a Timestamp
	                    //System.out.println("Timestamp created: " +
	                    //eResult.getTimestamp().getCreated());
	                    //System.out.println("Timestamp expires: " +
	                    //eResult.getTimestamp().getExpires());
	                	return null;
	                }
	            }
            }
        } catch (Exception e) {
            log.error("getSecurityPrincipals.exception: " + e.getMessage());
        	return null;
        }
        log.debug("getSecurityPrincipals.finish");
        return result;
    }

    
    
    /**
     *  Called from each of the messages to check that the user who signed the
     *  message is entered in the user table.
     *  First checks that there is a certificate in the message. This check
     *  should never fail unless the axis2/rampart configuration is incorrect.
     *
     * @return login A string with the login associated with the certSubject
     * @throws AAAFaultMessage
     */
    public String checkUser(CoreRmiInterface rmiClient) throws AAAFaultMessage {
        this.log.debug("checkUser.start");

        String login = null;
        HashMap<String, Principal> principals = getSecurityPrincipals();

        if (principals.get("subject") == null){
            this.log.error("checkUser: no certSubject found in message");
            AAAFaultMessage AAAErrorEx = new AAAFaultMessage(
                                 "checkUser: no certSubject found in message");
            throw AAAErrorEx;
        }

        // lookup up using input DN first
        String origDN = principals.get("subject").getName();
        this.log.info("checkUser original DN: " + origDN);
        
        try {
            login = rmiClient.verifyDN(origDN);
        } catch (Exception ex) {
            this.log.error(ex.getMessage());
            AAAFaultMessage AAAErrorEx = new AAAFaultMessage(ex.getMessage());
            throw AAAErrorEx;
        }
        
        this.log.info("checkUser authenticated user: " + login);
        this.log.debug("checkUser.end");
        return login;
    }

    
    
    /**                
     * Checks subscription ID in Notify message
     *
     * @param address the producer URL
     * @param msgSubRef the SubscriptionReference in the Notify message
     * @return the domain ID of the producer, or null if subscription not found
     */
     public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef) {
     	String methodName = "checkSubscriptionId";
        log.info(methodName+".start");

    	CoreRmiInterface rmiClient = null;
    	try {
    		rmiClient = RmiUtils.getCoreRmiClient(methodName, log);
    	} catch (RemoteException e) {
            this.log.error(methodName+" caught RemoteException: " + e.getMessage());
            return null;
    	}

    	String reply = null;
    	
        try {
            reply = rmiClient.checkSubscriptionId(address, msgSubRef);
        } catch (RemoteException e) {
            this.log.error(methodName+" caught RemoteException: " + e.getMessage());
        } catch (Exception e) {
            this.log.error(methodName+" caught RemoteException: " + e.getMessage());
        }
        log.info(methodName+".end");
        return reply;

     }

}
