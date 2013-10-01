
package net.es.oscars.nsibridge.client;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import net.es.oscars.nsibridge.config.ClientConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;

/**
 * Utility classes for building clients
 *
 */
public class ClientUtil {
    final public static String DEFAULT_URL = "https://localhost:8500/nsi-v2/ConnectionServiceProvider";
    final public static String DEFAULT_REQUESTER = "urn:oscars:nsa:client";
    final public static String DEFAULT_PROVIDER = DEFAULT_REQUESTER;
    final public static String DEFAULT_PROTOCOL_VERSION = "2.0";
    
    /**
     * Creates a client class can be used to call provider at given URL
     * 
     * @param url the URL of the provider to contact
     * @param clientBusFile the bus file that defines characteristics of HTTP connections
     * @return the ConnectionProviderPort that you can use as the client
     */
    public static ConnectionProviderPort createProviderClient(String url, String clientBusFile){

        prepareBus(url, clientBusFile);

        // set logging
        LoggingInInterceptor in = new LoggingInInterceptor();
        in.setPrettyLogging(true);

        LoggingOutInterceptor out = new LoggingOutInterceptor();
        out.setPrettyLogging(true);

        JaxWsProxyFactoryBean fb = new JaxWsProxyFactoryBean();
        fb.getInInterceptors().add(in);
        fb.getOutInterceptors().add(out);

        fb.setAddress(url);

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
        ConnectionProviderPort client = (ConnectionProviderPort) fb.create();
        
        return client;
    }
    
    /**
     * Creates client using bus file defined in beans definition
     * @param url URL of the provider to contact
     * @return the ConnectionProviderPort that you can use as the client
     */
    public static ConnectionProviderPort createProviderClient(String url){
        return createProviderClient(url, null);
    }
    
    /**
     * Creates a client for interacting with an NSA requester
     * 
     * @param url the URL of the requester to contact
     * @param clientBusFile the bus file that defines characteristics of HTTP connections
     * @return the ConnectionRequesterPort that you can use at the client
     */
    public static ConnectionRequesterPort createRequesterClient(String url, String clientBusFile){

        prepareBus(url, clientBusFile);


        // set logging
        LoggingInInterceptor in = new LoggingInInterceptor();
        in.setPrettyLogging(true);

        LoggingOutInterceptor out = new LoggingOutInterceptor();
        out.setPrettyLogging(true);

        JaxWsProxyFactoryBean fb = new JaxWsProxyFactoryBean();
        fb.getInInterceptors().add(in);
        fb.getOutInterceptors().add(out);

        fb.setAddress(url);

        Map props = fb.getProperties();
        if (props == null) {
            props = new HashMap<String, Object>();
        }
        props.put("jaxb.additionalContextClasses",
                new Class[] {
                        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory.class
                });
        fb.setProperties(props);

        fb.setServiceClass(ConnectionRequesterPort.class);
        ConnectionRequesterPort client = (ConnectionRequesterPort) fb.create();
        
        return client;
    }

    /**
     * Creates a client for interacting with an NSA requester
     * 
     * @param url the URL of the requester to contact
     * @return the ConnectionRequesterPort that you can use at the client
     */
    public static ConnectionRequesterPort createRequesterClient(String url){
        return createRequesterClient(url, null);
    }

    /**
     * Configures SSL and other basic client settings
     * @param url the URL of the server to contact
     */
    public static void prepareBus(String url, String clientBusFile) {
        String busFile = null;
        String sslBusFile = null;
        if(clientBusFile == null){
            String beansFile = System.getProperty("nsibridge.beans");
            if(beansFile == null || "".equals(beansFile)){
                beansFile = "config/beans.xml";
            }
            SpringContext.getInstance().initContext(beansFile);
            ClientConfig cc = SpringContext.getInstance().getContext().getBean("clientConfig", ClientConfig.class);
            sslBusFile = cc.getSslBus();
            busFile = cc.getBus();
        }else{
            busFile =clientBusFile;
            sslBusFile = clientBusFile;
        }
        
        SpringBusFactory bf = new SpringBusFactory();
        Bus bus;
        if (url.toLowerCase().startsWith("https")) {
            System.setProperty("javax.net.ssl.trustStore","DoNotUsecacerts");
            bus = bf.createBus(sslBusFile);
        } else {
            bus = bf.createBus(busFile);
        }
        SpringBusFactory.setDefaultBus(bus);
    }

    
    /**
     * Creates a basic header with a random Correlation ID and dfault requester
     * @return the generated header
     */
    public static  Holder<CommonHeaderType> makeClientHeader(){
        CommonHeaderType hd = new CommonHeaderType();
        hd.setRequesterNSA(DEFAULT_REQUESTER);
        hd.setProviderNSA(DEFAULT_PROVIDER);
        hd.setProtocolVersion(DEFAULT_PROTOCOL_VERSION);
        hd.setCorrelationId("urn:uuid:"+UUID.randomUUID().toString());
        Holder<CommonHeaderType> header = new Holder<CommonHeaderType>();
        header.value = hd;
        
        return header;
    }
    
    /**
     * Converts timestamp to an XML time
     * 
     * @param timestamp the timestamp to convert
     * @return the XMLGregorianCalendar representaion of the given timestamp
     * @throws DatatypeConfigurationException
     */
    public static XMLGregorianCalendar unixtimeToXMLGregCal(long timestamp) throws DatatypeConfigurationException{
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
    }
}
