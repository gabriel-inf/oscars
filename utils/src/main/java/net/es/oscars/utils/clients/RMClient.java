
package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;


import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.resourceManager.soap.gen.ResourceManagerService;
import net.es.oscars.resourceManager.soap.gen.RMPortType;

@OSCARSNetLoggerize(moduleName = ModuleName.RM)
@OSCARSService (
        implementor = "net.es.oscars.resourceManager.soap.gen.ResourceManagerService",
        namespace   = "http://oscars.es.net/OSCARS/resourceManager",
        serviceName = ServiceNames.SVC_RM
)
public class RMClient extends OSCARSSoapService<ResourceManagerService, RMPortType> {

    private RMClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, RMPortType.class);
    }
    
    static public RMClient getClient (URL host, URL wsdl) throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
        try {
            if (cc.getContext() != null) {
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                System.out.println("RMClient setting BusConfiguration from " + cxfClientPath);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else { // deprecated 
                String protocol = host.getProtocol();
                String clientCxf = "client-cxf-http.xml";
                if (protocol.equals("https")){
                    clientCxf = "client-cxf-ssl.xml";
                }
                OSCARSSoapService.setSSLBusConfiguration((
                        new URL("file:" + (new SharedConfig ("ResourceManagerService")).getFilePath(clientCxf))));
            }
        } catch (ConfigException e) {
            System.out.println("RMClient caught ConfigException");
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        RMClient client = new RMClient (host, wsdl);
        return client;
    }

}
