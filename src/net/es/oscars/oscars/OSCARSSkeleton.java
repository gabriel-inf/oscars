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
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.bss.BSSException;
import net.es.oscars.interdomain.InterdomainException;


/**
 * OSCARS Axis2 service
 */
public class OSCARSSkeleton implements OSCARSSkeletonInterface {
    private Logger log;
    private ReservationAdapter adapter;
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
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        // Check to see if user can create this  reservation
        int reqBandwidth = params.getBandwidth();
        // convert from milli-seconds to minutes
        int  reqDuration = (int)(params.getEndTime() - params.getStartTime())/6000;
        boolean specifyPE = false;
        if (params.getEgressNodeIP() != null || params.getIngressNodeIP() != null ||
                       params.getReqPath() != null )  {
            specifyPE = true;
        }
        AuthValue authVal = this.userMgr.checkModResAccess(login,
                                                     "Reservations", "create", reqBandwidth, reqDuration, specifyPE);
        if (authVal == AuthValue.DENIED ) {
                     throw new AAAFaultMessageException("createReservation: permission denied");
          }
        aaa.getTransaction().commit();
        try {
            reply = this.adapter.create(params, login);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("createReservation: " + e.getMessage());
            throw new BSSFaultMessageException("createReservation " +
                                               e.getMessage());
        }   catch (InterdomainException e) {
            bss.getTransaction().rollback();
            this.log.error("createReservation interdomain error: " + e.getMessage());
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
        boolean allUsers = false;
        System.out.println("OSCARSSkeleton cancelReservation");
        String login = this.checkUser();

        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        System.out.println("CancelReservation Session is " + bss);
        ResTag params = request.getCancelReservation();
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
         UserManager.AuthValue authVal = this.userMgr.checkAccess(login,
                                                     "Reservations", "modify");
        switch (authVal) {
            case DENIED: 
                   throw new AAAFaultMessageException("OSCARSSkeleton:cancelReservation: permission denied");
           case SELFONLY: allUsers = false; break;
            case ALLUSERS: allUsers = true; break;
            
        }
        aaa.getTransaction().commit();
        try {
            reply = this.adapter.cancel(params, login,allUsers);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation caught BSSException: " +
                           e.getMessage());
            throw new BSSFaultMessageException("cancelReservation: " +
                                               e.getMessage());
        }   catch (InterdomainException e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation interdomain error: " + e.getMessage());
            throw new BSSFaultMessageException("cancelReservation interdomain error " +
                                               e.getMessage());
        } catch (Exception e) {
            bss.getTransaction().rollback();
            this.log.error("cancelReservation caught Exception: " +
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
        boolean allUsers = false;

        String login = this.checkUser();
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        ResTag tag = request.getQueryReservation();
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
         UserManager.AuthValue authVal = this.userMgr.checkAccess(login,
                                                     "Reservations", "query");
        switch (authVal) {
            case DENIED: 
                throw new AAAFaultMessageException("queryReservation: permission denied");
            case SELFONLY: allUsers = false; break;
            case ALLUSERS: allUsers =  true;  break;
        }
        aaa.getTransaction().commit();
        try {
            reply = this.adapter.query(tag, login,allUsers);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("queryReservation: " + e.getMessage());
            throw new BSSFaultMessageException("queryReservation: " +
                                               e.getMessage());
        }  catch (InterdomainException e) {
            bss.getTransaction().rollback();
            this.log.error("queryReservation interdomain error: " + e.getMessage());
            throw new BSSFaultMessageException("queryReservation interdomain error " +
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
        boolean allUsers = false;  
        String login = this.checkUser();

        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        UserManager.AuthValue authVal = this.userMgr.checkAccess(login,
                                                     "Reservations", "list");
        aaa.getTransaction().commit();
        switch (authVal) {
            case DENIED: 
                   throw new AAAFaultMessageException("listReservations: permission denied");
            case SELFONLY: allUsers = false; break;
            case ALLUSERS: allUsers =  true; break;
        }

        try {
            reply = this.adapter.list(login, allUsers);
        } catch (BSSException e) {
            bss.getTransaction().rollback();
            this.log.error("listReservations: " + e.getMessage());
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
            this.log.error("forward.error, unrecognized request type" +
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
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("aaa");
        dbnames.add("bss");
        initializer.initDatabase(dbnames);

        System.out.println("******2nd OSCARS  init.start: starting*****");
        this.adapter = new ReservationAdapter();
        this.userMgr = new UserManager("aaa");
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
                    if ((eResult.getAction() == WSConstants.SIGN) ||
                        (eResult.getAction() == WSConstants.UT)) {
                        this.log.debug("setOperationContext.getSecurityInfo, " +
                                       "Principal's name: " +
                                       eResult.getPrincipal().getName());
                        this.setCertSubject(
                                eResult.getCertificate().getSubjectDN());
                        this.setCertIssuer(
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
            this.log.error("setOperationContext.exception: " + e.getMessage());
        }
        this.log.debug("setOperationContext.finish");
    }

    /**
     *  Called from each of the messages to check that the user who signed the message
     *  is entered in the user table.
     *  Also checks to see if there was a certifiate in the message which should never happen
     *  unless the axis2/rampart configuration is incorrect.
     *  
     * @return login A string with the login associated with the certSubject
     * @throws AAAFaultMessageException 
     */
    public String checkUser() throws AAAFaultMessageException {

        String login = null;
        String[] dnElems = null;
        setOperationContext();
 
        if (this.certSubject == null){
            this.log.error("checkUser: no certSubject found in message");
            AAAFaultMessageException AAAErrorEx = new AAAFaultMessageException(
                                 "checkUser: no certSubject found in message");
            throw AAAErrorEx;
        }

        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        
        // lookup up using input DN first
        String origDN = this.certSubject.getName();
        this.log.debug("checkUser: " + origDN);
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
        	this.log.debug("checkUser: " + dn);

        	login = this.userMgr.loginFromDN(dn);
        	if (login == null) {
        		this.log.error("checkUser invalid user: " + origDN);
        		AAAFaultMessageException AAAErrorEx = new AAAFaultMessageException(
                                               "checkUser: invalid user" + origDN);
        		aaa.getTransaction().rollback();
        		throw AAAErrorEx;
        	}
            }
        } catch (AAAException ex){
                this.log.error("checkUser no attributes for user: " + origDN);              
                AAAFaultMessageException AAAErrorEx = new AAAFaultMessageException(
                        "checkUser: no attributes for user " + origDN + " :  " + ex.getMessage());
                aaa.getTransaction().rollback();
                throw AAAErrorEx;
            }

        this.log.info("checkUser authenticated user: " + login);
        
         aaa.getTransaction().commit();
        return login;
    }
    
    
}
