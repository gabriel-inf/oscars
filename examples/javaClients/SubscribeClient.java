import net.es.oscars.notifybroker.ws.*;
import net.es.oscars.wsdlTypes.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import net.es.oscars.client.*;
import org.apache.axis2.AxisFault;
import java.util.HashMap;

public class SubscribeClient extends Client{
    private String url;
    private String repo;
    
    public Subscribe readArgs(String[] args) throws AxisFault{
        Subscribe subscribe = new Subscribe();
        FilterType filter = new FilterType();
        TopicExpressionType topicExpr = null;
        QueryExpressionType producerProps = null;
        QueryExpressionType msgFilter = null;
        EndpointReferenceType consumerRef = null;
        HashMap<String, Boolean> requiredFields = new HashMap<String, Boolean>();
        requiredFields.put("url", false);
        requiredFields.put("repo", false);
        requiredFields.put("consumer", false);
        requiredFields.put("producer", false);
        
        /* Set request parameters */
        try{
            for(int i = 0; i < args.length; i++){
                if(args[i].equals("-url")){
                    this.url = args[i+1];
                    requiredFields.put("url", true);
                }else if(args[i].equals("-repo")){
                    this.repo = args[i+1];
                    requiredFields.put("repo", true);
                }else if(args[i].equals("-topics")){
                    topicExpr = this.generateTopicExpression(args[i+1].replaceAll(",", "|"));
                }else if(args[i].equals("-consumer")){
                    consumerRef = this.generateEndpointReference(args[i+1]);
                    requiredFields.put("consumer", true);
                }else if(args[i].equals("-producer")){
                    producerProps = this.generateProducerProperties(args[i+1].split(","));
                    requiredFields.put("producer", true);
                }else if(args[i].equals("-message")){
                    msgFilter = this.generateQueryExpression(args[i+1]);
                }else if(args[i].equals("-inittermtime")){
                    String termTime = this.generateDateTime(Long.parseLong(args[i+1]));
                    subscribe.setInitialTerminationTime(termTime);
                }else if(args[i].equals("-help")){
                    this.printHelp();
                    System.exit(0);
                }
            }
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
            this.printHelp();
        }
        
        for(String key : requiredFields.keySet()){
            if(!requiredFields.get(key)){
                System.err.println("Missing required parameter '-" + key + 
                                   "'. Use the -help parameter for the full " +
                                   "list of required options.");
                System.exit(1);
            }
        }
        
        this.setUpNotify(true, this.url, this.repo, null);
        filter.addTopicExpression(topicExpr);
        filter.addProducerProperties(producerProps);
        filter.addMessageContent(msgFilter);
        subscribe.setConsumerReference(consumerRef);
        subscribe.setFilter(filter);
        
        return subscribe;
    }
    
    public void printHelp(){
		 System.out.println("Parameters:");
		 System.out.println("\t-help\t\t displays this message.");
		 System.out.println("\t-url\t\t required. the url of the IDC's notification broker. (i.e. https://example.org:8443/axis2/services/OSCARSNotify)");
		 System.out.println("\t-repo\t\t required. the location of the repo directory. (i.e. repo)");
		 System.out.println("\t-consumer\t required. the URL where notifications should be sent back to you.  (i.e. https://my-local-machine.org/clientListener)");
		 System.out.println("\t-producer\t required. a comma-delimited list of URLs of notification producers (i.e. an IDC) from which you'd like to receive notifications. (i.e. https://example.org:8443/axis2/services/OSCARS)");
		 System.out.println("\t-topics\t\t optional. Comma delimited list of topics to which you'd like to subscribe (i.e. idc:INFO,idc:ERROR)");
		 System.out.println("\t-message\t optional. XPath expression that can be used to further filter notifications received (i.e. /idc:event[idc:gri='example.org-1'])");
	     System.out.println("\t-inittermtime\t\t optional. the 'initial termination time' that the subscriptions will expire. Specified in seconds (i.e. 3600).");
	}
	
    public String getUrl(){
        return url;
    }
     
    public String getRepo(){
        return repo;
    }

    public static void main(String[] args){
        SubscribeClient client = new SubscribeClient();
        try{
            Subscribe request = client.readArgs(args);
            SubscribeResponse subscription = client.subscribe(request);
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
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
