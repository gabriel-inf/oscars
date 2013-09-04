
package net.es.oscars.nsibridge.client;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;

public class ClientUtil {

    public static ConnectionProviderPort createProviderClient(String url){
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
    
    public static ConnectionRequesterPort createRequesterClient(String url){
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
    
    public static  Holder<CommonHeaderType> makeClientHeader(){
        CommonHeaderType hd = new CommonHeaderType();
        hd.setRequesterNSA("urn:oscars:nsa:client");
        hd.setCorrelationId(UUID.randomUUID().toString());
        Holder<CommonHeaderType> header = new Holder<CommonHeaderType>();
        header.value = hd;
        
        return header;
    }
    
    public static XMLGregorianCalendar unixtimeToXMLGregCal(long timestamp) throws DatatypeConfigurationException{
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp*1000);
        
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
    }
}
