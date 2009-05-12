package net.es.oscars.client.improved;

import java.util.Date;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

import net.es.oscars.wsdlTypes.Layer2Info;
import net.es.oscars.wsdlTypes.Layer3Info;
import net.es.oscars.wsdlTypes.MplsInfo;
import net.es.oscars.wsdlTypes.PathInfo;
import net.es.oscars.wsdlTypes.ResDetails;

public class ResvToString {

    public static String convert(ResDetails resv){
         PathInfo pathInfo = resv.getPathInfo();
         CtrlPlanePathContent path = pathInfo.getPath();
         Layer2Info layer2Info = pathInfo.getLayer2Info();
         Layer3Info layer3Info = pathInfo.getLayer3Info();
         MplsInfo mplsInfo = pathInfo.getMplsInfo();

         @SuppressWarnings("unused")
         String resvSrc = "";
         if (layer2Info != null) {
             resvSrc = layer2Info.getSrcEndpoint().trim();
         } else if (layer3Info != null) {
             resvSrc = layer3Info.getSrcHost().trim();
         }
         @SuppressWarnings("unused")
         String resvDest = "";
         if (layer2Info != null) {
             resvDest = layer2Info.getDestEndpoint().trim();
         } else if (layer3Info != null) {
             resvDest = layer3Info.getDestHost().trim();
         }

         /* Print response information */
         StringBuilder sb = new StringBuilder();
         sb.append("GRI: " + resv.getGlobalReservationId() + "\n");
         sb.append("Login: " + resv.getLogin() + "\n");
         sb.append("Status: " + resv.getStatus() + "\n");
         sb.append("Start Time: " +
            new Date(resv.getStartTime()*1000).toString() + "\n");
         sb.append("End Time: " +
            new Date(resv.getEndTime()*1000).toString() + "\n");
         sb.append("Time of request: " +
            new Date(resv.getCreateTime()*1000).toString() + "\n");
         sb.append("Bandwidth: " + resv.getBandwidth() + "\n");
         sb.append("Description: " + resv.getDescription() + "\n");
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

         CtrlPlaneHopContent[] hops = path.getHop();
         if (hops == null) {
             return sb.toString();
         }

         for (CtrlPlaneHopContent hop : hops) {
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
         return sb.toString();
    }
}
