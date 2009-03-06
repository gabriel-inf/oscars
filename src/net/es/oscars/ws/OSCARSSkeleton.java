package net.es.oscars.ws;

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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.*;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

import net.es.oscars.wsdlTypes.*;

import net.es.oscars.aaa.AAAException;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.L2SwitchingCapType;

import net.es.oscars.rmi.bss.BssRmiInterface;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
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
        
        String methodName = "createReservation";
        this.log.info(methodName + ".start");
        CreateReservationResponse response = new CreateReservationResponse();
        String gri = request.getCreateReservation().getGlobalReservationId();
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            ReservationAdapter.releasePayloadSender(gri);
            handleException(methodName,ex);
        }
        String login = null;
        try{
            login = this.checkUser(aaaRmiClient);
        }catch(AAAFaultMessage e){
            ReservationAdapter.releasePayloadSender(gri);
            throw e;
        }
        CreateReply reply = null;
        ResCreateContent params = request.getCreateReservation();
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.create(params, login, bssRmiClient);
        } catch (Exception e) {
            ReservationAdapter.releasePayloadSender(gri);
            handleException(methodName,e);
        }
        response.setCreateReservationResponse(reply);
        this.log.info(methodName + ".end");
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

        AaaRmiInterface aaaRmiClient = null;
        BssRmiInterface bssRmiClient = null;
        try {
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
        } catch (RemoteException ex) {
            throw new BSSFaultMessage(ex.getMessage());
        }
        String username = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.cancel(request, username, bssRmiClient);
        } catch (Exception e) {
            handleException(methodName,e);
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
        AaaRmiInterface aaaRmiClient = null;
        BssRmiInterface bssRmiClient = null;
        try {
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
        } catch (RemoteException ex) {
            handleException(methodName,ex);
        }
        String username = this.checkUser(aaaRmiClient);
        ResDetails reply = null;
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.query(request, username, bssRmiClient);
        }  catch (Exception e) {
            handleException(methodName,e);
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
        String gri = request.getModifyReservation().getGlobalReservationId();
        AaaRmiInterface aaaRmiClient = null;
        BssRmiInterface bssRmiClient = null;
        try {
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
        } catch (RemoteException ex) {
            ReservationAdapter.releasePayloadSender(gri);
            handleException(methodName,ex);
        }
        String username = null;
        try{
            username = this.checkUser(aaaRmiClient);
        }catch(AAAFaultMessage e){
            ReservationAdapter.releasePayloadSender(gri);
            throw e;
        }
        ReservationAdapter resAdapter = new ReservationAdapter();
        ModifyResReply reply = null;
        ModifyReservationResponse response = new ModifyReservationResponse();
        ModifyResContent params = request.getModifyReservation();
        try {
            reply = resAdapter.modify(params, username, bssRmiClient);
        } catch (Exception e) {
            ReservationAdapter.releasePayloadSender(gri);
            handleException(methodName,e);
        }
        response.setModifyReservationResponse(reply);
        this.log.info(methodName + ".end");
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
    public ListReservationsResponse listReservations(ListReservations request)
            throws AAAFaultMessage, BSSFaultMessage {

        String methodName = "listReservations";
        log.info(methodName+".start");
        AaaRmiInterface aaaRmiClient = null;
        BssRmiInterface bssRmiClient = null;
        try {
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
        } catch (RemoteException ex) {
            handleException(methodName,ex);
        }
        String username = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        ListReply reply = null;
        ListRequest params = request.getListReservations();
        try {
            reply = resAdapter.list(params, username, bssRmiClient);
        } catch (Exception e) {
            handleException(methodName,e);
        }
        ListReservationsResponse response = new ListReservationsResponse();
        response.setListReservationsResponse(reply);
        log.info(methodName+".end");
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

        String methodName = "getNetworkTopology";
        log.info(methodName+".start");
        GetTopologyResponseContent reply = null;
        GetTopologyContent params = request.getGetNetworkTopology();
        GetNetworkTopologyResponse response = new GetNetworkTopologyResponse();
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            handleException(methodName,ex);
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.getNetworkTopology(params, login, bssRmiClient);
        } catch (Exception e) {
            handleException(methodName,e);
        } 
        response.setGetNetworkTopologyResponse(reply);
        log.info(methodName+".end");
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

        String methodName = "createPath";
        log.info(methodName+".start");
        CreatePathContent params = request.getCreatePath();
        CreatePathResponse response = new CreatePathResponse();
        CreatePathResponseContent reply = null;
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            handleException(methodName,ex);
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.createPath(params, login, bssRmiClient);
        } catch (Exception e) {
            handleException(methodName,e);
        } 
        response.setCreatePathResponse(reply);
        log.info(methodName+".end");
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

        String methodName = "refreshPath";
        log.info(methodName+".start");
        RefreshPathContent params = request.getRefreshPath();
        RefreshPathResponse response = new RefreshPathResponse();
        RefreshPathResponseContent reply = null;
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            handleException(methodName,ex);;
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.refreshPath(params, login, bssRmiClient);
        } catch (Exception e) {
            handleException(methodName,e);
        }
        response.setRefreshPathResponse(reply);
        log.info(methodName+".end");
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

        String methodName = "teardownPath";
        log.info(methodName+".start");
        TeardownPathContent params = request.getTeardownPath();
        TeardownPathResponse response = new TeardownPathResponse();
        TeardownPathResponseContent reply = null;
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            handleException(methodName,ex);
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.teardownPath(params, login, bssRmiClient);
        } catch (Exception e) {
            handleException(methodName,e);
        }
        response.setTeardownPathResponse(reply);
        log.info(methodName+".end");
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
            log.info("forward cancelReservation");
            CancelReservation message = new CancelReservation();
            GlobalReservationId params = forwardPayload.getCancelReservation();
            message.setCancelReservation(params);
            CancelReservationResponse response = this.cancelReservation(message);
            String reply = response.getCancelReservationResponse();
            forwardReply.setCancelReservation(reply);

        } else if (contentType.equals("createReservation")) {
            log.info("forward createReservation");
            CreateReservation message = new CreateReservation();
            ResCreateContent params = forwardPayload.getCreateReservation();
            message.setCreateReservation(params);
            ReservationAdapter.addPayloadSender(params.getGlobalReservationId(), payloadSender);
            CreateReservationResponse response = this.createReservation(message);
            CreateReply reply = response.getCreateReservationResponse();
            forwardReply.setCreateReservation(reply);

        } else if (contentType.equals("modifyReservation")) {
            log.info("forward modifyReservation");
            ModifyReservation message = new ModifyReservation();
            ModifyResContent params = forwardPayload.getModifyReservation();
            message.setModifyReservation(params);
            ReservationAdapter.addPayloadSender(params.getGlobalReservationId(), payloadSender);
            ModifyReservationResponse response = this.modifyReservation(message);
            ModifyResReply reply = response.getModifyReservationResponse();
            forwardReply.setModifyReservation(reply);

        } else if (contentType.equals("queryReservation")) {
            log.info("forward queryReservation");
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
            log.info("forward listReservations");
            ListReservations message = new ListReservations();
            message.setListReservations(forwardPayload.getListReservations());
            ListReservationsResponse response = this.listReservations(message);
            ListReply reply = response.getListReservationsResponse();
            forwardReply.setListReservations(reply);

        } else if (contentType.equals("createPath")) {
            log.info("forward createPath");
            CreatePath message = new CreatePath();
            message.setCreatePath(forwardPayload.getCreatePath());
            CreatePathResponse response = this.createPath(message);
            CreatePathResponseContent reply = response.getCreatePathResponse();
            forwardReply.setCreatePath(reply);

        } else if (contentType.equals("refreshPath")) {
            log.info("forward refreshPath");
            RefreshPath message = new RefreshPath();
            message.setRefreshPath(forwardPayload.getRefreshPath());
            RefreshPathResponse response = this.refreshPath(message);
            RefreshPathResponseContent reply =
                response.getRefreshPathResponse();
            forwardReply.setRefreshPath(reply);

        } else if (contentType.equals("teardownPath")) {
            log.info("forward teardownPath");
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
        this.log.info("Received Notify");
        BssRmiInterface bssRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient("HandleEvent", log);
        } catch (RemoteException ex) {
            this.log.error(ex.getMessage());
            return;
        }
        NotificationMessageHolderType[] holders = request.getNotificationMessage();
        for(NotificationMessageHolderType holder : holders){
            EndpointReferenceType prodRef = holder.getProducerReference();
            EndpointReferenceType subscrRef = holder.getSubscriptionReference();
            if(subscrRef.getReferenceParameters() == null){
                this.log.error("No ReferenceParameters provided in SubscriptionReference");
                return;
            }
            
            String producerUrl = prodRef.getAddress().toString();
            String subscriptionId = subscrRef.getReferenceParameters().getSubscriptionId();
            MessageType message = holder.getMessage();
            OMElement[] omEvents = message.getExtraElement();
            for(OMElement omEvent : omEvents){
                try{
                    EventContent event = EventContent.Factory.parse(omEvent.getXMLStreamReaderWithoutCaching());
                    this.log.debug("Event Type=" + event.getType());
                    bssRmiClient.handleEvent(WSDLTypeConverter.getOSCARSEvent(
                            event, producerUrl, subscriptionId));
                }catch(Exception e){ 
                    this.log.error(e.getMessage());
                    continue;
                }
            }
        }
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
        // TODO:  this might not be the best place
        L2SwitchingCapType.initGlobals();
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
                        this.log.debug("getSecurityPrincipals.getSecurityInfo, " +
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
    public String checkUser(AaaRmiInterface rmiClient) throws AAAFaultMessage , BSSFaultMessage{
        this.log.info("checkUser.start");

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
        this.log.info("checkUser DN: " + origDN);

        try {
            login = rmiClient.verifyDN(origDN);
        } catch (Exception ex) {
            this.log.info("check caught exception");
            handleException("checkUser",ex);
        }

        this.log.info("checkUser authenticated user: " + login);
        this.log.debug("checkUser.end");
        return login;
    }
    
    /**
     *  handles all exceptions - logs them and maps them to AAA or BSSFaultMessages
     *  @param methodName String containing name of operation
     *  @param ex Exception that was caught
     *  
     */
    public void handleException(String method, Exception ex) 
        throws AAAFaultMessage, BSSFaultMessage {

        String errorMsg = null;
        if (ex instanceof RemoteException) {
            Throwable nextEx = ex;
            // drill down to original error message
            while ((nextEx.getCause()) != null) {
                nextEx = nextEx.getCause();
            }
            if (nextEx instanceof AAAException) {
                ex = new AAAException(nextEx.getMessage());
            } else if (nextEx instanceof BSSException) {
                ex = new BSSException(nextEx.getMessage());
            } else {
                ex = new RemoteException("internal error in core. "  +nextEx.getMessage());
                this.log.error("internal error in core. "  + nextEx.getMessage());
            }
        }
        if (ex instanceof AAAException) {
            errorMsg = method + ": caught AAAException " + ex.getMessage();
            this.log.info(errorMsg);
            throw new AAAFaultMessage(errorMsg);
        } else if (ex instanceof BSSException) {
            errorMsg = method + ": caught BSSException " + ex.getMessage();
            this.log.info(errorMsg);
        } else if (ex instanceof RemoteException){
            errorMsg = method + " caught RemoteException " + ex.getMessage();
            this.log.info (errorMsg);
        } else {
            errorMsg = method + " internal error " + ex.toString();
            this.log.error(errorMsg, ex);
        }
        throw new BSSFaultMessage(errorMsg);
    }
}

