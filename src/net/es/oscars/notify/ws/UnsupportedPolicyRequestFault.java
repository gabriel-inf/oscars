
/**
 * UnsupportedPolicyRequestFault.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */

package net.es.oscars.notify.ws;

public class UnsupportedPolicyRequestFault extends java.lang.Exception{
    
    private org.oasis_open.docs.wsn.b_2.UnsupportedPolicyRequestFault faultMessage;
    
    public UnsupportedPolicyRequestFault() {
        super("UnsupportedPolicyRequestFault");
    }
           
    public UnsupportedPolicyRequestFault(java.lang.String s) {
       super(s);
    }
    
    public UnsupportedPolicyRequestFault(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(org.oasis_open.docs.wsn.b_2.UnsupportedPolicyRequestFault msg){
       faultMessage = msg;
    }
    
    public org.oasis_open.docs.wsn.b_2.UnsupportedPolicyRequestFault getFaultMessage(){
       return faultMessage;
    }
}
    