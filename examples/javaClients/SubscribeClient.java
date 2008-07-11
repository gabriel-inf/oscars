import net.es.oscars.notify.ws.*;
import net.es.oscars.wsdlTypes.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axis2.databinding.types.URI;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import net.es.oscars.client.security.KeyManagement;

public class SubscribeClient{
    private  OSCARSNotifyStub stub;
    private ConfigurationContext configContext;
    
    public void setUp(boolean useKeyStore, String url, String repo) 
                        throws AxisFault {

        if (useKeyStore) { KeyManagement.setKeyStore(repo); }
        this.configContext =
                ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repo, null);
        this.stub = new OSCARSNotifyStub(this.configContext, url);
        ServiceClient sc = this.stub._getServiceClient();
        Options opts = sc.getOptions();
        opts.setTimeOutInMilliSeconds(300000); // set to 5 minutes
        sc.setOptions(opts);
        this.stub._setServiceClient(sc);
    }
    
    
    public void subscribe(){
        String notifConUrl = "http://anna-lab3.internet2.edu:8080/axis2/services/OSCARSNotify";
        String msgContentFilterDialect = "http://oscars.es.net/OSCARS";
        
        try{
            Subscribe subscribe = new Subscribe();
            
            EndpointReferenceType conRef = new EndpointReferenceType();
            AttributedURIType conAttrUri = new AttributedURIType();
            URI conRefUri = new URI(notifConUrl);
            conAttrUri.setAnyURI(conRefUri);
            conRef.setAddress(conAttrUri);
            subscribe.setConsumerReference(conRef);
            
            FilterType filter = new FilterType();
            TopicExpressionType topicExpr = new TopicExpressionType();
            topicExpr.setString("idc:INFO");
            URI topicDialectUri = new URI("http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple");
            topicExpr.setDialect(topicDialectUri);
            filter.setTopicExpression(topicExpr);
            QueryExpressionType queryExpr = new QueryExpressionType();
            queryExpr.addResStatus("PENDING");
            URI msgDialectUri = new URI(msgContentFilterDialect);
            queryExpr.setDialect(msgDialectUri);
            filter.setMessageContent(queryExpr);
            subscribe.setFilter(filter);
            
            //Don't need to set, server will calc its own value
            //subscribe.setInitialTerminationTime("2008-07-10T10:30Z");
            
            SubscribeResponse subscription = stub.Subscribe(subscribe);
            EndpointReferenceType subRef = subscription.getSubscriptionReference();
            Calendar curTime = subscription.getCurrentTime();
            Calendar termTime = subscription.getTerminationTime();
            String DATE_FORMAT = "yyyy-MM-dd hh:mma z";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            
            System.out.println("Subscription Manager: " + subRef.getAddress());
            System.out.println("Subscription Id: " + 
                subRef.getReferenceParameters().getSubscriptionId());
            if(curTime != null){
                System.out.println("Current Time: " + 
                    sdf.format(curTime.getTime()));
            }
            if(termTime != null){
                System.out.println("Termination Time: " + 
                    sdf.format(termTime.getTime()));
            }
        }catch(RemoteException e){
            System.err.println(e);
        }catch(MalformedURIException e){
            System.err.println(e);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        String url = "http://anna-lab3.internet2.edu:8080/axis2/services/OSCARSNotify";
        SubscribeClient client = new SubscribeClient();
        try{
            client.setUp(true, url, "repo");
            client.subscribe();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}