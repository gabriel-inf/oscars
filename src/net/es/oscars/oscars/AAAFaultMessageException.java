
/**
 * AAAFaultMessageException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1.1-SNAPSHOT Nov 29, 2006 (02:53:00 GMT+00:00)
 */
package net.es.oscars.oscars;

public class AAAFaultMessageException extends java.lang.Exception{
    
    private net.es.oscars.wsdlTypes.AAAFault faultMessage;
    
    public AAAFaultMessageException() {
        super("AAAFaultMessageException");
    }
           
    public AAAFaultMessageException(java.lang.String s) {
       super(s);
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
    