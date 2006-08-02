

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import net.es.oscars.OSCARS.Dispatcher.*;

public class CreateReservationClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		 OSCARS_ServiceLocator serviceLoc = new OSCARS_ServiceLocator();
          
		 OSCARS_PortType port = null;
      try {
		   port = serviceLoc.getOSCARSSOAP();
      } catch (Exception e1){
    	  System.out.print("caught Exception from getOSCARSSOAP\n");
      }
		      
		    // set the input args
		    ResCreateContent resRequest = new ResCreateContent();
		    resRequest.setSrcHost("bosshog");
		    resRequest.setDestHost("pabst");
		    Calendar time = Calendar.getInstance();
		    time.setTime(new Date());
		    resRequest.setStartTime(time);
		    time.add(Calendar.HOUR,100);
		    resRequest.setEndTime(time);
		    resRequest.setOrigTimeZone("-07:00");
		    resRequest.setBandwidth(1000);
		    resRequest.setCreateRouteDirection(CreateRouteDirectionType.FORWARD);
		    resRequest.setProtocol(ResProtocolType.TCP);
		    resRequest.setDescription("mrt reservation");
		    
		    
		  // Make the call  
		     CreateReply resResponse = new CreateReply();
		    try {
		       resResponse = port.createReservation(resRequest);
		    } catch (OSCARSExceptionType e1){
		    	   System.out.print("OSCARS Exception returned from createReservation\n");
		    } catch (java.rmi.RemoteException e2){
		    	   System.out.print("RemoteException returned from CreateReservation\n");
		    
		    }
		   
		    System.out.println("Tag is "+ resResponse.getTag());
		    System.out.println("status is "+ resResponse.getStatus().getValue());
		    

	}

}
