package net.es.oscars.client.improved.topo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;

import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ImprovedClient;
import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.wsdlTypes.GetTopologyContent;
import net.es.oscars.wsdlTypes.GetTopologyResponseContent;

public class GetTopoClient extends ImprovedClient {

    public static final String DEFAULT_CONFIG_FILE = "topo.yaml";
    private GetTopologyResponseContent response = null;

    public void configure() {
    }



    public GetTopologyResponseContent getTopology() {
        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
            GetTopologyContent request = new GetTopologyContent();
            request.setTopologyType("all");
            response = oscarsClient.getNetworkTopology(request);
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
        return response;
    }

    public void print() {

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
    }

    public void setResponse(GetTopologyResponseContent response) {
        this.response = response;
    }

    public GetTopologyResponseContent getResponse() {
        return response;
    }



}
