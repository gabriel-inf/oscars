import java.rmi.RemoteException;

import net.es.oscars.client.Client;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.wsdlTypes.GlobalReservationId;

import org.apache.axis2.AxisFault;

public class CancelReservationCLI {
	private String url;
	private String repo;
	private GlobalReservationId gri;
	
	public void readArgs(String[] args){
		/* Set request parameters */
		try{
	        for(int i = 0; i < args.length; i++){
	        	if(args[i].equals("-url")){
	        		this.url = args[i+1];
	        	}else if(args[i].equals("-repo")){
	        		this.repo = args[i+1];
	        	}else if(args[i].equals("-gri")){
	        		this.gri = new GlobalReservationId();
	        		gri.setGri(args[i+1]);
	        	}else if(args[i].equals("-help")){
	        		this.printHelp();
	        		System.exit(0);
	        	}
	        }
		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
			this.printHelp();
		}
		
		if(this.url==null || this.repo==null || this.gri==null){
			this.printHelp();
			System.exit(0);
		}
	}
	
	public String getUrl(){
		return this.url;
	}
	
	public String getRepo(){
		return this.repo;
	}
	
	public GlobalReservationId getGri(){
		return this.gri;
	}
	
	public void printHelp(){
		 System.out.println("General Parameters:");
		 System.out.println("\t-help\t displays this message.");
		 System.out.println("\t-url\t required. the url of the IDC.");
		 System.out.println("\t-repo\t required. the location of the repo directory");
		 System.out.println("\t-gri\t required. the GRI of the reservation to cancel");
	}
	
	public static void main(String[] args){ 
        /* Initialize Values */ 
	 	CancelReservationCLI cli = new CancelReservationCLI();
        Client oscarsClient = new Client(); 
        cli.readArgs(args);
        String url = cli.getUrl(); 
        String repo = cli.getRepo(); 

        /* Initialize client instance */ 
        try {
			oscarsClient.setUp(true, url, repo);
			/* Send Request */ 
            String response = oscarsClient.cancelReservation(cli.getGri());
            System.out.println("STATUS: " + response);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AAAFaultMessage e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
             
            
   }
}
