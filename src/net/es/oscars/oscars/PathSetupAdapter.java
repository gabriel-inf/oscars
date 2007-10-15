package net.es.oscars.oscars;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.pss.*;
import net.es.oscars.bss.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.HibernateUtil;

/**
 * Intermediary between Axis2 and OSCARS libraries.
 *
 * All exceptions are passed back to OSCARSSkeleton, which logs them and maps
 * to the ADB classes that support SOAP faults.
 *  
 * @author Andrew Lake (alake@internet2.edu)
 */
public class PathSetupAdapter{
    private Logger log;
    private PathSetupManager pm;
    private Forwarder forwarder;
    
    /** Constructor */
    public PathSetupAdapter() {
        this.log = Logger.getLogger(this.getClass());
        this.pm = new PathSetupManager("bss");
        this.forwarder = new Forwarder();
    }
    
    /**
     * Sets up a path in response to a CreatePath request. Forwards request 
     * first, and sets-up path is reply successful. If there is an error during 
     * local path setup a teardownPath message is issued.
     *
     * @param params createPathContent request parameters
     * @param login the login of the user that made the request
     * @return the result containing ACTIVE status. 
     * @throws PSSException
     * @throws InterdomainException
     */
    public CreatePathResponseContent create(CreatePathContent params, 
                                            String login) 
                                   throws PSSException, InterdomainException {
        CreatePathResponseContent response = new CreatePathResponseContent();
        String gri = params.getGlobalReservationId();
        String status = null;
        Long currTime = System.currentTimeMillis();
        
        this.log.info("create.start");  
        
        /* Connect to DB */
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO("bss");
        
        /* Retrieve reservation */
        Reservation resv = resvDAO.queryByGRIAndLogin(gri, login); 
        if (resv == null) {
            throw new PSSException("No reservations match request");
        } else if (resv.getPath().getPathSetupMode() == null) {
            throw new PSSException("Path setup mode is null");
        } else if (!resv.getPath().getPathSetupMode().equals("user-xml")) {
            throw new PSSException("Path setup mode is not user-xml");
        } else if(!resv.getStatus().equals("PENDING")){
            throw new PSSException("Path cannot be created. " + 
            "Invalid reservation specified.");
        } else if(currTime.compareTo(resv.getStartTime()) < 0){
            throw new PSSException("Path cannot be created. Reservation " +
            "start time not yet reached.");
        } else if(currTime.compareTo(resv.getEndTime()) > 0){
            throw new PSSException("Path cannot be created. Reservation " +
            "end time has been reached.");
        }
        
        /* Forward to next domain */
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
        
        /* Setup path in this domain */
        try{     
            status = this.pm.create(resv);
            response.setStatus(status);
            response.setGlobalReservationId(gri);
        } catch(Exception e) {
            this.log.error("Error setting up local path for reservation, gri: [" +
                gri + "] Sending teardownPath.");
            
            this.log.error("error was: "+e.getMessage());
            this.forwardTeardown(resv, e.getMessage());

            throw new PSSException("Path cannot be created, error setting up path.");

        }finally{
            bss.getTransaction().commit();
        }
        
        this.log.info("create.end"); 
        
        return response;
    }
    
