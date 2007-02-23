
/**
 * BSSFaultMessageException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
 */
package net.es.oscars.oscars;
import net.es.oscars.wsdlTypes.*;

public class BSSFaultMessageException extends java.lang.Exception{
    
    private net.es.oscars.wsdlTypes.BSSFault faultMessage;
    
    public BSSFaultMessageException() {
        super("BSSFaultMessageException");
    }
           
    public BSSFaultMessageException(java.lang.String s) {
       super("BSSFaultMessageException");
       faultMessage = new BSSFault();
       faultMessage.setMsg(s);
    }
    
    public BSSFaultMessageException(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(net.es.oscars.wsdlTypes.BSSFault msg){
       faultMessage = msg;
    }
    
    public net.es.oscars.wsdlTypes.BSSFault getFaultMessage(){
       return faultMessage;
    }
}
    