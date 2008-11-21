package net.es.oscars.bss.topology;
// GraphViz.java - a simple API to call dot from Java programs

/*$Id$*/
/*
 ******************************************************************************
 *                                                                            *
 *              (c) Copyright 2003 Laszlo Szathmary                           *
 *                                                                            *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms of the GNU Lesser General Public License as published by   *
 * the Free Software Foundation; either version 2.1 of the License, or        *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY *
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public    *
 * License for more details.                                                  *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public License   *
 * along with this program; if not, write to the Free Software Foundation,    *
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.                              *
 *                                                                            *
 ******************************************************************************
 */

import java.io.*;
import java.util.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.bss.*;
import net.es.oscars.wsdlTypes.Layer2Info;
import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.wsdlTypes.ResDetails;

/**
 * <dl>
 * <dt>Purpose: GraphViz Java API
 * <dd>
 *
 * <dt>Description:
 * <dd> With this Java class you can simply call dot
 *      from your Java programs
 * <dt>Example usage:
 * <dd>
 * <pre>
 *    GraphViz gv = new GraphViz();
 *    gv.addln(gv.start_graph());
 *    gv.addln("A -> B;");
 *    gv.addln("A -> C;");
 *    gv.addln(gv.end_graph());
 *    System.out.println(gv.getDotSource());
 *
 *    File out = new File("out.png");
 *    gv.writeGraphToFile(gv.getGraph(gv.getDotSource()), out);
 * </pre>
 * </dd>
 *
 * </dl>
 *
 * @version v0.1, 2003/12/04 (Decembre)
 * @author  Laszlo Szathmary (<a href="szathml@delfin.unideb.hu">szathml@delfin.unideb.hu</a>)
 */
public class GraphVizExporter {
   /**
    * The dir where temporary files will be created.
    */
   private static String TEMP_DIR   = "/tmp";

   /**subgraph cluster_b
    * Where is your dot program located? It will be called externally.
    */
   private static String DOT        = "/usr/bin/dot";

   /**
    * The source of the graph written in dot language.
    */
    private StringBuffer graph = new StringBuffer();

    private StringBuffer xdotSource = new StringBuffer();

    public static String[] colors;

   /**
    * Constructor: creates a new GraphViz object that will contain
    * a graph.
    */
   public GraphVizExporter() {
       colors = new String[20];
       colors[0] = "red3";
       colors[1] = "blue";
       colors[2] = "aquamarine4";
       colors[3] = "gold4";
       colors[4] = "darkorange3";
       colors[5] = "purple4";
       colors[6] = "darkgreen";
       colors[7] = "brown4";
       colors[8] = "orange";
       colors[9] = "violet";
       colors[10] = "pink";
       colors[11] = "blueviolet";
       colors[12] = "coral4";
       colors[13] = "dodgerblue4";
       colors[14] = "chocolate4";
       colors[15] = "chartreuse4";
       colors[16] = "gray12";
       colors[17] = "tan4";
       colors[18] = "firebrick2";
       colors[19] = "olivedrab";
   }

   /**
    * Returns the graph's source description in dot language.
    * @return Source of the graph in dot language.
    */
   public String getDotSource() {
      return graph.toString();
   }

   public void setDotSource(String graph) {
       if (this.graph.length() > 0) {
           this.graph.delete(0, this.graph.length()-1);
       }
       this.graph.append(graph);
   }

   /**
    * Returns the graph's source description in dot language.
    * @return Source of the graph in dot language.
    */
   public String getXdotSource() {
      return xdotSource.toString();
   }

   public void setXdotSource(String xdot) {
       if (this.xdotSource.length() > 0) {
           this.xdotSource.delete(0, this.xdotSource.length()-1);
       }
       this.xdotSource.append(xdot);
   }

   /**
    * Adds a string to the graph's source (without newline).
    */
   public void add(String line) {
      graph.append(line);
   }

   /**
    * Adds a string to the graph's source (with newline).
    */
   public void addln(String line) {
      graph.append(line+"\n");
   }

   /**
    * Adds a newline to the graph's source.
    */
   public void addln() {
      graph.append('\n');
   }

