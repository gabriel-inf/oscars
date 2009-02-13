package net.es.oscars.notifybroker.jdom;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * Simple client for sending SOAP Messages over HTTP. Uses JDOM element to
 * construct the body of SOAP messages and the apache commons HTTPClient.
 * 
 * @author Andrew Lake (alake@internet2.edu)
 */
public class NBSoapClient {
    private Logger log;
    
    public NBSoapClient(){
        this.log = Logger.getLogger(this.getClass());
    }
    
    /**
     * Sends SOAP message that does not expect any SOAP in the response body.
     * 
     * @param url Where to send the request
     * @param action the SOAP action of the request
     * @param bodyElem the JDOM element used as the content of the SOAP body
     * @throws RemoteException
     */
    public void sendAsyncSoapMessage(String url, String action, Element bodyElem) throws RemoteException{
        Element envelope = new Element("Envelope", WSNotifyConstants.SOAP_NS);
        Element body = new Element("Body", WSNotifyConstants.SOAP_NS);
        body.addContent(bodyElem.detach());
        envelope.addContent(body);
        XMLOutputter outputter = new XMLOutputter();
        
        this.log.debug("Sending to URL " + url);
        PostMethod postMethod = new PostMethod(url);
        try {
            StringRequestEntity entity = new StringRequestEntity(outputter.outputString(envelope), "application/soap+xml",null);
            postMethod.setRequestEntity(entity);
            postMethod.addRequestHeader("action",action);
            HttpClient client = new HttpClient();
            HttpClientParams params = client.getParams();
            params.setSoTimeout(30000);
            int statusCode = client.executeMethod(postMethod);
            this.log.debug("Sent Notify and got HTTP status code " + statusCode);
        } catch (UnsupportedEncodingException e) {
            throw new RemoteException(e.getMessage() + " (" + url + ")");
        } catch (HttpException e) {
            throw new RemoteException(e.getMessage() + " (" + url + ")");
        } catch (IOException e) {
            throw new RemoteException(e.getMessage() + " (" + url + ")");
        }
    }
}
