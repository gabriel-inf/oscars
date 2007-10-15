package net.es.oscars.pss;

import java.util.*;
import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.*;

/**
 * PathSetupManager handles all direct interaction with the PSS module. 
 * It contains the factory to create the PSS and makes the necessary method
 * calls.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class PathSetupManager{
    private Logger log;
    private String dbname;
    private Properties props;
    private PSS pss;
    
    /** Constructor. */
    public PathSetupManager(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        PSSFactory pssFactory = new PSSFactory();
        
        this.props = propHandler.getPropertyGroup("pss", true);
        this.pss =
            pssFactory.createPSS(this.props.getProperty("method"), dbname);
        this.log = Logger.getLogger(this.getClass());
        this.log.info("PSS type is:["+this.props.getProperty("method")+"]");
        this.dbname = dbname;
    }
    
    /**
     * Creates path by contacting PSS module
     *
     * @param resv reservation to be created
     * @return the status returned by the the create operation
     * @throws PSSException
     */
    public String create(Reservation resv) throws PSSException{
        this.log.info("create.start");
        String status = null;
        
        /* Check reservation */
        if(this.pss == null){
            this.log.error("PSS is null");
            throw new PSSException("User signaling not currently supported.");
        }
        
        /* Create path */
        status = this.pss.createPath(resv);
        
        this.log.info("create.end");
        return status;
    }
    
    /**
     * Refreshes path by contacting PSS module
     *
     * @param resv reservation to be refreshed
     * @return the status returned by the the refresh operation
     * @throws PSSException
     */
    public String refresh(Reservation resv) throws PSSException{
        String status = null;
        
        /* Check reservation */
        if(this.pss == null){
            throw new PSSException("User signaling not currently supported.");
        }
        
        /* Refresh path */
        status = this.pss.refreshPath(resv);
        
        return status;
    }
    
    /**
     * Teardown path by contacting PSS module
     *
     * @param resv reservation to be torn down
     * @return the status returned by the the teardown operation
     * @throws PSSException
     */
    public String teardown(Reservation resv) throws PSSException{ 
        String status = null;
        
        /* Check reservation */
        if(this.pss == null){
            throw new PSSException("User signaling not currently supported.");
        }
        
        /* Teardown path */
        status = this.pss.teardownPath(resv);
        
        return status;
    }
}
