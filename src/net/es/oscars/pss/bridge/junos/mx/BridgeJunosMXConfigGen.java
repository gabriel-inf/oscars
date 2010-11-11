package net.es.oscars.pss.bridge.junos.mx;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.TemplateConfigGen;

public class BridgeJunosMXConfigGen extends TemplateConfigGen {
    private static BridgeJunosMXConfigGen instance;
    
    public static BridgeJunosMXConfigGen getInstance() {
        if (instance == null) instance = new BridgeJunosMXConfigGen();
        return instance;
    }
    
    
    private BridgeJunosMXConfigGen() { }
    
    public String generateL2Setup(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-junos-mx-setup.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan);
        
        return this.getConfig(root, templateFileName);
    }
    public String generateL2Teardown(String portA, String portZ, Integer vlan)  throws PSSException {
        String templateFileName = "bridge-junos-mx-teardown.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan);
        
        return this.getConfig(root, templateFileName);
    }

}
