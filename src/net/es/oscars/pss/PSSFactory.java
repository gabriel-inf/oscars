package net.es.oscars.pss;

import net.es.oscars.pss.common.PSSFailureHandler;
import net.es.oscars.pss.dragon.VlsrPSS;
import net.es.oscars.pss.impl.bridge.BridgePSS;
import net.es.oscars.pss.impl.sdn.SDNFailureHandler;
import net.es.oscars.pss.impl.sdn.SDNPSS;
import net.es.oscars.pss.stub.StubPSS;
import net.es.oscars.pss.vendor.VendorPSS;


/**
 * Factory class that creates an instance of a module that implements
 * PSS. Created instance will be used to configure network and create paths.
 *
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class PSSFactory{
    private static PSS pss;
    /**
     * Creates a new PSS instance of the given type.
     *
     * @param pssType the PSS type to create
     * @param dbname database to access
     * @return a new instance of a PSS. null if pss is not recognized.
     */
    public static PSS createPSS(String pssType) throws PSSException {
        
        if (pss == null) {
            // check for null in case config file doesn't not have pss.method
            if (pssType == null) {
                ;
            } else if (pssType.equals("sdn")) {
                pss = SDNPSS.getInstance();
            } else if (pssType.equals("bridge")) {
                pss = BridgePSS.getInstance();
            } else if (pssType.equals("dragon")) {
                pss = new VlsrPSS();
            } else if (pssType.equals("vendor")) {
                // this class chooses between configuring Cisco's or Juniper's
                pss = new VendorPSS();
            } else if (pssType.equals("stub")) {
                pss = new StubPSS();
            }
            
        }
        
        return pss;
    }
    
    public static PSSFailureHandler createFailureHandler(String pssType) {
        if (pssType.equals("sdn")) {
            return (SDNFailureHandler.getInstance());
        }
        return null;
        
    }
    
}

