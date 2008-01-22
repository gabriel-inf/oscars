package net.es.oscars.pss.stub;

import org.apache.log4j.*;
import net.es.oscars.bss.*;
import net.es.oscars.pss.*;


/**
 * A PSS that mimics path setup without actually creating the path
 * It makes modifications to the reservation database like a path is 
 * created or destroyed but never actually touches a real network. This
 * class is intended for us in testing.
 *  
 * @author Andrew Lake (alake@internet2.edu)
 */
public class StubPSS implements PSS{
    private Logger log;
    
    /** Constructor */
    public StubPSS(){
        this.log = Logger.getLogger(this.getClass());
    }
    
    /**
     * Sets LSP status to active in database. Mimics LSP creation.
     *
     * @param resv the reservation whose path will be created
     * @throws PSSException
     */
    public String createPath(Reservation resv) throws PSSException{
        this.log.info("vlsr.create.start");
        resv.setStatus("ACTIVE");
        this.log.info("vlsr.create.end");
        
        return resv.getStatus();
    }
    
    /**
     * Mimics LSP refresh. Simply returns status or reservation.
     *
     * @param resv the reservation whose path will be refreshed
     * @throws PSSException
     */
    public String refreshPath(Reservation resv) throws PSSException{
        this.log.info("vlsr.refresh.start");
        this.log.info("vlsr.refresh.end");
       
        return resv.getStatus();
    }
    
    /**
     * Mimics LSP teardown.
     *
     * @param resv the reservation whose path will be removed
     * @throws PSSException
     */
    public String teardownPath(Reservation resv) throws PSSException{
        this.log.info("vlsr.teardown.start");
        
        String prevStatus = resv.getStatus();
        long currTime = System.currentTimeMillis()/1000;
        
        /* Set the status of reservation */
        if(prevStatus.equals("PRECANCEL")){
            resv.setStatus("CANCELLED");
        }else if(currTime >= resv.getEndTime()){
            resv.setStatus("FINISHED");
        }else{
            resv.setStatus("PENDING");
        }
        
        this.log.info("vlsr.teardown.end");
        
        return resv.getStatus();
    }
}