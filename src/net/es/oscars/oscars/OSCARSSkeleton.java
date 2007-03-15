package net.es.oscars.oscars;

/**
 * 
 * OSCARSSkeleton.java  
 * 
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1.1-SNAPSHOT Nov 29, 2006 (02:53:00 GMT+00:00)
 * NOTE:  Axis2 knows not to write over this file after initial generation.
 *
 * @author Mary Thompson, David Robertson, Jason Lee
 */

import java.util.*;
import java.security.Principal;

import org.apache.axis2.context.*;
import org.apache.axis2.wsdl.*;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.hibernate.*;

import net.es.oscars.LogWrapper;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.bss.BSSException;
import net.es.oscars.interdomain.InterdomainException;

/**
 * OSCARS Axis2 service
 */
public class OSCARSSkeleton implements OSCARSSkeletonInterface {
    private LogWrapper log;
    private ReservationAdapter adapter;
    private UserManager userMgr;
    private Principal issuerDN;
    private Principal subjectDN;
    // private X509Certificate cert;

    public OSCARSSkeleton() {
        this.log = new LogWrapper(this.getClass());
    }

    /**
     * @param request CreateReservation instance with with request params
     * @return response CreateReservationResponse encapsulating library reply
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
    */
    public CreateReservationResponse
        createReservation(CreateReservation request)
            throws AAAFaultMessageException, BSSFaultMessageException {

        CreateReply reply = null;
        CreateReservationResponse response = new CreateReservationResponse();
        ResCreateContent params = request.getCreateReservation();
        String login = this.checkUser();
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        try {
            reply = this.adapter.create(params, login);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("createReservation", e.getMessage());
            throw new BSSFaultMessageException("createReservation " +
                                               e.getMessage());
        }   catch (InterdomainException e) {
            bss.getTransaction().rollback();
            this.log.error("createReservation interdomain error", e.getMessage());
            throw new BSSFaultMessageException("createReservation interdomain error " +
                                               e.getMessage());
        }
        response.setCreateReservationResponse(reply);
        bss.getTransaction().commit();
        return response;
    }

