
/**
 * AAAFaultMessage.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

package net.es.oscars.oscars;

public class AAAFaultMessage extends java.lang.Exception{
    
    private net.es.oscars.wsdlTypes.AAAFault faultMessage;
    
    public AAAFaultMessage() {
        super("AAAFaultMessage");
    }
           
    public AAAFaultMessage(java.lang.String s) {
       super(s);
    }
    
    public AAAFaultMessage(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(net.es.oscars.wsdlTypes.AAAFault msg){
       faultMessage = msg;
    }
    
    public net.es.oscars.wsdlTypes.AAAFault getFaultMessage(){
       return faultMessage;
    }
}
    