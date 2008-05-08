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
import net.es.oscars.bss.*;

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

   /**
    * Constructor: creates a new GraphViz object that will contain
    * a graph.
    */
   public GraphVizExporter() {
   }

   /**
    * Returns the graph's source description in dot language.
    * @return Source of the graph in dot language.
    */
   public String getDotSource() {
      return graph.toString();
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

   public void exportTopology(Topology topo, List<Reservation> resvs) {
       HashMap graphAttrs = new HashMap<String, String>();

       HashMap<String, String> linkedEdges = new HashMap<String, String>();
       HashMap<String, String> portPositions = new HashMap<String, String>();
       HashMap<String, ArrayList<String>> nodesToPorts = new HashMap<String, ArrayList<String>>();

       HashMap<String, HashMap<String, String>> graphNodes = new HashMap<String, HashMap<String, String>>();
       HashMap<String, HashMap<String, String>> graphEdges= new HashMap<String, HashMap<String, String>>();

       graphAttrs.put("sep", "0.5");
       graphAttrs.put("overlap", "\"orthoyx\"");
       graphAttrs.put("splines", "\"true\"");
       graphAttrs.put("concentrate", "\"true\"");
       graphAttrs.put("ratio", "0.7");
       graphAttrs.put("model", "\"subset\"");

       String linksDot = "";
       String nodesDot = "";

       for (Domain dom : topo.getDomains()) {
           String domId = dom.getTopologyIdent();

           Iterator nodeIt = dom.getNodes().iterator();
           while (nodeIt.hasNext()) {
               Node node = (Node) nodeIt.next();
               if (this.nodeIsPrintable(node)) {
                   String nodeId = dom.getTopologyIdent()+"_"+node.getTopologyIdent();

                   HashMap<String, String> nodeAttrs = new HashMap<String, String>();
                   graphNodes.put(nodeId, nodeAttrs);
                   ArrayList<String> nodePorts = new ArrayList<String>();
                   nodesToPorts.put(nodeId, nodePorts);

                   Iterator portIt = node.getPorts().iterator();
                   while (portIt.hasNext()) {
                       Port port = (Port) portIt.next();
                       if (this.portIsPrintable(port)) {
                           String portId = port.getTopologyIdent();
                           portId = portId.replaceAll("TenGigabitEthernet", "Te");
                           portId = portId.replaceAll("GigabitEthernet", "Ge");
                           nodePorts.add(portId);

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
       // collection from DB done, create DOT format


       // prep graph

       this.addln(this.start_graph());
       Iterator gpIt = graphAttrs.keySet().iterator();
       while (gpIt.hasNext()) {
           String key = (String) gpIt.next();
           String val = (String) graphAttrs.get(key);
           this.addln(key+"="+val+";");
       }


       // print NODES
       this.addln("{");
       this.addln("node [shape=none];");
       Iterator gnodeIt = graphNodes.keySet().iterator();
       while (gnodeIt.hasNext()) {
           String nodeId = (String) gnodeIt.next();
           String nodeDot = "\""+nodeId+"\"";
           nodeDot += " [label=<<table cellspacing=\"12\">\n";
           nodeDot += " <tr><td colspan=\"2\">"+nodeId+"</td></tr>\n";
           Iterator npIt = nodesToPorts.get(nodeId).iterator();
           int i = 0;
           while (npIt.hasNext()) {
               String portId = (String) npIt.next();
               if (i == 0) {
                   portPositions.put(nodeId+"_"+portId, "w");
               } else {
                   portPositions.put(nodeId+"_"+portId, "e");
               }

               if (i == 0) {
                   nodeDot += "<tr>\n";
               }
               nodeDot += "<td port=\""+portId+"\">"+portId+"</td>\n";
               i++;

               if (i == 2 || !npIt.hasNext()) {
                   nodeDot += "</tr>\n";
                   i = 0;
               }
           }
           nodeDot += "</table>>];";
           this.addln(nodeDot);
       }
       this.addln("}");


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

           boolean interdomain = false;
           if (!leftDomain.equals(rightDomain)) {
               interdomain = true;
           }

           Integer bwQuantum = 1;
           if (mbps >= 10000) {
               bwQuantum = 8;
           } else if (mbps >= 1000) {
               bwQuantum = 2;
           } else {
               bwQuantum = 1;
           }
           Integer teQuantum = 1;
           if (tem >= 1000) {
               teQuantum = 8;
           } else if (tem >= 100) {
               teQuantum = 2;
           } else {
               teQuantum = 1;
           }

           String width = "1";
           String weight = "1";
           String length = "1";
           String extraStyle = "";
           float importance = bwQuantum.floatValue()/teQuantum.floatValue();
           if (interdomain) {
               importance = importance * 2;
           }

           if (importance < 0.5) {
               length = "1";
               extraStyle = ",dotted";
           } else if (importance <= 2) {
               length = "2";
               width = "2";
               weight="8";
               extraStyle = ",dashed";
           } else {
               length = "4";
               width = "4";
               weight="64";
           }

           String style = "style=\"setlinewidth("+width+")"+extraStyle+"\"";

           edgeDot = left+":"+leftPortPos+" -- "+right+":"+rightPortPos+" [dir=both, "+style+", weight="+weight+", len="+length+"];";
           this.addln(edgeDot);

       }

       // finish graph
       this.addln(this.end_graph());

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