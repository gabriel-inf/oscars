package net.es.oscars.ws;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.pss.PathSetupManager;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.wsdlTypes.*;

/**
 * TODO:  put in PathSetupManager.
 *
 * All exceptions are passed back to OSCARSSkeleton, which logs them and maps
 * to the ADB classes that support SOAP faults.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class PathSetupAdapter{
    private Logger log;
    private String dbname;
    private PathSetupManager pm;
    private OSCARSCore core;
    
    /** Constructor */
    public PathSetupAdapter() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.dbname = this.core.getBssDbName();
        this.pm = this.core.getPathSetupManager();

    }

   /**
     * Extracts important information from Notify messages related to
     * signaling and schedules a job for execution.
     *
     * @param event the event that occurred
     * @param producerId the URL of the event producer
     * @param targStatus the target status of the reservation
     */
    public void handleEvent(EventContent event, String producerId, String targStatus){
        String eventType = event.getType();
        ResDetails resDetails = event.getResDetails();
        if(resDetails == null){
            this.log.error("No reservation details provided for event " + 
                           eventType + " from " + producerId);
            return;
        }
        String gri = resDetails.getGlobalReservationId();
        String upConfirmed = null;
        String downConfirmed = null;
        String failed = null;
        if(StateEngine.INSETUP.equals(targStatus)){
            upConfirmed = OSCARSEvent.UP_PATH_SETUP_CONFIRMED;
            downConfirmed = OSCARSEvent.DOWN_PATH_SETUP_CONFIRMED;
            failed = OSCARSEvent.PATH_SETUP_FAILED;
        }else if(StateEngine.INTEARDOWN.equals(targStatus)){
            upConfirmed = OSCARSEvent.UP_PATH_TEARDOWN_CONFIRMED;
            downConfirmed = OSCARSEvent.DOWN_PATH_TEARDOWN_CONFIRMED;
            failed = OSCARSEvent.PATH_TEARDOWN_FAILED;
        }else{
            this.log.error("Invalid target status given");
            return;
        }
        
        try{
            if(eventType.equals(upConfirmed)){
                this.pm.handleEvent(gri, producerId, targStatus, true);
            }else if(eventType.equals(downConfirmed)){
                this.pm.handleEvent(gri, producerId, targStatus, false);
            }else if(eventType.equals(failed)){
                String src = event.getErrorSource();
                String code = event.getErrorCode();
                String msg = event.getErrorMessage();
                this.pm.handleFailed(gri, producerId, src, code, msg, failed);
            }else{
                this.log.debug("Discarding event " + eventType);
            }
        }catch(BSSException e){
            this.log.error(e.getMessage());
        }
    }
}
