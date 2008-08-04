package net.es.oscars.notify.ws;

import java.util.*;
import java.security.Principal;
import java.security.cert.X509Certificate;

import org.apache.axis2.context.*;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.apache.log4j.*;
import org.hibernate.*;

import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import org.w3.www._2005._08.addressing.EndpointReferenceType;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.notify.ws.policy.*;

/**
 *  OSCARSNotifySkeleton java skeleton for the axisService
 */
public class OSCARSNotifySkeleton implements OSCARSNotifySkeletonInterface{
    private Logger log;
    private UserManager userMgr;
    private Principal certIssuer;
    private Principal certSubject;
    private SubscriptionAdapter sa;
    private ArrayList<NotifyPEP> notifyPEPs;
    private OSCARSNotifyCore core;
    
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
        this.log.info("OSCARSNotify init.start");
        this.core = OSCARSNotifyCore.init();
        this.userMgr = this.core.getUserManager();
        this.sa = this.core.getSubscriptionAdapter();
        this.notifyPEPs = this.core.getNotifyPEPs();
        this.log.info("OSCARSNotify init.end");
    }
    
	public void Notify(Notify request){
	    this.log.debug("Received a notification message from publisher");
	    try{
	        OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
            NotificationMessageHolderType[] holders = request.getNotificationMessage();
            for(NotificationMessageHolderType holder : holders){
                EndpointReferenceType producerRef = holder.getProducerReference();
                if(!this.sa.validatePublisherRegistration(producerRef)){ 
                    continue; 
                }
                //clear out publisherRegistrationId
                producerRef.getReferenceParameters().setPublisherRegistrationId(null);
                
                Session aaa = this.core.getAAASession();
		        aaa.beginTransaction();
		        ArrayList<NotifyPEP> matchingNotifyPEPs = new ArrayList<NotifyPEP>();
                HashMap<String, ArrayList<String>> permissionMap = new HashMap<String, ArrayList<String>>();
                OMElement omMessage = holder.getMessage().getOMElement(NotificationMessage.MY_QNAME, omFactory);
                for(NotifyPEP notifyPep : this.notifyPEPs){
                    if(notifyPep.matches(omMessage)){
                        permissionMap = notifyPep.prepare(omMessage);
                        matchingNotifyPEPs.add(notifyPep);
                        break;
                    }
                }
                aaa.getTransaction().commit();
                this.sa.schedProcessNotify(holder, permissionMap, matchingNotifyPEPs);
            }
	    }catch(Exception e){
	        this.log.error(e.getMessage());
	        e.printStackTrace();
	    }
	    return;
	}

	public SubscribeResponse Subscribe(Subscribe request)
           throws AAAFaultMessage, InvalidFilterFault, InvalidMessageContentExpressionFault, 
                  InvalidTopicExpressionFault, InvalidProducerPropertiesExpressionFault, 
                  NotifyMessageNotSupportedFault, ResourceUnknownFault, SubscribeCreationFailedFault,
                  TopicExpressionDialectUnknownFault, TopicNotSupportedFault,
                  UnacceptableInitialTerminationTimeFault, UnrecognizedPolicyRequestFault,
                  UnsupportedPolicyRequestFault{
                  
        String login = this.checkUser();   
        HashMap<String, String> permissionMap = new HashMap<String, String>();
		
		/* Get authorizations */
		Session aaa = this.core.getAAASession();
		aaa.beginTransaction();
		UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "Subscriptions", "create");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to create subscriptions.");
        }
		//TODO: Don't assume subscriber and consumer are the same
		authVal = this.userMgr.checkAccess(login, "Notifications", "query");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to view notifications.");
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            String institution = this.userMgr.getInstitution(login);
            permissionMap.put("institution", institution);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("loginConstraint", login);
        }
        aaa.getTransaction().commit();
		
		SubscribeResponse response = this.sa.subscribe(request, login, permissionMap);
		
		return response;
	}
    
	public RenewResponse Renew(Renew request)
	    throws AAAFaultMessage, ResourceUnknownFault, UnacceptableTerminationTimeFault{
	    String login = this.checkUser();
        HashMap<String, String> permissionMap = new HashMap<String, String>();
		
		/* Get authorizations */
		Session aaa = this.core.getAAASession();
		aaa.beginTransaction();
		UserManager.AuthValue modifyAuthVal = this.userMgr.checkAccess(login, "Subscriptions", "modify");
        if (modifyAuthVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to modify subscriptions.");
        }else if (modifyAuthVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("modifyLoginConstraint", login);
        }
        
		//TODO: Don't assume subscriber and consumer are the same
		UserManager.AuthValue notifyAuthVal = this.userMgr.checkAccess(login, "Notifications", "query");
        if (notifyAuthVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to view notifications.");
        }
        if (notifyAuthVal.equals(AuthValue.MYSITE)) {
            String institution = this.userMgr.getInstitution(login);
            permissionMap.put("institution", institution);
        } else if (notifyAuthVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("loginConstraint", login);
        }
        aaa.getTransaction().commit();
		
		RenewResponse response = this.sa.renew(request, permissionMap);
		
		return response;
	}

	public UnsubscribeResponse Unsubscribe(Unsubscribe request)
	    throws AAAFaultMessage, ResourceUnknownFault, UnableToDestroySubscriptionFault{
		String login = this.checkUser();
        HashMap<String, String> permissionMap = new HashMap<String, String>();
		
		/* Get authorizations */
		Session aaa = this.core.getAAASession();
		aaa.beginTransaction();
		UserManager.AuthValue modifyAuthVal = this.userMgr.checkAccess(login, "Subscriptions", "modify");
        if (modifyAuthVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to modify subscriptions.");
        }else if (modifyAuthVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("modifyLoginConstraint", login);
        }
        
        UnsubscribeResponse response = this.sa.unsubscribe(request, permissionMap);
        
        return response;
	}

	public PauseSubscriptionResponse PauseSubscription(
	       PauseSubscription request) throws AAAFaultMessage, PauseFailedFault, ResourceUnknownFault{
		String login = this.checkUser();
        HashMap<String, String> permissionMap = new HashMap<String, String>();
		
		/* Get authorizations */
		Session aaa = this.core.getAAASession();
		aaa.beginTransaction();
		UserManager.AuthValue modifyAuthVal = this.userMgr.checkAccess(login, "Subscriptions", "modify");
        if (modifyAuthVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to modify subscriptions.");
        }else if (modifyAuthVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("modifyLoginConstraint", login);
        }
        
        PauseSubscriptionResponse response = this.sa.pause(request, permissionMap);
        
        return response;
	}

	public ResumeSubscriptionResponse ResumeSubscription(
	       ResumeSubscription request)
	       throws AAAFaultMessage, ResourceUnknownFault, ResumeFailedFault{
		String login = this.checkUser();
        HashMap<String, String> permissionMap = new HashMap<String, String>();
		
		/* Get authorizations */
		Session aaa = this.core.getAAASession();
		aaa.beginTransaction();
		UserManager.AuthValue modifyAuthVal = this.userMgr.checkAccess(login, "Subscriptions", "modify");
        if (modifyAuthVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to modify subscriptions.");
        }else if (modifyAuthVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("modifyLoginConstraint", login);
        }
        
        ResumeSubscriptionResponse response = this.sa.resume(request, permissionMap);
        
        return response;
	}
	
	public RegisterPublisherResponse RegisterPublisher(RegisterPublisher request)
            throws ResourceUnknownFault, TopicNotSupportedFault,InvalidTopicExpressionFault,
                PublisherRegistrationFailedFault,
                UnacceptableInitialTerminationTimeFault,
                PublisherRegistrationRejectedFault{
                
        String login = "";
        try{
            login = this.checkUser();
        }catch(AAAFaultMessage e){
            throw new PublisherRegistrationRejectedFault(e.getMessage());
        }
		/* Get authorizations */
		Session aaa = this.core.getAAASession();
		aaa.beginTransaction();
		UserManager.AuthValue authVal = this.userMgr.checkAccess(login, "PublisherRegistrations", "create");
        if (authVal.equals(AuthValue.DENIED)) {
            throw new PublisherRegistrationRejectedFault("You do not have permission to publish notifications.");
        }
        aaa.getTransaction().commit();
		
		/* Build subscription */
		RegisterPublisherResponse response = this.sa.registerPublisher(request, login);
		
		return response;
    }

    public DestroyRegistrationResponse DestroyRegistration(DestroyRegistration request)
            throws AAAFaultMessage, ResourceNotDestroyedFault, ResourceUnknownFault{
        String login = this.checkUser();
        HashMap<String, String> permissionMap = new HashMap<String, String>();
		
		/* Get authorizations */
		Session aaa = this.core.getAAASession();
		aaa.beginTransaction();
		UserManager.AuthValue modifyAuthVal = this.userMgr.checkAccess(login, "PublisherRegistrations", "modify");
        if (modifyAuthVal.equals(AuthValue.DENIED)) {
            throw new AAAFaultMessage("You do not have permission to modify subscriptions.");
        }else if (modifyAuthVal.equals(AuthValue.SELFONLY)){
            permissionMap.put("modifyLoginConstraint", login);
        }
        
        DestroyRegistrationResponse response = this.sa.destroyRegistration(request, permissionMap);
        
        return response;
    }
    
	/**
	 * COPIED FROM net.es.oscars.oscars.OSCARSSkeleton
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
     *  COPIED FROM net.es.oscars.oscars.OSCARSSkeleton
     *  Called from each of the messages to check that the user who signed the
     *  message is entered in the user table.
     *  Also checks to see if there was a certificate in the message, which
     *  should never happen unless the axis2/rampart configuration is incorrect.
     *
     * @return login A string with the login associated with the certSubject
     * @throws AAAFaultMessage
     */
    public String checkUser() throws AAAFaultMessage {

        String login = null;
        String[] dnElems = null;
        setOperationContext();

        if (this.certSubject == null){
            this.log.error("checkUser: no certSubject found in message");
            AAAFaultMessage AAAErrorEx = new AAAFaultMessage(
                                 "checkUser: no certSubject found in message");
            throw AAAErrorEx;
        }

        Session aaa = this.core.getAAASession();
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
        return login;
    }
    
    public UserManager getUserManager() { return this.userMgr; }

    public void setCertSubject(Principal DN) { this.certSubject = DN; }

    public void setCertIssuer(Principal DN) { this.certIssuer = DN; }

}
