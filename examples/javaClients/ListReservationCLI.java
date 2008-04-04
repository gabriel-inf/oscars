import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

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
import net.es.oscars.wsdlTypes.ResDetails;

public class ListReservationCLI {
    private String url;
    private String repo;
    private String status = null;
    private String description = null;
    private String endpoint = null;
    private int numResults = 10;;

    public ListRequest readArgs(String[] args){
        /* Set request parameters */
        try{
            for(int i = 0; i < args.length; i++){
                if(args[i].equals("-url")){
                    this.url = args[i+1].trim();
                }else if(args[i].equals("-repo")){
                    this.repo = args[i+1].trim();
                }else if(args[i].equals("-status")){
                    this.status = args[i+1].trim();
                }else if(args[i].equals("-desc")){
                    this.description = args[i+1].trim();
                }else if(args[i].equals("-endpoint")){
                    this.endpoint = args[i+1].trim();
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
         /* Print response information */
         System.out.println("GRI: " + response.getGlobalReservationId());
         System.out.println("Login: " + response.getLogin());
         System.out.println("Status: " + response.getStatus());
         System.out.println("Start Time: " + response.getStartTime());
         System.out.println("End Time: " + response.getEndTime());
         System.out.println("Time of request: " + response.getCreateTime());
         System.out.println("Bandwidth: " + response.getBandwidth());
         System.out.println("Description: " + response.getDescription());
         System.out.println("Path Setup Mode: " + pathInfo.getPathSetupMode());
         if(layer2Info != null){
             System.out.println("Source Endpoint: " + layer2Info.getSrcEndpoint());
             System.out.println("Destination Endpoint: " + layer2Info.getDestEndpoint());
             System.out.println("Source VLAN: " + layer2Info.getSrcVtag());
             System.out.println("Destination VLAN: " + layer2Info.getDestVtag());
         }
         if(layer3Info != null){
             System.out.println("Source Host: " + layer3Info.getSrcHost());
             System.out.println("Destination Host: " + layer3Info.getDestHost());
             System.out.println("Source L4 Port: " + layer3Info.getSrcIpPort());
             System.out.println("Destination L4 Port: " + layer3Info.getDestIpPort());
             System.out.println("Protocol: " + layer3Info.getProtocol());
             System.out.println("DSCP: " + layer3Info.getDscp());
         }
         if(mplsInfo != null){
             System.out.println("Burst Limit: " + mplsInfo.getBurstLimit());
             System.out.println("LSP Class: " + mplsInfo.getLspClass());
         }
         System.out.println("Path: ");
         for (CtrlPlaneHopContent hop : path.getHop()){
             System.out.println("\t" + hop.getLinkIdRef());
         }
    }
    public void printHelp(){
         System.out.println("General Parameters:");
         System.out.println("\t-help\t displays this message.");
         System.out.println("\t-url\t required. the url of the IDC.");
         System.out.println("\t-repo\t required. the location of the repo directory");
         System.out.println("\t[-status STATUS]\t retrieve reservations with status matching the argument. Use commas to separate values. \n\t\tExample: -status \"ACTIVE,PENDING\"");
         System.out.println("\t[-desc DESCRIPTION]\t . retrieve reservations with description matching the argument. \n\t\tExample: -desc \"PRODUCTION\"");
         System.out.println("\t[-endpoint ENDPOINT]\t . retrieve reservations affecting this endpoint. Use commas to separate values. \n\t\tExample: -endpoint \"lambdastation.unl.edu,lambdastation.caltech.edu\"");
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

            System.out.println(numResults + " reservations match request.");
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
