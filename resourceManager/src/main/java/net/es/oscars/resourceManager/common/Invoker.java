package net.es.oscars.resourceManager.common;


import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.resourceManager.http.RMSoapServer;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.invoke.BaseInvoker;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;

import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;

public class Invoker {
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_RM);

    /**
     * Main program to start ResourceManagerService 
     * @param args [-h, ?] for help
     *              [-c PRODUCTION | SDK | DEVELOPMENT | INITTEST ] for context, defaults to PRODUCTION
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> invArgs = BaseInvoker.parseArgs(args);
        String context = invArgs.get(BaseInvoker.INV_CONTEXT);

        Logger log = null;
        
        System.out.println("starting resourceManager service with context " + context);
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_RM);
        try {
            cc.loadManifest(ServiceNames.SVC_RM,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            log = Logger.getLogger(Invoker.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.RM,"0000");
        OSCARSSoapService.setSSLBusConfiguration(
                new URL("file:" + cc.getFilePath(ConfigDefaults.CXF_SERVER)));
        
        RMSoapServer server = RMSoapServer.getInstance();
        server.startServer(false);
        log.info(netLogger.end("invoker","started with context: " + context));

    }

}
