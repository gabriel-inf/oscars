
/**
 * InvalidFilterFault.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

package net.es.oscars.notifybroker.ws;

public class InvalidFilterFault extends java.lang.Exception{
    
    private org.oasis_open.docs.wsn.b_2.InvalidFilterFault faultMessage;
    
    public InvalidFilterFault() {
        super("InvalidFilterFault");
    }
           
    public InvalidFilterFault(java.lang.String s) {
       super(s);
    }
    
    public InvalidFilterFault(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(org.oasis_open.docs.wsn.b_2.InvalidFilterFault msg){
       faultMessage = msg;
    }
    
    public org.oasis_open.docs.wsn.b_2.InvalidFilterFault getFaultMessage(){
       return faultMessage;
    }
}
    