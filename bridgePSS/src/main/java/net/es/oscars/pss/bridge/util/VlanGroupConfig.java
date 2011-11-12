package net.es.oscars.pss.bridge.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

public class VlanGroupConfig {
    private static Logger log = Logger.getLogger(VlanGroupConfig.class);
    public static HashMap<String, ArrayList<String>> vlanGroups = new HashMap<String, ArrayList<String>>();

    public static void configure() throws PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        Map<String, String> vlanGroupConfig;
        try {
            cc.loadManifest(ServiceNames.SVC_PSS,  ConfigDefaults.MANIFEST); // manifest.yaml
            String configFilePath = cc.getFilePath("config-vlan-groups.yaml");
            InputStream propFile = new FileInputStream(new File(configFilePath));
            vlanGroupConfig = (Map<String, String>) Yaml.load(propFile);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new PSSException(e);
        }
        for (String group : vlanGroupConfig.keySet()) {
            String groupConfig  = vlanGroupConfig.get(group);
            vlanGroups.put(group, new ArrayList<String>());
            
            String[] ranges = groupConfig.split("\\,");
            for (String range : ranges) {
                range = range.trim();
                if (range.contains("-")) {
                    String[] parts = range.split("-");
                    String start = parts[0];
                    String end = parts[1];
                    log.debug("start: "+start+" end:"+end);
                    for (int i = Integer.valueOf(start); i <= Integer.valueOf(end); i++) {
                        vlanGroups.get(group).add(i+"");
                    }
                } else {
                    String vlan = range.trim();
                    log.debug("vlan: "+vlan);
                    vlanGroups.get(group).add(vlan);
                }
            }
        }
        for (String group : vlanGroups.keySet()) {
            String out = group+ ": ";
            for (String vlan : vlanGroups.get(group)) {
                out += vlan+", ";
            }
            log.debug(out);

        }
    }
}
