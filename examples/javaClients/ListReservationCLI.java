import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Date;

import org.apache.axis2.AxisFault;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import edu.internet2.perfsonar.PSException;
import edu.internet2.perfsonar.dcn.DCNLookupClient;

import net.es.oscars.bss.topology.URNParser;
import net.es.oscars.bss.topology.GraphVizExporter;
import net.es.oscars.client.Client;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.Layer2Info;
import net.es.oscars.wsdlTypes.Layer3Info;
import net.es.oscars.wsdlTypes.ListRequest;
import net.es.oscars.wsdlTypes.ListReply;
import net.es.oscars.wsdlTypes.MplsInfo;
import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.wsdlTypes.VlanTag;
import net.es.oscars.wsdlTypes.ResDetails;

public class ListReservationCLI {
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

    public ListRequest readArgs(String[] args){
        DCNLookupClient lookupClient = null; 
        try{
            lookupClient = new DCNLookupClient("http://www.perfsonar.net/gls.root.hints");
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        
        /* Set request parameters */
        try{
            for(int i = 0; i < args.length; i++){
                if(args[i].equals("-url")){
                    if (args.length >= i && args[i+1] != null) {
                        this.url = args[i+1].trim();
                    } else {
                        System.out.println("Error: url parameter not specified");
                        System.exit(1);
                    }
                }else if(args[i].equals("-repo")){
                    if (args.length >= i && args[i+1] != null) {
                        this.repo = args[i+1].trim();
                    } else {
                        System.out.println("Error: repo parameter not specified");
                        System.exit(1);
                    }

                }else if(args[i].equals("-status")){
                    if (args.length >= i && args[i+1] != null) {
                        this.status = args[i+1].trim();
                    } else {
                        System.out.println("Error: status parameter not specified");
                        System.exit(1);
                    }
                }else if(args[i].equals("-vlan")){
                    if (args.length >= i && args[i+1] != null) {
                        this.vlan = args[i+1].trim();
                    } else {
                        System.out.println("Error: vlan parameter not specified");
                        System.exit(1);
                    }
                }else if(args[i].equals("-desc")){
                    if (args.length >= i && args[i+1] != null) {
                        this.description = args[i+1].trim();
                    } else {
                        System.out.println("Error: description parameter not specified");
                        System.exit(1);
                    }
                }else if(args[i].equals("-endpoint")){
                    if (args.length >= i && args[i+1] != null) {
                        this.endpoint = args[i+1].trim();
                        if (!this.endpoint.startsWith("urn:ogf:network")) {
                            try {
                                String lookupResult = lookupClient.lookupHost(this.between_a);
                                this.endpoint = lookupResult.trim();
                            } catch (PSException ex) {
                                System.out.println("Error: could not resolve ENDPOINT.\n\t"+ex.getMessage());
                                System.exit(1);
                            }
                        }
                    } else {
                        System.out.println("Error: endpoint parameter not specified");
                        System.exit(1);
                    }
                }else if(args[i].equals("-dot")){
                    if (args.length >= i && args[i+1] != null) {
                        this.dotfile = args[i+1].trim();
                    } else {
                        System.out.println("Error: dot parameter not specified");
                        System.exit(1);
                    }
                }else if(args[i].equals("-topnode")){
                    if (args.length >= i && args[i+1] != null) {
                        this.topNodes = args[i+1].trim().split(",");
                    } else {
                        System.out.println("Error: topnode parameter not specified");
                        System.exit(1);
                    }
                }else if(args[i].equals("-between")){
                    String between = "";
                    if (args.length >= i && args[i+1] != null) {
                        between = args[i+1].trim();
                    } else {
                        System.out.println("Error: between parameter not specified");
                        System.exit(1);
                    }
                    String[] parts = between.split(",");
                    if (parts != null && parts.length != 2) {
                        System.out.println("Error: use two comma separated values for -between.\n");
                        System.exit(1);
                    }
                    this.between_a = parts[0].trim();
                    this.between_b = parts[1].trim();
                    if (!this.between_a.startsWith("urn:ogf:network")) {
                        try {
                            String lookupResult = lookupClient.lookupHost(this.between_a);
                            this.between_a = lookupResult.trim();
                        } catch (PSException ex) {
                            System.out.println("Error: could not resolve ENDPOINT_A.\n\t"+ex.getMessage());
                            System.exit(1);
                        }
                    }
                    if (!this.between_b.startsWith("urn:ogf:network")) {
                        try {
                            String lookupResult = lookupClient.lookupHost(this.between_b);
                            this.between_b = lookupResult.trim();
                        } catch (PSException ex) {
                            System.out.println("Error: could not resolve ENDPOINT_B.\n\t"+ex.getMessage());
                            System.exit(1);
                        }
                    }

                }else if(args[i].equals("-src")){
                    this.src = args[i+1].trim();
                    if (!this.src.matches("urn\\:ogf\\:network")) {
                        try {
                            this.src = lookupClient.lookupHost(this.src).trim();
                        } catch (PSException ex) {
                            System.out.println("Error: could not resolve src.\n\t"+ex.getMessage());
                            System.exit(1);
                        }
                    }
                }else if(args[i].equals("-dst")){
                    this.dst= args[i+1].trim();
                    if (!this.dst.matches("urn\\:ogf\\:network")) {
                        try {
                            this.dst = lookupClient.lookupHost(this.dst).trim();
                        } catch (PSException ex) {
                            System.out.println("Error: could not resolve dst.\n\t"+ex.getMessage());
                            System.exit(1);
                        }
                    }
                }else if(args[i].equals("-numresults")){
                    int n = Integer.parseInt(args[i+1].trim());
                    this.numResults = n;
                }else if(args[i].equals("-help")){
                    this.printHelp();
                    System.exit(0);
                }
            }
        } catch(Exception e){
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            this.printHelp();
        }

        if(this.url==null || this.repo==null){
            this.printHelp();
            System.exit(0);
        }

        ListRequest listReq = new ListRequest();

        String[] statuses;
        if (this.status != null) {
            statuses = this.status.trim().split(",");
            for (String status: statuses) {
                status = status.trim();
                if (!status.equals("")) {
                    listReq.addResStatus(status);
                }
            }
        }

        if (this.description != null && !this.description.equals("")) {
            listReq.setDescription(this.description);
        }

        String[] links;
        if (this.endpoint != null) {
            links = this.endpoint.trim().split(",");
            for (String link: links) {
                link = link.trim();
                if (!link.equals("")) {
                    listReq.addLinkId(link);
                }
            }
        }

        String[] vlanTagList;
        if (this.vlan != null) {
            String[] vlans = this.vlan.trim().split(",");
            for (String v: vlans) {
                v = v.trim();
                if (!v.equals("")) {
                    VlanTag vlanTag = new VlanTag();
                    vlanTag.setString(v);
                    vlanTag.setTagged(true);
                    listReq.addVlanTag(vlanTag);
                }
            }
        }

        listReq.setResRequested(this.numResults);

        return listReq;
    }

    public String getUrl(){
        return url;
    }

    public String getRepo(){
        return repo;
    }


    public ResDetails[] filterResults(ResDetails[] details) {
        ResDetails[] temp = new ResDetails[details.length];
        for (int i = 0; i < details.length; i++) {
            temp[i] = null;
        }

        int numFilteredResults = 0;
        for (ResDetails detail : details) {

            PathInfo pathInfo = detail.getPathInfo();
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

            boolean isResult = true;

            if (this.between_a != null) {
                if (resvSrc.equals(this.between_a) && resvDest.equals(this.between_b)) {
                    isResult = true;
                } else if (resvSrc.equals(this.between_b) && resvDest.equals(this.between_a)) {
                    isResult = true;
                } else {
                    isResult = false;
                }
            } else {
                if (this.src != null && !this.src.equals("")) {
                    if (resvSrc.equals(this.src)) {
                        isResult = true;
                    } else {
                        isResult = false;
                    }
                }
                if (this.dst != null && !this.dst.equals("")) {
                    if (resvDest.equals(this.dst)) {
                        isResult = true;
                    } else {
                        isResult = false;
                    }
                }
            }
            if (isResult) {
                temp[numFilteredResults] = detail;
                numFilteredResults++;
            }
        }


        ResDetails[] filteredResults = new ResDetails[numFilteredResults];
        for (int i = 0; i < numFilteredResults; i++) {
            filteredResults[i] = temp[i];
        }
        return filteredResults;

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


    public void printHelp(){
         System.out.println("General Parameters:");
         System.out.println("\t-help\t displays this message.");
         System.out.println("\t-url\t required. the url of the IDC.");
         System.out.println("\t-repo\t required. the location of the repo directory");
         System.out.println("\t[-status \"STATUS\"]\t retrieve reservations with status matching the argument. Use commas to separate values. \n\t\tExample: -status \"ACTIVE,PENDING\"");
         System.out.println("\t[-desc \"DESCRIPTION\"]\t . retrieve reservations with description matching the argument. \n\t\tExample: -desc \"PRODUCTION\"");
         System.out.println("\t[-endpoint \"ENDPOINT\"]\t . retrieve reservations starting, ending, or passing over ENDPOINT(s). Use commas to separate values. \n\t\tExample: -endpoint \"lambdastation.unl.edu,lambdastation.caltech.edu\"");
         System.out.println("\t[-src \"SOURCE]\"\t . retrieve reservations with a source of SOURCE. \n\t\tExample: -src \"lambdastation.unl.edu\"");
         System.out.println("\t[-dst \"DESTINATION\"]\t . retrieve reservations with destination of DESTINATION\n\t\tExample: -dst \"lambdastation.unl.edu\"");
         System.out.println("\t[-between \"ENDPOINT_A,ENDPOINT_B\"]\t . retrieve reservations in either direction between ENDPOINT_A and ENDPOINT_B. Do not use with -src and -dst.\n\t\tExample: -between \"lambdastation.unl.edu,lambdastation.caltech.edu\"");
         System.out.println("\t[-dot DOT_FILENAME]\t . Export list results in DOT file format for visualization with the GraphViz library.\n\t\tExample: -dot graph.dot");
         System.out.println("\t[-topnode \"NODE_URN\"]\t . Specify node URN (comma-separate values for multiple) to be set at the top of the graph when creating the DOT file. Used to clean up some graphs. \n\t\tExample: -topnode \"urn:ogf:network:es.net:lbl-mr1\"");
    }



    public void createDOT(ResDetails[] resList) throws IOException {
        if (this.dotfile != null) {
            GraphVizExporter gve = new GraphVizExporter();
            String output = gve.exportReservations(resList, this.topNodes);
            gve.writeDotSourceToFile(output, this.dotfile);
            System.out.println("DOT output in "+this.dotfile);
        }
    }

    public static void main(String[] args){
        /* Initialize Values */
        ListReservationCLI cli = new ListReservationCLI();
        Client oscarsClient = new Client();
        ListRequest listReq = cli.readArgs(args);
        ListReply response = null;
        String url = cli.getUrl();
        String repo = cli.getRepo();

        /* Initialize client instance */
        try {
            oscarsClient.setUp(true, url, repo);

            /* Send Request */
            response = oscarsClient.listReservations(listReq);
            int numResults = response.getTotalResults();
            if (numResults == 0) {
                System.out.println("Empty results");
                return;
            }
            ResDetails[] details = response.getResDetails();
            ResDetails[] filteredDetails = cli.filterResults(details);

            System.out.println("Results: "+filteredDetails.length);

            for(int i = 0; filteredDetails != null && i < filteredDetails.length; i++){
                cli.printResDetails(filteredDetails[i]);
            }
            cli.createDOT(filteredDetails);



 //           System.out.println(numResults + " reservations match request.");
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            System.out.println("Error: "+e.getMessage());
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Error: "+e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AAAFaultMessage e) {
            // TODO Auto-generated catch block
            System.out.println("Error: "+e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Error: "+e.getMessage());
            e.printStackTrace();
        }


    }



}
