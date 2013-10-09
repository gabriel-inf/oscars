package net.es.oscars.topoUtil.output;

import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.spec.*;
import net.es.oscars.topoUtil.beans.spec.NetworkSpec;
import net.es.oscars.topoUtil.util.TemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OscarsOutputter {
    static final Logger LOG = LoggerFactory.getLogger(OscarsOutputter.class);
    public static String outputOscars(NetworkSpec networkSpec) throws IOException, TemplateException {
        Map root = new HashMap<String, Object>();
        Map base = new HashMap<String, Object>();
        base.put("idcId", networkSpec.getIdcId());
        base.put("topologyId", networkSpec.getTopologyId());
        base.put("domainId", networkSpec.getDomainId());
        root.put("base", base);
        ArrayList devices = new ArrayList();
        for (DeviceSpec deviceSpec : networkSpec.getDevices()) {
            LOG.debug("-"+ deviceSpec.getName());
            Map deviceMap = new HashMap<String, Object>();
            deviceMap.put("address", deviceSpec.getLoopback());
            deviceMap.put("name", deviceSpec.getName());

            ArrayList ports = new ArrayList();
            for (IfceSpec ifceSpec : deviceSpec.getIfces()) {
                LOG.debug("--"+ ifceSpec.getName());
                Map portMap = new HashMap<String, Object>();

                portMap.put("name", ifceSpec.getName());
                portMap.put("capacity", ifceSpec.getCapacity());

                if (ifceSpec.getReservable() == null) {
                    portMap.put("maxResCap", ifceSpec.getCapacity());
                } else {
                    portMap.put("maxResCap", ifceSpec.getReservable());
                }
                portMap.put("minResCap", 1000000);
                portMap.put("granularity", 1000000);
                ArrayList links = new ArrayList();

                for (CustomerLinkSpecSpec cl : ifceSpec.getCustLinks()) {
                    LOG.debug("---"+cl.getName());
                    Map linkMap = new HashMap<String, Object>();
                    linkMap.put("name", cl.getName());
                    linkMap.put("vlanRange", cl.getVlanRangeExpr());
                    linkMap.put("isMpls", false);
                    linkMap.put("canTranslate", "true");
                    linkMap.put("mtu", 9000);


                    linkMap.put("metric", 100000);
                    links.add(linkMap);
                }

                for (EthInternalLinkSpec el : ifceSpec.getEthLinks()) {
                    LOG.debug("---"+el.getName());
                    Map linkMap = new HashMap<String, Object>();
                    linkMap.put("name", el.getName());
                    linkMap.put("vlanRange", el.getVlanRangeExpr());
                    linkMap.put("isMpls", false);
                    linkMap.put("canTranslate", "true");
                    linkMap.put("mtu", 9000);
                    linkMap.put("metric", el.getMetric());
                    linkMap.put("remoteId", el.getRemote());
                    links.add(linkMap);
                }

                for (MplsInternalLinkSpec ml : ifceSpec.getMplsLinks()) {
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

        return TemplateLoader.populateTemplate(root, "config/templates", "oscars.ftl");


    }
}