   public void addLinkToPath(Link link, ArrayList<String> nodesOnPath, HashMap<String, ArrayList<String>> nodePortsOnPath) {
       if (link == null) {
           return;
       }
//       System.out.println(link.getFQTI());

       Port port = link.getPort();
       Node node = port.getNode();
       Domain dom = node.getDomain();
       String nodeId = dom.getTopologyIdent()+"_"+node.getTopologyIdent();
       nodesOnPath.add(nodeId);
       String portId = port.getTopologyIdent();
       portId = portId.replaceAll("TenGigabitEthernet", "Te");
       portId = portId.replaceAll("GigabitEthernet", "Ge");
       ArrayList<String> nodePorts = nodePortsOnPath.get(nodeId);
       if (nodePorts == null) {
           nodePorts = new ArrayList<String>();
           nodePorts.add(portId);
           nodePortsOnPath.put(nodeId, nodePorts);
       } else {
           if (!nodePorts.contains(portId)) {
               nodePorts.add(portId);
           }
       }
   }

   public void addResvEdge(PathElem pe, PathElem nextPe, String gri,
           HashMap<String, HashMap<String, HashMap<String, String>>> griToResvEdges,
           boolean isFirst,
           HashMap<String, ArrayList<String>> resvEndpointGroups) {
       if (pe == null) {
           return;
       }

       String startId = gri+"-start";
       String endId = gri+"-end";

       Link link = pe.getLink();
       Port port = link.getPort();
       Node node = port.getNode();
       Domain dom = node.getDomain();

       String nodeId = dom.getTopologyIdent()+"_"+node.getTopologyIdent();
       String portId = port.getTopologyIdent();
       portId = portId.replaceAll("TenGigabitEthernet", "Te");
       portId = portId.replaceAll("GigabitEthernet", "Ge");
       String left = "\""+nodeId+"\":\""+portId+"\"";
       String right = "";
       boolean addToGroup = false;

       if (isFirst) {
           String resvNode = "\"remote_"+nodeId+"\":\""+startId+"\"";
           this.addResvEdge(resvNode, left, gri, griToResvEdges);
           addToGroup = true;
       }

       if (nextPe != null) {
           Link nextLink = nextPe.getLink();
           Port nextPort = nextLink.getPort();
           Node nextNode = nextPort.getNode();
           Domain nextDom = nextNode.getDomain();
           String nextNodeId = nextDom.getTopologyIdent()+"_"+nextNode.getTopologyIdent();
           String nextPortId = nextPort.getTopologyIdent();
           if (!nodeId.equals(nextNodeId)) {
               nextPortId = nextPortId.replaceAll("TenGigabitEthernet", "Te");
               nextPortId = nextPortId.replaceAll("GigabitEthernet", "Ge");
               right = "\""+nextNodeId+"\":\""+nextPortId+"\"";
           }
       } else {
           right = "\"remote_"+nodeId+"\":\""+endId+"\"";
           addToGroup = true;
       }
       if (!right.equals("")) {
           this.addResvEdge(left, right, gri, griToResvEdges);
       }

       if (addToGroup) {
           ArrayList<String> resvEndpointGroup = resvEndpointGroups.get(nodeId);
           if (resvEndpointGroup == null) {
               resvEndpointGroup = new ArrayList<String>();
               resvEndpointGroups.put(nodeId, resvEndpointGroup);
           }
           String what;
           if (isFirst) {
               what = startId;
           } else {
               what = endId;
           }
           if (!resvEndpointGroup.contains(what)) {
               resvEndpointGroup.add(what);
           }
       }

   }

   public void addResvEdge(String left, String right, String gri, HashMap<String, HashMap<String, HashMap<String, String>>> griToResvEdges) {
       String edge = left + " -- " + right;
       HashMap<String, HashMap<String, String>> resvEdges = griToResvEdges.get(gri);
       if (resvEdges == null) {
           resvEdges = new HashMap<String, HashMap<String, String>>();
           griToResvEdges.put(gri, resvEdges);
       }
       HashMap<String, String> edgeAttrs = resvEdges.get(edge);
       if (edgeAttrs == null) {
           edgeAttrs = new HashMap<String, String>();
           resvEdges.put(edge, edgeAttrs);
//                   System.out.println("edge: "+edge);
       }
       edgeAttrs.put("left", left);
       edgeAttrs.put("right", right);
   }

