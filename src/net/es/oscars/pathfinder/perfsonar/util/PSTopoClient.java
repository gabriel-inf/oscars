package net.es.oscars.pathfinder.perfsonar.util;

import net.es.oscars.PropHandler;

import java.util.*;
import java.io.*;

import org.apache.log4j.*;

import net.es.oscars.bss.topology.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import net.es.oscars.lookup.LookupException;
import org.xml.sax.SAXException;
import java.lang.Exception;
import org.jdom.xpath.XPath;

public class PSTopoClient {
    String url;
    Topology topology;
    Namespace nmwgNs;
    Namespace topoNs;

    private Logger log;
    private Properties props;
    private String request_str = 
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" " +
            "     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "     xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"> " +
            "    <SOAP-ENV:Header/> "+
            "    <SOAP-ENV:Body> "+
            "<nmwg:message type=\"SetupDataRequest\" "+
               "xmlns:nmwg=\"http://ggf.org/ns/nmwg/base/2.0/\"> "+
               " <nmwg:metadata id=\"meta1\">" +
               "   <nmwg:eventType>http://ggf.org/ns/nmwg/topology/query/all/20070809</nmwg:eventType>" +
               "</nmwg:metadata>" +
               "<nmwg:data id=\"data1\" metadataIdRef=\"meta1\"/> "+
            "</nmwg:message>" +
            "</SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

    public PSTopoClient(String ts_url) {
        this.url = ts_url;
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("topo_client", true);
        this.nmwgNs = Namespace.getNamespace("nmwg", "http://ggf.org/ns/nmwg/base/2.0/");
        this.topoNs = Namespace.getNamespace("nmtopo", "http://ogf.org/schema/network/topology/base/20070828/");
    }

    public List<Domain> getDomains() {
        if (this.topology == null) {
            lookupTopology();
        }

        if (this.topology == null) {
            return null;
        }

        return topology.getDomains();
    }

    public Topology getTopology() {
        if (this.topology == null) {
            lookupTopology();
        }

        return this.topology;
    }

    private void lookupTopology() {
        String urn = null;
        Document responseMessage = null;

        //Generate and send response
        try {
             SAXBuilder xmlParser = new SAXBuilder();

            this.log.info("Connecting to "+this.url);
            PostMethod postMethod = new PostMethod(this.url);
            StringRequestEntity entity = new StringRequestEntity(request_str, "text/xml",null);
            postMethod.setRequestEntity(entity);

            HttpClient client = new HttpClient();

            this.log.info("Sending post");
            int statusCode = client.executeMethod(postMethod);
            this.log.info("Post done");

            String response = postMethod.getResponseBodyAsString();
            ByteArrayInputStream in = new ByteArrayInputStream(response.getBytes());
            this.log.info("Parsing start");
            responseMessage = xmlParser.build(in);
            this.log.info("Parsing done");

            this.parseResponseMessage(responseMessage);
        } catch (Exception e) {
            this.log.error("Error: " + e.getMessage());
        }
    }

    private void parseResponseMessage(Document doc) {
        Element root = doc.getRootElement();
        Element message = null;

        try {
            this.log.info("Looking for message");
            XPath xpath = XPath.newInstance("//nmwg:message");
            xpath.addNamespace(this.nmwgNs.getPrefix(), this.nmwgNs.getURI());

            message = (Element) xpath.selectSingleNode(root);
        } catch (org.jdom.JDOMException ex) {
            this.log.info("DOM Exception: "+ex.getMessage());
        }

        if (message == null) {
            this.log.info("No message in response");
            return;
        }

        this.log.info("Message found");

        this.log.info("Looking for metadata");
        List<Element> metadata_elms = message.getChildren("metadata", nmwgNs);
        for (Element metadata : metadata_elms) {
            String md_id = metadata.getAttributeValue("id");
            Element eventType_elm = metadata.getChild("eventType", nmwgNs);

            this.log.info("Found metadata "+md_id);

            if (eventType_elm == null)
                continue;

            String eventType = eventType_elm.getValue();

            this.log.info("Found eventType: "+eventType);

            if (eventType.equals("http://ggf.org/ns/nmwg/topology/query/all/20070809") == false)
                continue;

            List<Element> data_elms = message.getChildren("data", nmwgNs);
            for (Element data : data_elms) {
                String md_idRef = data.getAttributeValue("metadataIdRef");

                this.log.info("Found data -> "+md_idRef);
                if (md_idRef.equals(md_id) == false) {
                    continue;
                }

                Element topo = data.getChild("topology", topoNs);

                if (topo == null)
                    continue;

                TopologyXMLParser parser = new TopologyXMLParser(null);

                this.topology = parser.parse(topo, null);
            }
        }
    }
}
