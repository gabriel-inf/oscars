
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.6.1
 * 2012-11-01T10:15:37.971-07:00
 * Generated source version: 2.6.1
 */

@WebFault(name = "serviceException", targetNamespace = "http://schemas.ogf.org/nsi/2012/03/connection/interface")
public class ServiceException extends Exception {
    
    private net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType serviceException;

    public ServiceException() {
        super();
    }
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message, net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType serviceException) {
        super(message);
        this.serviceException = serviceException;
    }

    public ServiceException(String message, net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType serviceException, Throwable cause) {
        super(message, cause);
        this.serviceException = serviceException;
    }

    public net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType getFaultInfo() {
        return this.serviceException;
    }
}
