package net.es.oscars.client;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.*;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.PauseSubscription;
import org.oasis_open.docs.wsn.b_2.PauseSubscriptionResponse;
import org.oasis_open.docs.wsn.b_2.ResumeSubscription;
import org.oasis_open.docs.wsn.b_2.ResumeSubscriptionResponse;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.QueryExpressionType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;
import org.oasis_open.docs.wsn.br_2.DestroyRegistration;
import org.oasis_open.docs.wsn.br_2.DestroyRegistrationResponse;
import org.w3.www._2005._08.addressing.*;

import net.es.oscars.oscars.OSCARSStub;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.notify.ws.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.security.KeyManagement;


/**
 * Client handles functionality common to all clients (forwarders, tests,
 * etc.)
 */
public class Client {
    protected Logger log;
    protected ConfigurationContext configContext;
    protected OSCARSStub stub;
    protected OSCARSNotifyStub notifyStub;
    
    /* Constants */
    public static final String WS_TOPIC_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";
    public static final String WS_TOPIC_CONCRETE= "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";
    public static final String WS_TOPIC_FULL = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";
    public static final String XPATH_URI = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    
    public void setUp(boolean useKeyStore, String url, String repo)
            throws AxisFault {
        this.setUp(useKeyStore, url, null, repo, null);
    }

    public void setUp(boolean useKeyStore, String url, String repo,
                      String axisConfig) throws AxisFault {
        this.setUp(useKeyStore, url, null, repo, axisConfig);
    }

    public void setUp(boolean useKeyStore, String url, String notifyUrl, 
                     String repo, String axisConfig) throws AxisFault {

        if (useKeyStore) { KeyManagement.setKeyStore(repo); }
        this.log = Logger.getLogger(this.getClass());
        this.configContext =
                ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repo, axisConfig);

