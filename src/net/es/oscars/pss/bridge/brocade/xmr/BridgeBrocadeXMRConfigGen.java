package net.es.oscars.pss.bridge.brocade.xmr;

import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.TemplateConfigGen;

public class BridgeBrocadeXMRConfigGen extends TemplateConfigGen {
    private static BridgeBrocadeXMRConfigGen instance;
    
    public static BridgeBrocadeXMRConfigGen getInstance() {
        if (instance == null) instance = new BridgeBrocadeXMRConfigGen();
        return instance;
    }
    
    
    private BridgeBrocadeXMRConfigGen() { }
    
    public String generateL2Setup(String portA, String portZ, Integer vlan, String description)  throws PSSException {
        String templateFileName = "bridge-brocade-xmr-setup.txt";
        if (portA.equals("lag1")) {
            portA = "ethernet 1/1 ethernet 2/1 ethernet 3/1 ethernet 4/1";
        } else if (portZ.equals("lag1")) {
            portZ = "ethernet 1/1 ethernet 2/1 ethernet 3/1 ethernet 4/1";
        }
        if (portA.equals("lag2")) {
            portA = "ethernet 1/3 ethernet 2/3 ethernet 3/3 ethernet 4/3";
        } else if (portZ.equals("lag2")) {
            portZ = "ethernet 1/3 ethernet 2/3 ethernet 3/3 ethernet 4/3";
        }

        if (portA.equals("lag3")) {
            portA = "ethernet 1/2 ethernet 2/2 ethernet 2/4 ethernet 3/2 ethernet 3/4 ethernet 4/2 ethernet 4/4 ethernet 5/2 ethernet 6/2";
        } else if (portZ.equals("lag3")) {
            portZ = "ethernet 1/2 ethernet 2/2 ethernet 2/4 ethernet 3/2 ethernet 3/4 ethernet 4/2 ethernet 4/4 ethernet 5/2 ethernet 6/2";
        }
        
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
        String templateFileName = "bridge-brocade-xmr-teardown.txt";
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("vlan", vlan.toString());
        
        return this.getConfig(root, templateFileName);
    }
    
}
