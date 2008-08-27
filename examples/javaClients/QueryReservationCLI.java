import java.rmi.RemoteException;

import net.es.oscars.client.Client;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.wsdlTypes.GlobalReservationId;
import net.es.oscars.wsdlTypes.Layer2Info;
import net.es.oscars.wsdlTypes.Layer3Info;
import net.es.oscars.wsdlTypes.MplsInfo;
import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.wsdlTypes.ResDetails;

import org.apache.axis2.AxisFault;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;


public class QueryReservationCLI {
	private String url;
	private String repo;
	
	public GlobalReservationId readArgs(String[] args){
		GlobalReservationId request = null;
		
		/* Set request parameters */
		try{
	        for(int i = 0; i < args.length; i++){
	        	if(args[i].equals("-url")){
	        		this.url = args[i+1];
	        	}else if(args[i].equals("-repo")){
	        		this.repo = args[i+1];
	        	}else if(args[i].equals("-gri")){
	        		request = new GlobalReservationId();
	        		request.setGri(args[i+1]);
	        	}else if(args[i].equals("-help")){
	        		this.printHelp();
	        		System.exit(0);
	        	}
	        }
		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
			this.printHelp();
		}
		
		if(request == null || this.url==null || this.repo==null){
			this.printHelp();
			System.exit(0);
		}
			
		
		return request;
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getRepo(){
		return repo;
	}
	
	
	public void printHelp(){
		 System.out.println("General Parameters:");
		 System.out.println("\t-help\t displays this message.");
		 System.out.println("\t-url\t required. the url of the IDC.");
		 System.out.println("\t-repo\t required. the location of the repo directory");
		 System.out.println("\t-gri\t required. the GRI of the reservation to query");
	}
	
	public static void main(String[] args){ 
        /* Initialize Values */ 
	 	QueryReservationCLI cli = new QueryReservationCLI();
        Client oscarsClient = new Client(); 
        GlobalReservationId request = cli.readArgs(args); 
        String url = cli.getUrl(); 
        String repo = cli.getRepo(); 
        
        try { 
            /* Initialize client instance */ 
            oscarsClient.setUp(true, url, repo); 
             
            /* Send Request */ 
            ResDetails response = oscarsClient.queryReservation(request);
            PathInfo pathInfo = response.getPathInfo();
            CtrlPlanePathContent path = pathInfo.getPath();
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            Layer3Info layer3Info = pathInfo.getLayer3Info();
            MplsInfo mplsInfo = pathInfo.getMplsInfo();
            
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
            String output ="";
            for (CtrlPlaneHopContent hop : path.getHop()){
            	CtrlPlaneLinkContent link = hop.getLink();
                if(link==null){
                    //should not happen
                    output += "no link";
                    continue;
                }
                output += "\t" + link.getId();
                CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
                CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
                output += ", " + swcap.getEncodingType();
                if("ethernet".equals(swcap.getEncodingType())){
                    output += ", " + swcapInfo.getVlanRangeAvailability();
                }
                output += "\n";
            }
            System.out.print(output);
        } catch (AxisFault e) { 
            e.printStackTrace(); 
        } catch (RemoteException e) { 
            e.printStackTrace(); 
        } catch (AAAFaultMessage e) { 
            e.printStackTrace(); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 
}