    /**
     * Verifies a path in response to a refreshPath request. Checks local path 
     * first and then forward request. If local path is fine it forwards the
     * refreshPath request. If the local path has failed it forwards a teardown * message. If the forwardResponse indicates an downstream error the local 
     * path is removed and the exception passed upstream.
     *
     * @param params RefreshPathContent request parameters
     * @param login the login of the user that made the request
     * @return the result containing ACTIVE status if successful
     * @throws PSSException
     * @throws InterdomainException
     */
    public RefreshPathResponseContent refresh(RefreshPathContent params, 
                                            String login) 
                                   throws PSSException, InterdomainException {
        RefreshPathResponseContent response = new 
            RefreshPathResponseContent();
        String gri = params.getGlobalReservationId();
        String status = null;
        boolean stillActive = false;
        String errorMsg = null;
        
        this.log.info("refresh.start"); 
        
        /* Connect to DB */
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO("bss");
        String errorMessage = null;
        
        /* Retrieve reservation */
        Reservation resv = resvDAO.queryByGRIAndLogin(gri, login); 
        if(resv == null || resv.getPath().getPathSetupMode() == null ||
            (!resv.getPath().getPathSetupMode().equals("user-xml")) ){
            throw new PSSException("No reservations match request");
        }else if(!resv.getStatus().equals("ACTIVE")){
            throw new PSSException("Path cannot be refreshed. " + 
            "Reservation is not active. Please run createPath first.");
        }
        
        /* Refresh path in this domain */
        try{
            status = this.pm.refresh(resv);
            response.setStatus(status);
            response.setGlobalReservationId(gri);
            stillActive = true;
        }catch(Exception e){
            this.log.error("Reservation " + gri + " path failure. " + 
                "Sending teardownPath. Reason: " + e.getMessage());
            errorMsg = "A path failure has occurred. The path is no " +
                "longer active. Reason: " + e.getMessage();
        }
        
        /* Forward to next domain */
        if(stillActive){
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
                this.pm.teardown(resv);
                throw e;
             }catch(InterdomainException e){
                /* teardown local path if doesn't refresh in current domain */
                this.pm.teardown(resv);
                throw e;
             }finally{
                //make sure status gets updated
                bss.getTransaction().commit();
            }
         }else{
            try{
                this.forwardTeardown(resv, errorMsg);
            }catch(PSSException e){
                throw e;
            }finally{
                //make sure status gets updated
                bss.getTransaction().commit();
            }
         }
        
        this.log.info("refresh.end"); 
        
        return response;
    }
    
    /**
     * Removes a path in response to a teardown request. Removes local path 
     * first and then forwards request. If there is a failure in the local path
     * teardown the request is still forwarded. The exception is reported 
     * upstream. Returns PENDING status if successfil. It is returns PENDING and
     * not complete because it is possible the user could re-build the path 
     * later if the reservation is not expired.
     *
     * @param params TeardownPathContent request parameters
     * @param login the login of the user that made the request
     * @return the result containing PENDING status if successful
     * @throws PSSException
     * @throws InterdomainException
     */
    public TeardownPathResponseContent teardown(TeardownPathContent params, 
                                            String login) 
                                   throws PSSException, InterdomainException {
        TeardownPathResponseContent response = new 
            TeardownPathResponseContent();
        String gri = params.getGlobalReservationId();
        String status = null;
        String errorMsg = null;
        
        this.log.info("teardown.start"); 
        
        /* Connect to DB */
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO("bss");
        
        /* Retrieve reservation */
        Reservation resv = resvDAO.queryByGRIAndLogin(gri, login); 
        if(resv == null || resv.getPath().getPathSetupMode() == null ||
            (!resv.getPath().getPathSetupMode().equals("user-xml")) ){
            throw new PSSException("No reservations match request");
        }else if(!resv.getStatus().equals("ACTIVE")){
            throw new PSSException("Cannot teardown path. " + 
            "Reservation is not active. Please run createPath first.");
        }
        
        /* Teardown path in this domain */
        try{
            status = this.pm.teardown(resv);
            response.setStatus(status);
            response.setGlobalReservationId(gri);
        }catch(PSSException e){
            //still want to forward if error occurs
            this.log.error("Unable to teardown path for " + 
                gri + ". Reason: " + e.getMessage() +
                ". Proceeding with forward.");
            errorMsg = "Error tearing down local path. Reason: " + 
                e.getMessage();
        }
        
        /* Forward to next domain */
        try{
            this.forwardTeardown(resv, errorMsg);
        }catch(PSSException e){
            throw e;
        }finally{
            //make sure status gets updated
            bss.getTransaction().commit();
        }
        
        this.log.info("teardown.end"); 
        
        return response;
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
            }else if(!forwardReply.getStatus().equals("PENDING")){
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