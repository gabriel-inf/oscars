
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import javax.net.ssl.*;
import java.security.*;
import net.es.oscars.OSCARS.Dispatcher.*;

public class TestForwardClient {

	/**
	 * @param args
	 */
	public static void main(String argv[]) {
		
		try {
			  System.setProperty("javax.net.ssl.keyStoreType", "JKS");
	    	  System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.home") + "/.keystore");
	    	  System.setProperty("javax.net.ssl.trustStorePassword", "oscars");
	    	  System.setProperty( "java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol" );
	    	  Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
	    	  			
		} catch (Exception e1) {
			 e1.printStackTrace();
		}
		 OSCARS_ServiceLocator serviceLoc = new OSCARS_ServiceLocator();
          
		 OSCARS_PortType port = null;
      try {
		   port = serviceLoc.getOSCARSSOAP();
      } catch (Exception e1){
    	  System.out.print("caught Exception from getOSCARSSOAP\n");
      }
		if (argv.length == 0 ) {
			System.out.print ("Usage is: TestForwardClient [create | list | cancel <tag> | query <tag>]\n");
			System.exit (1);
		}
	   ForwardPayload payload = new ForwardPayload();
	   PayloadContentType payloadType = null;
	   
      if (argv[0].equals("create")){
		    // create a createReservationRequest
		    ResCreateContent resRequest = new ResCreateContent();
		    resRequest.setSrcHost("nettrash3.es.net");
		    resRequest.setDestHost("dc-cr1.es.net");
		    Calendar time = Calendar.getInstance();
		    time.setTime(new Date());
		    time.set(Calendar.MILLISECOND,0);
		    time.add(Calendar.HOUR,1);
		    resRequest.setStartTime(time);
		    time.add(Calendar.HOUR,1);
		    resRequest.setEndTime(time);
		    resRequest.setOrigTimeZone("-07:00");
		    resRequest.setBandwidth(10);
		    resRequest.setCreateRouteDirection(CreateRouteDirectionType.FORWARD);
		    resRequest.setProtocol(ResProtocolType.TCP);
		    resRequest.setDescription("mrt reservation");
		    
		    // create a Payload 

		    payloadType = PayloadContentType.fromString("createReservation");

		    payload.setCreateReservation(resRequest);
      }

      else if (argv[0].equals("cancel")){
    	  payloadType = PayloadContentType.fromString("cancelReservation");
    	  System.out.println("tag is " + argv[1] );
    	  payload.setCancelReservation(new ResTag(argv[1]));
    	  
      }      
      else if (argv[0].equals("query")){
    	  payloadType = PayloadContentType.fromString("queryReservation");
    	  payload.setQueryReservation(new ResTag(argv[1]));
      }
      else if (argv[0].equals("list")){
    	  payloadType = PayloadContentType.fromString("listReservations");
    	  payload.setListReservations(new EmptyArg());
      }
	else {
		System.out.print ("Usage is: TestForwardClient [create | list | cancel <tag> | query <tag>]\n");
		System.exit (1);
	 }
	    payload.setContentType(payloadType);
		  // Make the call  
		    Login userLogin = new Login("mrthompson@lbl.gov", "testmrt");
		    ForwardReply response = new ForwardReply();

		    try {
		       response = port.testForward(new TestForward(payload, "mrthompson@lbl.gov", userLogin));
		    } catch (OSCARSExceptionType e1){
		    	   System.out.print("OSCARS Exception returned from forward\n");
		    } catch (java.rmi.RemoteException e2){
		    	   System.out.print("RemoteException returned from forward\n");
			   System.out.print ("details are: " +  e2.getMessage());
		    
		    }

		   if (response.getContentType().equals(PayloadContentType.createReservation)){
			   System.out.println("response from createReservation: tag: "+ response.getCreateReservation().getTag() +
			   "   status: " + response.getCreateReservation().getStatus().getValue());
		   }
		   else if (response.getContentType().equals(PayloadContentType.listReservations)){
			   ResInfoContent reservations[]= response.getListReservations();
			   System.out.println("response from listReservations. Number of reservations: " + reservations.length);
			   System.out.println("reservation 1");
			   System.out.println("\t tag: " + reservations[0].getTag());
			   System.out.println("\t status: " + reservations[0].getStatus());
			   System.out.println("\t startTime: " + reservations[0].getStartTime().getTime().toString());
			   
			   System.out.println("reservation 2");
			   System.out.println("\t tag: " + reservations[1].getTag());
			   System.out.println("\t status: " + reservations[1].getStatus());
			   System.out.println("\t startTime: " + reservations[1].getStartTime().getTime().toString());
		   }
		   else if (response.getContentType().equals(PayloadContentType.queryReservation)){
			   ResDetails reservation = response.getQueryReservation();
			   System.out.println("response from queryReservation");
			   System.out.println("tag is " + reservation.getTag());
			   System.out.println("status is " + reservation.getStatus());
			   System.out.println("source host is " + reservation.getSrcHost());
			   System.out.println("destination host is " + reservation.getDestHost());
			   System.out.println("protocol is " + reservation.getProtocol());
		   }
		   else  if (response.getContentType().equals(PayloadContentType.cancelReservation)) {
		    System.out.println("response from forwardCancelReservation is " + response.getCancelReservation().getValue() );
		   }
		   else {
			    System.out.println("unrecognized response type is " + response.getContentType().toString());   
		   }

	}

}
