package net.es.oscars.client.improved.list;

import java.util.ArrayList;
import java.util.Map;

import net.es.oscars.client.GraphVizExporter;
import net.es.oscars.client.improved.ConfigHelper;
import net.es.oscars.wsdlTypes.ResDetails;

public class ListDotOutputter implements ListOutputterInterface {
    @SuppressWarnings("unchecked")
    public void output(ResDetails[] resvs) {
        Map config = ConfigHelper.getInstance().getConfiguration("list.yaml");
        Map outputCfg = (Map) config.get("output");
        Map dotConfig = (Map) outputCfg.get ("ListDotOutputter");

        String graphFilename = (String) dotConfig.get("graphFilename");
        assert graphFilename != null : "Must specify graphFilename";
        ArrayList<String> topNodeList = (ArrayList<String>) dotConfig.get("nodesNearTop");

        String[] topNodes;
        if (!topNodeList.isEmpty()) {
            topNodes = new String[topNodeList.size()];
            topNodes = topNodeList.toArray(topNodes);
        } else {
            topNodes = new String[1];
            topNodes[0] = "";
        }

        try {
            GraphVizExporter gve = new GraphVizExporter();
            String dotOutput = gve.exportReservations(resvs, topNodes);
            gve.writeDotSourceToFile(dotOutput, graphFilename);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
