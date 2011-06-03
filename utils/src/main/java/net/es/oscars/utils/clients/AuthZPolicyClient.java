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


import net.es.oscars.authZ.soap.gen.policy.AuthZPolicyPortType;
import net.es.oscars.authZ.soap.gen.policy.AuthZPolicyService;


@OSCARSService (
        implementor = "net.es.oscars.authZ.soap.gen.policy.AuthZPolicyService",
        namespace = "http://oscars.es.net/OSCARS/authZPolicy",
        serviceName = ServiceNames.SVC_AUTHZ_POLICY
)
public class AuthZPolicyClient extends OSCARSSoapService<AuthZPolicyService, AuthZPolicyPortType> {

    private AuthZPolicyClient (URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, AuthZPolicyPortType.class);
    }
    
    static public AuthZPolicyClient getClient (URL host, URL wsdl)
            throws MalformedURLException, OSCARSServiceException {
        ContextConfig cc = ContextConfig.getInstance();
        try {
            if (cc.getContext() != null) {
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                System.out.println("AuthZPolicyClient setting BusConfiguration from " + cxfClientPath);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else { // deprecated 
                String protocol = host.getProtocol();
                String clientCxf = "client-cxf-http.xml";
                if (protocol.equals("https")){
                    clientCxf = "client-cxf-ssl.xml";
                }
                OSCARSSoapService.setSSLBusConfiguration((
                        new URL("file:" + (new SharedConfig ("AuthZPolicyService")).getFilePath(clientCxf))));
            }
        } catch (ConfigException e) {
            System.out.println("AuthZPolicyClient caught ConfigException");
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        AuthZPolicyClient client = new AuthZPolicyClient (host, wsdl);
        return client;
    }
}
