package net.es.oscars.pss.vendor.jnx;

import java.io.*;
import java.util.*;

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
    private boolean allowLSP;
    private static String staticAllowLSP;

    public JnxLSP(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.commonProps = propHandler.getPropertyGroup("pss", true);
        if (staticAllowLSP != null) {
            this.allowLSP = staticAllowLSP.equals("1") ? true : false;
        } else {
            this.allowLSP =
                this.commonProps.getProperty("allowLSP").equals("1") ? true : false;
        }
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

        this.log.info("jnx.createPath.start");

        // only used if an explicit path was given
        List<String> hops = null;
        String param = null;

        if (lspData == null) {
            throw new PSSException("no path related configuration data present");
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
            if (lspData.getIngressLink() == null) {
                throw new PSSException("createPath called before getting path endpoints");
            }
            // get VLAN tag and loopbacks
            lspData.setLayer2PathInfo(true);
        } else if (layer3Data != null) {
            lspData.setLayer3PathInfo("Juniper");
        } else {
            throw new PSSException("no layer 2 or layer 3 data provided");
        }
        // Create map for filling in template.
        HashMap<String, String> hm = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        // capitalize circuit names for production circuits
        if (resv.getDescription().contains("PRODUCTION")) {
            circuitName = circuitName.toUpperCase();
        }
        hm.put("bandwidth", Long.toString(resv.getBandwidth()));
        if (mplsData != null) {
            if (mplsData.getLspClass() != null) {
                hm.put("lsp_class-of-service", mplsData.getLspClass());
            } else {
                hm.put("lsp_class-of-service", "4");
            }
            hm.put("policer_burst-size-limit", Long.toString(mplsData.getBurstLimit()));
        } else {
            Long burst_size = resv.getBandwidth() / 10;
            hm.put("policer_burst-size-limit", burst_size.toString());
            hm.put("lsp_class-of-service", "4");
        }
        if (layer2Data != null) {
            hm.put("resv-id", circuitName);
            hm.put("vlan_id", lspData.getVlanTag());
            hm.put("community", "65000:" + lspData.getVlanTag());
            if (direction.equals("forward")) {
                hm.put("lsp_from", lspData.getIngressRtrLoopback());
                hm.put("lsp_to", lspData.getEgressRtrLoopback());
                hm.put("egress-rtr-loopback", lspData.getEgressRtrLoopback());
                hm.put("interface", lspData.getIngressLink().getPort().getTopologyIdent());
                hm.put("port", lspData.getIngressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getIngressLink(), hm);
            } else {
                hm.put("lsp_from", lspData.getEgressRtrLoopback());
                hm.put("lsp_to", lspData.getIngressRtrLoopback());
                hm.put("egress-rtr-loopback", lspData.getIngressRtrLoopback());
                hm.put("interface", lspData.getEgressLink().getPort().getTopologyIdent());
                hm.put("port", lspData.getEgressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getEgressLink(), hm);
            }
        } else if (layer3Data != null) {
            hm.put("resv-id", circuitName);
            hm.put("lsp_from", lspData.getIngressRtrLoopback());
            hm.put("lsp_to", lspData.getEgressRtrLoopback());
            hm.put("source-address", layer3Data.getSrcHost());
            hm.put("destination-address", layer3Data.getDestHost());
            Integer intParam = layer3Data.getSrcIpPort();
            // Axis2 bug workaround
            if ((intParam != null) && (intParam != 0)) {
                hm.put("source-port", Integer.toString(intParam));
            }
            intParam = layer3Data.getSrcIpPort();
            if ((intParam != null) && (intParam != 0)) {
                hm.put("destination-port", Integer.toString(intParam));
            }
            param = layer3Data.getDscp();
            if (param != null) {
                hm.put("dscp", param);
            }
            param = layer3Data.getProtocol();
            if (param != null) {
                hm.put("protocol", param);
            }
            hm.put("internal_interface_filter", this.props.getProperty("internal_interface_filter"));
            hm.put("external_interface_filter", this.props.getProperty("external_interface_filter"));
            this.setupLogin(lspData.getIngressLink(), hm);
            hm.put("firewall_filter_marker", this.props.getProperty("firewall_filter_marker"));
        }
        hm.put("lsp_setup-priority", this.commonProps.getProperty("lsp_setup-priority"));
        hm.put("lsp_reservation-priority", this.commonProps.getProperty("lsp_reservation-priority"));

        // reset to beginning
        pathElem = path.getPathElem();
        hops = lspData.getHops(pathElem, direction, false);
        this.log.info("jnx.createPath.end");
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

        this.log.info("jnx.teardownPath.start");
        if (lspData == null) {
            throw new PSSException("no path related configuration data present");
        }
        if ((direction == null) || (!direction.equals("forward") &&
                                    !direction.equals("reverse"))) {
            throw new PSSException("illegal circuit direction");
        }
        Path path = resv.getPath();
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
        HashMap<String, String> hm = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        // capitalize circuit names for production circuits
        if (resv.getDescription().contains("PRODUCTION")) {
            circuitName = circuitName.toUpperCase();
        }
        if (layer2Data != null) {
            hm.put("resv-id", circuitName);
            hm.put("vlan_id", lspData.getVlanTag());
            if (direction.equals("forward")) {
                hm.put("egress-rtr-loopback", lspData.getEgressRtrLoopback());
                hm.put("interface", lspData.getIngressLink().getPort().getTopologyIdent());
                hm.put("port", lspData.getIngressLink().getPort().getTopologyIdent());

                this.setupLogin(lspData.getIngressLink(), hm);
            } else {
                hm.put("egress-rtr-loopback", lspData.getIngressRtrLoopback());
                hm.put("interface", lspData.getEgressLink().getPort().getTopologyIdent());
                hm.put("port", lspData.getEgressLink().getPort().getTopologyIdent());

                this.setupLogin(lspData.getEgressLink(), hm);
            }
        } else if (layer3Data != null) {
            hm.put("resv-id", circuitName);
            hm.put("internal_interface_filter", this.props.getProperty("internal_interface_filter"));
            hm.put("external_interface_filter", this.props.getProperty("external_interface_filter"));

            this.setupLogin(lspData.getIngressLink(), hm);
        }

        this.teardownLSP(hm);
        this.log.info("jnx.teardownPath.end");
    }

    /**
     * Allows overriding the allowLSP property.  Primarily for tests.
     * Must be called before JnxLSP is instantiated.
     * @param overrideAllowLSP boolean indicating whether LSP can be set up
     */
    public static void setConfigurable(String overrideAllowLSP) {
        staticAllowLSP = overrideAllowLSP;
    }

    /**
     * Sets up login to a router.
     *
     */
     private void setupLogin(Link link, HashMap<String, String> hm) {
        hm.put("login", this.props.getProperty("login"));
        hm.put("router", link.getPort().getNode().getNodeAddress().getAddress());
        String keyfile = System.getenv("CATALINA_HOME") + "/shared/classes/server/oscars.key";
        hm.put("keyfile", keyfile);
        hm.put("passphrase", this.props.getProperty("passphrase"));
    }

    /**
     * Sets up an LSP circuit.
     *
     * @param hops a list of hops only used if explicit path was given
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean setupLSP(List<String> hops, HashMap<String, String> hm) throws PSSException {

        LSPConnection conn = null;

        this.log.info("setupLSP.start");
        try {
            // this allows testing the template without trying to configure
            // a router
            if (this.allowLSP) {
                this.log.info("setting up connection for " + hm.get("router"));
                conn = new LSPConnection();
                conn.createSSHConnection(hm);
            } else {
                this.log.info("not setting up connection");
            }
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/classes/server/";
            if (hm.get("vlan_id") != null) {
                fname += this.props.getProperty("setupL2Template");
            } else {
                fname += this.props.getProperty("setupL3Template");
            }
            this.configureLSP(hops, fname, conn, hm);
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
    public boolean teardownLSP(HashMap<String, String> hm) throws PSSException {

        LSPConnection conn = null;

        this.log.info("teardownLSP.start");
        try {
            if (this.allowLSP) {
                conn = new LSPConnection();
                conn.createSSHConnection(hm);
            }
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/classes/server/";
            if (hm.get("vlan_id") != null) {
                fname += this.props.getProperty("teardownL2Template");
            } else {
                fname += this.props.getProperty("teardownL3Template");
            }
            this.configureLSP(null, fname, conn, hm);
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
    public boolean statusLSP(HashMap<String, String> hm) throws PSSException {

        boolean active = true;

        this.log.info("statusLSP.start");
        try {
            LSPConnection conn = new LSPConnection();
            conn.createSSHConnection(hm);
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
                                 String fname, LSPConnection conn, HashMap<String, String> hm)
            throws IOException, JDOMException, PSSException {

        OutputStream out = null;

        if (conn != null) { out = conn.out; }
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

    /**
     * @return the allowLSP
     */
    public boolean isAllowLSP() {
        return allowLSP;
    }
}
