

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import net.es.oscars.OSCARS.Dispatcher.*;

public class QueryReservationClient {

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
		      
		    // set the input arg
		    ResTag queryRequest = new ResTag();
		    queryRequest.setTag("1234");
		    
		  // Make the call  
		     ResDetails queryReply  = new ResDetails();
		    try {
		       queryReply = port.queryReservation(queryRequest);
		    } catch (OSCARSExceptionType e1){
		    	   System.out.print("OSCARS Exception returned from queryReservation\n");
		    } catch (java.rmi.RemoteException e2){
		    	   System.out.print("RemoteException returned from queryReservation\n");
		    
		    }
		   
		    System.out.println("Tag is "+ queryReply.getTag());
		    System.out.println("status is "+ queryReply.getStatus().getValue());
		    System.out.println("srcHost is " + queryReply.getSrcHost());
		    

	}

}
