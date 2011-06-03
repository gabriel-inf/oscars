package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pss.soap.gen.PSSPortType;
import net.es.oscars.pss.soap.gen.PSSService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

/**
 *
 *
 * @author haniotak
 *
 */
@OSCARSNetLoggerize(moduleName = ModuleName.PSS)
@OSCARSService (
        implementor = "net.es.oscars.pss.soap.gen.PSSService",
        namespace   = "http://oscars.es.net/OSCARS/pss",
        serviceName = ServiceNames.SVC_PSS
)
public class PSSClient extends OSCARSSoapService<PSSService, PSSPortType>  {
    private PSSClient (URL host, URL wsdlFile) throws OSCARSServiceException {
        super (host, wsdlFile, PSSPortType.class);
    }

    static public PSSClient getClient (URL host, URL wsdl)
        throws MalformedURLException, OSCARSServiceException {

        ContextConfig cc = ContextConfig.getInstance();
        try {
            if (cc.getContext() != null){
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                System.out.println("PSSClient setting BusConfiguration from " + cxfClientPath);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else { // deprecated
                String protocol = host.getProtocol();
                String clientCxf = "client-cxf-http.xml";
                if (protocol.equals("https")){
                    clientCxf = "client-cxf-ssl.xml";
                }
                OSCARSSoapService.setSSLBusConfiguration((
                        new URL("file:" + (new SharedConfig (ServiceNames.SVC_PSS)).getFilePath(clientCxf))));
            }
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
 /*
        URL clientCxf = new URL("file:" + (new SharedConfig (ServiceNames.SVC_PSS)).getFilePath(ConfigDefaults.CXF_CLIENT));
        System.out.println("PSSClient clientCxf is "+ clientCxf.toString());
        OSCARSSoapService.setSSLBusConfiguration((
                new URL("file:" + (new SharedConfig (ServiceNames.SVC_PSS)).getFilePath(ConfigDefaults.CXF_CLIENT))));
*/
        PSSClient client = new PSSClient (host, wsdl);
        return client;
    }

}




