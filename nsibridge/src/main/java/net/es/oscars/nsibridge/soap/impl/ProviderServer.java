package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.config.HttpConfig;
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
            instance = new ProviderServer(conf.getUrl(), conf.getBus());
        }
        return instance;
    }

    private ProviderServer(String url, String configFile) throws Exception {
        System.out.println("starting provider server at "+url);
        SpringBusFactory factory = new SpringBusFactory();
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



        LoggingInInterceptor in = new LoggingInInterceptor();
        in.setPrettyLogging(true);

        LoggingOutInterceptor out = new LoggingOutInterceptor();
        out.setPrettyLogging(true);
        Feature f = new LoggingFeature();
        sf.getFeatures().add(f);
        sf.getInInterceptors().add(in);
        sf.getOutInterceptors().add(out);

        sf.setServiceClass(ConnectionProviderPort.class);
        sf.setAddress(url);
        sf.setServiceBean(cp);

        server = sf.create();
        server.start();
        this.setRunning(true);
        System.out.println("    ... provider server started");
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
