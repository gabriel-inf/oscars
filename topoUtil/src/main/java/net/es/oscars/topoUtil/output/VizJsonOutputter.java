package net.es.oscars.topoUtil.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.*;
import net.es.oscars.topoUtil.beans.viz.VizDevice;
import net.es.oscars.topoUtil.beans.viz.VizLink;
import net.es.oscars.topoUtil.beans.viz.VizRoot;
import net.es.oscars.topoUtil.config.SpringContext;
import net.es.oscars.topoUtil.config.VizConfig;
import net.es.oscars.topoUtil.config.VizConfigProvider;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;

public class VizJsonOutputter implements Outputter {
    static final Logger LOG = LoggerFactory.getLogger(VizJsonOutputter.class);
    public void output(Network network) throws IOException, TemplateException {
        VizRoot root = new VizRoot();

        for (Device dev : network.getDevices()) {
            VizDevice vd = new VizDevice();
            vd.setName(dev.getName());
            for (Port port : dev.getPorts()) {
                VizLink vl = new VizLink();
                vl.setName(port.getName());
                vd.getChildren().add(vl);
            }

            root.getDevices().add(vd);
        }


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(root);

        SpringContext sc = SpringContext.getInstance();

        ApplicationContext ax = sc.getContext();

        VizConfigProvider cp = ax.getBean("vizConfigProvider", VizConfigProvider.class);
        VizConfig cf = cp.getVizConfig();


        File f = new File(cf.getOutputFile());
        FileUtils.writeStringToFile(f, jsonOutput);
    }
}
