package net.es.oscars.pss.bridge.cisco.nexus;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.TemplateConfigGen;

public class BridgeCiscoNexusConfigGen extends TemplateConfigGen {
    private static BridgeCiscoNexusConfigGen instance;
    
    public static BridgeCiscoNexusConfigGen getInstance() {
        if (instance == null) instance = new BridgeCiscoNexusConfigGen();
        return instance;
    }
    
    
    private BridgeCiscoNexusConfigGen() { }
    
    public String generateL2Setup(String portA, String portZ, Integer vlan, String description)  throws PSSException {
        String templateFileName = "bridge-cisco-nexus-setup.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("description", description);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    public String generateL2Teardown(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-cisco-nexus-teardown.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    
}