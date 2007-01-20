import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;

import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;


public class ListReservationsClient extends ExampleClient {
    /**
     * @param args
     *            [0] directory name of the client repository contains
     *            rampart.mar and axis2.xml [1] the default url of the service
     *            endpoint
     */
    public static void main(String[] args) {
        try {
            ListReservationsClient cl = new ListReservationsClient();
            cl.list(args, true);
        } catch (AAAFaultMessageException e1) {
            System.out.println(
                    "AAAFaultMessageException from listReservations");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            System.out.println(
                    "RemoteException returned from listReservations");
            System.out.println(e1.getMessage());
        } catch (Exception e1) {
            System.out.println(
                    "OSCARSStub threw exception in listReservations");
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }    
    }

    public ListReply list(String[] args, boolean isInteractive)
            throws AAAFaultMessageException, BSSFaultMessageException,
                   java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);
        /*  don't know if login is coming or going, but it is currently  not used
        String login = this.readParams(isInteractive);
        */
        String login = null;
        // make the call to the server
        ListReply response = this.getClient().listReservations(login);
        this.outputResponse(response);
        return response;
    }

    public String readParams(boolean isInteractive) {
        String login = null;

        // Prompt for input parameters
        try {
            if (isInteractive) {
                BufferedReader br =
                     new BufferedReader(new InputStreamReader(System.in));
                /* the following does  not work because getArg loops until it get an answer */
	              login = Args.getArg(br, "User login (may be left blank)");
            }
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        return login;
    }

    public void outputResponse(ListReply response) {
        ResInfoContent[] resInfo = response.getResInfo();
        for (int i=0; i < resInfo.length; i++) {
            System.out.println("Tag: " + resInfo[i].getTag());
            System.out.println("Source host: " + resInfo[i].getSrcHost());
            System.out.println("Destination host: " +
                               resInfo[i].getDestHost());
            System.out.println("Status: " + resInfo[i].getStatus().toString());
            System.out.println(" ");
        }
    }
}
