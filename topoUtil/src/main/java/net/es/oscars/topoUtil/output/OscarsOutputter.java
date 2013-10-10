package net.es.oscars.topoUtil.output;

import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.*;
import net.es.oscars.topoUtil.config.OscarsConfig;
import net.es.oscars.topoUtil.config.OscarsConfigProvider;
import net.es.oscars.topoUtil.config.SpringContext;
import net.es.oscars.topoUtil.util.TemplateLoader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OscarsOutputter implements Outputter {
    static final Logger LOG = LoggerFactory.getLogger(OscarsOutputter.class);
    public void output(Network network) throws IOException, TemplateException {

        SpringContext sc = SpringContext.getInstance();

        ApplicationContext ax = sc.getContext();

        OscarsConfigProvider ocp = ax.getBean("oscarsConfigProvider", OscarsConfigProvider.class);
        OscarsConfig oc = ocp.getOscarsConfig();


        Map root = new HashMap<String, Object>();
        Map base = new HashMap<String, Object>();
        base.put("idcId", oc.getIdcId());
        base.put("topologyId", oc.getTopologyId());
        base.put("domainId", oc.getDomainId());
        root.put("base", base);
        ArrayList devices = new ArrayList();
        for (Device device: network.getDevices()) {
            LOG.debug("-"+ device.getName());
            Map deviceMap = new HashMap<String, Object>();
            deviceMap.put("address", device.getLoopback());
            deviceMap.put("name", device.getName());

            ArrayList ports = new ArrayList();
            for (Port port : device.getPorts()) {
                LOG.debug("--"+ port.getName());
                Map portMap = new HashMap<String, Object>();

                portMap.put("name", port.getName());
                portMap.put("capacity", port.getCapacity()+"000000");

                if (port.getReservable() == null) {
                    portMap.put("maxResCap", port.getCapacity()+"000000");
                } else {
                    portMap.put("maxResCap", port.getReservable()+"000000");
                }
                portMap.put("minResCap", "1000000");
                portMap.put("granularity", "1000000");
                ArrayList links = new ArrayList();

                for (CustomerLink cl : port.getCustomerLinks()) {
                    LOG.debug("---"+cl.getName());
                    Map linkMap = new HashMap<String, Object>();
                    linkMap.put("name", cl.getName());
                    linkMap.put("vlanRange", cl.getVlanInfo().toOscarsString());
                    linkMap.put("isMpls", false);
                    linkMap.put("canTranslate", "true");
                    linkMap.put("mtu", 9000);


                    linkMap.put("metric", 100000);
                    links.add(linkMap);
                }
                for (PeeringLink pl : port.getPeeringLinks()) {
                    LOG.debug("---"+pl.getName());
                    Map linkMap = new HashMap<String, Object>();
                    linkMap.put("name", pl.getName());
                    linkMap.put("vlanRange", pl.getVlanInfo().toOscarsString());
                    linkMap.put("isMpls", false);
                    linkMap.put("canTranslate", "true");
                    linkMap.put("mtu", 9000);

                    linkMap.put("remoteId", pl.getRemote());

                    linkMap.put("metric", 1000);
                    links.add(linkMap);
                }

                for (EthInternalLink el : port.getEthLinks()) {
                    LOG.debug("---"+el.getName());
                    Map linkMap = new HashMap<String, Object>();
                    linkMap.put("name", el.getName());
                    linkMap.put("vlanRange", el.getVlanInfo().toOscarsString());
                    linkMap.put("isMpls", false);
                    linkMap.put("canTranslate", "true");
                    linkMap.put("mtu", 9000);
                    linkMap.put("metric", el.getMetric());
                    linkMap.put("remoteId", el.getRemote());
                    links.add(linkMap);
                }

                for (MplsInternalLink ml : port.getMplsLinks()) {
                    LOG.debug("---"+ml.getName());
                    Map linkMap = new HashMap<String, Object>();
                    linkMap.put("name", ml.getName());
                    linkMap.put("isMpls", true);
                    linkMap.put("canTranslate", "true");
                    linkMap.put("mtu", 9000);
                    linkMap.put("metric", ml.getMetric());
                    linkMap.put("remoteId", ml.getRemote());
                    links.add(linkMap);
                }


                portMap.put("links", links);
                ports.add(portMap);
            }
            deviceMap.put("ports", ports);
            devices.add(deviceMap);
        }
        root.put("devices", devices);

        String oscarsOutput = TemplateLoader.populateTemplate(root, "config/templates", "oscars.ftl");
        File f = new File(oc.getOutputFile());
        FileUtils.writeStringToFile(f, oscarsOutput);

    }
}
