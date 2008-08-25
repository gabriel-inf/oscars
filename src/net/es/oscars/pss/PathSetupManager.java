package net.es.oscars.pss;

import java.util.*;
import org.apache.log4j.*;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.OSCARSCore;

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
    private ReservationLogger rsvLogger;
    private OSCARSCore core;
    
    /** Constructor. */
    public PathSetupManager(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        PSSFactory pssFactory = new PSSFactory();
        this.core = OSCARSCore.getInstance();
        this.props = propHandler.getPropertyGroup("pss", true);
        this.pss = pssFactory.createPSS(this.props.getProperty("method"), dbname);
        this.log = Logger.getLogger(this.getClass());
        this.rsvLogger = new ReservationLogger(this.log);
//        this.log.info("PSS type is:["+this.props.getProperty("method")+"]");
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
        StateEngine se = this.core.getStateEngine();
        
        
        /* Check reservation */
        if(this.pss == null){
            this.log.error("PSS is null");
            throw new PSSException("Path setup not currently supported");
        }
        
        
        
        /* Create path */
        try{
            se.updateStatus(resv, StateEngine.INSETUP);
            /* Get next domain */
            Domain nextDomain = resv.getPath().getNextDomain();
            if(nextDomain == null){
                this.updateCreateStatus(2, resv);
            }else{
                //schedule time out
            }
            
            /* Get previous domain */
            PathElem firstElem= resv.getPath().getInterPathElem();
            Domain firstDomain = null;
            if(firstElem != null && firstElem.getLink() != null){
                firstDomain = firstElem.getLink().getPort().getNode().getDomain();
            }
            if(firstDomain == null || firstDomain.isLocal()){
                this.updateCreateStatus(4, resv);
            }else{
                //schedule timeout
            }
            status = this.pss.createPath(resv);
        } catch(Exception e) {
            this.log.error("Error setting up local path for reservation, gri: [" +
                gri + "]");
            e.printStackTrace();
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
        Forwarder forwarder = new Forwarder();
        RefreshPathResponseContent forwardReply = null;
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
            InterdomainException interException = null;
            try{
                forwardReply = forwarder.refreshPath(resv);
            }catch(InterdomainException e){
                interException = e;
            }finally{
                forwarder.cleanUp();
                if(interException != null){
                    throw interException;
                }
            }
        }else if(doForward){
            //this.forwardTeardown(resv, errorMsg);
        }

        /* Print forwarding status */
        if(forwardReply == null){
            this.log.info("last domain in signalling path");
        }else if(!forwardReply.getStatus().equals("ACTIVE")){
            String errMsg = "forwardReply returned an unrecognized status: " +
                forwardReply.getStatus();
            this.log.error(errMsg);
            throw new PSSException(errMsg);
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
    public String teardown(Reservation resv, String newStatus, boolean doForward)
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
            status = this.pss.teardownPath(resv, newStatus);
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
            //this.forwardTeardown(resv, errorMsg);
        }
        this.rsvLogger.stop();
        return status;
    }
    
    public void handleSetupEvent(String gri, String producerID, boolean upstream) 
                                throws BSSException{
        ReservationDAO dao = new ReservationDAO(this.dbname);
        Reservation resv = dao.query(gri);
        ReservationManager rm = this.core.getReservationManager();
        StateEngine se = this.core.getStateEngine();
        int newLocalStatus = upstream ? 4 : 2;
        String targetNeighbor = "downstream";
        if(upstream){
            targetNeighbor = "upstream";
        }
        if(resv == null){
            this.log.error("Reservation " + gri + " not found");
            return;
        }
        String login = resv.getLogin();
        
        Domain neighborDomain = rm.endPointDomain(resv, upstream);
        if(neighborDomain == null || neighborDomain.isLocal()){
            this.log.error("Could not identify " + targetNeighbor + 
                           " domain in path.");
            return;
        }else if(!neighborDomain.getTopologyIdent().equals(producerID)){
            this.log.debug("The event is from " + producerID + " not the " +
                           targetNeighbor + " domain " + 
                           neighborDomain.getTopologyIdent() + " so discarding");
            return;
        }
        
        /* Get the institution */
        Site site = neighborDomain.getSite();
        if(site == null){
            this.log.error("No site associated with domain " +  
                           neighborDomain.getTopologyIdent() + ". Please specify" +
                           " institution associated with domain in your " +
                           "bss.sites table.");
            return;
        }
        
        String institution = site.getName();
        if(institution == null){
            this.log.error("No institution associated with domain " +  
                           neighborDomain.getTopologyIdent() + ". Please specify" +
                           " institution associated with domain in your " +
                           "aaa.institution and bss.sites table.");
            return;
        }
        
        String status = se.getStatus(resv);
        if(StateEngine.INSETUP.equals(status)){
            this.updateCreateStatus(newLocalStatus, resv);
            return;
        }
        
        //schedule a new job
        
    }
    
    /**
     * Checks the interdomain status of path setup and changes status to ACTIVE
     * if upstream, downstream and local path setup status
     *
     * @param newLocalStatus the new amount to increase the local status field
     * @param resv the reservation to update
     */
     synchronized public void updateCreateStatus(int newLocalStatus, Reservation resv) throws BSSException{
        StateEngine se = this.core.getStateEngine();
        int localStatus = se.getLocalStatus(resv);
        se.updateLocalStatus(resv, localStatus + newLocalStatus);
        localStatus = se.getLocalStatus(resv);
        String login = resv.getLogin();
        EventProducer eventProducer = new EventProducer();
        
        //local path setup done
        if(newLocalStatus == 1){
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_CONFIRMED, login, "JOB", resv);
        }
        
        //downstream path setup done
        if(newLocalStatus <= 2 && (localStatus & 3) ==3){
            eventProducer.addEvent(OSCARSEvent.DOWN_PATH_SETUP_CONFIRMED, login, "JOB", resv);
        }
        
        //upstream path setup done
        if((newLocalStatus == 1 || newLocalStatus ==  3) && ((localStatus & 5) == 5)){
            eventProducer.addEvent(OSCARSEvent.UP_PATH_SETUP_CONFIRMED, login, "JOB", resv);
        }
        
        //everything complete
        if((localStatus & 7)  == 7){
            se.updateStatus(resv, StateEngine.ACTIVE);
            se.updateLocalStatus(resv, 0);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_COMPLETED, login, "JOB", resv);
        }
     }
}
