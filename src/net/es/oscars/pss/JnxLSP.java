package net.es.oscars.pss;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;

/**
 * JnxLSP performs setup/teardown of LSP paths on the router.
 *
 * @author Jason Lee, David Robertson
 */
public class JnxLSP {

    private Properties props;
    private Logger log;
    private TemplateHandler th;

    public JnxLSP() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss", true);
        this.log = Logger.getLogger(this.getClass());
        this.th = new TemplateHandler();
    }

    /**
     * Sets up an LSP circuit.
     *
     * @param hm a hash map with configuration values
     * @param hops a list of hops only used if explicit path was given
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean setupLSP(Map<String,String> hm, List<String> hops)
            throws PSSException {

        this.log.info("setupLSP.start");
        try {
            LSPConnection conn = new LSPConnection();
            conn.createSSHConnection(hm);
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/oscars.conf/server/" +
                this.props.getProperty("setupFile");
            this.configureLSP(hm, hops, fname, conn.out);
            this.readResponse(conn.in);
            conn.shutDown();
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        } catch (JDOMException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("setupLSP.finish");
        return true;
    }

    /**
     * Tears down an LSP circuit.
     *
     * @param hm A hash map with configuration values
     * @param hops a list of hops only used if explicit path was given
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean teardownLSP(Map<String,String> hm, List<String> hops) 
            throws PSSException {

        this.log.info("teardownLSP.start");
        try {
            LSPConnection conn = new LSPConnection();
            conn.createSSHConnection(hm);
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/oscars.conf/server/" +
                this.props.getProperty("teardownFile");
            this.configureLSP(hm, hops, fname, conn.out);
            this.readResponse(conn.in);
            conn.shutDown();
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        } catch (JDOMException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("teardownLSP.finish");
        return true;
    }

    /** 
     * Gets the LSP status on a Juniper router.
     *
     * @param hm a hash map with configuration values
     * @return an int, with value -1 if NA (e.g not found), 0 if down, 1 if up
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    public int statusLSP(Map<String,String> hm)
            throws IOException, JDOMException, PSSException {

        int status = -1;

        this.log.info("statusLSP.start");
        LSPConnection conn = new LSPConnection();
        conn.createSSHConnection(hm);
        status = getStatus(hm, conn);
        conn.shutDown();
        this.log.info("statusLSP.finish");
        return status;
    }

    /**
     * Shows example setting up of a DOM to request information from a router.
     * The ouputted document looks like:
     *
     *    <?xml version="1.0" encoding="us-ascii"?>
     *    <junoscript version="1.0" release="7.3">
     *       <rpc>
     *           <get-chassis-inventory>
     *               <detail />
     *           </get-chassis-inventory>
     *       </rpc>
     *     </junoscript>
     *
     * @param out
     * @param operation
     * @throws IOException
     */
    private void doConfig(OutputStream out, String operation)
            throws IOException {

        Element rpcElement = new Element("rpc");
        Document myDocument = new Document(rpcElement);
        Element commit = new Element(operation);
        rpcElement.addContent(commit);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        outputter.output(myDocument, out);
    }

    /**
     * Configure an LSP. Sends the LSP-XML command to the server
     * and sees what happens.
     *
     * @param hm hash map of info from reservation and OSCARS' configuration
     * @param hops a list of hops only used if explicit path was given
     * @param fname full path of template file
     * @param out SSH output stream
     * @return boolean indicating status (not done yet)
     * @throws IOException
     * @throws JDOMException
     */
    private boolean configureLSP(Map<String,String> hm, List<String> hops,
                                 String fname, OutputStream out) 
            throws IOException, JDOMException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());
        Document doc = this.th.fillTemplate(hm, hops, fname);

        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        // log, and then send to router
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        outputter.output(doc, out);
        // TODO:  get status
        this.log.info("configureLSP.end");
        return true;
    }

    /**
     * Checks the status of the LSP that was just set up or torn down.
     *
     * @param hm hash map
     * @param conn SSH connection
     * @return status (not done yet) 
     * @throws IOException
     * @throws JDOMException
     */
    private int getStatus(Map<String,String> hm, LSPConnection conn) 
            throws IOException, JDOMException {

        int status = -1;

        doConfig(conn.out, "get-mpls-lsp-information");
        // TODO:  FIX getting status
        String reply = readResponse(conn.in);
        return status;
    }

    /**
     * Reads the response from the socket created earlier into a DOM
     * (makes for easier parsing).
     *
     * @param in InputStream
     * @return string with TODO
     * @throws IOException
     * @throws JDOMException
     */
    private String readResponse(InputStream in) 
            throws IOException, JDOMException {

        ByteArrayOutputStream buff  = new ByteArrayOutputStream();
        Document d = null;
        SAXBuilder b = new SAXBuilder();
        d = b.build(in);
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(d, buff);
        /* XXX: parse output here! */
        String reply = buff.toString();
        return reply;
    }
}
