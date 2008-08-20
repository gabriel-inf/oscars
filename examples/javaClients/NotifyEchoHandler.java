import net.es.oscars.client.*;
import org.jdom.*;
import java.util.*;

/**
 * Example NotifyHandler that prints Notify messages as they are received. Includes a main()
 * method that starts a listener. 
 */
public class NotifyEchoHandler implements NotifyHandler{
    /**
     * Called when a Notify message is received. Prints the given NotificationMessage.
     *
     * @param notification a NotificationMessage JDOM Element from a Notify message
     */
    public void handleNotify(Element notification){
        Element message = notification.getChild("Message", NotifyListener.WSN_NS);
        Element event = message.getChild("event", NotifyListener.IDC_NS);
        if(event == null){
            System.out.println("No IDC event found!");
        }
        System.out.println("Event Type: " + event.getChildText("type", NotifyListener.IDC_NS));
        System.out.println("Timestamp: " + event.getChildText("timestamp", NotifyListener.IDC_NS));
        System.out.println("Triggered by user: " + event.getChildText("userLogin", NotifyListener.IDC_NS));
        
        String errCode = event.getChildText("errorCode", NotifyListener.IDC_NS);
        String errMsg = event.getChildText("errorMessage", NotifyListener.IDC_NS);
        if(errCode != null && errMsg != null){
            System.out.println("Error Code: " + errCode);
            System.out.println("Error Message: " + errMsg);
        }
        
        Element resDetails = event.getChild("resDetails", NotifyListener.IDC_NS);
        if(resDetails == null){
            return;
        }
        System.out.println("Reservation Details: ");
        System.out.println("GRI: " +  resDetails.getChildText("globalReservationId", NotifyListener.IDC_NS));
        System.out.println("Login: " +  resDetails.getChildText("login", NotifyListener.IDC_NS));
        System.out.println("Status: " +  resDetails.getChildText("status", NotifyListener.IDC_NS));
        System.out.println("Start Time: " +  resDetails.getChildText("startTime", NotifyListener.IDC_NS));
        System.out.println("End Time: " +  resDetails.getChildText("endTime", NotifyListener.IDC_NS));
        System.out.println("Create Time: " +  resDetails.getChildText("createTime", NotifyListener.IDC_NS));
        System.out.println("Bandwidth: " +  resDetails.getChildText("bandwidth", NotifyListener.IDC_NS));
        System.out.println("Description: " +  resDetails.getChildText("description", NotifyListener.IDC_NS));
        
        Element pathInfo = resDetails.getChild("pathInfo", NotifyListener.IDC_NS);
        System.out.println("Path Setup Mode: " +  pathInfo.getChildText("pathSetupMode", NotifyListener.IDC_NS));
        Element path = pathInfo.getChild("path", NotifyListener.IDC_NS);
        if(path != null){
            System.out.println("Inter-domain Path: ");
            List<Element> hops = path.getChildren("hop", NotifyListener.NMWG_CP_NS);
            for(Element hop : hops){
                String linkIdRef = hop.getChildText("linkIdRef", NotifyListener.NMWG_CP_NS);
                if(linkIdRef != null){
                    System.out.println("    " + linkIdRef + " (REF)");
                    continue;
                }
                String portIdRef = hop.getChildText("portIdRef", NotifyListener.NMWG_CP_NS);
                if(portIdRef != null){
                    System.out.println("    " + portIdRef + " (REF)");
                    continue;
                }
                String nodeIdRef = hop.getChildText("nodeIdRef", NotifyListener.NMWG_CP_NS);
                if(nodeIdRef != null){
                    System.out.println("    " + nodeIdRef + " (REF)");
                    continue;
                }
                String domainIdRef = hop.getChildText("domainIdRef", NotifyListener.NMWG_CP_NS);
                if(domainIdRef != null){
                    System.out.println("    " + domainIdRef + " (REF)");
                    continue;
                }
                Element link = hop.getChild("link", NotifyListener.NMWG_CP_NS);
                if(link != null){
                    System.out.print("    " + link.getAttributeValue("id"));
                    Element swcap = link.getChild("SwitchingCapabilityDescriptors", NotifyListener.NMWG_CP_NS);
                    String encodingType = swcap.getChildText("encodingType", NotifyListener.NMWG_CP_NS);
                    System.out.print(" (" + encodingType);
                    if("ethernet".equals(encodingType)){
                        Element swcapInfo = swcap.getChild("switchingCapabilitySpecificInfo", NotifyListener.NMWG_CP_NS);
                        String vlans = swcapInfo.getChildText("vlanRangeAvailability", NotifyListener.NMWG_CP_NS);
                        System.out.print(": " + vlans);
                    }
                    System.out.print(")\n");
                    continue;
                }
                Element port = hop.getChild("port", NotifyListener.NMWG_CP_NS);
                if(port != null){
                    System.out.println("    " + port.getChildText("id"));
                    continue;
                }
                Element node = hop.getChild("node", NotifyListener.NMWG_CP_NS);
                if(node != null){
                    System.out.println("    " + node.getChildText("id"));
                    continue;
                }
                Element domain = hop.getChild("domain", NotifyListener.NMWG_CP_NS);
                if(domain != null){
                    System.out.println("    " + domain.getChildText("id"));
                }
            }
        }
        
        Element l2Info = pathInfo.getChild("layer2Info", NotifyListener.IDC_NS);
        if(l2Info != null){
            System.out.println("Layer 2 Info:");
            System.out.println("    Source: " +  l2Info.getChildText("srcEndpoint", NotifyListener.IDC_NS));
            System.out.println("    Destination: " +  l2Info.getChildText("destEndpoint", NotifyListener.IDC_NS));
            System.out.println("    VLAN: " +  l2Info.getChildText("srcVtag", NotifyListener.IDC_NS));
        }
        
        Element l3Info = pathInfo.getChild("layer3Info", NotifyListener.IDC_NS);
        if(l3Info != null){
            System.out.println("Layer 3 Info:");
            System.out.println("    Source: " +  l3Info.getChildText("srcHost", NotifyListener.IDC_NS));
            System.out.println("    Destination: " +  l3Info.getChildText("destHost", NotifyListener.IDC_NS));
            System.out.println("    Transport Protocol: " +  l3Info.getChildText("protocol", NotifyListener.IDC_NS));
            System.out.println("    Source Port: " +  l3Info.getChildText("srcIpPort", NotifyListener.IDC_NS));
            System.out.println("    Destination Port: " +  l3Info.getChildText("destIpPort", NotifyListener.IDC_NS));
            System.out.println("    DSCP: " +  l3Info.getChildText("dscp", NotifyListener.IDC_NS));
        }
        
        Element mplsInfo = pathInfo.getChild("mplsInfo", NotifyListener.IDC_NS);
        if(mplsInfo != null){
            System.out.println("MPLS Info:");
            System.out.println("    Burst Limit: " +  mplsInfo.getChildText("burstLimit", NotifyListener.IDC_NS));
            System.out.println("    LSP Class: " +  mplsInfo.getChildText("lspClass", NotifyListener.IDC_NS));
        }
        System.out.println();
    }
    
    /**
     * Called when an error, such as a parsing error, occurs in NotifyListener
     *
     * @param type the type of error (SOCKET, IO, PARSING, JDOM, GENERAL)
     * @param e the exception to report
     */
    public void handleError(String type, Exception e){
        System.out.println("Error type: " + type);
        System.out.println("Error Message: " + e.getMessage());
    }
    
    public static void main(String[] args){
        /* parse arguments */
        boolean useSSL = false;
        int port = 8070;
        int state = 0;
        for(String arg : args){
            if("-ssl".equals(arg)){
                useSSL = true;
            }else if("-port".equals(arg)){
                state = 1;
            }else if(state == 1){
                port = Integer.parseInt(arg);
                state = 0;
            }
        }
        
        /* Start listening */
        try{
            NotifyEchoHandler handler = new NotifyEchoHandler();
            NotifyListener listener = new NotifyListener(port, handler, useSSL);
            listener.start();
            System.out.println("Listening on port " + port + "...");
            /* NOTE: You could shut it down after 5 seconds with the following code:
            Thread.sleep(5000);
            listener.shutdown(); */
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}