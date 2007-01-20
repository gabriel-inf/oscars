import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;
import java.rmi.RemoteException;

import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
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
        } catch (AAAFaultMessageException e) {
            System.out.println(
                    "AAAFaultMessageException from cancelReservation");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e) {
            System.out.println(
                    "BSSFaultMessageException from cancelReservation");
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
            throws AAAFaultMessageException, BSSFaultMessageException,
                   java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);
        ResTag rt = this.readParams(isInteractive);
        // make the call to the server
        String response = this.getClient().cancelReservation(rt);
        this.outputResponse(response);
        return response;
    }

    public ResTag readParams(boolean isInteractive) {
        ResTag rt = new ResTag();

        Properties props = this.getProperties();
		    try {
		        // Prompt for input parameters specific to query
            if (isInteractive) {
			          BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));
                rt.setTag(Args.getArg(br, "Tag of reservation to cancel"));
            } else {
                rt.setTag(props.getProperty("tag"));
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
