package net.es.oscars.topoUtil.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.*;
import net.es.oscars.topoUtil.beans.spec.DeviceSpec;
import net.es.oscars.topoUtil.beans.spec.IfceSpec;
import net.es.oscars.topoUtil.beans.viz.VizDevice;
import net.es.oscars.topoUtil.beans.viz.VizLink;
import net.es.oscars.topoUtil.beans.viz.VizRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VizJsonOutputter {
    static final Logger LOG = LoggerFactory.getLogger(VizJsonOutputter.class);
    public static String outputViz(Network network) throws IOException, TemplateException {
        VizRoot root = new VizRoot();

        for (DeviceSpec dev : network.getDeviceSpecs()) {
            VizDevice vd = new VizDevice();
            vd.setName(dev.getName());
            for (IfceSpec ifceSpec : dev.getIfces()) {
                VizLink vl = new VizLink();
                vl.setName(ifceSpec.getName());
                vd.getChildren().add(vl);
            }

            root.getDevices().add(vd);
        }


        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(root);
    }
}
