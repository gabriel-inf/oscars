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

import org.apache.axis2.context.*;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;

import net.es.oscars.wsdlTypes.*;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.L2SwitchingCapType;

import net.es.oscars.rmi.bss.BssRmiInterface;
import net.es.oscars.rmi.aaa.AaaRmiInterface;
import net.es.oscars.rmi.notifybroker.NotifyRmiInterface;
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
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            throw new BSSFaultMessage(ex.getMessage());
        }
        String login = this.checkUser(aaaRmiClient);
        CreateReply reply = null;
        ResCreateContent params = request.getCreateReservation();
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.create(params, login, bssRmiClient);
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
        } catch (BSSException e) {
            this.log.info(methodName + " caught BSSException: " + e.getMessage());
            throw new BSSFaultMessage(methodName + ": " + e.getMessage());
        } catch (Exception e) {
            this.log.error(methodName + " caught Exception: " + e.getMessage());
            throw new BSSFaultMessage(methodName + ": " + e.getMessage());
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
            throw new BSSFaultMessage(ex.getMessage());
        }
        String username = this.checkUser(aaaRmiClient);
        ResDetails reply = null;
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.query(request, username, bssRmiClient);
        }  catch (BSSException e) {
            this.log.info(methodName + " caught BSSException: " + e.getMessage());
            throw new BSSFaultMessage(methodName + ": " + e.getMessage());
        } catch (Exception e) {
            this.log.error(methodName + " caught Exception: " + e.getMessage());
            throw new BSSFaultMessage(methodName + ": " + e.getMessage());
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
        ModifyResReply reply = null;
        ModifyReservationResponse response = new ModifyReservationResponse();
        ModifyResContent params = request.getModifyReservation();
        try {
            reply = resAdapter.modify(params, username, bssRmiClient);
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
            throw new BSSFaultMessage(ex.getMessage());
        }
        String username = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        ListReply reply = null;
        ListRequest params = request.getListReservations();
        try {
            reply = resAdapter.list(params, username, bssRmiClient);
        } catch (BSSException e) {
            this.log.error("listReservations caught BSSException: " + e.getMessage());
            throw new BSSFaultMessage("listReservations: " + e.getMessage());
        } catch (Exception e) {
            this.log.error("listReservations caught Exception: " + e.getMessage());
            throw new BSSFaultMessage("listReservations: " + e.getMessage());
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

        GetTopologyContent params = request.getGetNetworkTopology();
        GetNetworkTopologyResponse response = new GetNetworkTopologyResponse();
        GetTopologyResponseContent reply = null;
        String methodName = "getNetworkTopology";
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            throw new BSSFaultMessage(ex.getMessage());
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.getNetworkTopology(params, login, bssRmiClient);
        } catch (BSSException e) {
            this.log.error("getNetworkTopology: " + e.getMessage());
            throw new BSSFaultMessage("getNetworkTopology " + e.getMessage());
        }
        response.setGetNetworkTopologyResponse(reply);
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

        CreatePathContent params = request.getCreatePath();
        CreatePathResponse response = new CreatePathResponse();
        CreatePathResponseContent reply = null;
        String methodName = "createPath";
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            throw new BSSFaultMessage(ex.getMessage());
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.createPath(params, login, bssRmiClient);
        } catch (BSSException e) {
            this.log.error("createPath: " + e.getMessage());
            throw new BSSFaultMessage("createPath " + e.getMessage());
        }
        response.setCreatePathResponse(reply);
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

        RefreshPathContent params = request.getRefreshPath();
        RefreshPathResponse response = new RefreshPathResponse();
        RefreshPathResponseContent reply = null;
        String methodName = "refreshPath";
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            throw new BSSFaultMessage(ex.getMessage());
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.refreshPath(params, login, bssRmiClient);
        } catch (BSSException e) {
            this.log.error("refreshPath: " + e.getMessage());
            throw new BSSFaultMessage("refreshPath " + e.getMessage());
        }
        response.setRefreshPathResponse(reply);
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

        TeardownPathContent params = request.getTeardownPath();
        TeardownPathResponse response = new TeardownPathResponse();
        TeardownPathResponseContent reply = null;
        String methodName = "teardownPath";
        BssRmiInterface bssRmiClient = null;
        AaaRmiInterface aaaRmiClient = null;
        try {
            bssRmiClient = RmiUtils.getBssRmiClient(methodName, log);
            aaaRmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        } catch (RemoteException ex) {
            throw new BSSFaultMessage(ex.getMessage());
        }
        String login = this.checkUser(aaaRmiClient);
        ReservationAdapter resAdapter = new ReservationAdapter();
        try {
            reply = resAdapter.teardownPath(params, login, bssRmiClient);
        } catch (BSSException e) {
            this.log.error("teardownPath: " + e.getMessage());
            throw new BSSFaultMessage("teardownPath " + e.getMessage());
        }
        response.setTeardownPathResponse(reply);
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
        //TODO: Implement this method
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
    public String checkUser(AaaRmiInterface rmiClient) throws AAAFaultMessage {
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
}
