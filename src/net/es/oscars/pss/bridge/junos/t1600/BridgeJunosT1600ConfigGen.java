package net.es.oscars.pss.bridge.junos.t1600;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.TemplateConfigGen;

public class BridgeJunosT1600ConfigGen extends TemplateConfigGen {
    private static BridgeJunosT1600ConfigGen instance;
    
    public static BridgeJunosT1600ConfigGen getInstance() {
        if (instance == null) instance = new BridgeJunosT1600ConfigGen();
        return instance;
    }
    
    
    private BridgeJunosT1600ConfigGen() { }
    
    public String generateL2Setup(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-junos-t1600-setup.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    public String generateL2Teardown(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-junos-t1600-teardown.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }

}