   public void exportTopology(Topology topo, List<Reservation> resvs) {
       DomainDAO domDAO = new DomainDAO("bss");

       HashMap graphAttrs = new HashMap<String, String>();

       HashMap<String, String> linkedEdges = new HashMap<String, String>();
       HashMap<String, String> portPositions = new HashMap<String, String>();
       HashMap<String, ArrayList<String>> nodesToPorts = new HashMap<String, ArrayList<String>>();

       HashMap<String, HashMap<String, String>> graphNodes = new HashMap<String, HashMap<String, String>>();
       HashMap<String, HashMap<String, String>> graphEdges = new HashMap<String, HashMap<String, String>>();


       ArrayList<String> nodesOnPath = new ArrayList<String>();
       HashMap<String, ArrayList<String>> nodePortsOnPath = new HashMap<String, ArrayList<String>>();

       ArrayList<String> resvNodes = new ArrayList<String>();
       HashMap<String, HashMap<String, String>> resvEndpointAttrs = new HashMap<String, HashMap<String, String>>();
       HashMap<String, HashMap<String, HashMap<String, String>>> griToResvEdges = new HashMap<String, HashMap<String, HashMap<String, String>>>();
       HashMap<String, ArrayList<String>> resvEndpointGroups = new HashMap<String, ArrayList<String>>();


       // find out which nodes & links we need to print: only those on reservation paths
       for (Reservation resv : resvs) {
           String gri = resv.getGlobalReservationId();
           String resvDescription = resv.getDescription().trim();
           resvDescription = resvDescription.replaceAll("\\[PRODUCTION\\]", "");
           resvDescription = resvDescription.replaceAll("\\[PRODUCTION CIRCUIT\\]", "");
           String startId = gri+"-start";
           String endId = gri+"-end";
           String label = resvDescription;
           HashMap<String, String> startEndpointAttrs = new HashMap<String, String>();
           HashMap<String, String> endEndpointAttrs = new HashMap<String, String>();
           startEndpointAttrs.put("label", label);
           endEndpointAttrs.put("label", label);
           resvEndpointAttrs.put(startId, startEndpointAttrs);
           resvEndpointAttrs.put(endId, endEndpointAttrs);


//           System.out.println(gri);
           resvNodes.add(gri);
           Path path = null;
           try {
        	   path = resv.getPath(PathType.INTRADOMAIN);
           } catch (BSSException ex) {
        	   // FIXME: do some error handling
        	   return;
           }
           List<PathElem> pathElems = path.getPathElems();
           PathElem nextPe = null;
           for (int i = 0; i < pathElems.size(); i++) {
               PathElem pe = pathElems.get(i);
               if (i < pathElems.size()-1) {
                  nextPe = pathElems.get(i+1);
               } else {
                  nextPe = null;
               }
               Link link = pe.getLink();
               this.addResvEdge(pe, nextPe, gri, griToResvEdges, true,
                                resvEndpointGroups);
               this.addLinkToPath(link, nodesOnPath, nodePortsOnPath);
           }
           /* INTERDOMAIN:  FIXME (wasn't previously functional)
           path = resv.getPath("inter");
           pathElems = path.getPathElems();
           for (int i = 0; i < pathElems.size(); i++) {
               PathElem pe = pathElems.get(i);
               if (i < pathElems.size()-1) {
                  nextPe = pathElems.get(i+1);
               } else {
                  nextPe = null;
               }
               Link link = pe.getLink();
               this.addResvEdge(pe, nextPe, gri, griToResvEdges, true);
               this.addLinkToPath(link, nodesOnPath, nodePortsOnPath);
           }
           Layer2Data l2data = path.getLayer2Data();
           if (l2data != null) {
               String src = l2data.getSrcEndpoint();
               String dst = l2data.getDestEndpoint();

               Link srcLink = domDAO.getFullyQualifiedLink(src);
               Link dstLink = domDAO.getFullyQualifiedLink(dst);
               this.addLinkToPath(srcLink, nodesOnPath, nodePortsOnPath);
               this.addLinkToPath(dstLink, nodesOnPath, nodePortsOnPath);
           }
           */
       }


       graphAttrs.put("sep", "0.2");
       graphAttrs.put("overlap", "\"portho_yx\"");
       graphAttrs.put("splines", "\"false\"");
       graphAttrs.put("ratio", "0.7");
//       graphAttrs.put("model", "\"subset\"");


       for (Domain dom : topo.getDomains()) {
           String domId = dom.getTopologyIdent();

           Iterator nodeIt = dom.getNodes().iterator();
           while (nodeIt.hasNext()) {
                 Node node = (Node) nodeIt.next();
               String nodeId = dom.getTopologyIdent()+"_"+node.getTopologyIdent();
               if (this.nodeIsPrintable(node) && nodesOnPath.contains(nodeId)) {
                   HashMap<String, String> nodeAttrs = new HashMap<String, String>();
                   graphNodes.put(nodeId, nodeAttrs);
                   ArrayList<String> nodePorts = new ArrayList<String>();
                   nodesToPorts.put(nodeId, nodePorts);

                   Iterator portIt = node.getPorts().iterator();
                   while (portIt.hasNext()) {
                       Port port = (Port) portIt.next();
                       String portId = port.getTopologyIdent();
                       portId = portId.replaceAll("TenGigabitEthernet", "Te");
                       portId = portId.replaceAll("GigabitEthernet", "Ge");
                       ArrayList<String> thisNodePortsOnPath = nodePortsOnPath.get(nodeId);
                       if (thisNodePortsOnPath.contains(portId)) {
                           nodePorts.add(portId);
                       }

                       if (this.portIsPrintable(port) && thisNodePortsOnPath.contains(portId)) {

                           Iterator linkIt = port.getLinks().iterator();
                           while (linkIt.hasNext()) {
                               Link link = (Link) linkIt.next();
                               if (this.linkIsPrintable(link)) {
                                   Link remLink = link.getRemoteLink();
                                   Port remPort = remLink.getPort();
                                   Node remNode = remPort.getNode();
                                   Domain remDom = remNode.getDomain();
                                   String remDomId = remDom.getTopologyIdent();

                                   String remPortId = remPort.getTopologyIdent();
                                   remPortId = remPortId.replaceAll("TenGigabitEthernet", "Te");
                                   remPortId = remPortId.replaceAll("GigabitEthernet", "Ge");

                                   String remNodeId = remDom.getTopologyIdent()+"_"+remNode.getTopologyIdent();
                                   ArrayList<String> thatNodePortsOnPath = nodePortsOnPath.get(remNodeId);
                                   if (nodesOnPath.contains(remNodeId) && thatNodePortsOnPath.contains(remPortId)) {

                                       String left = "\""+nodeId+"\":\""+portId+"\"";
                                       String right = "\""+remNodeId+"\":\""+remPortId+"\"";
                                       String edge = left + " -- " + right;
                                       String tem = link.getTrafficEngineeringMetric();

                                       Long bandwidth = link.getMaximumReservableCapacity();
                                       Long bwMbps = bandwidth / 1000000;

                                       HashMap<String, String> edgeAttrs = new HashMap<String, String>();
                                       edgeAttrs.put("Mbps", bwMbps.toString());
                                       edgeAttrs.put("tem", tem);
                                       edgeAttrs.put("left", left);
                                       edgeAttrs.put("right", right);
                                       edgeAttrs.put("leftPort", portId);
                                       edgeAttrs.put("rightPort", remPortId);
                                       edgeAttrs.put("leftNode", nodeId);
                                       edgeAttrs.put("rightNode", remNodeId);
                                       edgeAttrs.put("leftDomain", domId);
                                       edgeAttrs.put("rightDomain", remDomId);
                                       graphEdges.put(edge, edgeAttrs);
                                       linkedEdges.put(left, right);
                                   }
                               }
                           }
                       }
                   }
               }
           }
       }
       // topology collection from DB done





       // prep graph

       this.addln(this.start_graph());
       Iterator gpIt = graphAttrs.keySet().iterator();
       while (gpIt.hasNext()) {
           String key = (String) gpIt.next();
           String val = (String) graphAttrs.get(key);
           this.addln(key+"="+val+";");
       }


       String nodesDot = this.getTopoNodesDot(graphNodes, nodesToPorts, portPositions);
       this.addln(nodesDot);


       String edgesDot = this.getTopoEdgesDot(graphEdges, portPositions);
       this.addln(edgesDot);

       // print RESERVATION NODES
       this.addln("{");
       this.addln("node [shape=none];");
       Iterator egIt = resvEndpointGroups.keySet().iterator();
       while (egIt.hasNext()) {
           String nodeId = (String) egIt.next();
           ArrayList<String> endpointGroup = resvEndpointGroups.get(nodeId);
           String groupId = "\"remote_"+nodeId+"\"";
           this.addln(groupId + " [label=<<table cellspacing=\"16\">");
           for (String endpoint : endpointGroup) {
               this.addln("<tr><td port=\""+endpoint+"\">"+endpoint+"</td></tr>");
           }
           this.addln("</table>>];");
       }
       this.addln("}");

       // print RESERVATION EDGES
       Iterator griIt = griToResvEdges.keySet().iterator();
       int colorIndex = 0;
       while (griIt.hasNext()) {
           String gri = (String) griIt.next();
           String color = colors[colorIndex % colors.length];
//           System.out.println(gri+"_"+color);

           HashMap<String, HashMap<String, String>> resvEdges = griToResvEdges.get(gri);
           Iterator edgeIt = resvEdges.keySet().iterator();
           while (edgeIt.hasNext()) {
               String edge = (String) edgeIt.next();
               String style = "[color = "+color+", penwidth=2]";
               this.addln(edge+" "+style+";");
//               System.out.println(edge);
           }
           colorIndex++;
       }



       // finish graph
       this.addln(this.end_graph());

   }

