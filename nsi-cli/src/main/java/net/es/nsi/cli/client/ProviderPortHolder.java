package net.es.nsi.cli.client;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProviderPortHolder {

    private ProviderPortHolder () {}
    private static ProviderPortHolder instance;
    public static ProviderPortHolder getInstance() {
        if (instance == null) instance = new ProviderPortHolder();
        return instance;
    }

    HashMap<URL, ConnectionProviderPort> ports = new HashMap<URL, ConnectionProviderPort>();
    public synchronized ConnectionProviderPort getPort(URL url) {
        if (ports.get(url) == null) {
            ports.put(url, createPort(url));
        }
        return ports.get(url);
    }

    private ConnectionProviderPort createPort(URL url) {


        // set logging
        LoggingInInterceptor in = new LoggingInInterceptor();
        in.setPrettyLogging(true);

        LoggingOutInterceptor out = new LoggingOutInterceptor();
        out.setPrettyLogging(true);

        JaxWsProxyFactoryBean fb = new JaxWsProxyFactoryBean();
        fb.getInInterceptors().add(in);
        fb.getOutInterceptors().add(out);

        fb.setAddress(url.toString());

        Map props = fb.getProperties();
        if (props == null) {
            props = new HashMap<String, Object>();
        }
        props.put("jaxb.additionalContextClasses",
                new Class[] {
                        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory.class
                });
        fb.setProperties(props);

        fb.setServiceClass(ConnectionProviderPort.class);
        ConnectionProviderPort port = (ConnectionProviderPort) fb.create();
        return port;
    }

}
