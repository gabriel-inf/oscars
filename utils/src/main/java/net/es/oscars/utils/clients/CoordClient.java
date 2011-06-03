
package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.coord.soap.gen.CoordPortType;
import net.es.oscars.coord.soap.gen.CoordService;

@OSCARSNetLoggerize(moduleName = ModuleName.COORD)
@OSCARSService (
        implementor = "net.es.oscars.coord.soap.gen.CoordService",
        namespace = "http://oscars.es.net/OSCARS/coord",
        serviceName = ServiceNames.SVC_COORD
)
public class CoordClient extends OSCARSSoapService<CoordService, CoordPortType> {

    private CoordClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, CoordPortType.class);
    }
    
    static public CoordClient getClient (URL host, URL wsdl) throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
        boolean loggingInit = false;
        String event = "coordGetClient";
        Logger LOG = Logger.getLogger(CoordClient.class);
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        if (netLogger.getGUID() != null) {
            loggingInit = true;
        }
        try {
            if (cc.getContext() != null) {
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                if (loggingInit ) {
                    LOG.debug(netLogger.start(event,"setting BusConfiguration from " + cxfClientPath));
                } else {
                    System.out.println("CoordClient setting BusConfiguration from " + cxfClientPath);
                }
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else { // deprecated 
                String protocol = host.getProtocol();
                String clientCxf = "client-cxf-http.xml";
                if (protocol.equals("https")){
                    clientCxf = "client-cxf-ssl.xml";
                }
                OSCARSSoapService.setSSLBusConfiguration((
                        new URL("file:" + (new SharedConfig ("CoordService")).getFilePath(clientCxf))));
            }
        } catch (ConfigException e) {
            if (loggingInit) {
                LOG.error(netLogger.error(event,ErrSev.MAJOR, " caughtException: " + e.getMessage()));
            } else {
                System.out.println("CoordClient caught ConfigException");
                e.printStackTrace();
            }
            throw new OSCARSServiceException(e.getMessage());
        }
        CoordClient client = new CoordClient (host, wsdl);
        return client;
    }

}
