import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Date;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;

public class QueryReservationClient extends ExampleClient {
    /**
     * @param args
     *            [0] directory name of the client repository contains
     *            rampart.mar and axis2.xml [1] the default url of the service
     *            endpoint [2] the tag of the reservation to query
     */
    public static void main(String[] args) {
        try {
            QueryReservationClient cl = new QueryReservationClient();
            cl.query(args, true);
        } catch (AAAFaultMessage e1) {
            System.out
                    .println("AAAFaultMessage from queryReservation");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessage e1) {
            System.out
                    .println("BSSFaultMessage from queryReservation");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            System.out
                    .println("RemoteException returned from queryReservation");
            System.out.println(e1.getMessage());
        } catch (Exception e1) {
            System.out
                    .println("OSCARSStub threw exception in queryReservation");
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
    }

    public ResDetails query(String[] args, boolean isInteractive)
            throws AAAFaultMessage, BSSFaultMessage,
            java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);
        GlobalReservationId rt = this.readParams(isInteractive);
        // make the call to the server
        ResDetails response = this.getClient().queryReservation(rt);
        this.outputResponse(response);
        return response;
    }

    public GlobalReservationId readParams(boolean isInteractive) {
        GlobalReservationId rt = new GlobalReservationId();

        // Prompt for input parameters specific to query
        try {
            if (isInteractive) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        System.in));
                rt.setGri(Args.getArg(br, "GRI of reservation to query"));
            } else {
                rt.setGri(this.getProperties().getProperty("tag"));
            }
        } catch (IOException ioe) {
            System.out.println("IO error reading query input");
            System.exit(1);
        }
        return rt;
    }

    public void outputResponse(ResDetails response) {
        System.out.println("GRI: " + response.getGlobalReservationId());
        System.out.println("Status: " +
                response.getStatus().toString());
        System.out.println("Login: " + response.getLogin());
        System.out.println("Start time: "
                + new Date(response.getStartTime()*1000).toString());
        System.out.println("End time: "
                + new Date(response.getEndTime()*1000).toString());
        System.out.println("Bandwidth: "
                + Integer.toString(response.getBandwidth()));
        System.out.println("Description: " + response.getDescription());
        PathInfo pathInfo = response.getPathInfo();
        if (pathInfo == null) {
            System.err.println("No path information in response");
            return;
        } 
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info != null) {
            System.out.println("Source host: " + layer3Info.getSrcHost());
            System.out.println("Destination host: " +
                               layer3Info.getDestHost());
            if (layer3Info.getProtocol() != null) {
                System.out.println("Protocol: " +
                        layer3Info.getProtocol().toString());
            }
            CtrlPlanePathContent path = pathInfo.getPath();
            if (path != null) {
                this.outputHops(path);
            }
        }
        MplsInfo mplsInfo = pathInfo.getMplsInfo();
        if (mplsInfo != null) {
            if (mplsInfo.getBurstLimit() != 0) {
                System.out.println("Burst limit: "
                    + Integer.toString(mplsInfo.getBurstLimit()));
            }
        }
        Layer2Info layer2info = pathInfo.getLayer2Info();
        if (layer2info != null) {
        	String srcE = layer2info.getSrcEndpoint();
        	String dstE = layer2info.getDestEndpoint();
        	String srcVt = layer2info.getSrcVtag().getString();
        	String dstVt = layer2info.getDestVtag().getString();
        	System.out.println("Layer 2 info");
        	System.out.println("Src endpoint:" + srcE);
        	System.out.println("Dst endpoint:" + dstE);
        	System.out.println("Src vtag:" + srcVt);
        	System.out.println("Dst vtag:" + dstVt);
        	
            CtrlPlanePathContent path = pathInfo.getPath();
            if (path != null) {
                this.outputHops(path);
            }
       	
        }
        System.out.println(" ");
    }
}
