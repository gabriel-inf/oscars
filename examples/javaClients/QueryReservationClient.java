import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.HashMap;

import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
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
        } catch (AAAFaultMessageException e1) {
            System.out.println(
                    "AAAFaultMessageException from queryReservation");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e1) {
            System.out.println(
                    "BSSFaultMessageException from queryReservation");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            System.out.println(
                    "RemoteException returned from queryReservation");
            System.out.println(e1.getMessage());
        } catch (Exception e1) {
            System.out.println(
                    "OSCARSStub threw exception in queryReservation");
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }    
    }

    public ResDetails query(String[] args, boolean isInteractive)
            throws AAAFaultMessageException, BSSFaultMessageException,
                   java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);
        ResTag rt = this.readParams(isInteractive);
        // make the call to the server
        ResDetails response = this.getClient().queryReservation(rt);
        this.outputResponse(response);
        return response;
    }

    public ResTag readParams(boolean isInteractive) {
        ResTag rt = new ResTag();

		    // Prompt for input parameters specific to query
		    try {
            if (isInteractive) {
			          BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));
                rt.setTag(Args.getArg(br, "Tag of reservation to query"));
            } else {
                rt.setTag(this.getProperties().getProperty("tag"));
            }
        } catch (IOException ioe) {
			      System.out.println("IO error reading query input");
			      System.exit(1);
        }
        return rt;
    }

    public void outputResponse(ResDetails response) {
        System.out.println("Tag: " + response.getTag());
			  System.out.println("Status: " + response.getStatus().toString());
			  System.out.println("Source host: " + response.getSrcHost());
			  System.out.println("Destination host: " +
                response.getDestHost());
        System.out.println("Start time: " +
                response.getStartTime().getTime().toString());
        System.out.println("End time: " +
                response.getEndTime().getTime().toString());
        System.out.println("Bandwidth: " +
                Integer.toString(response.getBandwidth()));
        System.out.println("Burst limit: " +
                Integer.toString(response.getBurstLimit()));
        System.out.println("Protocol: " + response.getProtocol().toString());
        System.out.println("Description: " + response.getDescription());
       System.out.println(" ");
        //response.put("path", qreply.getPath());
		}
}
