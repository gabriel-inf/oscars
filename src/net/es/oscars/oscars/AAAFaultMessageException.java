
/**
 * AAAFaultMessageException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
 */
package net.es.oscars.oscars;
import net.es.oscars.wsdlTypes.*;

public class AAAFaultMessageException extends java.lang.Exception{
    
    private net.es.oscars.wsdlTypes.AAAFault faultMessage;
    
    public AAAFaultMessageException() {
        super("AAAFaultMessageException");
    }
           
    public AAAFaultMessageException(java.lang.String s) {
        super("AAAFaultMessageException");
        faultMessage = new AAAFault();
        faultMessage.setMsg(s);
   
    }
    
    public AAAFaultMessageException(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(net.es.oscars.wsdlTypes.AAAFault msg){
       faultMessage = msg;
    }
    
    public net.es.oscars.wsdlTypes.AAAFault getFaultMessage(){
       return faultMessage;
    }
}
    