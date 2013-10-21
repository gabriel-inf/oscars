package net.es.oscars.nsibridge.prov;

import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RequesterPortHolder {
    private RequesterPortHolder () {}
    private static RequesterPortHolder instance;
    public static RequesterPortHolder getInstance() {
        if (instance == null) instance = new RequesterPortHolder();
        return instance;
    }

    HashMap<URL, ConnectionRequesterPort> ports = new HashMap<URL, ConnectionRequesterPort>();
    public synchronized ConnectionRequesterPort getPort(URL url) {
        if (ports.get(url) == null) {
            ports.put(url, createPort(url));
        }
        return ports.get(url);
    }

    private ConnectionRequesterPort createPort(URL url) {


        // set logging
        LoggingInInterceptor in = new LoggingInInterceptor();
        in.setPrettyLogging(true);

        LoggingOutInterceptor out = new LoggingOutInterceptor();
        out.setPrettyLogging(true);

        JaxWsProxyFactoryBean fb = new JaxWsProxyFactoryBean();
        fb.getInInterceptors().add(in);
        fb.getOutInterceptors().add(out);

        if (url != null) {
            fb.setAddress(url.toString());
        }

        Map props = fb.getProperties();
        if (props == null) {
            props = new HashMap<String, Object>();
        }
        props.put("jaxb.additionalContextClasses",
                new Class[] {
                        net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory.class
                });
        fb.setProperties(props);

        fb.setServiceClass(ConnectionRequesterPort.class);
        ConnectionRequesterPort port = (ConnectionRequesterPort) fb.create();
        return port;


    }


}
