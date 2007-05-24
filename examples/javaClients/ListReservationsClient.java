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
            System.out
                    .println("AAAFaultMessageException from listReservations");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            System.out
                    .println("RemoteException returned from listReservations");
            System.out.println(e1.getMessage());
        } catch (Exception e1) {
            System.out
                    .println("OSCARSStub threw exception in listReservations");
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
    }

    public ListReply list(String[] args, boolean isInteractive)
            throws AAAFaultMessageException, BSSFaultMessageException,
            java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);

        // make the call to the server
        ListReply response = this.getClient().listReservations();
        this.outputResponse(response);
        return response;
    }

    public void outputResponse(ListReply response) {
        ResInfoContent[] resInfo;
        if ((response != null) && (resInfo = response.getResInfo()) != null) {
            for (int i = 0; i < resInfo.length; i++) {
                System.out.println("Tag: " + resInfo[i].getTag());
                System.out.println("Source host: " + resInfo[i].getSrcHost());
                System.out.println("Destination host: "
                        + resInfo[i].getDestHost());
                System.out.println("Status: "
                        + resInfo[i].getStatus().toString());
                System.out.println(" ");
            }
        } else {
            System.out.println("no reservations were found");
        }
    }
}
