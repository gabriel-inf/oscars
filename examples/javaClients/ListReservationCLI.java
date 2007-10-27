import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import net.es.oscars.client.Client;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.wsdlTypes.Layer2Info;
import net.es.oscars.wsdlTypes.Layer3Info;
import net.es.oscars.wsdlTypes.ListReply;
import net.es.oscars.wsdlTypes.MplsInfo;
import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.wsdlTypes.ResDetails;


public class ListReservationCLI {
	private String url;
	private String repo;
	private boolean onlyActive;
	
	public void readArgs(String[] args){
		/* Set request parameters */
		this.onlyActive = false;
		try{
	        for(int i = 0; i < args.length; i++){
	        	if(args[i].equals("-url")){
	        		this.url = args[i+1];
	        	}else if(args[i].equals("-repo")){
	        		this.repo = args[i+1];
	        	}else if(args[i].equals("-active")){
	        		this.onlyActive = true;
	        	}else if(args[i].equals("-help")){
	        		this.printHelp();
	        		System.exit(0);
	        	}
	        }
		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
			this.printHelp();
		}
		
		if(this.url==null || this.repo==null){
			this.printHelp();
			System.exit(0);
		}
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getRepo(){
		return repo;
	}
	
	public void printResDetails(ResDetails response){
		 PathInfo pathInfo = response.getPathInfo();
         CtrlPlanePathContent path = pathInfo.getPath();
         Layer2Info layer2Info = pathInfo.getLayer2Info();
         Layer3Info layer3Info = pathInfo.getLayer3Info();
         MplsInfo mplsInfo = pathInfo.getMplsInfo();
         
         if(this.onlyActive && (!response.getStatus().equals("ACTIVE"))){
        	 return;
         }
         
         /* Print repsponse information */ 
         System.out.println("GRI: " + response.getGlobalReservationId()); 
         System.out.println("Login: " + response.getLogin()); 
         System.out.println("Status: " + response.getStatus()); 
         System.out.println("Start Time: " + response.getStartTime()); 
         System.out.println("End Time: " + response.getEndTime()); 
         System.out.println("Time of request: " + response.getCreateTime());
         System.out.println("Bandwidth: " + response.getBandwidth());
         System.out.println("Description: " + response.getDescription());
         System.out.println("Path Setup Mode: " + pathInfo.getPathSetupMode());
         if(layer2Info != null){
         	System.out.println("Source Endpoint: " + layer2Info.getSrcEndpoint());
         	System.out.println("Destination Endpoint: " + layer2Info.getDestEndpoint());
         	System.out.println("Source VLAN: " + layer2Info.getSrcVtag());
         	System.out.println("Destination VLAN: " + layer2Info.getDestVtag());
         }
         if(layer3Info != null){
         	System.out.println("Source Host: " + layer3Info.getSrcHost());
         	System.out.println("Destination Host: " + layer3Info.getDestHost());
         	System.out.println("Source L4 Port: " + layer3Info.getSrcIpPort());
         	System.out.println("Destination L4 Port: " + layer3Info.getDestIpPort());
         	System.out.println("Protocol: " + layer3Info.getProtocol());
         	System.out.println("DSCP: " + layer3Info.getDscp());
         }
         if(mplsInfo != null){
         	System.out.println("Burst Limit: " + mplsInfo.getBurstLimit());
         	System.out.println("LSP Class: " + mplsInfo.getLspClass());
         }
         System.out.println("Path: ");
         for (CtrlPlaneHopContent hop : path.getHop()){
         	System.out.println("\t" + hop.getLinkIdRef());
         }
	}
	public void printHelp(){
		 System.out.println("General Parameters:");
		 System.out.println("\t-help\t displays this message.");
		 System.out.println("\t-url\t required. the url of the IDC.");
		 System.out.println("\t-repo\t required. the location of the repo directory");
		 System.out.println("\t-active\t optional. indicates that only active reservations should be printed");
	}
	
	public static void main(String[] args){ 
        /* Initialize Values */ 
	 	ListReservationCLI cli = new ListReservationCLI();
        Client oscarsClient = new Client(); 
        cli.readArgs(args);
        String url = cli.getUrl(); 
        String repo = cli.getRepo(); 

        /* Initialize client instance */ 
        try {
			oscarsClient.setUp(true, url, repo);
			/* Send Request */ 
            ListReply response = oscarsClient.listReservations();
            ResDetails[] details = response.getResDetails();
            
            for(int i = 0; i < details.length; i++){
            	cli.printResDetails(details[i]);
            }
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
