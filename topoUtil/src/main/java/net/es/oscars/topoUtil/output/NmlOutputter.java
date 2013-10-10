package net.es.oscars.topoUtil.output;

import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.*;
import net.es.oscars.topoUtil.config.*;
import net.es.oscars.topoUtil.util.TemplateLoader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NmlOutputter implements Outputter {
    static final Logger LOG = LoggerFactory.getLogger(NmlOutputter.class);
    public void output(Network network) throws IOException, TemplateException {

        SpringContext sc = SpringContext.getInstance();

        ApplicationContext ax = sc.getContext();

        NmlConfigProvider cp = ax.getBean("nmlConfigProvider", NmlConfigProvider.class);
        NmlConfig nc = cp.getNmlConfig();


        Map root = new HashMap<String, Object>();
        root.put("peers", nc.getPeers());

        Map base = new HashMap<String, Object>();
        base.put("nsa", nc.getNsa());
        base.put("timestamp", new Date().toString());
        root.put("base", base);

        Map location = new HashMap<String, Object>();
        location.put("id", nc.getLocationId());
        location.put("latitude", nc.getLatitude());
        location.put("longitude", nc.getLongitude());
        root.put("location", location);

        Map service = new HashMap<String, Object>();
        service.put("id", nc.getServiceId());
        service.put("link", nc.getServiceLink());
        root.put("service", service);

        Map topo = new HashMap<String, Object>();

        topo.put("id", nc.getTopologyId());
        topo.put("name", nc.getTopologyName());
        root.put("topo", topo);

        ArrayList unis = new ArrayList();
        ArrayList nnis = new ArrayList();
        root.put("unis", unis);
        root.put("nnis", nnis);

        for (Device device: network.getDevices()) {

            for (Port port : device.getPorts()) {
                for (CustomerLink cl : port.getCustomerLinks()) {
                    String linkName = nc.getTopologyPrefix()+device.getName()+"_"+port.getName()+"_"+cl.getName();
                    Map uniMap = new HashMap<String, Object>();
                    uniMap.put("inId", linkName+"-in");
                    uniMap.put("outId", linkName+"-out");
                    uniMap.put("inVlans", cl.getVlanInfo().toOscarsString());
                    uniMap.put("outVlans", cl.getVlanInfo().toOscarsString());
                    unis.add(uniMap);
                }
                for (PeeringLink pl : port.getPeeringLinks()) {

                    if (pl.getNmlRemote() == null) continue;
                    String linkName = nc.getTopologyPrefix()+device.getName()+"_"+port.getName()+"_"+pl.getName();

                    Map nniMap = new HashMap<String, Object>();
                    nniMap.put("inId", pl.getNmlRemote());
                    nniMap.put("outId", linkName+"-out");
                    nniMap.put("inVlans", pl.getVlanInfo().toOscarsString());
                    nniMap.put("outVlans", pl.getVlanInfo().toOscarsString());

                    nnis.add(nniMap);
                }
            }
        }

        String nmlOutput = TemplateLoader.populateTemplate(root, "config/templates", "nml.ftl");
        File f = new File(nc.getOutputFile());
        FileUtils.writeStringToFile(f, nmlOutput);

    }
}
