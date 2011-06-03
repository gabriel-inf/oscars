package net.es.oscars.utils.clients;

import java.net.MalformedURLException;
import java.net.URL;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerPortType;
import net.es.oscars.wsnbroker.soap.gen.WSNBrokerService;

@OSCARSNetLoggerize(moduleName = ModuleName.WSNBROKER)
@OSCARSService (
        implementor = "net.es.oscars.wsnbroker.soap.gen.WSNBrokerService",
        namespace = "http://oscars.es.net/OSCARS/wsnbroker", 
        serviceName = ServiceNames.SVC_WSNBROKER
)
public class WSNBrokerClient extends OSCARSSoapService<WSNBrokerService, WSNBrokerPortType>{
    
    /**
     * Constructor. 
     * 
     * @param host The location of the host to contact
     * @param wsdl The location of the WSDL file for this service
     * @throws OSCARSServiceException
     */
    public WSNBrokerClient(URL host, URL wsdl) throws OSCARSServiceException {
        super(host, wsdl, WSNBrokerPortType.class);
    }
    
    /**
     * Creates a WSNBrokerClient object that can be used to call the external 
     * ws-notification broker server.
     * 
     * @param host The location of the host to contact as a URL string
     * @param wsdl The location of the service wsdl as a URL string
     * @return A WSNBrokerClient object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public WSNBrokerClient getClient(String host, String wsdl) throws OSCARSServiceException, MalformedURLException{
        ContextConfig cc = ContextConfig.getInstance();
        try {
            cc.setLog4j();
        } catch (ConfigException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("WSNBrokerClient.getClient");
        try {
            String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
            System.out.println("cxfClientPath=" + cxfClientPath);
            OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        if(wsdl == null){
            return WSNBrokerClient.getClient(host);
        }
        return new WSNBrokerClient(new URL(host), new URL(wsdl));
    }
    
    /**
     * Creates a WSNBrokerClient object that can be used to call the external ws-notification 
     * service given only the address of the service. The location of the WSDL
     * is assumed to be <i>host</i>?wsdl.
     * 
     * @param host The location of the host to contact as a URL string
     * @return A WSNBrokerClient object that can be used to send request to the service
     * @throws OSCARSServiceException
     * @throws MalformedURLException
     */
    static public WSNBrokerClient getClient(String host) throws OSCARSServiceException, MalformedURLException{
        return WSNBrokerClient.getClient(host, host+"?wsdl");
    }
}