        if(url != null){
            this.stub = new OSCARSStub(this.configContext, url);
            ServiceClient sc = this.stub._getServiceClient();
            Options opts = sc.getOptions();
            opts.setTimeOutInMilliSeconds(300000); // set to 5 minutes
            sc.setOptions(opts);
            this.stub._setServiceClient(sc);
        }
        if(notifyUrl != null){
            this.notifyStub = new OSCARSNotifyStub(this.configContext, notifyUrl);
            ServiceClient sc = this.notifyStub._getServiceClient();
            Options opts = sc.getOptions();
            opts.setTimeOutInMilliSeconds(300000); // set to 5 minutes
            sc.setOptions(opts);
            this.notifyStub._setServiceClient(sc);
        }
    }

    public void setUpNotify(boolean useKeyStore, String url, 
                            String repo, String axisConfig) throws AxisFault {
        this.setUp(useKeyStore, null, url, repo, axisConfig);
    }
    
    /**
     * Terminates the Axis2 ConfigurationContext. You only need to call
     * this if you are running the client in an environment such as 
     * Tomcat to prevent memory leaks.
     */
    public void cleanUp(){
        if(this.configContext == null){
            return;
        }
        try{
            this.configContext.terminate();
        }catch(AxisFault e){
            this.log.warn("Unable to terminate Axis2 configuration context." +
                "There may be a memory leak so you should watch Tomcat's " +
                "thread count. " + e.getMessage());
        }
    }
    
    /**
     * Makes call to server to cancel a reservation.
     *
     * @param gri a GlobalReservationId instance with reservation's unique id
     * @return a string with the reservation's status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public String cancelReservation(GlobalReservationId gri)
            throws AAAFaultMessage, java.rmi.RemoteException,
                  Exception {

        CancelReservation canRes = null;
        CancelReservationResponse rs = new CancelReservationResponse();
        canRes = new CancelReservation();
        canRes.setCancelReservation(gri);
        rs = this.stub.cancelReservation(canRes);
        return rs.getCancelReservationResponse();
    }

    /**
     * Makes call to server to create a reservation.
     *
     * @param resRequest a ResCreateContent with reservation parameters
     * @return crr a CreateReply instance with the reservation's GRI and status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public CreateReply createReservation(ResCreateContent resRequest)
           throws AAAFaultMessage, java.rmi.RemoteException,
                  Exception {

        CreateReply crr =  null;
        CreateReservationResponse resResponse =
            new CreateReservationResponse();
        CreateReservation createRes = new CreateReservation();
        createRes.setCreateReservation(resRequest);
        resResponse = this.stub.createReservation(createRes);
        crr = resResponse.getCreateReservationResponse();
        return crr;
    }

    /**
     * Makes call to server to modify a reservation.
     *
     * @param resRequest a ModifyResContent with reservation parameters
     * @return crr a ModifyResReply instance with the reservation's GRI and status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public ModifyResReply modifyReservation(ModifyResContent resRequest)
           throws AAAFaultMessage, java.rmi.RemoteException,
                  Exception {

        ModifyResReply crr =  null;
        ModifyReservationResponse resResponse = new ModifyReservationResponse();
        ModifyReservation modifyRes = new ModifyReservation();
        modifyRes.setModifyReservation(resRequest);
        resResponse = this.stub.modifyReservation(modifyRes);
        crr = resResponse.getModifyReservationResponse();
        return crr;
    }

    /**
     * Makes call to server to get list of reservations.
     *
     * @param listReq the reservation list request object
     * @return response a ListReply instance with summaries of each reservation
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public ListReply listReservations(ListRequest listReq)
           throws AAAFaultMessage, BSSFaultMessage,java.rmi.RemoteException
    {

        ListReservations listRes = new ListReservations();
        listRes.setListReservations(listReq);
        ListReservationsResponse lrr = this.stub.listReservations(listRes);
        ListReply listReply = lrr.getListReservationsResponse();
        return listReply;
    }

    /**
     * Makes call to server to get a reservation's details, given its GRI.
     *
     * @param gri a GlobalReservationId instance with reservation's unique id
     * @return a ResDetails instance containing the reservations tag and status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
      */
    public ResDetails queryReservation(GlobalReservationId gri)
           throws AAAFaultMessage, java.rmi.RemoteException,
              Exception {

        QueryReservation queryRes = new QueryReservation();
        queryRes.setQueryReservation(gri);
        QueryReservationResponse qrr = this.stub.queryReservation(queryRes);
        ResDetails qreply = qrr.getQueryReservationResponse();
        return qreply;
    }

    /**
     * Makes call to server to get a global view of domain's topology
     *
     * @param request a getNetworkTopology request containing th request type
     * @return a GetNetworkTopologyResponseContent instance containing the topology data
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public GetTopologyResponseContent getNetworkTopology(
        GetTopologyContent request) throws AAAFaultMessage,
        java.rmi.RemoteException, Exception {

        GetNetworkTopology getTopo = new GetNetworkTopology();
        getTopo.setGetNetworkTopology(request);
        GetNetworkTopologyResponse topo = this.stub.getNetworkTopology(getTopo);
        GetTopologyResponseContent response = topo.getGetNetworkTopologyResponse();
        return response;
    }

    /**
     * Tells the server to update its topology
     *
     * @param request a initiateTopologyPull request containing the request type
     * @return a InitiateTopologyPullResponseContent instance containing the result
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public InitiateTopologyPullResponseContent initiateTopologyPull(
           InitiateTopologyPullContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        InitiateTopologyPull initTopoPull = new InitiateTopologyPull();
        initTopoPull.setInitiateTopologyPull(request);
        InitiateTopologyPullResponse initResult =
            this.stub.initiateTopologyPull(initTopoPull);
        InitiateTopologyPullResponseContent response =
            initResult.getInitiateTopologyPullResponse();
        return response;
    }

    /**
     * Signals that a previously made reservation should be created
     *
     * @param request a createPath request containing the reservation to be built
     * @return the response received from the request
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public CreatePathResponseContent createPath(
           CreatePathContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        CreatePath cpath = new CreatePath();
        cpath.setCreatePath(request);
        CreatePathResponse createResult =
            this.stub.createPath(cpath);
        CreatePathResponseContent response =
            createResult.getCreatePathResponse();

        return response;
    }

    /**
     * Verifies that a previously setup path is still active
     *
     * @param request a refreshPath request
     * @return the response received from the request
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public RefreshPathResponseContent refreshPath(
           RefreshPathContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        RefreshPath rpath = new RefreshPath();
        rpath.setRefreshPath(request);
        RefreshPathResponse refreshResult =
            this.stub.refreshPath(rpath);
        RefreshPathResponseContent response =
            refreshResult.getRefreshPathResponse();

        return response;
    }

    /**
     * Tearsdown a previously setup path
     *
     * @param request a teardownPath request
     * @return the response received from the request
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public TeardownPathResponseContent teardownPath(
           TeardownPathContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        TeardownPath tpath = new TeardownPath();
        tpath.setTeardownPath(request);
        TeardownPathResponse teardownResult =
            this.stub.teardownPath(tpath);
        TeardownPathResponseContent response =
            teardownResult.getTeardownPathResponse();

        return response;
    }

    /**
     * Makes call to server to forward a create reservation request to the
     * next domain.
     *
     * @param fwd a Forward object
     * @return response a ForwardReply object
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public ForwardReply forward(Forward fwd)
           throws AAAFaultMessage, BSSFaultMessage,
                  java.rmi.RemoteException {

        ForwardReply freply = null;
        ForwardResponse frsp = null;

        frsp = this.stub.forward(fwd);
        freply = frsp.getForwardResponse();
        return freply;
    }
    
    /**
     * Sends notification of event to a subscriber or NotificationBroker
     *
     * @param msgHolder the notification message to send
     * @throws RemoteException
     */
    public void notify(NotificationMessageHolderType msgHolder) 
                        throws RemoteException{
        Notify notification = new Notify();
        notification.addNotificationMessage(msgHolder);
        this.notifyStub.Notify(notification);
    }
    
    /**
     * Subscribes to notifications that match request parameters from an I
     * DC or other service.
     *
     * @param request the Subscribe message to send
     * @throws AAAFaultMessage
     * @throws InvalidFilterFault
     * @throws InvalidMessageContentExpressionFault
     * @throws InvalidProducerPropertiesExpressionFault
     * @throws InvalidTopicExpressionFault
     * @throws NotifyMessageNotSupportedFault
     * @throws RemoteException
     * @throws ResourceUnknownFault
     * @throws SubscribeCreationFailedFault
     * @throws TopicExpressionDialectUnknownFault
     * @throws TopicNotSupportedFault
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws UnrecognizedPolicyRequestFault
     */
    public SubscribeResponse subscribe(Subscribe request) 
                  throws RemoteException, TopicNotSupportedFault,
                  InvalidTopicExpressionFault, UnsupportedPolicyRequestFault,
                  UnacceptableInitialTerminationTimeFault,
                  InvalidMessageContentExpressionFault,
                  InvalidProducerPropertiesExpressionFault,
                  ResourceUnknownFault,
                  SubscribeCreationFailedFault,
                  TopicExpressionDialectUnknownFault,
                  InvalidFilterFault,NotifyMessageNotSupportedFault,
                  UnrecognizedPolicyRequestFault,
                  net.es.oscars.notify.ws.AAAFaultMessage{
       return this.notifyStub.Subscribe(request);
    }
    
    /**
     * Renews an existing subscription
     *
     * @param request the Renew message to send
     * @return the response of the Renew request
     * @throws AAAFaultMessage
     * @throws RemoteException
     * @throws ResourceUnknownFault
     * @throws UnacceptableTerminationTimeFault
     */
    public RenewResponse renew(Renew request) 
                  throws RemoteException, ResourceUnknownFault,
                  UnacceptableTerminationTimeFault,
                  net.es.oscars.notify.ws.AAAFaultMessage{
       return this.notifyStub.Renew(request);
    }
    
    /**
     * Cancels an existing subscription
     *
     * @param request the Unsubscribe message to send
     * @return the response of the Unsubscribe request
     * @throws AAAFaultMessage
     * @throws RemoteException
     * @throws ResourceUnknownFault
     * @throws UnableToDestroySubscriptionFault
     */
    public UnsubscribeResponse unsubscribe(Unsubscribe request) 
                  throws RemoteException, ResourceUnknownFault,
                  UnableToDestroySubscriptionFault,
                  net.es.oscars.notify.ws.AAAFaultMessage{
       return this.notifyStub.Unsubscribe(request);
    }
    
    /**
     * Pauses an existing suscription
     *
     * @param request the Pause message to send
     * @return the response of the Pause request
     * @throws AAAFaultMessage
     * @throws RemoteException
     * @throws ResourceUnknownFault
     * @throws PauseFailedFault
     */
    public PauseSubscriptionResponse pauseSubscription(PauseSubscription request) 
                  throws RemoteException, ResourceUnknownFault,
                  PauseFailedFault,
                  net.es.oscars.notify.ws.AAAFaultMessage{
       return this.notifyStub.PauseSubscription(request);
    }
    
    /**
     * Pauses a paused suscription
     *
     * @param request the Unsubscribe message to send
     * @return the response of the Unsubscribe request
     * @throws AAAFaultMessage
     * @throws RemoteException
     * @throws ResourceUnknownFault
     * @throws ResumeFailedFault
     */
    public ResumeSubscriptionResponse resumeSubscription(ResumeSubscription request) 
                  throws RemoteException, ResourceUnknownFault,
                  ResumeFailedFault,
                  net.es.oscars.notify.ws.AAAFaultMessage{
       return this.notifyStub.ResumeSubscription(request);
    }
    
    /**
     * Registers a publisher with a notification broker. This should be
     * used by services that produce notifications they would like to share
     * with other groups.
     *
     * @param request the registration message
     * @return the result of the registration
     * @throws InvalidTopicExpressionFault
     * @throws PublisherRegistrationFailedFault
     * @throws PublisherRegistrationRejectedFault
     * @throws RemoteException
     * @throws ResourceUnknownFault
     * @throws TopicNotSupportedFault
     * @throws UnacceptableInitialTerminationTimeFault
     */
    public RegisterPublisherResponse registerPublisher(RegisterPublisher request) 
                  throws TopicNotSupportedFault,
                         InvalidTopicExpressionFault,
                         PublisherRegistrationFailedFault,
                         ResourceUnknownFault,
                         UnacceptableInitialTerminationTimeFault,
                         PublisherRegistrationRejectedFault,
                         RemoteException{
       return this.notifyStub.RegisterPublisher(request);
    }
    
    /**
     * Destroys an existing PublisherRegistration. This should be use by
     * publishers that want to invalid a PublisherRegistration ID prior
     * to its set expiration time. 
     *
     * @param request the request with the id of the registration to destroy
     * @return the result of the DestroyRegistration request
     * @throws RemoteException
     * @throws ResourceNotDestroyedFault
     * @throws ResourceUnknownFault
     */
    public DestroyRegistrationResponse destroyRegistration(DestroyRegistration request) 
                  throws ResourceNotDestroyedFault, ResourceUnknownFault,
                         RemoteException, net.es.oscars.notify.ws.AAAFaultMessage{
       return this.notifyStub.DestroyRegistration(request);
    }
    
    /**
     * Utility function for generating WS-Addressing endpoint references. This
     * function is useful when formatting ConsumerReference in a Subscribe 
     * message.
     *
     * @param address the URI to go in the Address field of the EndpointReference
     * @return the generated Endpoint Reference
     * @throws MalformedURIException
     */
    public EndpointReferenceType generateEndpointReference(String address)
            throws MalformedURIException{
        return this.generateEndpointReference(address, null);
    }
    
    /**
     * Utility function for generating WS-Addressing endpoint references. This
     * function is useful when requests and responses that require an address 
     * and a subscriptionId
     *
     * @param address the URI to go in the Address field of the EndpointReference
     * @param subscriptionId the UUID of a subscription
     * @return the generated Endpoint Reference
     * @throws MalformedURIException
     */
    public EndpointReferenceType generateEndpointReference(
        String address, String subscriptionId) throws MalformedURIException{
        EndpointReferenceType epr = new EndpointReferenceType();
        AttributedURIType attrUri = new AttributedURIType();
        URI refUri = new URI(address);
        attrUri.setAnyURI(refUri);
        epr.setAddress(attrUri);
        //set ReferenceParameters
        if(subscriptionId != null){
            ReferenceParametersType refParams = new ReferenceParametersType();
            refParams.setSubscriptionId(subscriptionId);
            epr.setReferenceParameters(refParams); 
        }
        
        return epr;
    }
    
    /**
     * Utility function for generating a PublisherRegistration EPR.
     *
     * @param address the URI to go in the Address field of the EndpointReference
     * @param pubRegistrationId the UUID of a PublisherRegistration
     * @return the generated Endpoint Reference
     * @throws MalformedURIException
     */
    public EndpointReferenceType generatePublisherRegistrationRef(
        String address, String pubRegistrationId) throws MalformedURIException{
        EndpointReferenceType epr = this.generateEndpointReference(address, null);
        //set ReferenceParameters
        if(pubRegistrationId != null){
            ReferenceParametersType refParams = new ReferenceParametersType();
            refParams.setPublisherRegistrationId(pubRegistrationId);
            epr.setReferenceParameters(refParams); 
        }
        
        return epr;
    }
    
    /** 
     * Utility function for generating a topic expression
     *
     * @param topics a string adhering to the Full expression dialect of WS-Topic
     * @return the generated topic expression
     */
    public TopicExpressionType generateTopicExpression(String topics){
        TopicExpressionType topicExpr = new TopicExpressionType();
        //dialect is a constant so there should be no malformed exception
        try{
            URI topicDialectUri = new URI(Client.WS_TOPIC_FULL);
            topicExpr.setDialect(topicDialectUri);
        }catch(MalformedURIException e){}
        topicExpr.setString(topics);
        return topicExpr;
    }
    
    /** 
     * Utility function for generating a ProducerProperties filter given a
     * list of producer URLs. The list is converted to an XPath OR expression.
     *
     * @param urls a list of URLs that idenitfy producers from which the subscriber would like to receieve notifications
     * @return the generated query
     */
    public QueryExpressionType generateProducerProperties(String[] urls){
        boolean multiple = false;
        String xpath = "";
        for(String url : urls){
            xpath += (multiple ? " or " : "");
            xpath += "/wsa:Address='" + url + "'";
            multiple = true;
        }
        return this.generateQueryExpression(xpath);
    }
    
    /** 
     * Utility function for generating a query expression using XPath
     *
     * @param xpath an Xpath expression used to match producers
     * @return the generated query
     */
    public QueryExpressionType generateQueryExpression(String xpath){
        QueryExpressionType query = new QueryExpressionType();
        //dialect is a constant so there should be no malformed exception
        try{
            URI dialect = new URI(Client.XPATH_URI);
            query.setDialect(dialect);
        }catch(MalformedURIException e){}
        query.setString(xpath);
        
        return query;
    }
    
    /**
     * Generates an xsd:datetime String that is X number of seconds in the future
     * where X is a supplied parameter.
     *
     * @param seconds the number of seconds in the fututre the xsd:datetime should represent
     * @return an xsd:datetime string that is the requeted number of seconds in the future
     *
     */
    public String generateDateTime(long seconds){
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        cal.setTimeInMillis(System.currentTimeMillis() + (seconds*1000));
        return df.format(cal.getTime());
    }
}
