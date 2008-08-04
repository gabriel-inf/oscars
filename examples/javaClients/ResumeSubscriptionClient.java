import net.es.oscars.notify.ws.*;
import net.es.oscars.wsdlTypes.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.w3.www._2005._08.addressing.*;
import java.rmi.RemoteException;
import net.es.oscars.client.*;
import org.apache.axis2.AxisFault;
import java.util.HashMap;

public class ResumeSubscriptionClient extends Client{
    private String url;
    private String repo;
    
    public ResumeSubscription readArgs(String[] args) throws AxisFault{
        ResumeSubscription resume = new ResumeSubscription();
        EndpointReferenceType subRef = null;
        String subId = "";
        HashMap<String, Boolean> requiredFields = new HashMap<String, Boolean>();
        requiredFields.put("url", false);
        requiredFields.put("repo", false);
        requiredFields.put("id", false);
        
        /* Set request parameters */
        try{
            for(int i = 0; i < args.length; i++){
                if(args[i].equals("-url")){
                    this.url = args[i+1];
                    requiredFields.put("url", true);
                }else if(args[i].equals("-repo")){
                    this.repo = args[i+1];
                    requiredFields.put("repo", true);
                }else if(args[i].equals("-id")){
                    subId = args[i+1];
                    requiredFields.put("id", true);
                }
            }
            subRef = this.generateEndpointReference(this.url, subId);
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
        resume.setSubscriptionReference(subRef);
        
        return resume;
    }
    
    public void printHelp(){
		 System.out.println("Parameters:");
		 System.out.println("\t-help\t\t displays this message.");
		 System.out.println("\t-url\t\t required. the url of the IDC's notification broker. (i.e. https://example.org:8443/axis2/services/OSCARSNotify)");
		 System.out.println("\t-repo\t\t required. the location of the repo directory. (i.e. repo)");
		 System.out.println("\t-id\t required. the id of the subscription to resume. (i.e. urn:uuid:14c88353-df91-44f7-9193-88fce3ae7ba2");
	}
	
    public String getUrl(){
        return url;
    }
     
    public String getRepo(){
        return repo;
    }

    public static void main(String[] args){
        ResumeSubscriptionClient client = new ResumeSubscriptionClient();
        try{
            ResumeSubscription request = client.readArgs(args);
            ResumeSubscriptionResponse response = client.resumeSubscription(request);
            EndpointReferenceType subRef = response.getSubscriptionReference();
            System.out.println("Subscription Manager: " + subRef.getAddress());
            System.out.println("Subscription Id: " + 
                subRef.getReferenceParameters().getSubscriptionId());
            System.out.println("Subscription resumed.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}