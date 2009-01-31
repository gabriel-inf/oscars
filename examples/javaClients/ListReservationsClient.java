import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.ws.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;
import net.es.oscars.client.GraphVizExporter;
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

        int numResults = response.getTotalResults();
        if (numResults == 0) {
            System.out.println("Empty results");
            return response;
        }
        ResDetails[] details = response.getResDetails();

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
         StringBuilder sb = new StringBuilder();
         sb.append("GRI: " + response.getGlobalReservationId() + "\n");
         sb.append("Login: " + response.getLogin() + "\n");
         sb.append("Status: " + response.getStatus() + "\n");
         sb.append("Start Time: " +
            new Date(response.getStartTime()*1000).toString() + "\n");
         sb.append("End Time: " +
            new Date(response.getEndTime()*1000).toString() + "\n");
         sb.append("Time of request: " +
            new Date(response.getCreateTime()*1000).toString() + "\n");
         sb.append("Bandwidth: " + response.getBandwidth() + "\n");
         sb.append("Description: " + response.getDescription() + "\n");
         sb.append("Path Setup Mode: " + pathInfo.getPathSetupMode() + "\n");
         if (layer2Info != null) {
             sb.append("Source Endpoint: " + layer2Info.getSrcEndpoint() + "\n");
             sb.append("Destination Endpoint: " + layer2Info.getDestEndpoint() + "\n");
         }
         if (layer3Info != null) {
             sb.append("Source Host: " + layer3Info.getSrcHost() + "\n");
             sb.append("Destination Host: " + layer3Info.getDestHost() + "\n");
             sb.append("Source L4 Port: " + layer3Info.getSrcIpPort() + "\n");
             sb.append("Destination L4 Port: " + layer3Info.getDestIpPort() + "\n");
             sb.append("Protocol: " + layer3Info.getProtocol() + "\n");
             sb.append("DSCP: " + layer3Info.getDscp() + "\n");
         }
         if (mplsInfo != null) {
             sb.append("Burst Limit: " + mplsInfo.getBurstLimit() + "\n");
             sb.append("LSP Class: " + mplsInfo.getLspClass() + "\n");
         }
         sb.append("Path: \n");
         for (CtrlPlaneHopContent hop : path.getHop()) {
            CtrlPlaneLinkContent link = hop.getLink();
            if (link==null) {
                //should not happen
                sb.append("no link");
                continue;
            }
            sb.append("\t" + link.getId());
            CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
            sb.append(", " + swcap.getEncodingType());
            if("ethernet".equals(swcap.getEncodingType())){
                String vlanRange = swcapInfo.getVlanRangeAvailability();
                if (vlanRange != null) {
                    sb.append(", " + vlanRange);
                }
            }
            sb.append("\n");
         }
         System.out.println(sb.toString());
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
