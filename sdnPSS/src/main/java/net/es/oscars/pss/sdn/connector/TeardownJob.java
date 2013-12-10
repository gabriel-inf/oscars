package net.es.oscars.pss.sdn.connector;

import net.es.oscars.pss.sdn.connector.ISDNConnector.ISDNConnectorResponse;
import net.es.oscars.topoBridge.sdn.SDNCapability;
import net.es.oscars.topoBridge.sdn.SDNHop;

import org.apache.log4j.Logger;

public class TeardownJob extends Thread{
    private final Logger log = Logger.getLogger(TeardownJob.class.getName());
    SDNHop hop;
    FloodlightSDNConnector connector;
    ISDNConnectorResponse response;
    String circuitID;

    public TeardownJob(SDNHop hop, FloodlightSDNConnector connector,String circuitID){
        this.hop = hop;
        this.connector = connector;
        this.circuitID = circuitID;
        this.response = null;
    }

    public void run() {
        // Check for capabilities
        try{
            if (this.hop.getCapabilities().contains(SDNCapability.L2)) {
                if (!this.hop.isEntryHop() && !this.hop.isExitHop())
                    this.response = this.connector.teardownL2Bypass(this.hop, this.circuitID);
                else
                    this.response = this.connector.teardownL2Hop(this.hop, this.circuitID);
            }else {
                this.response = this.connector.teardownL1Hop(this.hop, this.circuitID);
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
