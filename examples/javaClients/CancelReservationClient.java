import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;
import java.rmi.RemoteException;

import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;


public class CancelReservationClient extends ExampleClient {
    /**
     * @param args
     *            [0] directory name of the client repository contains
     *            rampart.mar and axis2.xml [1] the default service url
     *            endpoint [2] the tag of the reservation to cancel
     */
    public static void main(String[] args) {
        try {
            CancelReservationClient cl = new CancelReservationClient();
            cl.cancel(args, true);
        } catch (AAAFaultMessage e) {
            System.out.println(
                    "AAAFaultMessage from cancelReservation");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (BSSFaultMessage e) {
            System.out.println(
                    "BSSFaultMessage from cancelReservation");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e) {
            System.out.println(
                    "RemoteException returned from cancelReservation");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("OSCARSStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }    
    }

    public String cancel(String[] args, boolean isInteractive)
            throws AAAFaultMessage, BSSFaultMessage,
                   java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);
        GlobalReservationId rt = this.readParams(isInteractive);
        // make the call to the server
        String response = this.getClient().cancelReservation(rt);
        this.outputResponse(response);
        return response;
    }

    public GlobalReservationId readParams(boolean isInteractive) {
        GlobalReservationId rt = new GlobalReservationId();

        Properties props = this.getProperties();
		    try {
		        // Prompt for input parameters specific to query
            if (isInteractive) {
			          BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));
                rt.setGri(Args.getArg(br, "GRI of reservation to cancel"));
            } else {
                rt.setGri(props.getProperty("tag"));
            }
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        return rt;
    }

    public void outputResponse(String response) {
        System.out.println("Status: " +  response);
    }
}
