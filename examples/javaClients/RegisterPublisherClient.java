import net.es.oscars.notifybroker.ws.*;
import net.es.oscars.wsdlTypes.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import org.w3.www._2005._08.addressing.*;
import java.rmi.RemoteException;
import net.es.oscars.client.*;
import org.apache.axis2.AxisFault;
import java.util.HashMap;
import java.util.GregorianCalendar;

public class RegisterPublisherClient extends Client{
    private String url;
    private String repo;
    
    public RegisterPublisher readArgs(String[] args) throws AxisFault{
        RegisterPublisher request = new RegisterPublisher();
        TopicExpressionType topicExpr = null;
        EndpointReferenceType publisherRef = null;
        HashMap<String, Boolean> requiredFields = new HashMap<String, Boolean>();
        requiredFields.put("url", false);
        requiredFields.put("repo", false);
        requiredFields.put("publisher", false);
        
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
                }else if(args[i].equals("-publisher")){
                    publisherRef = this.generateEndpointReference(args[i+1]);
                    requiredFields.put("publisher", true);
                }else if(args[i].equals("-demand")){
                    request.setDemand(true);
                }else if(args[i].equals("-inittermtime")){
                    long seconds = Long.parseLong(args[i+1]);
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(System.currentTimeMillis() + (seconds*1000));
                    request.setInitialTerminationTime(cal);
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
        request.addTopic(topicExpr);
        request.setPublisherReference(publisherRef);
        
        return request;
    }
    
    public void printHelp(){
		 System.out.println("Parameters:");
		 System.out.println("\t-help\t\t displays this message.");
		 System.out.println("\t-url\t\t required. the url of the IDC's notification broker. (i.e. https://example.org:8443/axis2/services/OSCARSNotify)");
		 System.out.println("\t-repo\t\t required. the location of the repo directory. (i.e. repo)");
		 System.out.println("\t-publisher\t required. the URL that identifies the publisher. (i.e. https://my-local-machine.org/publisher)");
		 System.out.println("\t-topics\t\t optional. Comma delimited list of topics that will be published (i.e. idc:INFO,idc:ERROR)");
		 System.out.println("\t-demand\t optional. if this option is present then the broker should do demand publishing.");
	     System.out.println("\t-inittermtime\t\t optional. the 'initial termination time' that the registration will expire. Specified in seconds (i.e. 3600).");
	}
	
    public String getUrl(){
        return url;
    }
     
    public String getRepo(){
        return repo;
    }

    public static void main(String[] args){
        RegisterPublisherClient client = new RegisterPublisherClient();
        try{
            RegisterPublisher request = client.readArgs(args);
            RegisterPublisherResponse response = client.registerPublisher(request);
            EndpointReferenceType pubRef = response.getPublisherRegistrationReference();
            EndpointReferenceType conRef = response.getConsumerReference();
            
            System.out.println("Publisher Registration Manager: " + pubRef.getAddress());
            System.out.println("Publisher Registration Id: " + 
                pubRef.getReferenceParameters().getPublisherRegistrationId());
            System.out.println("Notification Consumer: " + conRef.getAddress());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
