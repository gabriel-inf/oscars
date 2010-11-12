package net.es.oscars.pss.bridge.alcatel.sr;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.TemplateConfigGen;

public class BridgeAlcatelSRConfigGen extends TemplateConfigGen {
    private static BridgeAlcatelSRConfigGen instance;
    
    public static BridgeAlcatelSRConfigGen getInstance() {
        if (instance == null) instance = new BridgeAlcatelSRConfigGen();
        return instance;
    }
    
    
    private BridgeAlcatelSRConfigGen() { }
    
    public String generateL2Setup(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-alu-sr7750-setup.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    public String generateL2Teardown(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-alu-sr7750-teardown.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    
}
