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
    
    /** Constructor */
    public PathSetupAdapter() {
        this.log = Logger.getLogger(this.getClass());
        this.pm = new PathSetupManager("bss");
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
        
        /* Start signaling and setup path */
        try{     
            status = this.pm.create(resv, true);
            response.setStatus(status);
            response.setGlobalReservationId(gri);
        }catch(PSSException e) {
            throw e;
        }catch(InterdomainException e) {
            throw e;
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
        
        /* Refresh path */
        try{
            status = this.pm.refresh(resv, true);
            response.setStatus(status);
            response.setGlobalReservationId(gri);
        }catch(PSSException e){
            throw e;
        }catch(InterdomainException e){
            throw e;
        }finally{
                //make sure status gets updated
                bss.getTransaction().commit();
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
            status = this.pm.teardown(resv, true);
            response.setStatus(status);
            response.setGlobalReservationId(gri);
        }catch(PSSException e){
            throw e;
        }finally{
            //make sure status gets updated
            bss.getTransaction().commit();
        }
        
        this.log.info("teardown.end"); 
        
        return response;
    }
   
}