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

import net.es.oscars.authN.soap.gen.policy.AuthNPolicyPortType;
import net.es.oscars.authN.soap.gen.policy.AuthNPolicyService;


@OSCARSService (
        implementor = "net.es.oscars.authN.soap.gen.policy.AuthNPolicyService",
        namespace = "http://oscars.es.net/OSCARS/authNPolicy",
        serviceName = "AuthNPolicyService"
)
public class AuthNPolicyClient extends OSCARSSoapService<AuthNPolicyService, AuthNPolicyPortType> {

    private AuthNPolicyClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, AuthNPolicyPortType.class);
    }
    
    static public AuthNPolicyClient getClient (URL host, URL wsdl)
            throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
        try {
            if (cc.getContext() != null ) {  // use new configuration method
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                System.out.println("AuthNPolicyClient setting BusConfiguration from " + cxfClientPath);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else { // deprecated
                String protocol = host.getProtocol();
                String clientCxf = "client-cxf-http.xml";
                if (protocol.equals("https")){
                    clientCxf = "client-cxf-ssl.xml";
                }
                OSCARSSoapService.setSSLBusConfiguration((
                        new URL("file:" + (new SharedConfig (ServiceNames.SVC_AUTHN_POLICY)).getFilePath(clientCxf))));
            }
        } catch (ConfigException e) {
            System.out.println("AuthNPolicyClient caught ConfigException");
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        AuthNPolicyClient client = new AuthNPolicyClient (host, wsdl);
        return client;
    }
}
