import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.ws.BSSFaultMessage;
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
            fc.forward(args, true);
        } catch (IOException e1) {
            ;
        }
        /*
        } catch (AAAFaultMessage e1) {
            System.out.println(
                    "AAAFaultMessage from forward");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessage e1) {
            System.out.println(
                    "BSSFaultMessage from forward");
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

    public void forward(String[] args, boolean isInteractive)
            throws IOException {

        String operation = null;
        String sender = null;
 
        super.init(args, isInteractive);
         BufferedReader br = 
                new BufferedReader(new InputStreamReader(System.in));
        sender =  Args.getArg(br,"Name of user originating the request");
        operation = Args.getArg(br, "Operation: create, cancel, " +
           "query, or list.  Type quit to exit.");
        try {
        while (!operation.equals("quit")) {
            Forward fwd =  new Forward();
            fwd.setPayloadSender(sender);
            ForwardPayload forPayload = new ForwardPayload();

            if (operation.equals("list")) {
                forPayload.setContentType(operation + "Reservations");
            } else {forPayload.setContentType(operation + "Reservation"); }

            /*
            if (operation.equals("create")) {
                CreateReservationClient createRes =
                    new CreateReservationClient();
                forPayload.setCreateReservation(createRes.readParams(isInteractive));
                fwd.setPayload(forPayload);
                ForwardReply reply = this.getClient().forward(fwd);
                createRes.outputResponse(reply.getCreateReservation());
                
            } else if (operation.equals("cancel")) {
            */
            if (operation.equals("cancel")) {
                CancelReservationClient canRes=
                    new CancelReservationClient();
                forPayload.setCancelReservation( canRes.readParams(isInteractive));
                fwd.setPayload(forPayload);
                ForwardReply reply = this.getClient().forward(fwd);
                canRes.outputResponse(reply.getCancelReservation());

            } else if (operation.equals("query")) {
                QueryReservationClient queryRes= new QueryReservationClient();
                forPayload.setQueryReservation(queryRes.readParams(isInteractive));
                fwd.setPayload(forPayload);
                ForwardReply reply = this.getClient().forward(fwd);
                queryRes.outputResponse(reply.getQueryReservation());
                
            } else if (operation.equals("list")) {
                ListReservationsClient listRes = new ListReservationsClient();
                
                forPayload.setListReservations(listRes.readParams(isInteractive));
                fwd.setPayload(forPayload);
                ForwardReply reply = this.getClient().forward(fwd);
                listRes.outputResponse(reply.getListReservations());

            } else {
                System.out.println("Unable to submit request that is not " +
                                   "create, cancel, query, list, or quit");
            }
            operation = Args.getArg(br, "Operation: create, cancel, " +
                                        "query, or list.  Type quit to exit.");
        }
        } catch (AAAFaultMessage e1) {
    	    System.out.println(
                    "AAAFaultMessage from queryReservation");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessage e1) {
            System.out.println(
                    "BSSFaultMessage from queryReservation");
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

    public String readParams(boolean isInteractive) {

        String sender = null;
        // Prompt for input parameters
        try {
            BufferedReader br = 
                new BufferedReader(new InputStreamReader(System.in));
            sender =  Args.getArg(br,"Name of user originating the request");
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        return sender;
    }

    public HashMap<String,String> readProperties() {
        HashMap<String,String> params = null;
        return params;
    }
}