   public String getTopoNodesDot(HashMap<String, HashMap<String, String>> graphNodes, HashMap<String, ArrayList<String>> nodesToPorts, HashMap<String, String> portPositions) {
       // print NODES
       String nodesDot = "{\n";
       nodesDot += "node [shape=none];\n";
       Iterator graphNodeIt = graphNodes.keySet().iterator();
       while (graphNodeIt.hasNext()) {
           String nodeId = (String) graphNodeIt.next();
           String nodeDot = "\""+nodeId+"\"";
           nodeDot += " [label=<<table cellspacing=\"24\" cellpadding=\"4\">\n";
           nodeDot += " <tr><td colspan=\"2\">"+nodeId+"</td></tr>\n";
           Iterator npIt = nodesToPorts.get(nodeId).iterator();
           int i = 0;
           while (npIt.hasNext()) {
               String portId = (String) npIt.next();
               if (i == 0) {
                   portPositions.put(nodeId+"_"+portId, " ");
               } else if (i == 1) {
                   portPositions.put(nodeId+"_"+portId, " ");
               } else {
//                   portPositions.put(nodeId+"_"+portId, "se");
               }

               if (i == 0) {
                   nodeDot += "<tr>\n";
               }
               nodeDot += "<td port=\""+portId+"\">"+portId+"</td>\n";

               i++;

               if (i == 1 || !npIt.hasNext()) {
                   nodeDot += "</tr>\n";
                   i = 0;
               }
           }
           nodeDot += "</table>>];\n";
           nodesDot += nodeDot;
       }
       nodesDot += "}\n";
       return nodesDot;
   }

