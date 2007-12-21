package net.es.oscars.pss.jnx;

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
import net.es.oscars.pss.*;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;

/**
 * JnxLSP performs setup/teardown of LSP paths on the router.
 *
 * @author David Robertson, Jason Lee
 */
public class JnxLSP {

    private String dbname;
    private Properties commonProps;
    private Properties props;
    private TemplateHandler th;
    private Logger log;
    private Map<String,String> hm;
    private boolean allowLSP;

    public JnxLSP(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.commonProps = propHandler.getPropertyGroup("pss", true);
        this.allowLSP =
            this.commonProps.getProperty("allowLSP").equals("1") ? true : false;
        this.props = propHandler.getPropertyGroup("pss.jnx", true);
        this.th = new TemplateHandler();
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
    }

    /**
     * Formats reservation parameters for circuit setup.
     *
     * @param resv a reservation instance
     * @param lspData LSPData instance containing path-related configuration
     * @param direction boolean indicating whether forward path
     * @throws PSSException
     */
    public void createPath(Reservation resv, LSPData lspData,
                             String direction) throws PSSException {

        // only used if an explicit path was given
        List<String> hops = null;
        String param = null;

        if (lspData == null) {
            throw new PSSException(
                    "no path related configuration data present");
        }
        if ((direction == null) || (!direction.equals("forward") &&
                                    !direction.equals("reverse"))) {
            throw new PSSException("illegal circuit direction");
        }
        Path path = resv.getPath();
        this.log.info("path id: " + path.getId());
        MPLSData mplsData = path.getMplsData();
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        PathElem pathElem = path.getPathElem();
        if (layer2Data != null) {
            if (!path.isExplicit()) {
                throw new PSSException(
                        "no explicit path provided for layer 2 Juniper setup");
            }
            if (lspData.getIngressLink() == null) {
                throw new PSSException(
                        "createPath called before getting path endpoints");
            }
            // get VLAN tag and loopbacks
            lspData.setLayer2PathInfo(true);
        } else if (layer3Data != null) {
            lspData.setLayer3PathInfo("Juniper");
        } else {
            throw new PSSException("no layer 2 or layer 3 data provided");
        }
        // Create map for filling in template.
        this.hm = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in name parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        this.hm.put("bandwidth", Long.toString(resv.getBandwidth()));
        if (mplsData != null) {
            if (mplsData.getLspClass() != null) {
                this.hm.put("lsp_class-of-service", mplsData.getLspClass());
            } else {
                this.hm.put("lsp_class-of-service", "4");
            }
            this.hm.put("policer_burst-size-limit",
                    Long.toString(mplsData.getBurstLimit()));
        } else {
            throw new PSSException(
                    "No MPLS data associated with path");
        }
        if (layer2Data != null) {
            this.hm.put("resv-id", circuitName);
            this.hm.put("vlan_id", lspData.getVlanTag());
            this.hm.put("community", "65000:" + lspData.getVlanTag());
            if (direction.equals("forward")) {
                this.hm.put("lsp_from", lspData.getIngressRtrLoopback());
                this.hm.put("lsp_to", lspData.getEgressRtrLoopback());
                this.hm.put("egress-rtr-loopback",
                        lspData.getEgressRtrLoopback());
                this.hm.put("interface",
                        lspData.getIngressLink().getPort().getTopologyIdent());
                this.hm.put("port",
                    lspData.getIngressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getIngressLink());
            } else {
                this.hm.put("lsp_from", lspData.getEgressRtrLoopback());
                this.hm.put("lsp_to", lspData.getIngressRtrLoopback());
                this.hm.put("egress-rtr-loopback",
                        lspData.getIngressRtrLoopback());
                this.hm.put("interface",
                        lspData.getEgressLink().getPort().getTopologyIdent());
                this.hm.put("port",
                        lspData.getEgressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getEgressLink());
            }
        } else if (layer3Data != null) {
            this.hm.put("name", circuitName);
            this.hm.put("from", lspData.getIngressRtrLoopback());
            this.hm.put("to", lspData.getEgressRtrLoopback());
            this.hm.put("source-address", layer3Data.getSrcHost());
            this.hm.put("destination-address", layer3Data.getDestHost());
            Integer intParam = layer3Data.getSrcIpPort();
            // Axis2 bug workaround
            if ((intParam != null) && (intParam != 0)) {
                this.hm.put("source-port", Integer.toString(intParam));
            }
            intParam = layer3Data.getSrcIpPort();
            if ((intParam != null) && (intParam != 0)) {
                this.hm.put("destination-port", Integer.toString(intParam));
            }
            param = layer3Data.getDscp();
            if (param != null) {
                this.hm.put("dscp", param);
            }
            param = layer3Data.getProtocol();
            if (param != null) {
                this.hm.put("protocol", param);
            }
            param = resv.getDescription();
            if (param != null) {
                this.hm.put("lsp_description", param);
            } else {
                this.hm.put("lsp_description", "no description provided");
            }
            this.hm.put("internal_interface_filter",
                 this.props.getProperty("internal_interface_filter"));
            this.hm.put("external_interface_filter",
                 this.props.getProperty("external_interface_filter"));
            this.setupLogin(lspData.getIngressLink());
        }
        this.hm.put("lsp_setup-priority",
             this.commonProps.getProperty("lsp_setup-priority"));
        this.hm.put("lsp_reservation-priority",
             this.commonProps.getProperty("lsp_reservation-priority"));
        this.hm.put("firewall_filter_marker", 
             this.props.getProperty("firewall_filter_marker"));

        // Additional information from the template will be used if
        // an explicit path was given.
        if (path.isExplicit()) {
            // reset to beginning
            pathElem = path.getPathElem();
            hops = lspData.getHops(pathElem, direction, false);
        }
        boolean active = this.setupLSP(hops);
        // TODO:  makes assumption forward called first
        if ((layer3Data != null) || direction.equals("reverse")) {
            resv.setStatus("ACTIVE");
        }
    }

