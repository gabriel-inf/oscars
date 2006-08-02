
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import net.es.oscars.OSCARS.*;

public class PingClient {

	/**
	 * @param args
	 */
	public static void main(String argv[]) {
		
		 OSCARS_ServiceLocator serviceLoc = new OSCARS_ServiceLocator();
          
		 OSCARS_PortType port = null;
      try {
		   port = serviceLoc.getOSCARSSOAP();
      } catch (javax.xml.rpc.ServiceException e1){
    	   System.out.print("ServiceException returned from ping0\n");
	       System.out.print ("details are: " +  e1.getMessage());
      }
		      
      if  (argv[0].equals("0" )) {
    	    System.out.print("calling ping0\n");
    	     Ping0Response  response = new Ping0Response();
    		  try {
    			  Ping0 input = new Ping0();
    			  response = port.ping0(input);
    		  } catch (java.rmi.RemoteException e2){
		    	   System.out.print("RemoteException returned from ping0\n");
			       System.out.print ("details are: " +  e2.getMessage());
			       System.exit (1);
    		  }
		      System.out.println("response from ping0 is " + response.getMsg() );
      } 
      else if ( argv[0].equals( "1") ) {
  	      System.out.print("calling ping1\n");
    	   EnString response = new EnString("nothing");
    	    try {
    	    	EnString input = new EnString("hello world");
			     response = port.ping1(input);
    	    } catch (java.rmi.RemoteException e2){
	    	   System.out.print("RemoteException returned from ping1\n");
		       System.out.print ("details are: " +  e2.getMessage());
		       System.exit (1);
    	    }  	  
    	    System.out.println("response from ping1 is " + response.getMsg	());
      }
      else if (argv[0].equals("2") ) {
    	    System.out.print("calling ping2\n");
    	    	     EnString  response = new EnString("nothing");
    	    		  try {
    	    			  Ping2 input = new Ping2("hello"," world");
    	    			  response = port.ping2(input);
    	    		  } catch (java.rmi.RemoteException e2){
    			    	   System.out.print("RemoteException returned from ping2\n");
    				       System.out.print ("details are: " +  e2.getMessage());
    				       System.exit (1);
    	    		  }
    			      System.out.println("response from ping2 is " + response.getMsg() );
    	      } 
    else if (argv[0].equals("en") ) {
	    System.out.print("calling pingEn\n");
	    	     EnString  response = new EnString("nothing");
	    		  try {
	    			  EnumType msg = EnumType.fromString("Message 2");
	    			  Pingenum input =new Pingenum(msg);
	    			  response = port.pingenum(input);
	    		  } catch (java.rmi.RemoteException e2){
			    	   System.out.print("RemoteException returned from ping2\n");
				       System.out.print ("details are: " +  e2.getMessage());
				       System.exit (1);
	    		  } catch (java.lang.IllegalArgumentException e1 ){
	    			  System.out.print("IllegalArgument from Enumtype");
	    			  System.exit(1);
	    		  }
			      System.out.println("response from ping2 is " + response.getMsg() );
	      } 
	}    
}
