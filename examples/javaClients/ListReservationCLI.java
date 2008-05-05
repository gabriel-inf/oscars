import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.omg.CORBA_2_3.portable.InputStream;

import net.es.oscars.client.Client;
import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.PSLookupClient;
import net.es.oscars.oscars.AAAFaultMessage;
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
    private int numResults = 10;;

    public ListRequest readArgs(String[] args){
        PSLookupClient lookupClient = new PSLookupClient();

        java.io.InputStream lookupXMLStream  = this.getClass().getClassLoader().getResourceAsStream("perfSONAR-LSQuery.xml");
//        this.getClass().getClassLoader().getResource("xxx").

        try {

            StringBuilder xmlRequestBuilder = new StringBuilder("");
            byte[] buf = new byte[1500];
            while(lookupXMLStream.read(buf, 0, buf.length) > 0){
                char[] tmp = new char[1500];
                for (int i = 0; i < buf.length; i++) {
                    tmp[i] = (char) buf[i];
                }
                xmlRequestBuilder.append(tmp);
            }
            String xmlRequest = xmlRequestBuilder.toString();

            lookupClient.setXmlRequest(xmlRequest);
            lookupClient.setUrl("http://packrat.internet2.edu:8009/perfSONAR_PS/services/LS");
        } catch (IOException ex) {
            System.out.println("IO error: "+ex.getMessage());
            System.exit(1);

        }
        /* Set request parameters */
        try{
            for(int i = 0; i < args.length; i++){
                if(args[i].equals("-url")){
                    this.url = args[i+1].trim();
                }else if(args[i].equals("-repo")){
                    this.repo = args[i+1].trim();
                }else if(args[i].equals("-status")){
                    this.status = args[i+1].trim();
                }else if(args[i].equals("-vlan")){
                    this.vlan = args[i+1].trim();
                }else if(args[i].equals("-desc")){
                    this.description = args[i+1].trim();
                }else if(args[i].equals("-endpoint")){
                    this.endpoint = args[i+1].trim();
                }else if(args[i].equals("-between")){
                    String between = args[i+1].trim();
                    String[] parts = between.split(",");
                    if (parts.length != 2) {
                        System.out.println("Error: use two comma separated values for -between.\n");
                        System.exit(1);
                    }
                    this.between_a = parts[0].trim();
                    this.between_b = parts[1].trim();
                    if (!this.between_a.matches("urn\\:ogf\\:network")) {
                        try {
                            this.between_a = lookupClient.lookup(this.between_a).trim();
                        } catch (LookupException ex) {
                            System.out.println("Error: could not resolve ENDPOINT_A.\n\t"+ex.getMessage());
                            System.exit(1);
                        }
                    }
                    if (!this.between_b.matches("urn\\:ogf\\:network")) {
                        try {
                            this.between_b = lookupClient.lookup(this.between_b).trim();
                        } catch (LookupException ex) {
                            System.out.println("Error: could not resolve ENDPOINT_B.\n\t"+ex.getMessage());
                            System.exit(1);
                        }
                    }

                }else if(args[i].equals("-src")){
                    this.src = args[i+1].trim();
                    if (!this.src.matches("urn\\:ogf\\:network")) {
                        try {
                            this.src = lookupClient.lookup(this.src).trim();
                        } catch (LookupException ex) {
                            System.out.println("Error: could not resolve src.\n\t"+ex.getMessage());
                            System.exit(1);
                        }
                    }
                }else if(args[i].equals("-dst")){
                    this.dst= args[i+1].trim();
                    if (!this.dst.matches("urn\\:ogf\\:network")) {
                        try {
                            this.dst = lookupClient.lookup(this.dst).trim();
                        } catch (LookupException ex) {
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

         boolean isResult = true;

         if (this.between_a != null) {
             /*
             System.out.println("between_a:"+this.between_a);
             System.out.println("between_b:"+this.between_b);
             System.out.println("resvSrc:"+resvSrc);
             System.out.println("resvDest:"+resvDest);
             */
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
         if(layer3Info != null){	                 isResult = true;

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
         if (isResult) {
             System.out.println(output);
         }
    }
    public void printHelp(){
         System.out.println("General Parameters:");
         System.out.println("\t-help\t displays this message.");
         System.out.println("\t-url\t required. the url of the IDC.");
         System.out.println("\t-repo\t required. the location of the repo directory");
         System.out.println("\t[-status STATUS]\t retrieve reservations with status matching the argument. Use commas to separate values. \n\t\tExample: -status \"ACTIVE,PENDING\"");
         System.out.println("\t[-desc DESCRIPTION]\t . retrieve reservations with description matching the argument. \n\t\tExample: -desc \"PRODUCTION\"");
         System.out.println("\t[-endpoint ENDPOINT]\t . retrieve reservations starting, ending, or passing over ENDPOINT(s). Use commas to separate values. \n\t\tExample: -endpoint \"lambdastation.unl.edu,lambdastation.caltech.edu\"");
         System.out.println("\t[-src SOURCE]\t . retrieve reservations with a source of SOURCE. \n\t\tExample: -src \"lambdastation.unl.edu\"");
         System.out.println("\t[-dst DESTINATION]\t . retrieve reservations with destination of DESTINATION\n\t\tExample: -dst \"lambdastation.unl.edu\"");
         System.out.println("\t[-between ENDPOINT_A,ENDPOINT_B]\t . retrieve reservations in either direction between ENDPOINT_A and ENDPOINT_B. Do not use with -src and -dst.\n\t\tExample: -between \"lambdastation.unl.edu,lambdastation.caltech.edu\"");
    }

    public static void main(String[] args){
        /* Initialize Values */
         ListReservationCLI cli = new ListReservationCLI();
        Client oscarsClient = new Client();
        ListRequest listReq = cli.readArgs(args);
        String url = cli.getUrl();
        String repo = cli.getRepo();

        // to be used if / when we have reverse lookup
        String lookupURL = "http://packrat.internet2.edu:8009/perfSONAR_PS/services/LS";
        PSLookupClient lookupClient = new PSLookupClient();
        lookupClient.setFname("repo/perfSONAR-LSQuery.xml");
        lookupClient.setUrl(lookupURL);

        /* Initialize client instance */
        try {
            oscarsClient.setUp(true, url, repo);

            /* Send Request */
            ListReply response = oscarsClient.listReservations(listReq);
            ResDetails[] details = response.getResDetails();
            int numResults = response.getTotalResults();

            for(int i = 0; details != null && i < details.length; i++){
                cli.printResDetails(details[i]);
            }

 //           System.out.println(numResults + " reservations match request.");
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AAAFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
   }
}