    /**
     * Verifies LSP is still active: TODO
     *
     * @param resv the reservation whose path will be refreshed
     * @param lspData LSPData instance containing path-related configuration
     * @throws PSSException
     */
    public String refreshPath(Reservation resv, LSPData lspData)
            throws PSSException {
        return "ACTIVE";
    }

    /**
     * Formats reservation parameters for circuit teardown.
     *
     * @param resv a reservation instance
     * @param lspData LSPData instance containing path-related configuration
     * @param direction boolean indicating whether forward path
     * @throws PSSException
     */
    public void teardownPath(Reservation resv, LSPData lspData,
                             String direction) throws PSSException {

        String newStatus = null;

        if (lspData == null) {
            throw new PSSException(
                    "no path related configuration data present");
        }
        if ((direction == null) || (!direction.equals("forward") &&
                                    !direction.equals("reverse"))) {
            throw new PSSException("illegal circuit direction");
        }
        Path path = resv.getPath();
        PathElem pathElem = path.getPathElem();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        if (layer2Data != null) {
            if (lspData.getIngressLink() == null) {
                throw new PSSException(
                        "teardownPath called before getting path endpoints");
            }
            // get VLAN tag and loopbacks
            lspData.setLayer2PathInfo(true);
        } else if (layer3Data != null) {
            lspData.setLayer3PathInfo("Juniper");
        } else {
            throw new PSSException("no layer 2 or layer 3 data provided");
        }
        // Create map for filling in template.
        this.hm = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in name parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        if (layer2Data != null) {
            this.hm.put("resv-id", circuitName);
            this.hm.put("vlan_id", lspData.getVlanTag());
            if (direction.equals("forward")) {
                this.hm.put("egress-rtr-loopback",
                    lspData.getEgressRtrLoopback());
                this.hm.put("interface",
                    lspData.getIngressLink().getPort().getTopologyIdent());
                this.hm.put("port",
                    lspData.getIngressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getIngressLink());
            } else {
                this.hm.put("egress-rtr-loopback",
                    lspData.getIngressRtrLoopback());
                this.hm.put("interface",
                    lspData.getEgressLink().getPort().getTopologyIdent());
                this.hm.put("port",
                    lspData.getEgressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getEgressLink());
            }
        }
        else if (layer3Data != null) {
            this.hm.put("name", circuitName);
            this.hm.put("internal_interface_filter",
                 this.props.getProperty("internal_interface_filter"));
            this.hm.put("external_interface_filter",
                 this.props.getProperty("external_interface_filter"));
            this.setupLogin(lspData.getIngressLink());
        }
        boolean active = this.teardownLSP();
        if ((layer3Data != null) || direction.equals("reverse")) {
            String prevStatus = resv.getStatus();
            if (!prevStatus.equals("PRECANCEL")) {
                newStatus = "FINISHED";
            } else {
                newStatus = "CANCELLED";
            }
            resv.setStatus(newStatus);
        }
    }

