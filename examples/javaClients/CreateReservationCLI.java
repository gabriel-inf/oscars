import java.rmi.RemoteException;

import net.es.oscars.client.Client;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.wsdlTypes.*;

import org.apache.axis2.AxisFault;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;


public class CreateReservationCLI {
	private String url;
	private String repo;
	
	 public ResCreateContent readArgs(String[] args){
	 	ResCreateContent request = new ResCreateContent();
	 	PathInfo pathInfo = new PathInfo(); 
        Layer2Info layer2Info = null;
        Layer3Info layer3Info = null;
        MplsInfo mplsInfo = null;
        VlanTag vtag1 = new VlanTag();
		VlanTag vtag2 = new VlanTag();
		
		/* Initialize vtags to default values */
		vtag1.setTagged(true);
		vtag1.setString("any");
		vtag2.setTagged(true);
		vtag2.setString("any");
		
		if(args.length < 10 && (args.length != 1 && args[0].equals("-help"))){
			System.out.println("Invalid paramter count.");
			this.printHelp();
			System.exit(0);
		}
		
        /* Set request parameters */
		try{
	        for(int i = 0; i < args.length; i++){
	        	if(args[i].equals("-url")){
	        		this.url = args[i+1];
	        	}else if(args[i].equals("-repo")){
	        		this.repo = args[i+1];
	        	}else if(args[i].equals("-l2source")){
	        		if(layer2Info == null){layer2Info = new Layer2Info();}
	        		layer2Info.setSrcEndpoint(args[i+1]);
	        	}else if(args[i].equals("-l2dest")){
	        		if(layer2Info == null){layer2Info = new Layer2Info();}
	        		layer2Info.setDestEndpoint(args[i+1]);
	        	}else if(args[i].equals("-l3source")){
	        		if(layer3Info == null){layer3Info = new Layer3Info();}
	        		layer3Info.setSrcHost(args[i+1]);
	        	}else if(args[i].equals("-protocol")){
	        		if(layer3Info == null){layer3Info = new Layer3Info();}
	        		layer3Info.setProtocol(args[i+1]);
	        	}else if(args[i].equals("-sourceport")){
	        		if(layer3Info == null){layer3Info = new Layer3Info();}
	        		layer3Info.setSrcIpPort(Integer.parseInt(args[i+1]));
	        	}else if(args[i].equals("-destport")){
	        		if(layer3Info == null){layer3Info = new Layer3Info();}
	        		layer3Info.setDestIpPort(Integer.parseInt(args[i+1]));
	        	}else if(args[i].equals("-dscp")){
	        		if(layer3Info == null){layer3Info = new Layer3Info();}
	        		layer3Info.setDscp(args[i+1]);
	        	}else if(args[i].equals("-burstlimit")){
	        		if(mplsInfo == null){mplsInfo = new MplsInfo();}
	        		mplsInfo.setBurstLimit(Integer.parseInt(args[i+1]));
	        	}else if(args[i].equals("-lspclass")){
	        		if(mplsInfo == null){mplsInfo = new MplsInfo();}
	        		mplsInfo.setLspClass(args[i+1]);
	        	}else if(args[i].equals("-l3dest")){
	        		if(layer3Info == null){layer3Info = new Layer3Info();}
	        		layer3Info.setDestHost(args[i+1]);
	        	}else if(args[i].equals("-pathsetup")){
	        		pathInfo.setPathSetupMode(args[i+1]); 
	        	}else if(args[i].equals("-start")){
	        		long start = Long.parseLong(args[i+1]);
	        		request.setStartTime(start); 
	        	}else if(args[i].equals("-end")){
	        		long end = Long.parseLong(args[i+1]);
	        		request.setEndTime(end); 
	        	}else if(args[i].equals("-bwidth")){
	        		int bw = Integer.parseInt(args[i+1]);
	        		request.setBandwidth(bw);
	        	}else if(args[i].equals("-desc")){
	        		request.setDescription(args[i+1]);
	        	}else if(args[i].equals("-vlan")){
	        		if(layer2Info == null){layer2Info = new Layer2Info();}
	        		vtag1.setString(args[i+1]);
	        		vtag2.setString(args[i+1]);
	        	}else if(args[i].equals("-tagSource")){
	        		if(layer2Info == null){layer2Info = new Layer2Info();}
	        		if(args[i+1].equals("1")){
	        			vtag1.setTagged(true);
	        			vtag2.setTagged(true);
	        		}else{
	        			vtag1.setTagged(false);
	        			vtag2.setTagged(false);
	        		}
	        	}else if(args[i].equals("-tagDest")){
	        		if(layer2Info == null){layer2Info = new Layer2Info();}
	        		if(args[i+1].equals("1")){
	        			vtag1.setTagged(true);
	        			vtag2.setTagged(true);
	        		}else{
	        			vtag1.setTagged(false);
	        			vtag2.setTagged(false);
	        		}
	        	}else if(args[i].equals("-path")){
	        		CtrlPlanePathContent path = new CtrlPlanePathContent();
	        		String[] hops = args[i+1].split(",");
	        		for(String hop: hops){
	        			CtrlPlaneHopContent newHop = new CtrlPlaneHopContent();
	        			newHop.setLinkIdRef(hop);
	        			path.addHop(newHop);
	        		}
	        		pathInfo.setPath(path);
	        	}else if(args[i].equals("-help")){
	        		this.printHelp();
	        		System.exit(0);
	        	}
	        }
		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
			this.printHelp();
		}
        
        if(layer2Info != null){
        	layer2Info.setSrcVtag(vtag1);
        	layer2Info.setDestVtag(vtag2);
        	pathInfo.setLayer2Info(layer2Info);
        }
        if(layer3Info != null){
        	pathInfo.setLayer3Info(layer3Info);
        }
        if(mplsInfo != null){
        	pathInfo.setMplsInfo(mplsInfo);
        }
        request.setPathInfo(pathInfo); 
        
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
		 System.out.println("\t-pathsetup\t required. the method used to setup the reserved path. (signal-xml or timer-automatic)");
		 System.out.println("\t-start\t required. the start time of this reservation as a UNIX timestamp");
		 System.out.println("\t-end\t required. the end time of this reservation as a UNIX timestamp");
		 System.out.println("\t-bwidth\t required. the amount of bandwidth for this reservation.");
		 System.out.println("\t-desc\t required. a brief description of this reservation.");
		 System.out.println("\t-path\t optional. comma separated list of hops in a path");
		 System.out.println();
		 System.out.println("Layer 2 Specific Parameters");
		 System.out.println("\t-l2source\t required for L2. the fully-qualified ID of the reservation source");
		 System.out.println("\t-l2dest\t required for L2. the fully-qualified ID of the reservation destination");
		 System.out.println("\t-vlan\t optional. VLAN tag to use for request. May be 'any' or an int between 2-4095. Defaults to 'any'");
		 System.out.println("\t-tagSource\t optional. 1 if source interface should use a tagged VLAN, otherwise untagged. Defaults to 1.");
		 System.out.println("\t-tagDest\t optional. 1 if destination interface should use a tagged VLAN, otherwise untagged. Defaults to 1.");
		 System.out.println();
		 System.out.println("Layer 3 Specific Parameters");
		 System.out.println("\t-l3source\t required for L3. the source hostname, IP, or fully-qualified name");
		 System.out.println("\t-l3dest\t required for L3. the destination hostname, IP, or fully-qualified name");
		 System.out.println("\t-protocol\t optional. the transport protocol to be used (TCP or UDP)");
		 System.out.println("\t-srcport\t optional. the transport port to be used for the source");
		 System.out.println("\t-destport\t optional. the transport port to be used for the destination");
		 System.out.println("\t-dscp\t optional. the differentiated services code point (DSCP) value");
		 System.out.println();
		 System.out.println("MPLS Specific Parameters");
		 System.out.println("\t-burtlimit\t optional. the burst limit for this reservation");
		 System.out.println("\t-lspclass\t optional. the class to be used for the requested lsp");
	 }
	 
	 public static void main(String[] args){ 
	        /* Initialize Values */ 
		 	CreateReservationCLI cli = new CreateReservationCLI();
	        Client oscarsClient = new Client(); 
	        ResCreateContent request = cli.readArgs(args); 
	        String url = cli.getUrl(); 
	        String repo = cli.getRepo(); 
	        
	        try { 
	            /* Initialize client instance */ 
	            oscarsClient.setUp(true, url, repo); 
	             
	            /* Send Request */ 
	            CreateReply response = oscarsClient.createReservation(request); 
	             
	            /* Print repsponse information */ 
	            System.out.println("GRI: " + response.getGlobalReservationId()); 
	            System.out.println("Status: " + response.getStatus()); 
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
