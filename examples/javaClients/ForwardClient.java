import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;


public class ForwardClient extends ExampleClient {
    /**
     * @param args  [0] directory name of the client repository
     *                  contains rampart.mar and axis2.xml
     *              [1] the default url of the service endpoint
     */
    public static void main(String[] args) {
        try {
            ForwardClient fc = new ForwardClient();
            fc.forwardingTests(args, true);
        } catch (IOException e1) {
            ;
        }
        /*
        } catch (AAAFaultMessageException e1) {
            System.out.println(
                    "AAAFaultMessageException from forward");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e1) {
            System.out.println(
                    "BSSFaultMessageException from forward");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            System.out.println(
                    "RemoteException returned from forward");
            System.out.println(e1.getMessage());
        } catch (Exception e1) {
            System.out.println(
                    "OSCARSStub threw exception in forward");
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        } */
    }

    public void forwardingTests(String[] args, boolean isInteractive)
            throws IOException {

        String operation = null;

        super.init(args, isInteractive);
        HashMap<String,String> params = this.readParams(isInteractive);
        BufferedReader br = 
                new BufferedReader(new InputStreamReader(System.in));
        operation = Args.getArg(br, "Operation: create, cancel, " +
                                    "query, or list.  Type quit to exit.");
        while (!operation.equals("quit")) {
            if (operation.equals("list")) {
                params.put("operation", operation + "Reservations");
            } else { params.put("operation", operation + "Reservation"); }

            if (operation.equals("create")) {
                CreateReservationClient payload =
                    new CreateReservationClient();

            } else if (operation.equals("cancel")) {
                CancelReservationClient payload =
                    new CancelReservationClient();

            } else if (operation.equals("query")) {
                QueryReservationClient payload = new QueryReservationClient();
                ResTag rt = payload.readParams(false); 

            } else if (operation.equals("list")) {
                ListReservationsClient payload = new ListReservationsClient();

            } else {
                System.out.println("Unable to submit request that is not " +
                                   "create, cancel, query, list, or quit");
            }
            operation = Args.getArg(br, "Operation: create, cancel, " +
                                        "query, or list.  Type quit to exit.");
        }
    }

    public HashMap<String,String> readParams(boolean isInteractive) {

        HashMap<String,String> params = new HashMap<String,String>();
        // Prompt for input parameters
        try {
            BufferedReader br = 
                new BufferedReader(new InputStreamReader(System.in));
            params.put("payloadSender",
                    Args.getArg(br,"Name of user originating the request"));
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        return params;
    }

    public HashMap<String,String> readProperties() {
        HashMap<String,String> params = null;
        return params;
    }
}
