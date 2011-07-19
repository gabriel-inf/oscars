package net.es.oscars.api.http;

import javax.xml.ws.WebServiceContext;

import org.oasis_open.docs.wsn.b_2.Notify;

import net.es.oscars.api.compat.DataTranslator05;
import net.es.oscars.api.soap.gen.v05.OSCARSNotifyOnly;

import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSServiceException;


@javax.jws.WebService(
        serviceName = "OSCARSNotifyOnlyService",
        portName = "OSCARSNotifyOnly",
        targetNamespace = "http://oscars.es.net/OSCARS",
        endpointInterface = "net.es.oscars.api.soap.gen.v05.OSCARSNotifyOnly")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class OSCARSNotify05SoapHandler implements OSCARSNotifyOnly{
    
    @javax.annotation.Resource
    private WebServiceContext myContext;

    public void notify(Notify notify) {
        String event = "OSCARSNotify05SoapHandler.notify";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        
        net.es.oscars.api.soap.gen.v06.InterDomainEventContent interDomain06 = new net.es.oscars.api.soap.gen.v06.InterDomainEventContent();
        try {
            interDomain06 =  DataTranslator05.translate (notify);
        } catch (OSCARSServiceException e) {
            // handle
        }
        OSCARSSoapHandler06.interDomainEvent (interDomain06, this.myContext);
    }

 
}
