package net.es.oscars.pss;

import net.es.oscars.pss.dragon.VlsrPSS;
import net.es.oscars.pss.stub.StubPSS;
/**
 * Factory class that creates an instance of a module that implements
 * PSS. Created instance will be used to configure network and create paths.
 *  
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class PSSFactory{
    /**
     * Creates a new PSS instance of the given type.
     *
     * @param pssType the PSS type to create
     * @param dbname database to access
     * @return a new instance of a PSS. null if pss is not recognized.
     */
    public PSS createPSS(String pssType, String dbname) {
        
        // check for null in case config file doesn't not have pss.method
        if (pssType == null) {
            ;
        } else if (pssType.equals("dragon")) {
            return new VlsrPSS();
        } else if (pssType.equals("vendor")) {
            // this class chooses between configuring Cisco's or Juniper's
            return new VendorPSS(dbname);
        } else if (pssType.equals("stub")) {
            return new StubPSS();
        }
        return null;
    }
}

