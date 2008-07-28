import net.es.oscars.notify.ws.*;
import net.es.oscars.wsdlTypes.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.ADBException;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;

public class NotifyClient{
    public static void main(String[] args){
        String url = "http://anna-lab3.internet2.edu:8080/axis2/services/OSCARSNotify";
        String subMgrUrl = "http://anna-lab3.internet2.edu:8080/axis2/services/OSCARSNotify";
        String notifProdUrl = "http://anna-lab3.internet2.edu:8080/axis2/services/OSCARSNotify";
        String subscriptionId = "dcn.internet2.edu-1";
        
        try{
            OSCARSNotifyStub stub = new OSCARSNotifyStub(url);
            Notify notification = new Notify();
            NotificationMessageHolderType msgHolder = new NotificationMessageHolderType();
            
            EndpointReferenceType subRef = new EndpointReferenceType();
            AttributedURIType subAttrUri = new AttributedURIType();
            URI subRefUri = new URI(subMgrUrl);
            subAttrUri.setAnyURI(subRefUri);
            subRef.setAddress(subAttrUri);
            //set ReferenceParameters
            ReferenceParametersType subRefParams = new ReferenceParametersType();
            subRefParams.setSubscriptionId(subscriptionId);
            subRef.setReferenceParameters(subRefParams);
            
            TopicExpressionType topicExpr = new TopicExpressionType();
            topicExpr.setString("INFO");
            URI topicDialectUri = new URI("http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple");
            topicExpr.setDialect(topicDialectUri);
            
            EndpointReferenceType prodRef = new EndpointReferenceType();
            AttributedURIType prodAttrUri = new AttributedURIType();
            URI prodRefUri = new URI(notifProdUrl);
            prodAttrUri.setAnyURI(prodRefUri);
            prodRef.setAddress(prodAttrUri);
            
            MessageType msg = new MessageType();
            EventContent event = new EventContent();
            event.setId("event-101");
            event.setType("RESERVATION_CREATE_FAILED");
            PathDetailLevel pathDetailLvl = new PathDetailLevel();
            pathDetailLvl.setString("interdomain");
            pathDetailLvl.setPathIdRef("path-1");
            event.addPathDetailLevel(pathDetailLvl);
            event.setTimestamp(System.currentTimeMillis()/1000L);
            event.setUserLogin("batman");
            event.setErrorCode("AUTHN_FAILED");
            event.setErrorMessage("Identity cannot be determined.");
            
            OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
            OMElement omEvent = event.getOMElement(Event.MY_QNAME, omFactory);
            msg.addExtraElement(omEvent);
            
            msgHolder.setSubscriptionReference(subRef);
            msgHolder.setTopic(topicExpr);
            msgHolder.setProducerReference(prodRef);
            msgHolder.setMessage(msg);
            
            notification.addNotificationMessage(msgHolder);
            stub.Notify(notification);
        }catch(RemoteException e){
            System.err.println(e);
        }catch(MalformedURIException e){
            System.err.println(e);
        }catch(ADBException e){
            System.err.println(e);
        }
    }
}