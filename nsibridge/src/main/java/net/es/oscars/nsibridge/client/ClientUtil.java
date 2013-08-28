
package net.es.oscars.nsibridge.client;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.apache.cxf.frontend.ClientProxy;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionServiceProvider;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;

public class ClientUtil {
    
    public static ConnectionProviderPort createProviderClient(String url){
        ConnectionServiceProvider serviceProvider = new ConnectionServiceProvider();
        ConnectionProviderPort client = serviceProvider.getConnectionServiceProviderPort();
        ClientProxy.getClient(client).getRequestContext().put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", url);
        
        return client;
    }
    
    public static  Holder<CommonHeaderType> makeClientHeader(){
        CommonHeaderType hd = new CommonHeaderType();
        hd.setRequesterNSA("urn:oscars:nsa:client");
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
