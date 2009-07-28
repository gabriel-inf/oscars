package net.es.oscars.client.improved.list;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import net.es.oscars.bss.topology.URNParser;
import net.es.oscars.client.improved.ConfigHelper;
import net.es.oscars.wsdlTypes.Layer2Info;
import net.es.oscars.wsdlTypes.Layer3Info;
import net.es.oscars.wsdlTypes.ResDetails;

public class ListMaintDbOutputter implements ListOutputterInterface {
    @SuppressWarnings("unchecked")
    public void output(ResDetails[] resvs) {
        Map config = ConfigHelper.getInstance().getConfiguration("list.yaml");
        Map outputCfg = (Map) config.get("output");

        Map maintDbConfig = (Map) outputCfg.get ("ListMaintDbOutputter");

        String maintDbFilename = (String) maintDbConfig.get("maintDbFilename");
        assert maintDbFilename != null : "Must specify maintDbFilename";



        Calendar cal = Calendar.getInstance();
        String nowSecs = ""+cal.getTimeInMillis()/1000;
        String output = "";
        for (ResDetails resv : resvs) {
            String gri = resv.getGlobalReservationId();
            String prefix = "oscars-+-"+gri+"-+-";

            String startTime = ""+resv.getStartTime();
            String endTime = ""+resv.getEndTime();
            String creator = resv.getLogin();
            String desc = resv.getDescription();
            Layer2Info l2Info = resv.getPathInfo().getLayer2Info();
            Layer3Info l3Info = resv.getPathInfo().getLayer3Info();

            String layer = "";
            String src = "";
            String dst = "";
            if (l2Info != null) {
                layer = "L2";
                src = l2Info.getSrcEndpoint();
                dst = l2Info.getDestEndpoint();
            } else if (l3Info != null) {
                layer = "L3";
                src = l3Info.getSrcHost();
                dst = l3Info.getDestHost();
            } else {
                System.err.println("WARN: Both L2info and L3info are null for: "+gri);
                continue;
            }


            CtrlPlaneHopContent[] hops = resv.getPathInfo().getPath().getHop();

            String hopOutput = "";
            String carrier = "";

            int i = 0;
            for (CtrlPlaneHopContent hop : hops) {
                String fullLinkId = hop.getLink().getId();
                Hashtable<String, String> parsed = URNParser.parseTopoIdent(fullLinkId);
                String domainId = parsed.get("domainId");
                if (i == 0) {
                    carrier = domainId;
                }
                String nodeId = parsed.get("nodeId");
                String portId = parsed.get("portId");
                String linkId = parsed.get("linkId");
                String outHop = nodeId+":";
                if (linkId.equals("*")) {
                    outHop += portId+"."+hop.getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
                } else {
                    outHop += linkId;
                }
                // TODO: support for multiple paths
                hopOutput += prefix+"path-+-0-+-"+i+"-+-hop-+-"+outHop+"\n";
                i++;
            }
            Integer bw = resv.getBandwidth();

            output += prefix +"carrier-+-"+carrier+"\n";
            output += prefix +"id-+-"+gri+"\n";
            output += prefix +"layer-+-"+layer+"\n";
            output += prefix +"last_update-+-"+nowSecs+"\n";
            output += prefix +"creator-+-"+creator+"\n";
            output += prefix +"contact-+-"+creator+"\n";
            output += prefix +"description-+-"+desc+"\n";
            output += prefix +"startTime-+-"+startTime+"\n";
            output += prefix +"endTime-+-"+endTime+"\n";
            output += prefix +"source-+-"+src+"\n";
            output += prefix +"destination-+-"+dst+"\n";
            output += prefix +"bandwidth-+-"+bw+"000000\n";
            if (resv.getPathInfo().getLayer2Info() != null) {
                String vlan = resv.getPathInfo().getLayer2Info().getSrcVtag().getString();
                output += prefix +"vlan-+-"+vlan+"\n";
            }
            output += hopOutput;
        }

        try {
           File maintDbFile = new File(maintDbFilename);
           FileWriter fout = new FileWriter(maintDbFile);
           fout.write(output);
           fout.close();
        } catch (Exception e) {
           System.err.println("Error: I/O error while writing maintenance DB data to file: "+ maintDbFilename);
           System.exit(1);
        }

    }

}
