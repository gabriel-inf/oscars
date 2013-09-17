package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.config.HttpConfig;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import java.util.HashMap;
import java.util.Map;

public class ProviderServer {
    private org.apache.cxf.endpoint.Server server;
    private boolean running = false;

    private static ProviderServer instance;

    public static ProviderServer getInstance(){
        return instance;
    }
    public static ProviderServer makeServer(HttpConfig conf) throws Exception {
        if (instance == null) {
            instance = new ProviderServer(conf);
        }
        return instance;
    }

    private ProviderServer(HttpConfig conf) throws Exception {
        String url = conf.getUrl();
        String sslBusConfigFile = conf.getSslBus();
        String busConfigFile = conf.getBus();
        boolean httpBasic = conf.isBasicAuth();



        System.out.println("Starting ConnectionProvider SOAP server at "+url+" ..");

        ConnectionProvider cp = new ConnectionProvider();
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();

        Map props = sf.getProperties();
        if (props == null) {
            props = new HashMap<String, Object>();
        }
        props.put("jaxb.additionalContextClasses",
                        new Class[] {
                            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory.class
                        });
        sf.setProperties(props);


        /*
        OscarsHttpBasicAuthNInInterceptor basicInt = OscarsHttpBasicAuthNInInterceptor.getInstance();
        sf.getInInterceptors().add(basicInt);
        */

        SpringBusFactory factory = new SpringBusFactory();
        boolean useSSL = false;
        if (url.toLowerCase().startsWith("https")) {
            useSSL = true;
        }

        if (useSSL) {
            factory.createBus(sslBusConfigFile);
        } else {
            factory.createBus(busConfigFile);
        }

        if (!useSSL && !httpBasic) {
            throw new ServiceException("No authentication mechanism specified.");
        }

        if (useSSL && httpBasic) {
            System.out.println("Both HTTPS and HTTP-Basic enabled. ");
        }

        if (useSSL) {
            System.out.println("HTTPS client certificates will be used for authentication.");
            OscarsCertInInterceptor certInt = OscarsCertInInterceptor.getInstance();
            if (httpBasic) {
                System.out.println("HTTPS client certificate authentication will fail over to HTTP-Basic.");
                certInt.setHttpBasicFailover(true);
            }
            sf.getInInterceptors().add(certInt);
        }

        if (httpBasic) {
            if (!useSSL) {
                System.out.println("HTTP-Basic will be used for authentication.");
            }
            OscarsHttpBasicAuthNInInterceptor authInt = OscarsHttpBasicAuthNInInterceptor.getInstance();
            sf.getInInterceptors().add(authInt);
        }


        sf.setServiceClass(ConnectionProviderPort.class);
        sf.setAddress(url);
        sf.setServiceBean(cp);

        server = sf.create();
        server.start();
        this.setRunning(true);
        System.out.println("    .. SOAP server started.");
    }

    public void stop() {
        server.stop();
        server.destroy();
        this.setRunning(false);

    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
