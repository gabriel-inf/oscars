import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;

import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
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
        } catch (AAAFaultMessage e1) {
            System.out
                    .println("AAAFaultMessage from listReservations");
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
            throws AAAFaultMessage, BSSFaultMessage,
            java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);
        ListRequest listReq = this.readParams(isInteractive);

        // make the call to the server
        ListReply response = this.getClient().listReservations(listReq);
        this.outputResponse(response);
        return response;
    }
    
    public ListRequest readParams(boolean isInteractive) {
        ListRequest listReq = new ListRequest();

        // Prompt for input parameters specific to query
        try {
        	String linkId = "";
        	String[] statuses;
        	String strResults = "";
        	String strOffset = "";
        	String description = "";
        	
            if (isInteractive) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Statuses (comma separated, blank for all): ");
                String statusesInput = br.readLine().trim();
                statuses = statusesInput.split(",");
                System.out.print("Input a link topoId to only get reservations affecting that: ");
                linkId = br.readLine().trim();
                System.out.print("Input a string to only get reservations with that as part of the description: ");
                description = br.readLine().trim();
                System.out.print("Number of results (default 10): ");
                strResults = br.readLine().trim();
                System.out.print("Offset (default 0): ");
                strOffset = br.readLine().trim();
            } else {
            	String statusesInput = this.getProperties().getProperty("statuses").trim();
                statuses = statusesInput.split(",");
                linkId = this.getProperties().getProperty("linkId");
            }

            Integer resRequested = 10;
            Integer resOffset = 0;
            
            try {
            	resRequested = Integer.parseInt(strResults);
            } catch (Exception ex) { }
            
            try {
            	resOffset = Integer.parseInt(strOffset);
            } catch (Exception ex) { }
            
            listReq.setResRequested(resRequested);
            listReq.setResOffset(resOffset);
            
            for (String status: statuses) {
            	listReq.addResStatus(status.trim());
            }                
            if (!linkId.equals("")) {
            	listReq.addLinkId(linkId.trim());
            }
            if (!description.equals("")) {
            	listReq.setDescription(description);
            }
            
        } catch (IOException ioe) {
            System.out.println("IO error reading query input");
            System.exit(1);
        }
        return listReq;
    }


    public void outputResponse(ListReply response) {
        ResDetails[] resList;
        if ((response != null) && (resList = response.getResDetails()) != null) {
            for (int i = 0; i < resList.length; i++) {
                System.out.println("GRI: " + resList[i].getGlobalReservationId());
                System.out.println("Login: " + resList[i].getLogin());
                System.out.println("Status: "
                        + resList[i].getStatus().toString());
                PathInfo pathInfo = resList[i].getPathInfo();
                if (pathInfo == null) {
                    System.err.println("No path for this reservation. ");
                    continue;
                }
                Layer3Info layer3Info = pathInfo.getLayer3Info();
                if (layer3Info != null) {
                    System.out.println("Source host: " +
                            layer3Info.getSrcHost());
                    System.out.println("Destination host: " +
                            layer3Info.getDestHost());
                }
                System.out.println(" ");
            }
        } else {
            System.out.println("no reservations were found");
        }
    }
}
