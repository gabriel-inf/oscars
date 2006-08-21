/*
Copyright (c) 2006, The Regents of the University of California, through
Lawrence Berkeley National Laboratory (subject to receipt of any required
approvals from the U.S. Dept. of Energy). All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

(1) Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

(2) Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

(3) Neither the name of the University of California, Lawrence Berkeley
    National Laboratory, U.S. Dept. of Energy nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes, patches,
or upgrades to the features, functionality or performance of the source
code ("Enhancements") to anyone; however, if you choose to make your
Enhancements available either publicly, or directly to Lawrence Berkeley
National Laboratory, without imposing a separate written license agreement
for such Enhancements, then you hereby grant the following license: a
non-exclusive, royalty-free perpetual license to install, use, modify,
prepare derivative works, incorporate into other computer software,
distribute, and sublicense such enhancements or derivative works thereof,
in binary and source code form. */


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
