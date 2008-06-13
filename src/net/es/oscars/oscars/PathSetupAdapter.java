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
    private String dbname;
    private PathSetupManager pm;
    private ReservationManager rm;
    
    /** Constructor */
    public PathSetupAdapter() {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = "bss";
        this.pm = new PathSetupManager(this.dbname);
        this.rm = new ReservationManager(this.dbname);
    }
    
    /**
     * Sets up a path in response to a CreatePath request. Forwards request 
     * first, and sets-up path is reply successful. If there is an error during 
     * local path setup a teardownPath message is issued.
     *
     * @param params createPathContent request parameters
     * @param login if not null only reservations of this user or matching
     *        the input token are allowed.
     * @param institution  if not null only reservations that start or end 
     * 		at the institution or matching the token are allowed
     * @return the result containing ACTIVE status. 
     * @throws PSSException
     * @throws InterdomainException
     */
    public CreatePathResponseContent create(CreatePathContent params, 
                                            String login, String institution) 
                                   throws PSSException, InterdomainException {
        CreatePathResponseContent response = new CreatePathResponseContent();
        String gri = params.getGlobalReservationId();
        String status = null;
        String tokenValue = params.getToken();
        Long currTime = System.currentTimeMillis()/1000;
        
        this.log.info("create.start");  
        
        /* Connect to DB */
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();

        
        Reservation resv = getConstrainedResv(gri,tokenValue, login,institution);
        
        /* Check reservation parameters to make sure it can be created */
        if (resv.getPath().getPathSetupMode() == null) {
            throw new PSSException("Path setup mode is null");
        } else if (!resv.getPath().getPathSetupMode().equals("signal-xml")) {
            throw new PSSException("Path setup mode is not signal-xml");
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
     * @param login if not null only reservations of this user or with
     *        the token set are allowed.
     * @param institution  if not null only reservations that start or end 
     * 		at the institution are allowed
     * @return the result containing ACTIVE status if successful
     * @throws PSSException
     * @throws InterdomainException
     */
    public RefreshPathResponseContent refresh(RefreshPathContent params, 
                                            String login, String institution) 
                                   throws PSSException, InterdomainException {
        RefreshPathResponseContent response = new 
            RefreshPathResponseContent();
        String gri = params.getGlobalReservationId();
        String status = null;
        String tokenValue = params.getToken();
        
        this.log.info("refresh.start"); 
        
        /* Connect to DB */
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        
        
        Reservation resv = getConstrainedResv(gri,tokenValue,login,institution);
        
        /* Check reservation parameters */
        if(resv.getPath().getPathSetupMode() == null ||
            (!resv.getPath().getPathSetupMode().equals("signal-xml")) ){
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
     * @param login if not null only reservations of this user or with
     *        the token set are allowed.
     * @param institution  if not null only reservations that start or end 
     * 		at the institution are allowed
     * @return the result containing PENDING status if successful
     * @throws PSSException
     * @throws InterdomainException
     */
    public TeardownPathResponseContent teardown(TeardownPathContent params, 
                                            String login, String institution) 
                                   throws PSSException, InterdomainException {
        TeardownPathResponseContent response = new 
            TeardownPathResponseContent();
        String gri = params.getGlobalReservationId();
        String tokenValue = params.getToken();
        String status = null;
        
        this.log.info("teardown.start"); 
        
        /* Connect to DB */
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO(this.dbname);
        
        Reservation resv = getConstrainedResv(gri,tokenValue,login,institution);
        
        /* Check reservation parameters */
        if(resv.getPath().getPathSetupMode() == null ||
            (!resv.getPath().getPathSetupMode().equals("signal-xml")) ){
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
    
    /**
     * Retrieves a reservation by token. throws an exception if not found
     *
     * @param tokenValue the value of the token to use in lookup
     * @return the matching reservation
     * @throws PSSException
     */
    private Reservation validateToken(String tokenValue, String gri) 
            throws PSSException{
        TokenDAO tokenDAO = new TokenDAO(this.dbname);
        Token token = tokenDAO.fromValue(tokenValue);
        Reservation resv = null;
        
        /* Check token value */
        this.log.info("token received");
        if(token == null){
            this.log.error("unrecognized token");
            throw new PSSException("Unrecognized token specified.");
        }
        
        /* Check GRI */
        resv = token.getReservation();
        if(!resv.getGlobalReservationId().equals(gri)){
            this.log.error("token and GRI do not match");
            throw new PSSException("Token and GRI do not match");
        }
        this.log.info("token matched");
        
        return token.getReservation();
        
    }
    
    /**
     *  finds the reservations that matches all the constraints
     *  
     *  @param gri String global reservation Id identifies the reservation
     *  @param token String token value - authorization token, sufficient to authorize action
     *  @param String loginConstraint = reservation must be owned by this login
     *  @param String institutionConstraint - reservation must belong to this institution
     *  
     *  @return Reservation - a reservation that meets the constraint,
     *  @throws PSSException if no such reservation exists
     *  
     */
   private Reservation getConstrainedResv(String gri, String tokenValue, String loginConstraint,
	   		String institutionConstraint)  throws PSSException {
       
       ReservationDAO resvDAO = new ReservationDAO(this.dbname);

       Reservation resv = null;
       // check  token first, it is sufficient to authorize request
       if (tokenValue != null){
           resv = this.validateToken(tokenValue, gri);    
       }
       if (resv == null && (loginConstraint == null || institutionConstraint != null )) {
           // see if the reservation exists
           try {
       	       resv = resvDAO.query(gri);
           } catch ( BSSException e ){
               this.log.error("No reservation matches gri: " + gri);
       	       throw new PSSException("No reservations match request");
           } 
           if (institutionConstraint != null) {
       	       //check the institution
       	       if (!this.rm.checkInstitution(resv, institutionConstraint) ) {
       	           resv = null;
       	       }
           }
       }
       if (resv == null && loginConstraint != null) {
           resv = resvDAO.queryByGRIAndLogin(gri, loginConstraint);  
       }
       if (resv == null) {
	   this.log.error("No reservation matches gri: " + gri + " login: " +
		   loginConstraint + " institution: " + institutionConstraint);
           throw new PSSException("No reservations match request");
       } 
       return resv;
   }
}