   public String getTopoEdgesDot(HashMap<String, HashMap<String, String>> graphEdges, HashMap<String, String> portPositions) {
       HashMap<String, ArrayList<String>> alreadyLinked = new HashMap<String, ArrayList<String>>();

       String edgesDot = "";
       // print EDGES
       Iterator gedgeIt = graphEdges.keySet().iterator();
       while (gedgeIt.hasNext()) {
           String edgeDot = "";
           String edge = (String) gedgeIt.next();
           HashMap edgeAttrs = graphEdges.get(edge);
           String left = (String) edgeAttrs.get("left");
           String right = (String) edgeAttrs.get("right");
           String leftPort = (String) edgeAttrs.get("leftPort");
           String rightPort = (String) edgeAttrs.get("rightPort");
           String leftNode = (String) edgeAttrs.get("leftNode");
           String rightNode = (String) edgeAttrs.get("rightNode");
           String leftDomain = (String) edgeAttrs.get("leftDomain");
           String rightDomain = (String) edgeAttrs.get("rightDomain");
           String leftPortPos = portPositions.get(leftNode+"_"+leftPort);
           String rightPortPos = portPositions.get(rightNode+"_"+rightPort);
           String bwMbps = (String) edgeAttrs.get("Mbps");
           String trafficEng = (String) edgeAttrs.get("tem");
           Integer mbps = Integer.parseInt(bwMbps);
           Integer tem = Integer.parseInt(trafficEng);

           boolean isFirstEdgeBetween = true;
           ArrayList<String> linksFromLeft = alreadyLinked.get(left);
           ArrayList<String> linksFromRight = alreadyLinked.get(right);
           if (linksFromLeft == null) {
               linksFromLeft = new ArrayList<String>();
               alreadyLinked.put(left, linksFromLeft);
               linksFromLeft.add(right);
           } else {
               if (linksFromLeft.contains(right)) {
                   isFirstEdgeBetween = false;
               }
           }
           if (linksFromRight == null) {
               linksFromRight = new ArrayList<String>();
               alreadyLinked.put(right, linksFromRight);
               linksFromRight.add(left);
           } else {
               if (linksFromRight.contains(left)) {
                   isFirstEdgeBetween = false;
               }
           }

           boolean interdomain = false;
           if (!leftDomain.equals(rightDomain)) {
               interdomain = true;
           }


           String width = "4";
           String weight = "1";
           String length = "1";
           String extraStyle = "dashed";
           if (interdomain) {
               width = "3";
           }
           if (!isFirstEdgeBetween) {
               extraStyle = ", invis";
           }

           // leftPortPos = ":"+leftPortPos;
           // rightPortPos = ":"+rightPortPos;
           leftPortPos = "";
           rightPortPos = "";

           String style = "style=\""+extraStyle+"\", penwidth="+width;

           edgeDot = left+leftPortPos+" -- "+right+rightPortPos+" [dir=both, "+style+", weight="+weight+", len="+length+"];\n";
           edgesDot += edgeDot;
       }
       return edgesDot;
   }


