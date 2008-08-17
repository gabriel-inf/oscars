import net.es.oscars.client.*;
import net.es.oscars.wsdlTypes.*;
import java.rmi.RemoteException;
import net.es.oscars.oscars.*;
import org.ogf.schema.network.topology.ctrlplane.*;

public class InitiateTopologyPullClient extends ExampleClient{
    public static void main(String[] args){
    
        InitiateTopologyPullClient topoClient = new InitiateTopologyPullClient();
        
        topoClient.init(args, true);
        InitiateTopologyPullContent request = new InitiateTopologyPullContent();
        request.setTopologyType("all");
        try{
            InitiateTopologyPullResponseContent response = topoClient.getClient().initiateTopologyPull(request);
			System.out.println("Result: " + response.getResultMsg());
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