    /**
     * Allows overriding the allowLSP property.  Primarily for tests.
     * @param allowLSP boolean indicating whether LSP can be set up
     */
    public void setConfigurable(boolean allowLSP) {
        this.allowLSP = allowLSP;
    }

    /**
     * Sets up login to a router.
     *
     */
     private void setupLogin(Link link) {
        this.hm.put("login", this.props.getProperty("login"));
        this.hm.put("router",
                link.getPort().getNode().getNodeAddress().getAddress());
        String keyfile = System.getenv("CATALINA_HOME") +
                             "/shared/classes/server/oscars.key";
        this.hm.put("keyfile", keyfile);
        this.hm.put("passphrase", this.props.getProperty("passphrase"));
    }

    /**
     * Sets up an LSP circuit.
     *
     * @param hops a list of hops only used if explicit path was given
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean setupLSP(List<String> hops) throws PSSException {

        LSPConnection conn = null;

        this.log.info("setupLSP.start");
        try {
            // this allows testing the template without trying to configure
            // a router
            if (this.allowLSP) {
                this.log.info("setting up connection for " + this.hm.get("router"));
                conn = new LSPConnection();
                conn.createSSHConnection(this.hm);
            } else {
                this.log.info("not setting up connection");
            }
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/classes/server/";
            if (this.hm.get("vlan_id") != null) {
                fname += this.props.getProperty("setupL2Template");
            } else {
                fname += this.props.getProperty("setupL3Template");
            }
            this.configureLSP(hops, fname, conn);
            if (conn != null) {
                this.readResponse(conn.in);
                conn.shutDown();
            }
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
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean teardownLSP() throws PSSException {

        LSPConnection conn = null;

        this.log.info("teardownLSP.start");
        try {
            if (this.allowLSP) {
                conn = new LSPConnection();
                conn.createSSHConnection(this.hm);
            }
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/classes/server/";
            if (this.hm.get("vlan_id") != null) {
                fname += this.props.getProperty("teardownL2Template");
            } else {
                fname += this.props.getProperty("teardownL3Template");
            }
            this.configureLSP(null, fname, conn);
            if (conn != null) {
                this.readResponse(conn.in);
                conn.shutDown();
            }
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
     * @return boolean indicating status
     * @throws PSSException
     */
    public boolean statusLSP() throws PSSException {

        boolean active = true;

        this.log.info("statusLSP.start");
        try {
            LSPConnection conn = new LSPConnection();
            conn.createSSHConnection(this.hm);
            int status = getStatus(conn);
            conn.shutDown();
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        } catch (JDOMException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("statusLSP.finish");
        return active;
    }

    /**
     * Shows example setting up of a DOM to request information from a router.
     * The ouputted document looks like:
     *
     *           <get-chassis-inventory>
     *               <detail />
     *           </get-chassis-inventory>
     *
     * @param out output stream
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
     * @param hops a list of hops only used if explicit path was given
     * @param fname full path of template file
     * @param conn LSP connection
     * @return boolean indicating status (not done yet)
     * @throws IOException
     * @throws JDOMException
     */
    private boolean configureLSP(List<String> hops,
                                 String fname, LSPConnection conn)
            throws IOException, JDOMException, PSSException {

        OutputStream out = null;

        if (conn != null) { out = conn.out; }
        StringBuilder sb = new StringBuilder();

        this.log.info("configureLSP.start");
        for (String key: this.hm.keySet()) {
            sb.append(key + ": " + this.hm.get(key) + "\n");
        }
        this.log.info(sb.toString());
        Document doc = this.th.fillTemplate(this.hm, hops, fname);

        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        format.setEncoding("US-ASCII");
        outputter.setFormat(format);
        // log, and then send to router
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        // this will only be null if allowLSP is false
        if (out != null) {
            outputter.output(doc, out);
        }
        // TODO:  get status
        this.log.info("configureLSP.end");
        return true;
    }

    /**
     * Checks the status of the LSP that was just set up or torn down.
     *
     * @param conn SSH connection
     * @return status (not done yet) 
     * @throws IOException
     * @throws JDOMException
     */
    private int getStatus(LSPConnection conn) 
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
