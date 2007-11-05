package net.es.oscars.bss.topology;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.es.oscars.bss.BSSException;
import net.es.oscars.PropHandler;

/**
 * Class used to retrieve the URNs assocaited with a given hostname 
 * via the perfSONAR Lookup Service 
 *  
 * @author Andrew Lake
 */
public class PSLookupClient {
    private Logger log;
    private HttpClient client;
    private String fname;
    private String url;
    private Properties props;
    
    /** Contructor */
    public PSLookupClient(){
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("lookup", true);
        this.url = this.props.getProperty("url");
        this.log = Logger.getLogger(this.getClass());
        this.client = new HttpClient();
        this.fname =  System.getenv("CATALINA_HOME") +
        "/shared/classes/server/perfSONAR-LSQuery.xml";
    }
    
    /**
     * Retieves the URN for a given hostname from the perfSONAR
     * Lookup Service. The URL of the service is defined in oscars.properties.
     *
     * @param hostname a String containing the hostname of the URN to lookup
     * @return String of URN found. null if URN is not found by Lookup Service.
     * throws BSSException
     */
    public String lookup(String hostname) throws BSSException{
        String urn = null;
        Document topologyXMLDoc = null;
        
        //Generate and send response
        try{
            PostMethod postMethod = this.generateRequest(this.url, hostname);
            String response = this.sendRequest(postMethod);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            ByteArrayInputStream in = new ByteArrayInputStream(response.getBytes());
            topologyXMLDoc = docBuilder.parse(in);
        }catch (HttpException e) {
            this.log.error("HTTP Error: " + e.getMessage());
            throw new BSSException("HTTP Error: " + e.getMessage());
        }catch (SAXException e) {
            this.log.error("SAX Error: " + e.getMessage());
            throw new BSSException("SAX Error: " + e.getMessage());
        }catch (ParserConfigurationException e) {
            this.log.error("Parser Error: " + e.getMessage());
            throw new BSSException("Parser Error: " + e.getMessage());
        }catch (IOException e) {
            this.log.error("IO Error: " + e.getMessage());
            throw new BSSException("IO Error: " + e.getMessage());
        }
        
        //Parse response
        NodeList urnList = topologyXMLDoc.getElementsByTagName("psservice:datum");
        Node urnNode = urnList.item(0);
        if(urnNode == null){
            return null;
        }
        
        urn = urnNode.getTextContent();
        if(urn == null || urn.equals("Nothing returned for search.")){
            return null;
        }
        
        //return urn with trailing whitespace removed
        return urn.replaceAll("\\s$", "");
    }
    
    /**
     * Private method that creates the request to send to the lookup service.
     * It reads an XML file, replaces the correct fieldswith the hostname,
     * and returns a PostMethod for use by HTTPClient
     *
     * @param url the URL of the perfSONAR lookup service
     * @param hostname the name of the host to lookup
     * @return a PostMethod that is callable by HttpClient
     * @throws IOException
     */
    private PostMethod generateRequest(String url, String hostname) 
        throws IOException{
        PostMethod postMethod = new PostMethod(url);
        FileReader fin = new FileReader(this.fname);
        StringBuilder xmlRequestBuilder = new StringBuilder("");
        String xmlRequest = null;
        StringRequestEntity entity = null;
        
        char[] buf = new char[1500];
        while(fin.read(buf, 0, buf.length) > 0){
            xmlRequestBuilder.append(buf);
        }
        xmlRequest = xmlRequestBuilder.toString();
        xmlRequest = xmlRequest.replaceAll("<!--hostname-->", hostname);
        entity = new StringRequestEntity(xmlRequest, "text/xml",null);
        postMethod.setRequestEntity(entity);
        
        return postMethod;
    }
    
    /**
     * Sends the given lookup request to the server
     *
     * @param postMethod the PostMethod created by a call to generateRequest
     * @return the raw XML response from the server
     * @throws HttpException
     * @throwsIOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private String sendRequest(PostMethod postMethod) throws HttpException, 
        IOException, ParserConfigurationException, SAXException{
        int statusCode = client.executeMethod(postMethod);
        this.log.info("LOOKUP REQUEST HTTP STATUS: " + statusCode);
        String response = postMethod.getResponseBodyAsString();
        
        return response;
    }
}

