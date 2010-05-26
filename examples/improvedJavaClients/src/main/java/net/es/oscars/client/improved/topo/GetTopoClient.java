package net.es.oscars.client.improved.topo;

import java.rmi.RemoteException;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;

import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ConfigHelper;
import net.es.oscars.client.improved.ImprovedClient;
import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.wsdlTypes.GetTopologyContent;
import net.es.oscars.wsdlTypes.GetTopologyResponseContent;

public class GetTopoClient extends ImprovedClient {

    public static final String DEFAULT_CONFIG_FILE = "topo.yaml";

    public void configure() {
        if (configFile == null) {
            configFile = DEFAULT_CONFIG_FILE;
        }

        ConfigHelper cfg = ConfigHelper.getInstance();
        config = cfg.getConfiguration(this.configFile);
    }

    public GetTopologyContent formRequest() {
        GetTopologyContent request = new GetTopologyContent();
        request.setTopologyType("all");
        return request;
    }



    public GetTopologyResponseContent performRequest(GetTopologyContent request) {
        Client oscarsClient = new Client();
        GetTopologyResponseContent response = null;
        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
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
        } finally {
            oscarsClient.cleanUp();
        }
        return response;
    }

    public static void print(GetTopologyResponseContent response) {

        CtrlPlaneDomainContent[] domains = response.getTopology().getDomain();
        /* Output topology in response */
        System.out.println("Topology: ");
        for(CtrlPlaneDomainContent d : domains){
            System.out.println("Domain: " + d.getId());
            CtrlPlaneNodeContent[] nodes = d.getNode();
            if (nodes != null) {
                for(CtrlPlaneNodeContent n : nodes){
                    System.out.println("\tNode:" + n.getId());
                    CtrlPlanePortContent[] ports = n.getPort();
                    if (ports != null) {
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
                }
            }
            System.out.println();
        }
    }




}
