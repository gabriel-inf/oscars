package net.es.oscars.notifybroker.ws;

import java.util.*;
import java.rmi.RemoteException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import net.es.oscars.rmi.aaa.AaaRmiClient;
import net.es.oscars.rmi.notifybroker.NBValidator;

import org.apache.axis2.context.*;
import org.apache.axiom.om.OMElement;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import org.apache.log4j.*;

import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import org.w3.www._2005._08.addressing.EndpointReferenceType;

/**
 *  OSCARSNotifySkeleton java skeleton for the axisService
 */
public class OSCARSNotifySkeleton implements OSCARSNotifySkeletonInterface{
    private Logger log;
    private Principal certIssuer;
    private Principal certSubject;
    private SubscriptionAdapter sa;
    
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
        this.sa =  new SubscriptionAdapter();
        this.log.info("OSCARSNotify init.end");
    }

    public void Notify(Notify request){
        return;
    }

    public SubscribeResponse Subscribe(Subscribe request)
           throws AAAFaultMessage, InvalidFilterFault, InvalidMessageContentExpressionFault,
                  InvalidTopicExpressionFault, InvalidProducerPropertiesExpressionFault,
                  NotifyMessageNotSupportedFault, ResourceUnknownFault, SubscribeCreationFailedFault,
                  TopicExpressionDialectUnknownFault, TopicNotSupportedFault,
                  UnacceptableInitialTerminationTimeFault, UnrecognizedPolicyRequestFault,
                  UnsupportedPolicyRequestFault{
        SubscribeResponse response = null;
        try {
            String login = this.verifyCert();
            response = this.sa.subscribe(request, login);
        } catch (RemoteException e) {
            this.log.debug(e);
            throw new SubscribeCreationFailedFault(e.getMessage());
        }
        
        return response;
    }

    public RenewResponse Renew(Renew request)
        throws AAAFaultMessage, ResourceUnknownFault, UnacceptableTerminationTimeFault{
        
        RenewResponse response = null;
        try {
            String login = this.verifyCert();
            response = this.sa.renew(request, login);
        } catch (RemoteException e) {
            this.log.debug(e);
            throw new ResourceUnknownFault(e.getMessage());
        }
        
        return response;
    }

    public UnsubscribeResponse Unsubscribe(Unsubscribe request)
        throws AAAFaultMessage, ResourceUnknownFault, UnableToDestroySubscriptionFault{
        UnsubscribeResponse response = null;
        try {
            String login = this.verifyCert();
            response = this.sa.unsubscribe(request, login);
        } catch (RemoteException e) {
            this.log.debug(e);
            throw new ResourceUnknownFault(e.getMessage());
        }
        
        return response;
    }

    public PauseSubscriptionResponse PauseSubscription(
           PauseSubscription request) throws AAAFaultMessage, PauseFailedFault, ResourceUnknownFault{
        return null;
    }

    public ResumeSubscriptionResponse ResumeSubscription(
           ResumeSubscription request)
           throws AAAFaultMessage, ResourceUnknownFault, ResumeFailedFault{
        return null;
    }

    public RegisterPublisherResponse RegisterPublisher(RegisterPublisher request)
            throws ResourceUnknownFault, TopicNotSupportedFault,InvalidTopicExpressionFault,
                PublisherRegistrationFailedFault,
                UnacceptableInitialTerminationTimeFault,
                PublisherRegistrationRejectedFault{

        RegisterPublisherResponse response = null;
        try {
            String login = this.verifyCert();
            response = this.sa.registerPublisher(request, login);
        } catch (RemoteException e) {
            this.log.debug(e);
            throw new PublisherRegistrationFailedFault(e.getMessage());
        }

        return response;
    }

    public DestroyRegistrationResponse DestroyRegistration(DestroyRegistration request)
            throws AAAFaultMessage, ResourceNotDestroyedFault, ResourceUnknownFault{
        DestroyRegistrationResponse response = null;
        try {
            String login = this.verifyCert();
            this.log.info("DestroyRegistration.start");
            response = this.sa.destroyRegistration(request, login);
        } catch (RemoteException e) {
            this.log.debug(e);
            throw new ResourceNotDestroyedFault(e.getMessage());
        }

        return response;
    }
    
    public String verifyCert() throws RemoteException{
        this.setOperationContext();
        String dn = this.certSubject.getName();
        this.log.debug("Received message from user " + dn);
        AaaRmiClient aaaRmiClient = NBValidator.createAaaRmiClient(log);
        
        String login = null;
        try{
            login = aaaRmiClient.verifyDN(dn);
        }catch(RemoteException e){
            throw new RemoteException("Unable to authenticate user " + dn);
        }
        this.log.debug("Found account " + login + " for " + dn);
        
        return login;
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
                }
            }

            }
        } catch (Exception e) {
            this.log.error("setOperationContext.exception: " + e.getMessage());
        }
        this.log.debug("setOperationContext.finish");
    }


    public void setCertSubject(Principal DN) { this.certSubject = DN; }

    public void setCertIssuer(Principal DN) { this.certIssuer = DN; }

}
