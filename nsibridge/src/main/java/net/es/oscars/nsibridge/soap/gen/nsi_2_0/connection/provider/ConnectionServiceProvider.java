package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.6.1
 * 2012-11-01T10:15:38.058-07:00
 * Generated source version: 2.6.1
 * 
 */
@WebServiceClient(name = "ConnectionServiceProvider", 
                  wsdlLocation = "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_provider_v2_0.wsdl",
                  targetNamespace = "http://schemas.ogf.org/nsi/2012/03/connection/provider") 
public class ConnectionServiceProvider extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://schemas.ogf.org/nsi/2012/03/connection/provider", "ConnectionServiceProvider");
    public final static QName ConnectionServiceProviderPort = new QName("http://schemas.ogf.org/nsi/2012/03/connection/provider", "ConnectionServiceProviderPort");
    static {
        URL url = null;
        try {
            url = new URL("file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_provider_v2_0.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(ConnectionServiceProvider.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_provider_v2_0.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public ConnectionServiceProvider(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ConnectionServiceProvider(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ConnectionServiceProvider() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns ConnectionProviderPort
     */
    @WebEndpoint(name = "ConnectionServiceProviderPort")
    public ConnectionProviderPort getConnectionServiceProviderPort() {
        return super.getPort(ConnectionServiceProviderPort, ConnectionProviderPort.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ConnectionProviderPort
     */
    @WebEndpoint(name = "ConnectionServiceProviderPort")
    public ConnectionProviderPort getConnectionServiceProviderPort(WebServiceFeature... features) {
        return super.getPort(ConnectionServiceProviderPort, ConnectionProviderPort.class, features);
    }

}
