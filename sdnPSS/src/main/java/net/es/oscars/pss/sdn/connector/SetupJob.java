package net.es.oscars.pss.sdn.connector;

import org.apache.log4j.Logger;

import net.es.oscars.pss.sdn.connector.ISDNConnector.ISDNConnectorResponse;
import net.es.oscars.pss.sdn.openflow.OFRule;
import net.es.oscars.topoBridge.sdn.SDNCapability;
import net.es.oscars.topoBridge.sdn.SDNHop;

public class SetupJob extends Thread{
    private final Logger log = Logger.getLogger(SetupJob.class.getName());
    SDNHop hop;
    FloodlightSDNConnector connector;
    ISDNConnectorResponse response;
    String circuitID;
    OFRule rule;
    
    public SetupJob(SDNHop hop, FloodlightSDNConnector connector,String circuitID, OFRule rule){
        this.hop = hop;
        this.connector = connector;
        this.rule = rule;
        this.circuitID = circuitID;
        this.response = null;
    }

    public void run() {
        // Check for capabilities
        try{
            if (this.hop.getCapabilities().contains(SDNCapability.L2) && rule != null) {
                if (!this.hop.isEntryHop() && !this.hop.isExitHop())
                    response = this.connector.setupL2Bypass(this.hop, circuitID, rule);
                else
                    response = this.connector.setupL2Hop(this.hop, circuitID, rule);
            }else {
                response = this.connector.setupL1Hop(this.hop, circuitID);
            }
        }catch(Exception e){
            e.printStackTrace();
            this.log.error(e.getMessage());
            this.response = ISDNConnectorResponse.FAILURE;
        }
    }
    
    public ISDNConnectorResponse getResponse() {
        return response;
    }

}
