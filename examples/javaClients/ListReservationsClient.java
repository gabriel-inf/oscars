import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;
import net.es.oscars.bss.topology.GraphVizExporter;
import net.es.oscars.bss.topology.URNParser;

public class ListReservationsClient extends ExampleClient {

    private String url;
    private String repo;
    private String status = null;
    private String vlan = null;
    private String description = null;
    private String endpoint = null;
    private String src = null;
    private String dst = null;
    private String between_a = null;
    private String between_b = null;
    private String dotfile = null;
    private String[] topNodes = null;
    private int numResults = 10;;

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
            System.out.println("AAAFaultMessage from listReservations");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            System.out.println("RemoteException returned from listReservations");
            System.out.println(e1.getMessage());
        } catch (Exception e1) {
            System.out.println("OSCARSStub threw exception in listReservations");
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

        ResDetails[] details = response.getResDetails();
        int numResults = response.getTotalResults();

        System.out.println("Results: "+details.length);

        for(int i = 0; details != null && i < details.length; i++){
            this.printResDetails(details[i]);
        }
        this.createDOT(details);
        return response;
    }

    public ListRequest readParams(boolean isInteractive) {
        ListRequest listReq = new ListRequest();

        // Prompt for input parameters specific to query
        try {
            String linkId = "";
            // arbitrary
            String[] vlanTagList = new String[20];
            String[] statuses = new String[20];
            String strResults = "";
            String strOffset = "";
            String description = "";
            String strDotfile = "";

            if (isInteractive) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Statuses (comma separated, blank for all): ");
                String statusesInput = br.readLine().trim();
                statuses = statusesInput.split(",");
                System.out.print("Input a link topoId to only get reservations affecting that: ");
                linkId = br.readLine().trim();
                System.out.print(
                        "Input a comma-separated set of vlanTags or ranges " +
                        "to get only associated reservations: ");
                String vlansInput = br.readLine().trim();
                vlanTagList = vlansInput.split(",");
                System.out.print("Input a string to only get reservations with that as part of the description: ");
                description = br.readLine().trim();
                System.out.print("Number of results (default 10): ");
                strResults = br.readLine().trim();
                System.out.print("Offset (default 0): ");
                strOffset = br.readLine().trim();
                System.out.print("DOT filename (default none): ");
                strDotfile = br.readLine().trim();
                if (!strDotfile.equals("")) {
                    this.dotfile = strDotfile;
                }
                System.out.print("Nodes to put on top of graph? (comma-separated list of node ids, default is auto): ");
                String strTopNode  = br.readLine().trim();
                if (!strTopNode.equals("")) {
                    this.topNodes = strTopNode.split(",");
                }

            } else {
                String statusesInput = this.getProperties().getProperty("statuses");
                if (statusesInput != null) {
                    statusesInput = statusesInput.trim();
                    statuses = statusesInput.split(",");
                }
                String vlansInput = this.getProperties().getProperty("vlans");
                if (vlansInput != null) {
                    vlansInput = vlansInput.trim();
                    vlanTagList = vlansInput.split(",");
                }
                linkId = this.getProperties().getProperty("linkId");
            }

            Integer resRequested = 10;
            Integer resOffset = 0;

            try {
                if (!strResults.equals("")) {
                    resRequested = Integer.parseInt(strResults);
                }
            } catch (Exception ex) {
                System.out.println("Could not parse number of results!\n\t" + ex.getMessage());
            }

            try {
                if (!strOffset.equals("")) {
                    resOffset = Integer.parseInt(strOffset);
                }
            } catch (Exception ex) {
                System.out.println("Could not parse offset!\n\t" + ex.getMessage());

            }

            listReq.setResRequested(resRequested);
            listReq.setResOffset(resOffset);

            for (String status: statuses) {
                listReq.addResStatus(status.trim());
            }
            for (String v: vlanTagList) {
                VlanTag vlanTag = new VlanTag();
                // number legality checks currently performed only on server
                vlanTag.setString(v.trim());
                vlanTag.setTagged(true);
                listReq.addVlanTag(vlanTag);
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




    public void printResDetails(ResDetails response){
         PathInfo pathInfo = response.getPathInfo();
         CtrlPlanePathContent path = pathInfo.getPath();
         Layer2Info layer2Info = pathInfo.getLayer2Info();
         Layer3Info layer3Info = pathInfo.getLayer3Info();
         MplsInfo mplsInfo = pathInfo.getMplsInfo();

         String resvSrc = "";
         if (layer2Info != null) {
             resvSrc = layer2Info.getSrcEndpoint().trim();
         } else if (layer3Info != null) {
             resvSrc = layer3Info.getSrcHost().trim();
         }
         String resvDest = "";
         if (layer2Info != null) {
             resvDest = layer2Info.getDestEndpoint().trim();
         } else if (layer3Info != null) {
             resvDest = layer3Info.getDestHost().trim();
         }


         /* Print response information */
         String output = "";
         output += "GRI: " + response.getGlobalReservationId() + "\n";
         output += "Login: " + response.getLogin() + "\n";
         output += "Status: " + response.getStatus() + "\n";
         output += "Start Time: " + response.getStartTime() + "\n";
         output += "End Time: " + response.getEndTime() + "\n";
         output += "Time of request: " + response.getCreateTime() + "\n";
         output += "Bandwidth: " + response.getBandwidth() + "\n";
         output += "Description: " + response.getDescription() + "\n";
         output += "Path Setup Mode: " + pathInfo.getPathSetupMode() + "\n";
         if(layer2Info != null){
             output += "Source Endpoint: " + layer2Info.getSrcEndpoint() + "\n";
             output += "Destination Endpoint: " + layer2Info.getDestEndpoint() + "\n";
             output += "Source VLAN: " + layer2Info.getSrcVtag() + "\n";
             output += "Destination VLAN: " + layer2Info.getDestVtag() + "\n";
         }
         if(layer3Info != null){

             output += "Source Host: " + layer3Info.getSrcHost() + "\n";
             output += "Destination Host: " + layer3Info.getDestHost() + "\n";
             output += "Source L4 Port: " + layer3Info.getSrcIpPort() + "\n";
             output += "Destination L4 Port: " + layer3Info.getDestIpPort() + "\n";
             output += "Protocol: " + layer3Info.getProtocol() + "\n";
             output += "DSCP: " + layer3Info.getDscp() + "\n";
         }
         if(mplsInfo != null){
             output += "Burst Limit: " + mplsInfo.getBurstLimit() + "\n";
             output += "LSP Class: " + mplsInfo.getLspClass() + "\n";
         }
         output += "Path: \n";
         for (CtrlPlaneHopContent hop : path.getHop()){
             output += "\t" + hop.getLinkIdRef() + "\n";
         }
         System.out.println(output);
    }

    public void createDOT(ResDetails[] resList) throws IOException {
        if (this.dotfile != null) {
            GraphVizExporter gve = new GraphVizExporter();
            String output = gve.exportReservations(resList, this.topNodes);
            gve.writeDotSourceToFile(output, this.dotfile);
            System.out.println("DOT output in "+this.dotfile);
        }
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
