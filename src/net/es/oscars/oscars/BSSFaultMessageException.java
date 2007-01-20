
/**
 * BSSFaultMessageException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1.1-SNAPSHOT Nov 29, 2006 (02:53:00 GMT+00:00)
 */
package net.es.oscars.oscars;

public class BSSFaultMessageException extends java.lang.Exception{
    
    private net.es.oscars.wsdlTypes.BSSFault faultMessage;
    
    public BSSFaultMessageException() {
        super("BSSFaultMessageException");
    }
           
    public BSSFaultMessageException(java.lang.String s) {
       super(s);
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
    