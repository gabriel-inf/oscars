import net.es.oscars.client.*;
import net.es.oscars.wsdlTypes.*;
import java.rmi.RemoteException;
import net.es.oscars.oscars.*;
import org.ogf.schema.network.topology.ctrlplane.*;

public class GetNetworkTopologyClient extends ExampleClient{
    public static void main(String[] args){
    
        GetNetworkTopologyClient topoClient = new GetNetworkTopologyClient();
        
        topoClient.init(args, true);
        GetTopologyContent request = new GetTopologyContent();
        request.setTopologyType("all");
        try{
            GetTopologyResponseContent response = topoClient.getClient().getNetworkTopology(request);
            
            CtrlPlaneDomainContent[] domains = response.getTopology().getDomain();
			
			/* Output topology in response */
			System.out.println("Topology: ");
			for(CtrlPlaneDomainContent d : domains){
				System.out.println("Domain: " + d.getId());
				CtrlPlaneNodeContent[] nodes = d.getNode();
				for(CtrlPlaneNodeContent n : nodes){
					System.out.println("\tNode:" + n.getId());
					CtrlPlanePortContent[] ports = n.getPort();
					for(CtrlPlanePortContent p : ports){
						System.out.println("\t\tPort: " + p.getId());
						CtrlPlaneLinkContent[] links = p.getLink();
						if(links != null){
							for(CtrlPlaneLinkContent l : links){
								System.out.println("\t\t\tLink:" + l.getId());
							}
						}
					}
				}
				System.out.println();
		    }
        }catch(AAAFaultMessage e){
            System.out.println("AAA Error: " + e.getMessage());
        }catch(BSSFaultMessage e){
             System.out.println("BSS Error: " + e.getMessage());
        }catch(RemoteException e){
             System.out.println("Remote Error: " + e.getMessage());
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        
        
    }
}