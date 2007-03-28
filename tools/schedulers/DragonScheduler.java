import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.Scheduler;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.IpaddrDAO;
import net.es.oscars.bss.topology.Router;
import net.es.oscars.bss.topology.RouterDAO;
import net.es.oscars.pathfinder.dragon.DragonLocalIdMap;
import net.es.oscars.pathfinder.dragon.DragonLocalIdMapDAO;
import net.es.oscars.pathfinder.Path;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;

import org.hibernate.Session;

import edu.internet2.hopi.dragon.DragonCSA;
import edu.internet2.hopi.dragon.DragonLSP;
import edu.internet2.hopi.dragon.DragonLocalID;

/**
 * Creates and deletes LSPs using DRAGON software. For now, runs in cron.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class DragonScheduler{
	private DragonCSA csa;
	
	/* Constants */
	private final int CSA_PORT = 2611;
	private final String CSA_PASSWORD = "brugon316";
	
	/**
	* Constructor that initializes DRAGON CSA
	*/
	public DragonScheduler(){
		csa = new DragonCSA();
	}
	
	/**
	* Checks for reservations that needs to be setup or deleted and then takes the appropriate action
	*/
	public void checkReservations(){
		/* Initialize database connection */
		Initializer initializer = new Initializer();
		initializer.initDatabase();
		Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
		Scheduler s = new Scheduler();
		List<Reservation> reservations;
		
		/* Begin using database */
		session.beginTransaction();
		try{
			/* Retrieve expired reservations */
			teardownReservations(s);
			
			/* Retireve pending reservations */
			setupReservations(s);
			
		}catch(BSSException e){
			System.out.println("BSS Error: " + e.getMessage());
			e.printStackTrace();
		}catch(Exception e){
			System.out.println("General Error: " + e.getMessage());
			e.printStackTrace();
		}
		
		session.getTransaction().commit();
	}
	
	 /**
     * Gets loopback IP, given beginning path instance, and loopback type
     * @param path beginning path instance
     * @param loopbackType string, either "ingress" or "egress"
     * @return InetAddress with the loopback IP, if any. 
     */
    private InetAddress getLoopback(Path path, String loopbackType) {
    	/* Initialize */
        Ipaddr ipaddr = null;
        Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
        
        /* Parse path */
        while (path != null) {
            String addressType = path.getAddressType();
            if (loopbackType.equals(addressType)){//|| (loopbackType.equals("egress") && path.getNextPath() == null)) {
                ipaddr = path.getIpaddr();
                Router r = routerDAO.fromIp(ipaddr.getIp());
                if(r != null){
                	String loopbackIp = ipaddrDAO.getIpType(r.getName(), "loopback");
                	InetAddress loopback = null;
                 	try {
     					loopback = InetAddress.getByName(loopbackIp);
     				} catch (UnknownHostException e) {
     					//TODO: error report exception
     					e.printStackTrace();
     				}
     				return loopback;
                }
            }
            
            /* Added to get egress the way we want it for DRAGON */
            path = path.getNextPath();
        }
        return null;
    }
    
    /**
    * Builds all pending reservations
    *
    * @param resvDAO ReservationDAO used to access the MySQL database
    */
    private void setupReservations(Scheduler s) throws BSSException{
		/* Setup each pending reservations */
		List<Reservation> reservations = s.listPendingReservations(0);
		for(Reservation r : reservations){
			/* Get LSP parameters */
			InetAddress srcIP = getLoopback(r.getPath(), "ingress");
			InetAddress dstIP = getLoopback(r.getPath(), "egress") ;
			
			/* Get local ids */
			DragonLocalID srcLocalID = getLocalID(r.getSrcHost(), srcIP.getHostAddress(), true);
			DragonLocalID dstLocalID= getLocalID(r.getDestHost(), dstIP.getHostAddress(), false);
			
			String bandwidth = bandwidthToString(r.getBandwidth());
			
			DragonLSP lsp = new DragonLSP(srcIP, srcLocalID, dstIP, dstLocalID, bandwidth);
			
			System.out.println(r.getId() + ": " + srcIP.getCanonicalHostName() + "<---->" + dstIP.getCanonicalHostName());
			
			/* login to VLSR and build LSP */
			csa.login(srcIP.getHostName(), CSA_PORT, CSA_PASSWORD);
			if(csa.setupLSP(lsp)){
				Integer id = r.getId().intValue();
				s.updateReservationStatus(id, "ACTIVE");
				System.out.println("Setup LSP " + lsp.getLSPName());
				//TODO: Log result
				//TODO: Send email
			}else{
				System.out.println(csa.getError());
			}
		}
    }
    
    /**
    * Tearsdown all expired reservations
    *
    * @param resvDAO ReservationDAO used to access the MySQL database
    */
    private void teardownReservations(Scheduler s) throws BSSException{
		/* Teardown expired reservations */
		List<Reservation> reservations = s.listExpiredReservations(0);
		for(Reservation r : reservations){
			/* Get LSP parameters so LSP name can be generated */
			InetAddress srcIP = getLoopback(r.getPath(), "ingress");
			InetAddress dstIP = getLoopback(r.getPath(), "egress") ;
			
			/* Get local ids */
			DragonLocalID srcLocalID = getLocalID(r.getSrcHost(), srcIP.getHostAddress(), true);
			DragonLocalID dstLocalID= getLocalID(r.getDestHost(), dstIP.getHostAddress(), false);
			
			String bandwidth = bandwidthToString(r.getBandwidth());
			
			DragonLSP lsp = new DragonLSP(srcIP, srcLocalID, dstIP, dstLocalID, bandwidth);
			
			/* Delete LSP */
			csa.login(srcIP.getHostName(), CSA_PORT, CSA_PASSWORD);
			if(csa.teardownLSP(lsp.getLSPName())){
				Integer id = r.getId().intValue();
				s.updateReservationStatus(id, "FINISHED");
				System.out.println("Deleted LSP " + lsp.getLSPName());
				//TODO: Log result
				//TODO: Send email
			}else{
				System.out.println("Unable to delete LSP: " + csa.getError());
			}
			
		}
    }
    
    /**
    * Converts long value of bandwidth to DRAGON CSA CLI acceptable string
    *
    * @param l long value of bandwidth
    * @return CSA CLI acceptable string representation of bandwidth
    */
    private String bandwidthToString(Long l){
    	 /* TODO: Make smoother conversion and allow for non-ethernet */
    	long mbits = l.longValue()/1000000;
    	
    	if((mbits % 100) == 0){
    		if(mbits < 1000){
    			return "eth" + mbits + "M";
    		}else if(mbits == 1000){
    			return DragonLSP.BANDWIDTH_GIGE;
    		}else if((mbits % 1000) == 0){
    			return (mbits/1000) + "gige";
    		}
    	}
    	
    	/* default to 100M for now */
    	return DragonLSP.BANDWIDTH_ETHERNET_100M;
    }
    
    /**
    * Get local ID based on ingress/egress VLSR IP and host IP.
    * @param s hibernate session
    * @param hostIp IP address of the host
    * @param IP address of the VLSR to which it is connected
    * @return dragon local ID. If none found a default of untagged port 1 is returned.
    */
    private DragonLocalID getLocalID(String hostIP, String vlsrIP, boolean isSource){
    	DragonLocalID localID = null;
    	Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
    	
    	DragonLocalIdMapDAO lidMapDAO = new DragonLocalIdMapDAO();
    	lidMapDAO.setSession(session);
    	
    	DragonLocalIdMap lidMap = lidMapDAO.getFromIPs(hostIP, vlsrIP);
    	
    	if(lidMap != null){
    		localID = new DragonLocalID(lidMap.getNumber(), lidMap.getType());
    	}else if(isSource){
    		localID = new DragonLocalID(1000, "lsp-id");
    	}else{
    		localID = new DragonLocalID(1000, "tunnel-id");
    	}
    	
    	System.out.println("LOCAL ID: " + localID.getNumber());
    	
    	return localID;
    }
    
    public static void main(String[] args){
    	DragonScheduler s = new DragonScheduler();
    	s.checkReservations();
    }
}
