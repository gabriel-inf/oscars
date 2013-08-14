package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.config.http.HttpConfig;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

public class ProviderServer {
    private org.apache.cxf.endpoint.Server server;

    private static ProviderServer instance;

    public static ProviderServer getInstance(){
        return instance;
    }
    public static ProviderServer makeServer(HttpConfig conf) throws Exception {
        if (instance == null) {
            instance = new ProviderServer(conf.url, conf.bus);
        }
        return instance;
    }

    private ProviderServer(String url, String configFile) throws Exception {
        System.out.println("starting provider server at "+url);
        SpringBusFactory factory = new SpringBusFactory();
        ConnectionProvider cp = new ConnectionProvider();
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setServiceClass(ConnectionProviderPort.class);
        sf.setAddress(url);
        sf.setServiceBean(cp);
        server = sf.create();
        server.start();
        System.out.println("... started");
    }

    public void stop() {
        server.stop();
        server.destroy();

    }
}
