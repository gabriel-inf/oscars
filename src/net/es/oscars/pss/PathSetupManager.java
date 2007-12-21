package net.es.oscars.pss;

import java.util.*;
import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.wsdlTypes.*; 
import net.es.oscars.Notifier;

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
    private Forwarder forwarder;
    private ReservationLogger rsvLogger; 
    private Notifier notifier;
 
    /** Constructor. */
    public PathSetupManager(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        PSSFactory pssFactory = new PSSFactory();
        
        this.notifier = new Notifier();
        this.forwarder = new Forwarder();
        this.props = propHandler.getPropertyGroup("pss", true);
        this.pss =
            pssFactory.createPSS(this.props.getProperty("method"), dbname);
        this.log = Logger.getLogger(this.getClass());
        this.rsvLogger = new ReservationLogger(this.log);
        this.log.info("PSS type is:["+this.props.getProperty("method")+"]");
        this.dbname = dbname;
    }
    
    /**
     * Creates path by contacting PSS module
     *
     * @param resv reservation to be created
     * @param doForward forward teardown to next domain if true
     * @return the status returned by the the create operation
     * @throws PSSException
     */
    public String create(Reservation resv, boolean doForward) throws PSSException, 
                    InterdomainException {
    	this.rsvLogger.redirect(resv.getGlobalReservationId());
        this.log.info("create.start");
        String status = null;
        String gri = resv.getGlobalReservationId();
        
        /* Check reservation */
        if(this.pss == null){
            this.log.error("PSS is null");
            throw new PSSException("Path setup not currently supported");
        }
        
        /* Forward to next domain */
        if(doForward){
            this.log.info("thinks forwarding, calling createPath");
            CreatePathResponseContent forwardReply = 
                this.forwarder.createPath(resv);
            if(forwardReply == null){
                this.log.info("last domain in signaling path");
            }else if(!forwardReply.getStatus().equals("ACTIVE")){
                String errMsg = "forwardReply returned an unrecognized status: " +         
                    forwardReply.getStatus();
                this.log.error(errMsg);
                throw new PSSException(errMsg);
            }
        }
        
        /* Create path */
        try{     
            this.log.info("past forwarding, trying regular createPath");
            status = this.pss.createPath(resv);
        } catch(Exception e) {
            this.log.error("Error setting up local path for reservation, gri: [" +
                gri + "] Sending teardownPath.");
            
            this.log.error("error was: "+e.getMessage());
            this.forwardTeardown(resv, e.getMessage());

            throw new PSSException("Path cannot be created, error setting up path.");
        }
        this.log.info("create.end");
        this.rsvLogger.stop();
        return status;
    }
    
    /**
     * Refreshes path by contacting PSS module
     *
     * @param resv reservation to be refreshed
     * @param doForward forward teardown to next domain if true
     * @return the status returned by the the refresh operation
     * @throws PSSException
     */
    public String refresh(Reservation resv, boolean doForward) throws PSSException, 
                    InterdomainException{
        String status = null;
        boolean stillActive = false;
        String errorMsg = null;
        String gri = resv.getGlobalReservationId();
    	this.rsvLogger.redirect(resv.getGlobalReservationId());
        
        /* Check reservation */
        if(this.pss == null){
            throw new PSSException("Path setup not currently supported");
        }
        
        /* Refresh path in this domain */
        try{
            status = this.pss.refreshPath(resv);
            stillActive = true;
        }catch(Exception e){
            this.log.error("Reservation " + gri + " path failure. " + 
                "Sending teardownPath. Reason: " + e.getMessage());
            errorMsg = "A path failure has occurred. The path is no " +
                "longer active. Reason: " + e.getMessage();
        }
        
        /* Forward to next domain */
        if(stillActive && doForward){
            try{
                RefreshPathResponseContent forwardReply = 
                    this.forwarder.refreshPath(resv);
                if(forwardReply == null){
                    this.log.info("last domain in signaling path");
                }else if(!forwardReply.getStatus().equals("ACTIVE")){
                    String errMsg = "forwardReply returned an unrecognized status: " +         
                        forwardReply.getStatus();
                    this.log.error(errMsg);
                    throw new PSSException(errMsg);
                }
             }catch(PSSException e){
                /* teardown local path if doesn't refresh in current domain */
                this.pss.teardownPath(resv);
                throw e;
             }catch(InterdomainException e){
                /* teardown local path if doesn't refresh in current domain */
                this.pss.teardownPath(resv);
                throw e;
             }
         }else if(doForward){
            this.forwardTeardown(resv, errorMsg);
         }
        
        this.rsvLogger.stop();
        return status;
    }
    
    /**
     * Teardown path by contacting PSS module
     *
     * @param resv reservation to be torn down
     * @param doForward forward teardown to next domain if true
     * @return the status returned by the the teardown operation
     * @throws PSSException
     */
    public String teardown(Reservation resv, boolean doForward) 
                    throws PSSException{ 
        String prevStatus = resv.getStatus();
        String status = null;
        String errorMsg = null;
        String gri = resv.getGlobalReservationId();
    	this.rsvLogger.redirect(resv.getGlobalReservationId());
        
        /* Check reservation */
        if(this.pss == null){
            throw new PSSException("Path teardown not currently supported");
        }
        
        /* Teardown path in this domain */
        try{
            status = this.pss.teardownPath(resv);
        }catch(PSSException e){
            //still want to forward if error occurs
            this.log.error("Unable to teardown path for " + 
                gri + ". Reason: " + e.getMessage() +
                ". Proceeding with forward.");
            
            if(!doForward){
                throw e;
            }
            errorMsg = "Error tearing down local path. Reason: " + 
                    e.getMessage();
        }
        
        /* Forward to next domain */
        if(doForward && (!prevStatus.equals("PRECANCEL"))){
            this.forwardTeardown(resv, errorMsg);
        }
        this.rsvLogger.stop();
        return status;
    }
    
    /**
     * Private method used by create, refresh, and teardown to forward 
     * teardownPath messages. Called when there is a failure or when a
     * teardownPath request needs to forward a message.
     *
     * @param resv the reservation to teardown
     * @param errorMsg any existing error messages 
     * @throws PSSException
     */
    private void forwardTeardown(Reservation resv, String errorMsg) 
            throws PSSException{
        String gri = resv.getGlobalReservationId();
        
        try{
            TeardownPathResponseContent forwardReply = 
                this.forwarder.teardownPath(resv);
            if(forwardReply == null){
                this.log.info("last domain in signaling path");
            }else if((!forwardReply.getStatus().equals("PENDING")) &&
                        (!forwardReply.getStatus().equals("FINISHED"))){
                String errMsg = "forwardReply returned an unrecognized status: " +         
                    forwardReply.getStatus();
                this.log.error(errMsg);
                throw new PSSException(errMsg);
            }  
         }catch(Exception e){
            /* check for forward error */
            if(errorMsg != null){
                errorMsg = "Multiple Exceptions for " + gri + 
                ":\n\nLocal Exception: " + errorMsg + "\n\nRemote Exception: ";
            }
            errorMsg += e.getMessage();
            this.log.error("Forward error for " + gri + ": " + e.getMessage());
         }
        
        /* Since exception may occur remotely or locally throw both here */
        if(errorMsg != null){
            throw new PSSException(errorMsg);
        }
        
        return;
    }
}