    /**
     * @param request CancelReservation instance with with request params
     * @return response CancelReservationResponse encapsulating library reply
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     */
    public CancelReservationResponse
        cancelReservation(CancelReservation request)
            throws AAAFaultMessageException, BSSFaultMessageException {

        // ResStatus reply = null;
        String reply = null;
        System.out.println("OSCARSSkeleton cancelReservation");
        String login = this.checkUser();

        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        System.out.println("CancelReservation Session is " + bss);
        ResTag params = request.getCancelReservation();
        try {
            reply = this.adapter.cancel(params, login);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation caught BSSException",
                           e.getMessage());
            throw new BSSFaultMessageException("cancelReservation: " +
                                               e.getMessage());
        } catch (Exception e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation caught Exception",
                           e.getMessage());
            throw new BSSFaultMessageException("cancelReservation: " +
                           e.getMessage());
        }
        CancelReservationResponse response = new CancelReservationResponse();
        response.setCancelReservationResponse(reply);
        bss.getTransaction().commit();
        return response;
    }

    /**
     * @param request QueryReservation instance with with request params
     * @return response QueryReservationResponse encapsulating library reply
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     */
    public QueryReservationResponse queryReservation(QueryReservation request)
            throws AAAFaultMessageException, BSSFaultMessageException {

        ResDetails reply = null;

        String login = this.checkUser();
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        ResTag params = request.getQueryReservation();
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.userMgr.setSession();
        aaa.beginTransaction();
        boolean authorized = this.userMgr.verifyAuthorized(login,
                                                    "Reservations", "manage");
        aaa.getTransaction().commit();
        try {
            reply = this.adapter.query(params, authorized);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("queryReservation", e.  getMessage());
            throw new BSSFaultMessageException("queryReservation: " +
                                               e.getMessage());
        }
        QueryReservationResponse response = new QueryReservationResponse();
        response.setQueryReservationResponse(reply);
        bss.getTransaction().commit();
        return response;
    }

    /**
     * @param request currently always empty
     * @return response ListReservationsResponse encapsulating library reply
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     */
    public ListReservationsResponse listReservations(ListReservations request)
            throws AAAFaultMessageException, BSSFaultMessageException {

        ListReply reply = null;
        String login = this.checkUser();

        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.userMgr.setSession();
        aaa.beginTransaction();
        boolean authorized = this.userMgr.verifyAuthorized(login,
                                                     "Reservations", "manage");
        aaa.getTransaction().commit();
        try {
            reply = this.adapter.list(login, authorized);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("listReservations", e.getMessage());
            throw new BSSFaultMessageException("listReservations: " +
                                               e.getMessage());
        }
        ListReservationsResponse response = new ListReservationsResponse();
        response.setListReservationsResponse(reply);
        bss.getTransaction().commit();
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
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     */
    public ForwardResponse forward(Forward request)
            throws BSSFaultMessageException, AAAFaultMessageException {

        ForwardReply forwardReply = new ForwardReply();
        ForwardResponse forwardResponse = new ForwardResponse();
        ForwardPayload forwardPayload = request.getPayload();
        String payloadSender = request.getPayloadSender();

        String contentType = forwardPayload.getContentType();
        forwardReply.setContentType(contentType);

        if (contentType.equals("cancelReservation")) {
            CancelReservation message = new CancelReservation();
            ResTag params = forwardPayload.getCancelReservation();
            message.setCancelReservation(params);
            CancelReservationResponse response =
                    this.cancelReservation(message);
            String reply = response.getCancelReservationResponse();
            forwardReply.setCancelReservation(reply);

        } else if (contentType.equals("createReservation")) {
            CreateReservation message = new CreateReservation();
            ResCreateContent params = forwardPayload.getCreateReservation();
            message.setCreateReservation(params);
            CreateReservationResponse response =
                    this.createReservation(message);
            CreateReply reply = response.getCreateReservationResponse();
            forwardReply.setCreateReservation(reply);

        } else if (contentType.equals("queryReservation")) {
            QueryReservation message = new QueryReservation();
            ResTag params = forwardPayload.getQueryReservation();
            message.setQueryReservation(params);
            QueryReservationResponse response = this.queryReservation(message);
            ResDetails reply = response.getQueryReservationResponse();
            forwardReply.setQueryReservation(reply);

        } else if (contentType.equals("listReservations")) {
            ListReservations message = new ListReservations();
            message.setListReservations(forwardPayload.getListReservations());
            ListReservationsResponse response = this.listReservations(message);
            ListReply reply = response.getListReservationsResponse();
            forwardReply.setListReservations(reply);

        } else {
            this.log.error("forward.error", "unrecognized request type" +
                                            contentType);
            throw new BSSFaultMessageException(
                "Forward: unrecognized request type" + contentType);
        }
        forwardResponse.setForwardResponse(forwardReply);
        return forwardResponse;
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

        System.out.println("******OSCARS  init.start: starting*****");
        String catalinaHome = System.getProperty("catalina.home");
         System.out.println("catalina.home is "+ catalinaHome);
        Initializer initializer = new Initializer();
        initializer.initDatabase();

        System.out.println("******2nd OSCARS  init.start: starting*****");
        this.adapter = new ReservationAdapter();
        this.userMgr = new UserManager();
    }

    public UserManager getUserManager() { return this.userMgr; }

    public void setSubjectDN(Principal DN) { this.subjectDN = DN; }

    public void setIssuerDN(Principal DN) { this.issuerDN = DN; }

    /**
     * Called from checkUser to get the DN out of the message context.
     * 
     * @param opContext includes the MessageContext containing the message
     *                  signer
     */
    private void setOperationContext() {

        this.log.debug("OSCARS:  setOperationContext.start", "starting");
        this.subjectDN = null;
        this.issuerDN = null;
        try {
            MessageContext inContext =
                    MessageContext.getCurrentMessageContext();
            // opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inContext == null) {
                this.log.debug("setOperationContext.start", "NULL");
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
                    if ((eResult.getAction() == WSConstants.SIGN) ||
                        (eResult.getAction() == WSConstants.UT)) {
                        this.log.debug("setOperationContext.getSecurityInfo",
                                       "Principal's name: " +
                                       eResult.getPrincipal().getName());
                        this.setSubjectDN(
                                eResult.getCertificate().getSubjectDN());
                        this.setIssuerDN(
                                eResult.getCertificate().getIssuerDN());
                    } else if (eResult.getAction() == WSConstants.ENCR) {
                        // Encryption action returns what ?
                    } else if (eResult.getAction() == WSConstants.TS) {
                        // Timestamp action returns a Timestamp
                        //System.out.println("Timestamp created: " +
                                       //eResult.getTimestamp().getCreated());
                        //System.out.println("Timestamp expires: " +
                        //eResult.getTimestamp().getExpires());
                    } 
                }
            }
        } catch (Exception e) {
            this.log.error("setOperationContext.exception", e.getMessage());
        }
        this.log.debug("setOperationContext.finish", "done.");
    }

    /**
     *  Called from each of the messages to check that the user who signed the message
     *  is entered in the user table.
     *  Also checks to see if there was a certifiate in the message which should never happen
     *  unless the axis2/rampart configuration is incorrect.
     *  
     * @return login A string with the login associated with the subjectDN
     * @throws AAAFaultMessageException 
     */
    public String checkUser() throws AAAFaultMessageException {

        String login = null;
        String[] dnElems = null;
        setOperationContext();
 
        if (this.subjectDN == null){
            this.log.error("checkUser: ", "no certificate found in message");
            AAAFaultMessageException AAAErrorEx = new AAAFaultMessageException(
                                 "checkUser: no certificate found in message");
            throw AAAErrorEx;
        }

        // serious hack to reverse the elements in the DN to match
        // the order in the data base.
        // we should change the lookup to be on just the CN
        String origDN = this.subjectDN.getName();
        dnElems = origDN.split(",");
        String dn = " " + dnElems[0];
        for (int i = 1; i < dnElems.length; i++) {
            dn = dnElems[i] + "," + dn;
        }
        dn = dn.substring(1);
        this.log.debug("checkUser", this.subjectDN.getName());

        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.userMgr.setSession();
        aaa.beginTransaction();
        login = this.userMgr.loginFromDN(dn);
        if (login == null) {
            this.log.error("checkUser invalid user: ", dn);
            AAAFaultMessageException AAAErrorEx = new AAAFaultMessageException(
                                               "checkUser: invalid user" + dn);
           aaa.getTransaction().rollback();
           throw AAAErrorEx;
        }
        aaa.getTransaction().commit();
        this.log.info("checkUser authenticated user: " ,login);
        return login;
    }
}