   public String exportReservations(ResDetails[] resList, String[] topNodes) throws IOException {

       String color;
       Hashtable<String, Hashtable<String, ArrayList<String>>> nodePorts = new Hashtable<String, Hashtable<String, ArrayList<String>>>();
       Hashtable<String, ArrayList<String>> portLinks;
       ArrayList<String> links;
       ArrayList<ArrayList<String>> coloredHops = new ArrayList<ArrayList<String>>();
       String[] gris = new String[500];

       String output = "\n\n\ndigraph reservations {\n";
       String sources = "";
       String dests = "";

       for (int i = 0; i < resList.length; i++) {
           ResDetails resv = resList[i];
           color = colors[i % colors.length];

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

               boolean reverseHops = false;
               if (topNodes != null) {
                   String topoId = hops[hops.length - 1].getLink().getId();
                   Hashtable<String, String> parsed = URNParser.parseTopoIdent(topoId);
                   String nodeId = parsed.get("nodeId");
                   for (String topNode : topNodes) {
                       if (nodeId.matches(topNode)) {
                           reverseHops = true;
                       }
                   }
               }

               if (reverseHops) {
                   int temp;
                   int low = 0 ;
                   int high = hops.length - 1 ;
                   while (low < high) {
                       CtrlPlaneHopContent tmpHop = hops[low];
                      hops[low] = hops[high];
                      hops[high] = tmpHop;
                      low++;
                      high--;
                   }
               }


               ArrayList<String> theseHops = new ArrayList<String>();

               for (int j = 0; j < hops.length; j++) {

                   CtrlPlaneHopContent hop = hops[j];
                   String topoId = hop.getLink().getId();
                   if (topoId == null || topoId.equals("")) {
                       System.err.println(gri+" empty topoid");
                       continue;
                   }
//                       System.out.println(topoId);

                   Hashtable<String, String> parsed = URNParser.parseTopoIdent(topoId);
                   if (parsed == null || parsed.get("type") == null || !parsed.get("type").equals("link")) {
                       System.err.println(gri+" could not parse: "+topoId);
                       continue;
                   }
                   String domainId = parsed.get("domainId");
                   String nodeId   = parsed.get("nodeId");
                   String portId   = parsed.get("portId");
                   String linkId   = parsed.get("linkId");
                   portId = portId.replaceAll("TenGigabitEthernet", "Te");
                   linkId = linkId.replaceAll("TenGigabitEthernet", "Te");

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
//                       output += "\""+nodeId+":"+portId+"\";\n";
                   theseHops.add("\""+nodeId+":"+portId+"\"");

               }
               coloredHops.add(theseHops);
           }
       } // end for loop

       output += "{\n";
       output += "rank=source;\nnode [shape=record];\n";
       output += sources;
       output += "}\n";

       output += "{\n";
       output += "rank=sink;\nnode [shape=record];\n";
       output += dests;
       output += "}\n";

