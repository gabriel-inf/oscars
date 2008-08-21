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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.security.cert.X509Certificate;

import org.apache.axis2.context.*;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.apache.log4j.*;
import org.apache.axiom.om.OMElement;
import org.hibernate.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;

import net.es.oscars.wsdlTypes.*;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.bss.BSSException;
import net.es.oscars.tss.TSSException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.interdomain.ServiceManager;
import net.es.oscars.interdomain.InterdomainException;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.bss.topology.*;


/**
 * OSCARS Axis2 service
 */
public class OSCARSSkeleton implements OSCARSSkeletonInterface {
    private Logger log;
    private ReservationAdapter adapter;
    private TopologyExchangeAdapter topoAdapter;
    private PathSetupAdapter pathSetupAdapter;
    private OSCARSCore core;
    private UserManager userMgr;
    private Principal certIssuer;
    private Principal certSubject;
    // private X509Certificate cert;

    public OSCARSSkeleton() {
        this.log = Logger.getLogger(this.getClass());
    }


    /**
     * @param request CreateReservation instance with with request params
     * @return response CreateReservationResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
    */
    public CreateReservationResponse
        createReservation(CreateReservation request)
            throws AAAFaultMessage, BSSFaultMessage {

        if (this.core.initialized) {
            this.log.debug("Core has been initialized");
        }

        TypeConverter tc = core.getTypeConverter();

        CreateReply reply = null;
        CreateReservationResponse response = new CreateReservationResponse();
        ResCreateContent params = request.getCreateReservation();
        String login = this.checkUser();

//        Session aaa = HibernateUtil.getSessionFactory(core.getAaaDbName()).getCurrentSession();
        Session aaa = core.getAaaSession();

        aaa.beginTransaction();
        // Check to see if user can create this  reservation
        int reqBandwidth = params.getBandwidth();
        // convert from milli-seconds to minutes
        int  reqDuration = (int)(params.getEndTime() - params.getStartTime())/6000;

        boolean specifyPath = tc.checkPathAuth(params.getPathInfo());
        boolean specifyGRI = (params.getGlobalReservationId() != null);
        AuthValue authVal = this.userMgr.checkModResAccess(login, "Reservations", "create",
                                       reqBandwidth, reqDuration, specifyPath, specifyGRI);
        // read-only
        aaa.getTransaction().commit();
        if (authVal == AuthValue.DENIED ) {
            this.log.info("denied");
            throw new AAAFaultMessage("createReservation: permission denied");
        }
        this.log.debug("AAA complete");


//        Session bss = HibernateUtil.getSessionFactory(core.getBssDbName()).getCurrentSession();
        Session bss = core.getBssSession();

        bss.beginTransaction();
        try {
            reply = this.adapter.create(params, login);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("createReservation: " + e.getMessage());
            throw new BSSFaultMessage("createReservation " + e.getMessage());
        }
        response.setCreateReservationResponse(reply);
        bss.getTransaction().commit();
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

        // ResStatus reply = null;
        String reply = null;
        System.out.println("OSCARSSkeleton cancelReservation");
        String login = this.checkUser();
        String institution = null;
        String loginConstraint = null;
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        GlobalReservationId params = request.getCancelReservation();
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "modify");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("cancelReservation: permission denied");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = this.userMgr.getInstitution(login);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = login;
        }
        // read-only
        aaa.getTransaction().commit();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        try {
            reply = this.adapter.cancel(params,loginConstraint,institution);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation caught BSSException: " + e.getMessage());
            throw new BSSFaultMessage("cancelReservation: " + e.getMessage());
        }   catch (InterdomainException e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation interdomain error: " + e.getMessage());
            throw new BSSFaultMessage("cancelReservation interdomain error " + e.getMessage());
        } catch (Exception e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation caught Exception: " + e.getMessage());
            throw new BSSFaultMessage("cancelReservation: " + e.getMessage());
        }
        CancelReservationResponse response = new CancelReservationResponse();
        response.setCancelReservationResponse(reply);
        bss.getTransaction().commit();
        return response;
    }

    /**
     * @param request QueryReservation instance containing request params
     * @return response QueryReservationResponse encapsulating library reply
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     */
    public QueryReservationResponse
        queryReservation(QueryReservation request)
            throws AAAFaultMessage, BSSFaultMessage {

        ResDetails reply = null;

        String login = this.checkUser();
        String loginConstraint = null;
        String institution = null;
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        GlobalReservationId gri = request.getQueryReservation();
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "query");
        if (authVal.equals(AuthValue.DENIED)) {
                throw new AAAFaultMessage("queryReservation: permission denied");
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
            reply = this.adapter.query(gri, loginConstraint, institution);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("queryReservation: " + e.getMessage());
            throw new BSSFaultMessage("queryReservation: " + e.getMessage());
        } catch (InterdomainException e) {
            bss.getTransaction().rollback();
            this.log.error("queryReservation interdomain error: " + e.getMessage());
            throw new BSSFaultMessage("queryReservation interdomain error " + e.getMessage());
        }
        QueryReservationResponse response = new QueryReservationResponse();
        response.setQueryReservationResponse(reply);
        bss.getTransaction().commit();
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

        ModifyResReply reply = null;
        ModifyReservationResponse response = new ModifyReservationResponse();
        ModifyResContent params = request.getModifyReservation();
        String loginConstraint = null;
        String institution = null;
        String login = this.checkUser();

        Session aaa = core.getAaaSession();

        aaa.beginTransaction();
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "modify");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("modifyReservation: permission denied");
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
            reply = this.adapter.modify(params, loginConstraint, institution );
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("modifyReservation caught BSSException: " + e.getMessage());
            throw new BSSFaultMessage("modifyReservation: " + e.getMessage());
        }   catch (InterdomainException e) {
            bss.getTransaction().rollback();
            this.log.error("modifyReservation interdomain error: " + e.getMessage());
            throw new BSSFaultMessage("modifyReservation interdomain error " + e.getMessage());
        } catch (Exception e) {
            bss.getTransaction().rollback();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            this.log.error("modifyReservation caught Exception: " + e.getMessage() + "\n" + sw.toString());
            throw new BSSFaultMessage("modifyReservation: " + e.getMessage());
        }
        response.setModifyReservationResponse(reply);
        bss.getTransaction().commit();
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

        ListReply reply = null;
        String loginConstraint = null;
        String institution = null;
        String login = this.checkUser();

        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "list");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("listReservations: permission denied");
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
        GetTopologyResponseContent responseContent = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        /* Check user attributes. Must have query permissions
           on Domains resources */
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Domains", "query");
        aaa.getTransaction().commit();

       if (authVal.equals(AuthValue.DENIED)) {
           this.log.info("denied");
           throw new AAAFaultMessage("OSCARSSkeleton:getNetworkTopology: permission denied");
       }

        Session bss = core.getBssSession();
        bss.beginTransaction();
        /* Retrieve topology from TEDB */
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
        InitiateTopologyPullResponseContent responseContent = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        /* Check user attributes. Must have modify permissions
           on Domains resources */
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Domains", "modify");
        aaa.getTransaction().commit();
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new AAAFaultMessage("OSCARSSkeleton:initiateTopologyPull: permission denied");
        }

        Session bss = core.getBssSession();
        bss.beginTransaction();
        /* Pull topology and store in TEDB */
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
        CreatePathResponseContent responseContent = null;
        String loginConstraint = null;
        String institution = null; // not used yet
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        /* Check user attributes. Must have signal permissions
           on Reservations resources */
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "signal");
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
        try {
            responseContent = this.pathSetupAdapter.create(requestContent, loginConstraint, institution);
            response.setCreatePathResponse(responseContent);
        } catch(PSSException e) {
            throw new BSSFaultMessage("createPath: " + e.getMessage());
        } catch(InterdomainException e) {
            throw new BSSFaultMessage("createPath: " + e.getMessage());
        } catch(Exception e) {
            throw new AAAFaultMessage("createPath: " + e.getMessage());
        }

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

        String loginConstraint = null;
        String institution = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        /* Check user attributes. Must have signal permissions
           on Reservations resources */
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "signal");
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
        try {
            responseContent = this.pathSetupAdapter.refresh(requestContent, loginConstraint, institution);
            response.setRefreshPathResponse(responseContent);
        } catch (PSSException e) {
            throw new BSSFaultMessage("refreshPath: " + e.getMessage());
        } catch (InterdomainException e) {
            throw new BSSFaultMessage("refreshPath: " + e.getMessage());
        } catch (Exception e) {
            throw new AAAFaultMessage("refreshPath: " + e.getMessage());
        }

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

        String loginConstraint = null;
        String institution = null;
        String login = this.checkUser();
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        /* Check user attributes. Must have signal permissions
           on Reservations resources */
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Reservations", "signal");
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
        try{
            responseContent = this.pathSetupAdapter.teardown(requestContent, loginConstraint, institution);
            response.setTeardownPathResponse(responseContent);
        } catch(PSSException e) {
            throw new BSSFaultMessage("teardownPath: " + e.getMessage());
        } catch(InterdomainException e) {
            throw new BSSFaultMessage("teardownPath: " + e.getMessage());
        } catch(Exception e) {
            throw new AAAFaultMessage("teardownPath: " + e.getMessage());
        }

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
        this.log.info("Received Notify");
        NotificationMessageHolderType[] holders = request.getNotificationMessage();
        for(NotificationMessageHolderType holder : holders){
            EndpointReferenceType prodRef = holder.getProducerReference();
            String address = prodRef.getAddress().toString();
            Session bss = core.getBssSession();
            bss.beginTransaction();
            String producerId = this.checkSubscriptionId(address,
                                holder.getSubscriptionReference());
            if(producerId == null){
                return;
            }
            this.log.info("Found subscription for " + address);
            MessageType message = holder.getMessage();
            OMElement[] omEvents = message.getExtraElement();
            for(OMElement omEvent : omEvents){
                try{
                    EventContent event = EventContent.Factory.parse(omEvent.getXMLStreamReaderWithoutCaching());
                    String eventType = event.getType();
                    if(eventType.startsWith("RESERVATION_CREATE")){
                        this.adapter.handleCreateEvent(event, producerId);
                    }else if(eventType.startsWith("RESERVATION_MODIFY")){
                    
                    }else if(eventType.startsWith("RESERVATION_CANCEL")){
                    
                    }else if(eventType.contains("PATH_SETUP")){
                    
                    }else if(eventType.contains("PATH_REFRESH")){
                    
                    }else if(eventType.contains("PATH_TEARDOWN")){
                    
                    }else{
                        this.log.debug("Received unkown event " + eventType);
                    }
                }catch(Exception e){ 
                    e.printStackTrace();
                    continue;
                }
             }
             bss.getTransaction().commit();
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

        this.log = Logger.getLogger(this.getClass());
        this.log.info("OSCARS init.start");
        String catalinaHome = System.getProperty("catalina.home");
        this.log.info("catalina.home is "+ catalinaHome);

        OSCARSCore core = null;
        core = OSCARSCore.init();

        if (core == null) {
            this.log.fatal("OSCARS core not initialized!");
            return;
        }

        this.core = core;
        this.userMgr = this.core.getUserManager();

        this.adapter = this.core.getReservationAdapter();
        this.topoAdapter = this.core.getTopologyExchangeAdapter();
        this.pathSetupAdapter = this.core.getPathSetupAdapter();

        this.log.info("OSCARS init.finish");
    }

    public void destroy(ServiceContext sc) {
        this.log.info("Axis2 destroy.start");
        OSCARSCore core = OSCARSCore.getInstance();
        core.shutdown();
        this.log.info("Axis2 destroy.finish");
    }

    public UserManager getUserManager() { return this.userMgr; }

    public void setCertSubject(Principal DN) { this.certSubject = DN; }

    public void setCertIssuer(Principal DN) { this.certIssuer = DN; }

    /**
     * Called from checkUser to get the DN out of the message context.
     *
     * @param opContext includes the MessageContext containing the message
     *                  signer
     */
    private void setOperationContext() {

        this.log.debug("setOperationContext.start");
        this.certSubject = null;
        this.certIssuer = null;
        try {
            MessageContext inContext =
                    MessageContext.getCurrentMessageContext();
            // opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inContext == null) {
                this.log.debug("setOperationContext.start: context is NULL");
                return;
            }
            Vector results = (Vector)
                        inContext.getProperty(WSHandlerConstants.RECV_RESULTS);
            for (int i = 0; results != null && i < results.size(); i++) {
                WSHandlerResult hResult = (WSHandlerResult) results.get(i);
                Vector hResults = hResult.getResults();
                for (int j = 0; j < hResults.size(); j++) {
                    WSSecurityEngineResult eResult =
                            (WSSecurityEngineResult) hResults.get(j);
                    // An encryption or timestamp action does not have an
                    // associated principal. Only Signature and UsernameToken
                    // actions return a principal.
                    if ((((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.SIGN) ||
                        (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.UT)) {
                    this.log.debug("setOperationContext.getSecurityInfo, " +
                        "Principal's name: " +
                        ((Principal) eResult.get(
                            WSSecurityEngineResult.TAG_PRINCIPAL)).getName());
                    this.setCertSubject(((X509Certificate) eResult.get(
                            WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getSubjectDN());
                    this.setCertIssuer(((X509Certificate) eResult.get(
                            WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getIssuerDN());
                } else if (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.ENCR) {
                    // Encryption action returns what ?
                } else if (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.TS) {
                    // Timestamp action returns a Timestamp
                    //System.out.println("Timestamp created: " +
                    //eResult.getTimestamp().getCreated());
                    //System.out.println("Timestamp expires: " +
                    //eResult.getTimestamp().getExpires());
                }
            }

            }
        } catch (Exception e) {
            this.log.error("setOperationContext.exception: " + e.getMessage());
        }
        this.log.debug("setOperationContext.finish");
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
    public String checkUser() throws AAAFaultMessage {
        this.log.debug("checkUser.start");

        String login = null;
        String[] dnElems = null;
        setOperationContext();

        if (this.certSubject == null){
            this.log.error("checkUser: no certSubject found in message");
            AAAFaultMessage AAAErrorEx = new AAAFaultMessage(
                                 "checkUser: no certSubject found in message");
            throw AAAErrorEx;
        }

        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        // lookup up using input DN first
        String origDN = this.certSubject.getName();
        this.log.debug("checkUser original DN: " + origDN);
        try {
            login = this.userMgr.loginFromDN(origDN);
            if (login == null) {
                // if that fails try the reverse of the elements in the DN
                dnElems = origDN.split(",");
                String dn = " " + dnElems[0];
                for (int i = 1; i < dnElems.length; i++) {
                    dn = dnElems[i] + "," + dn;
                }
                dn = dn.substring(1);
                this.log.debug("checkUser reverse DN: " + dn);

                login = this.userMgr.loginFromDN(dn);
                if (login == null) {
                    this.log.error("checkUser invalid user: " + origDN);
                    AAAFaultMessage AAAErrorEx =
                        new AAAFaultMessage("checkUser: invalid user" + origDN);
                    aaa.getTransaction().rollback();
                    throw AAAErrorEx;
                }
            }
        } catch (AAAException ex) {
            this.log.error("checkUser: no attributes for user: " + origDN);
            AAAFaultMessage AAAErrorEx =
                new AAAFaultMessage("checkUser: no attributes for user " + origDN + " :  " + ex.getMessage());
            aaa.getTransaction().rollback();
            throw AAAErrorEx;
        }
        this.log.info("checkUser authenticated user: " + login);
        aaa.getTransaction().commit();
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
     public String checkSubscriptionId(String address, EndpointReferenceType msgSubRef){
        ServiceManager sm = this.core.getServiceManager();
        DomainDAO domainDAO = new DomainDAO(this.core.getBssDbName());
        Domain producer = domainDAO.queryByParam("url", address);
        if(producer == null){
            this.log.error("Event producer not found in Notify message.");
            return null;
        }
        EndpointReferenceType subRef = (EndpointReferenceType) sm.getServiceMapData("NB", producer.getTopologyIdent());
        if(subRef == null){
            this.log.error("Subscription in Notify message not found.");
            return null;
        }
        ReferenceParametersType refParams = subRef.getReferenceParameters();
        ReferenceParametersType msgSubRefParams = msgSubRef.getReferenceParameters();
        if(refParams == null || msgSubRefParams == null){
            this.log.error("No reference parameters for Notify message.");
            return null;
        }
        String subId = refParams.getSubscriptionId();
        String msgSubId = msgSubRefParams.getSubscriptionId();
        if(subId == null || msgSubId == null || (!subId.equals(msgSubId))){
            this.log.error("Subscription ID in Notify message invalid.");
            this.log.error("Found " +msgSubId+", expected " + subId);
            return null;
        }
        
        return producer.getTopologyIdent();
     }

}
