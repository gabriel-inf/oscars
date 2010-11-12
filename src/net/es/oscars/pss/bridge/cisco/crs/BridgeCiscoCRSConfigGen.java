package net.es.oscars.pss.bridge.cisco.crs;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.TemplateConfigGen;

public class BridgeCiscoCRSConfigGen extends TemplateConfigGen {
    private static BridgeCiscoCRSConfigGen instance;
    
    public static BridgeCiscoCRSConfigGen getInstance() {
        if (instance == null) instance = new BridgeCiscoCRSConfigGen();
        return instance;
    }
    
    
    private BridgeCiscoCRSConfigGen() { }
    
    public String generateL2Setup(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-cisco-crs-setup.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    public String generateL2Teardown(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-cisco-crs-teardown.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    
}