       // place ports of the same node in their own node [shape=record];\n cluster
       Iterator nodeIt = nodePorts.keySet().iterator();
       while (nodeIt.hasNext()) {
           String nodeId = (String) nodeIt.next();
           portLinks = nodePorts.get(nodeId);
//               output += "\""+nodeId+"\" [shape=record,label=\"" + nodeId + " | { ";

           Iterator portIt = portLinks.keySet().iterator();
           output += "subgraph \"cluster_"+nodeId+"\" {\n";
           output += " label=\""+nodeId+"\";\n";
           output += " fontsize=20;\n";
           output += " node [style=filled, fillcolor=white];\n";
           output += " style=filled;\nfillcolor=\"#E0E0E0s\";\n";
           while (portIt.hasNext()) {

               String portId = (String) portIt.next();
               output += "\""+nodeId+":"+portId+"\" [label=\""+portId+"\"];\n";
//                   output += "<"+portId+"> "+portId;
               if (portIt.hasNext()) {
//                       output += " | ";
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
               String revEdge = nextHop+":"+hop;
               if (j == 0 || j % 2 > 0 ) {
                   int edgeBW = 0;
                   if (edgeBWs.containsKey(edge)) {
                       edgeBW = edgeBWs.get(edge);
                   }
                   edgeBW += bandwidth;
                   edgeBWs.put(edge, edgeBW);

                   edgeBW = 0;
                   if (edgeBWs.containsKey(revEdge)) {
                       edgeBW = edgeBWs.get(revEdge);
                   }
                   edgeBW += bandwidth;
                   edgeBWs.put(revEdge, edgeBW);

//                       output += hop+" -> "+nextHop+" [color="+color+",dir=both, style=bold];\n";
//                   } else {
//                       output += hop+" -> "+nextHop+" [color="+color+",dir=both,style=bold];\n";
               }
           }
       }

       // output edges
       i = 0;
       for (ArrayList<String> theHops : coloredHops) {
           ResDetails resv = resList[i];
           String gri = resv.getGlobalReservationId();
           int bandwidth = resv.getBandwidth();
           String lineweight;
           if (bandwidth >= 10000) {
               lineweight = "\"setlinewidth(6)\"";
           } else if (bandwidth >= 5000) {
               lineweight = "\"setlinewidth(4)\"";
           } else if (bandwidth >= 1000) {
               lineweight = "\"setlinewidth(2)\"";
           } else {
               lineweight = "bold";
           }
           color = colors[i % 20];
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
                   output += "\""+gri+"-start\" -> "+hop+" [color="+color+",dir=both, style="+lineweight+", weight=5, group="+color+", label=\""+label+"\"];\n";
               }
               if (j % 2 > 0 ) {
                   output += hop+" -> "+nextHop+" [color="+color+",dir=both, style="+lineweight+", weight=5, group="+color+", label=\""+label+"\"];\n";
               } else {
                   output += hop+" -> "+nextHop+" [color="+color+",dir=both, group="+color+"];\n";
               }
               if (j == theHops.size() -2) {
                   output += nextHop +" -> \""+gri+"-end\" [color="+color+",dir=both, style="+lineweight+", weight=5, group="+color+"];\n";
               }
           }
       }
       output += "}\n";

       this.setDotSource(output);
       return output;

   }

   public boolean nodeIsPrintable(Node node) {
       if (!node.isValid()) {
           return false;
       }
       Iterator portIt = node.getPorts().iterator();
       while (portIt.hasNext()) {
           Port port = (Port) portIt.next();
           if (this.portIsPrintable(port)) {
               return true;
           }
       }
       return false;

   }

   public boolean portIsPrintable(Port port) {
       if (!port.isValid()) {
           return false;
       }
       Iterator linkIt = port.getLinks().iterator();
       while (linkIt.hasNext()) {
           Link link  = (Link) linkIt.next();
           if (this.linkIsPrintable(link)) {
               return true;
           }
       }
       return false;
   }
   public boolean linkIsPrintable(Link link) {
       String out = link.getFQTI()+" printable? ";
       if (!link.isValid()) {
           return false;
       }
       Link remLink = link.getRemoteLink();
       if (remLink != null && remLink.isValid()) {
           Port remPort = remLink.getPort();
           if (remPort != null & remPort.isValid()) {
               Node remNode = remPort.getNode();
               if (remNode != null && remNode.isValid()) {
                   return true;
               }
           }
       }
       return false;
   }


   /**
    * Returns the graph as an image in binary format.
    * @param dot_source Source of the graph to be drawn.
    * @return A byte array containin
    * g the image of the graph.
    */
   public byte[] getGraph(String dot_source)
   {
      File dot;
      byte[] img_stream = null;

      try {
         dot = writeDotSourceToFile(dot_source);
         if (dot != null)
         {
            img_stream = get_img_stream(dot);
            if (dot.delete() == false)
               System.err.println("Warning: "+dot.getAbsolutePath()+" could not be deleted!");
            return img_stream;
         }
         return null;
      } catch (java.io.IOException ioe) { return null; }
   }

   /**
    * Writes the graph's image in a file.
    * @param img   A byte array containing the image of the graph.
    * @param file  Name of the file to where we want to write.
    * @return Success: 1, Failure: -1
    */
   public int writeGraphToFile(byte[] img, String file)
   {
      File to = new File(file);
      return writeGraphToFile(img, to);
   }

   /**
    * Writes the graph's image in a file.
    * @param img   A byte array containing the image of the graph.
    * @param to    A File object to where we want to write.
    * @return Success: 1, Failure: -1
    */
   public int writeGraphToFile(byte[] img, File to)
   {
      try {
         FileOutputStream fos = new FileOutputStream(to);
         fos.write(img);
         fos.close();
      } catch (java.io.IOException ioe) { return -1; }
      return 1;
   }
   public int writeGraphToFile(String to) {
       String dot = this.getDotSource();
       byte[] img = this.getGraph(dot);
       return this.writeGraphToFile(img, to);
   }


   /**
    * It will call the external dot program, and return the image in
    * binary format.
    * @param dot Source of the graph (in dot language).
    * @return The image of the graph in .png format.
    */
   private byte[] get_img_stream(File dot)
   {
      File img;
      byte[] img_stream = null;

      try {
         img = File.createTempFile("graph_", ".png", new File(this.TEMP_DIR));
         String temp = img.getAbsolutePath();

         Runtime rt = Runtime.getRuntime();
         String cmd = DOT + " -Tpng "+dot.getAbsolutePath()+" -o"+img.getAbsolutePath();
         Process p = rt.exec(cmd);
         p.waitFor();

         FileInputStream in = new FileInputStream(img.getAbsolutePath());
         img_stream = new byte[in.available()];
         in.read(img_stream);
         // Close it if we need to
         if( in != null ) in.close();

         if (img.delete() == false)
            System.err.println("Warning: "+img.getAbsolutePath()+" could not be deleted!");
      }
      catch (java.io.IOException ioe) {
         System.err.println("Error:    in I/O processing of tempfile in dir "+this.TEMP_DIR+"\n");
         System.err.println("       or in calling external command");
         ioe.printStackTrace();
      }
      catch (java.lang.InterruptedException ie) {
         System.err.println("Error: the execution of the external program was interrupted");
         ie.printStackTrace();
      }

      return img_stream;
   }


   public void transformToXdot(String dot) {
          try {
              File tempDot = File.createTempFile("graph_", ".dot", new File(this.TEMP_DIR));
              String temp = tempDot.getAbsolutePath();

              Runtime rt = Runtime.getRuntime();
              String cmd = DOT + " -Txdot"+tempDot.getAbsolutePath();
              Process p = rt.exec(cmd);
              p.waitFor();
              this.setXdotSource("");
              String a;

              BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()),5000);
              while ((a = in.readLine()) != null) {
                  this.xdotSource.append(a);
              }

              // Close it if we need to
              if( in != null ) in.close();
              if (tempDot.delete() == false)
                  System.err.println("Warning: "+tempDot.getAbsolutePath()+" could not be deleted!");

           }
           catch (java.io.IOException ioe) {
              System.err.println("Error:    in I/O processing of tempfile in dir "+this.TEMP_DIR+"\n");
              System.err.println("       or in calling external command");
              ioe.printStackTrace();
           }
           catch (java.lang.InterruptedException ie) {
              System.err.println("Error: the execution of the external program was interrupted");
              ie.printStackTrace();
           }

   }


   public File writeDotSourceToFile(String dot, String fileName) throws java.io.IOException {
      File temp;
      try {
         temp = new File(fileName);
         FileWriter fout = new FileWriter(temp);
         fout.write(dot);
         fout.close();
      }
      catch (Exception e) {
         System.err.println("Error: I/O error while writing the dot source to file!");
         return null;
      }
      return temp;
   }

   /**
    * Writes the source of the graph in a file, and returns the written file
    * as a File object.
    * @param str Source of the graph (in dot language).
    * @return The file (as a File object) that contains the source of the graph.
    */
   private File writeDotSourceToFile(String str) throws java.io.IOException
   {
      File temp;
      try {
         temp = File.createTempFile("graph_", ".dot.tmp", new File(this.TEMP_DIR));
         FileWriter fout = new FileWriter(temp);
         fout.write(str);
         fout.close();
      }
      catch (Exception e) {
         System.err.println("Error: I/O error while writing the dot source to temp file!");
         return null;
      }

      return temp;
   }

   /**
    * Returns a string that is used to start a graph.
    * @return A string to open a graph.
    */
   public String start_graph() {
      return "graph G {";
   }

   /**
    * Returns a string that is used to end a graph.
    * @return A string to close a graph.
    */
   public String end_graph() {
      return "}";
   }

    public static String getTEMP_DIR() {
        return TEMP_DIR;
    }

    public static void setTEMP_DIR(String temp_dir) {
        TEMP_DIR = temp_dir;
    }

    public static String getDOT() {
        return DOT;
    }

    public static void setDOT(String dot) {
        DOT = dot;
    }
}
