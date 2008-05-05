import java.io.BufferedReader;
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
import net.es.oscars.bss.topology.URNParser;

public class ListReservationsClient extends ExampleClient {

    private boolean createDot = false;

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
//            this.createDOT(response);
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
            String strCreateDot = "";

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
                /*
                System.out.print("Create DOT file y/n (default n): ");
                strCreateDot = br.readLine().trim();
                if (strCreateDot.equals("y") || strCreateDot.equals("Y")) {
                    this.createDot = true;
                }
                */
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



    public void createDOT(ListReply response) {
        String[] colors = new String[10];
        colors[0] = "red";
        colors[1] = "blue";
        colors[2] = "green";
        colors[3] = "yellow";
        colors[4] = "black";
        colors[5] = "purple";
        colors[6] = "pink";
        colors[7] = "brown";
        colors[8] = "orange";
        colors[9] = "violet";
        String color;

        ResDetails[] resList;
        if ((response != null) && (resList = response.getResDetails()) != null) {

            Hashtable<String, Hashtable<String, ArrayList<String>>> nodePorts = new Hashtable<String, Hashtable<String, ArrayList<String>>>();
            Hashtable<String, ArrayList<String>> portLinks;
            ArrayList<String> links;
            ArrayList<ArrayList<String>> coloredHops = new ArrayList<ArrayList<String>>();
            String[] gris = new String[100];

            String output = "\n\n\ndigraph reservations {\n";
            String sources = "";
            String dests = "";

            for (int i = 0; i < resList.length; i++) {
                ResDetails resv = resList[i];
                color = colors[i];

                String gri = resv.getGlobalReservationId();
                gris[i] = gri;

                String resvDescription = resv.getDescription().trim();
                resvDescription = resvDescription.replaceAll("\\[PRODUCTION\\]", "");
                resvDescription = resvDescription.replaceAll("\\[PRODUCTION CIRCUIT\\]", "");

                sources += "\""+gri+"-start\" [label=\""+gri+"\\n"+resvDescription+"\"];\n";
                dests += "\""+gri+"-end\" [label=\""+gri+"\\n"+resvDescription+"\"];\n";

                int bandwidth = resv.getBandwidth();
                PathInfo pathInfo = resList[i].getPathInfo();
                Layer2Info layer2Info = pathInfo.getLayer2Info();
                if (layer2Info != null) {
                    CtrlPlanePathContent path = pathInfo.getPath();
                    CtrlPlaneHopContent[] hops = path.getHop();
                    ArrayList<String> theseHops = new ArrayList<String>();

                    for (int j = 0; j < hops.length; j++) {

                        CtrlPlaneHopContent hop = hops[j];
                        String topoId = "urn:ogf:network:"+hop.getId();

                        Hashtable<String, String> parsed = URNParser.parseTopoIdent(topoId);
                        String domainId = parsed.get("domainId");
                        String nodeId   = parsed.get("nodeId");
                        String portId   = parsed.get("portId");
                        String linkId   = parsed.get("linkId");


                        if (linkId.equals("*")) {
                            portId = portId+":"+linkId;
                        } else {
                            portId = linkId;
                        }

                        if (nodePorts.get(nodeId) != null) {
                            portLinks = (Hashtable<String, ArrayList<String>>) nodePorts.get(nodeId);
                        } else {
                            portLinks = new Hashtable<String, ArrayList<String>>();
                            nodePorts.put(nodeId, portLinks);
                        }

                        if (portLinks.containsKey(portId)) {
                            links = portLinks.get(portId);
                        } else {
                            links = new ArrayList<String>();
                            portLinks.put(portId, links);
                        }
//                        output += "\""+nodeId+":"+portId+"\";\n";
                        theseHops.add("\""+nodeId+":"+portId+"\"");

                    }
                    coloredHops.add(theseHops);
                }
            } // end for loop

            output += "{\n";
            output += "rank=source;\n";
            output += sources;
            output += "}\n";

            output += "{\n";
            output += "rank=sink;\n";
            output += dests;
            output += "}\n";

            // place ports of the same node in their own cluster
            Iterator nodeIt = nodePorts.keySet().iterator();
            while (nodeIt.hasNext()) {
                String nodeId = (String) nodeIt.next();
                portLinks = nodePorts.get(nodeId);
//                output += "\""+nodeId+"\" [shape=record,label=\"" + nodeId + " | { ";

                Iterator portIt = portLinks.keySet().iterator();
                output += "subgraph \"cluster_"+nodeId+"\" {\n";
                output += " label=\""+nodeId+"\";\n";
                while (portIt.hasNext()) {

                    String portId = (String) portIt.next();
                    output += "\""+nodeId+":"+portId+"\" [label=\""+portId+"\"];\n";
//                    output += "<"+portId+"> "+portId;
                    if (portIt.hasNext()) {
//                        output += " | ";
                    }
                }
                output += "}\n";
//s                output += "} \"];\n";
            }

            // make labels for inter-node hops
            int i = 0;
            Hashtable<String, Integer> edgeBWs = new Hashtable<String, Integer>();
            for (ArrayList<String> theHops : coloredHops) {
                ResDetails resv = resList[i];
                String gri = resv.getGlobalReservationId();
                int bandwidth = resv.getBandwidth();
                i++;
                for (int j = 0; j < theHops.size() - 1; j++) {
                    String hop = theHops.get(j);
                    String nextHop = theHops.get(j+1);
                    String edge = hop+":"+nextHop;
                    if (j == 0 || j % 2 > 0 ) {
                        int edgeBW = 0;
                        if (edgeBWs.containsKey(edge)) {
                            edgeBW = edgeBWs.get(edge);
                        }
                        edgeBW += bandwidth;
                        edgeBWs.put(edge, edgeBW);

//                        output += hop+" -> "+nextHop+" [color="+color+",dir=both, style=bold];\n";
//                    } else {
//                        output += hop+" -> "+nextHop+" [color="+color+",dir=both,style=bold];\n";
                    }
                }
            }

            // output edges
            i = 0;
            for (ArrayList<String> theHops : coloredHops) {
                ResDetails resv = resList[i];
                String gri = resv.getGlobalReservationId();
                int bandwidth = resv.getBandwidth();
                color = colors[i];
                i++;
                for (int j = 0; j < theHops.size() - 1; j++) {
                    String hop = theHops.get(j);
                    String nextHop = theHops.get(j+1);
                    String edge = hop+":"+nextHop;
                    String reverseEdge = nextHop+":"+hop;
                    int edgeBW = 0;
                    String label="";
                    if (edgeBWs.containsKey(edge)) {
                        edgeBW = edgeBWs.get(edge);
                        label = "total:\\n"+edgeBW+" Mbps";
                        edgeBWs.remove(edge);
                        edgeBWs.remove(reverseEdge);
                    } else if (edgeBWs.containsKey(reverseEdge)) {
                        edgeBW = edgeBWs.get(reverseEdge);
                        label = "total:\\n"+edgeBW+" Mbps";
                        edgeBWs.remove(edge);
                        edgeBWs.remove(reverseEdge);
                    }

                    if (j == 0) {
                        label = bandwidth+" Mbps";
                        output += "\""+gri+"-start\" -> "+hop+" [color="+color+",dir=both, style=bold, weight=5, group="+color+", label=\""+label+"\", minlen=2];\n";
                    }
                    if (j % 2 > 0 ) {
                        output += hop+" -> "+nextHop+" [color="+color+",dir=both, style=bold, weight=5, group="+color+", label=\""+label+"\", minlen=3];\n";
                    } else {
                        output += hop+" -> "+nextHop+" [color="+color+",dir=both, group="+color+"];\n";
                    }
                    if (j == theHops.size() -2) {
                        output += nextHop +" -> \""+gri+"-end\" [color="+color+",dir=both, style=bold, weight=5, group="+color+", minlen=2s];\n";
                    }
                }
            }
            output += "}\n";

            System.out.println(output);
        }
    }

}
