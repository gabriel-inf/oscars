package net.es.oscars.pss.vendor.jnx;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.xpath.*;

import org.apache.log4j.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.es.oscars.ConfigFinder;
import net.es.oscars.PropHandler;
import net.es.oscars.pss.*;
import net.es.oscars.pss.vendor.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;

/**
 * JnxLSP performs setup/teardown of LSP paths on the router.
 *
 * @author David Robertson, Jason Lee
 */
public class JnxLSP {

    private static final Map<String,String> statuses =
        Collections.unmodifiableMap(new HashMap<String,String>() {{
            put("EI", "encapsulation invalid");
            put("MM", "mtu mismatch");
            put("EM", "encapsulation mismatch");
            put("CM", "control-word mismatch");
            put("VM", "vlan id mismatch");
            put("OL", "no outgoing label");
            put("NC", "interface encapsulation not CCC/TCC");
            put("CB", "received cell-bundle size bad");
            put("NP", "interface hardware not present");
            put("Dn", "down circuit");
            put("Vc-Dn", "virtual circuit down");
            put("Up", "operational status");
            put("CF", "call admission control failure");
            put("XX", "unknown error");
        }});

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

        List<String> hops = null;
        String param = null;
        Ipaddr ipaddr = null;

        if (lspData == null) {
            throw new PSSException("no path related configuration data present");
        }

        if ((direction == null) || (!direction.equals("forward") &&
                                    !direction.equals("reverse"))) {
            throw new PSSException("illegal circuit direction");
        }

        Path path = null;
        try {
            path = resv.getPath(PathType.LOCAL);
        } catch (BSSException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("path id: " + path.getId());

        MPLSData mplsData = path.getMplsData();
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        List<PathElem> pathElems = path.getPathElems();
        boolean isL2 = false;
        boolean isL3 = false;

        try {
            isL2 = path.isLayer2();
            isL3 = path.isLayer3();
        } catch (BSSException ex) {
            throw new PSSException(ex.getMessage());
        }

        if (isL2) {
            if (lspData.getIngressLink() == null) {
                throw new PSSException("createPath called before getting path endpoints");
            }
            // get VLAN tag and loopbacks
            lspData.setLayer2PathInfo(true);
        } else if (isL3) {
            lspData.setLayer3PathInfo("Juniper");
        } else {
            // won't ever get here
            throw new PSSException("no layer 2 or layer 3 data provided");
        }

        // Create map for filling in template.
        HashMap<String, String> hm = new HashMap<String, String>();
        String circuitName = this.getCircuitName(resv.getGlobalReservationId(),
                                                resv.getDescription());
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

        if (isL2) {
            hm.put("resv-id", circuitName);
            hm.put("vlan_id", lspData.getVlanTag());
            hm.put("community", "65000:" + lspData.getVlanTag());
            String lspFwdTo = null;
            String lspRevTo = null;
            if (direction.equals("forward")) {
                // get IP associated with physical interface before egress
                ipaddr =
                    lspData.getLastXfaceElem().getLink().getValidIpaddr();
                if (ipaddr != null) {
                    lspFwdTo = ipaddr.getIP();
                } else {
                    throw new PSSException("Egress port has no IP in DB!");
                }
                hm.put("lsp_from", lspData.getIngressRtrLoopback());
                hm.put("lsp_to", lspFwdTo);
                hm.put("egress-rtr-loopback", lspData.getEgressRtrLoopback());
                hm.put("interface", lspData.getIngressLink().getPort().getTopologyIdent());
                hm.put("port", lspData.getIngressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getIngressLink(), hm);
            } else {
                // get IP associated with first in-facing physical interface
                PathElem ingressPathElem = lspData.getIngressPathElem();
                int nextSeqNumber = ingressPathElem.getSeqNumber() + 1;
                ipaddr =
                    path.getPathElems().get(nextSeqNumber).getLink().getValidIpaddr();
                if (ipaddr != null) {
                    lspRevTo = ipaddr.getIP();
                } else {
                    throw new PSSException("Egress port has no IP in DB!");
                }
                hm.put("lsp_from", lspData.getEgressRtrLoopback());
                hm.put("lsp_to", lspRevTo);
                hm.put("egress-rtr-loopback", lspData.getIngressRtrLoopback());
                hm.put("interface", lspData.getEgressLink().getPort().getTopologyIdent());
                hm.put("port", lspData.getEgressLink().getPort().getTopologyIdent());
                this.setupLogin(lspData.getEgressLink(), hm);
            }
        } else if (isL3) {
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
            hm.put("tester_interface_filter", this.props.getProperty("tester_interface_filter"));
            hm.put("internal_interface_filter", this.props.getProperty("internal_interface_filter"));
            hm.put("external_interface_filter", this.props.getProperty("external_interface_filter"));
            this.setupLogin(lspData.getIngressLink(), hm);
            hm.put("firewall_filter_marker", this.props.getProperty("firewall_filter_marker"));
        }
        hm.put("lsp_setup-priority", this.commonProps.getProperty("lsp_setup-priority"));
        hm.put("lsp_reservation-priority", this.commonProps.getProperty("lsp_reservation-priority"));

        // reset to beginning
        pathElems = path.getPathElems();
        hops = lspData.getHops(pathElems, direction, false);
        this.setupLSP(hops, hm);
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
        Path path = null;
        try {
            path = resv.getPath(PathType.LOCAL);
        } catch (BSSException ex) {
            throw new PSSException(ex.getMessage());
        }
        boolean isL2 = false;
        boolean isL3 = false;

        try {
            isL2 = path.isLayer2();
            isL3 = path.isLayer3();
        } catch (BSSException ex) {
            throw new PSSException(ex.getMessage());
        }

        if (isL2) {
            if (lspData.getIngressLink() == null) {
                throw new PSSException(
                        "teardownPath called before getting path endpoints");
            }
            // get VLAN tag and loopbacks
            lspData.setLayer2PathInfo(true);
        } else if (isL3) {
            lspData.setLayer3PathInfo("Juniper");
        } else {
            throw new PSSException("no layer 2 or layer 3 data provided");
        }
        // Create map for filling in template.
        HashMap<String, String> hm = new HashMap<String, String>();
        String circuitName = this.getCircuitName(resv.getGlobalReservationId(),
                                                resv.getDescription());
        if (isL2) {
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
        } else if (isL3) {
            hm.put("resv-id", circuitName);
            hm.put("tester_interface_filter", this.props.getProperty("tester_interface_filter"));
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
     * Sets up login to a router given a link.
     *
     */
     private void setupLogin(Link link, HashMap<String, String> hm) {
        hm.put("login", this.props.getProperty("login"));
        hm.put("router", link.getPort().getNode().getNodeAddress().getAddress());
        String keyfile = null;
        try {
            keyfile = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR, "oscars.key");
        } catch (RemoteException e) {
            this.log.error(e.getMessage());
        }
        hm.put("keyfile", keyfile);
        hm.put("passphrase", this.props.getProperty("passphrase"));
    }

    /**
     * Sets up login to a router given a router name.
     *
     */
     private void setupLogin(String router, HashMap<String, String> hm) {
        hm.put("login", this.props.getProperty("login"));
        hm.put("router", router);
        this.log.info("router: " + router);
        String keyfile = null;
        try {
            keyfile = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR, "oscars.key");
        } catch (RemoteException e) {
            this.log.error(e.getMessage());
        }
        hm.put("keyfile", keyfile);
        hm.put("passphrase", this.props.getProperty("passphrase"));
    }

    /**
     * Sets up an LSP circuit.
     *
     * @param hops list of hops
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean setupLSP(List<String> hops, HashMap<String, String> hm) throws PSSException {

        LSPConnection conn = null;

        this.log.info("setupLSP.start");
        ConfigFinder configFinder = ConfigFinder.getInstance();
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
            String fname = null;
            if (hm.get("vlan_id") != null) {
                fname = configFinder.find(ConfigFinder.PSS_DIR,
                        this.props.getProperty("setupL2Template"));
            } else {
                fname = configFinder.find(ConfigFinder.PSS_DIR,
                        this.props.getProperty("setupL3Template"));
            }
            this.configureLSP(hops, fname, conn, hm);
            if (conn != null) {
                this.readBytes(conn.in);
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
        ConfigFinder configFinder = ConfigFinder.getInstance();
        this.log.info("teardownLSP.start");
        try {
            if (this.allowLSP) {
                conn = new LSPConnection();
                conn.createSSHConnection(hm);
            }
            String fname = null;
            if (hm.get("vlan_id") != null) {
                fname = configFinder.find(ConfigFinder.PSS_DIR,
                        this.props.getProperty("teardownL2Template"));
            } else {
                fname = configFinder.find(ConfigFinder.PSS_DIR,
                        this.props.getProperty("teardownL3Template"));
            }
            this.configureLSP(null, fname, conn, hm);
            if (conn != null) {
                this.readBytes(conn.in);
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
     * Gets the LSP status of a set of VLAN's on a Juniper router.
     *
     * @param router string with router id
     * @param statusInputs HashMap of VLAN's to check with their associated info
     * @return circuitStatuses HashMap with VLAN's and their statuses
     * @throws PSSException
     */
    public Map<String,VendorStatusResult> statusLSP(String router,
                                     Map<String,VendorStatusInput> statusInputs)
            throws PSSException {

        boolean status = false;
        String errMsg = null;
        Map<String,VendorStatusInput>
            layer2Inputs = new HashMap<String,VendorStatusInput>();
        Map<String,VendorStatusInput>
            layer3Inputs = new HashMap<String,VendorStatusInput>();
        Map<String,VendorStatusResult> circuitStatuses =
            new HashMap<String,VendorStatusResult>();

        this.log.info("statusLSP.start");
        if (!this.allowLSP) {
            return null;
        }
        for (String circuitId: statusInputs.keySet()) {
            VendorStatusInput statusInput = statusInputs.get(circuitId);
            if (statusInput.getLayer().equals("2")) {
                layer2Inputs.put(circuitId, statusInput);
            } else {
                layer3Inputs.put(circuitId, statusInput);
            }
        }
        try {
            HashMap<String, String> hm = new HashMap<String, String>();
            this.setupLogin(router, hm);
            LSPConnection conn = new LSPConnection();
            conn.createSSHConnection(hm);
            if (!layer2Inputs.isEmpty()) {
                String fname = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR,
                        this.props.getProperty("statusL2Template"));
                Document doc = this.th.buildTemplate(fname);
                this.sendCommand(doc, conn.out);
                doc = this.readResponse(conn.in);
                circuitStatuses.putAll(this.parseLayer2Response(layer2Inputs, doc));
            }
            if (!layer3Inputs.isEmpty()) {
                String fname = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR,
                        this.props.getProperty("statusL3Template"));
                Document doc = this.th.buildTemplate(fname);
                this.sendCommand(doc, conn.out);
                doc = this.readResponse(conn.in);
                circuitStatuses.putAll(this.parseLayer3Response(layer3Inputs, doc));
            }
            conn.shutDown();
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        } catch (JDOMException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("statusLSP.finish");
        return circuitStatuses;
    }

    /**
     * Gets the circuit name given the reservation gri and d
     *
     * @param gri string with global reservation id
     * @param description string with reservation description
     * @return circuitName string with circuit name
     */
    public String getCircuitName(String gri, String description) {
        String circuitStr = "oscars_" + gri;
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        // capitalize circuit names for production circuits
        if (description.contains("PRODUCTION")) {
            circuitName = circuitName.toUpperCase();
        }
        return circuitName;
    }

    /**
     * Configure an LSP. Sends the LSP-XML command to the server
     * and sees what happens.
     *
     * @param hops list of hops
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
        this.sendCommand(doc, out);
        this.log.info("configureLSP.end");
        return true;
    }

    /**
     * Sends the XML command to the server.
     * @param doc XML document with Junoscript commands
     * @param out output stream
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    private void sendCommand(Document doc, OutputStream out)
            throws IOException, JDOMException, PSSException {

        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        format.setEncoding("US-ASCII");
        outputter.setFormat(format);
        // log, and then send to router
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + "SENDING\n" + logOutput);
        // this will only be null if allowLSP is false
        if (out != null) {
            outputter.output(doc, out);
        }
    }

    /**
     * Reads the XML response from the socket created earlier into a DOM
     * (makes for easier parsing).
     *
     * @param in InputStream
     * @return XML document with response from server
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    private Document readResponse(InputStream in)
            throws IOException, JDOMException, PSSException {

        ByteArrayOutputStream buff  = new ByteArrayOutputStream();
        Document doc = null;
        SAXBuilder b = new SAXBuilder();
        doc = b.build(in);
        if (doc == null) {
            throw new PSSException("Juniper router did not return a response");
        }
        // for logging purposes only
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(doc, buff);
        this.log.info(buff.toString());
        return doc;
    }

    /**
     * Parse the DOM response to see if the VLAN is up or not.
     *
     * @param statusInputs HashMap of VLAN's to check with their associated info
     * @param doc XML document with response from server
     * @return circuitStatuses HashMap with VLAN's and their statuses
     * @throws IOException
     * @throws JDOMException
     */
    private Map<String,VendorStatusResult>
        parseLayer2Response(Map<String,VendorStatusInput> statusInputs,
                            Document doc)
            throws IOException, JDOMException {

        this.log.info("parseLayer2Response.start");
        List connectionList = this.getElements(doc, "connection");
        Map<String,VendorStatusResult> circuitStatuses =
            new HashMap<String,VendorStatusResult>();
        Map<String,JnxStatusResult> currentVlans =
            new HashMap<String,JnxStatusResult>();
        Pattern pattern = Pattern.compile(".*\\(vc (\\d{3,4})\\)$");
        for (Iterator i = connectionList.iterator(); i.hasNext();) {
            Element conn = (Element) i.next();
            List connectionChildren = conn.getChildren();
            String vlanId = null;
            JnxStatusResult statusResult = new JnxStatusResult();
            for (Iterator j = connectionChildren.iterator(); j.hasNext();) {
                Element e = (Element) j.next();
                if (e.getName().equals("connection-id")) {
                    Matcher m = pattern.matcher(e.getText());
                    // this should always match; extracting the VLAN id
                    // depends on connection-id being first child
                    if (m.matches()) {
                        vlanId = m.group(1);
                    } else {
                        // this should never happen
                        this.log.error("no vlan id found in connection-id!");
                        continue;
                    }
                } else if (e.getName().equals("connection-status")) {
                    if (vlanId != null) {
                        statusResult.setConnectionStatus(e.getText());
                    }
                } else if (e.getName().equals("local-interface")) {
                    List localInterfaces = e.getChildren();
                    for (Iterator k = localInterfaces.iterator(); k.hasNext();) {
                        Element interElem = (Element) k.next();
                        if (interElem.getName().equals("interface-status")) {
                            if (vlanId != null) {
                                statusResult.setInterfaceStatus(
                                        interElem.getText());
                            }
                        } else if (interElem.getName().equals(
                                    "interface-description")) {
                            if (vlanId != null) {
                                statusResult.setInterfaceDescription(
                                    interElem.getText());
                            }
                        }
                    }
                }
            }
            if (vlanId != null) {
                currentVlans.put(vlanId, statusResult);
            }
        }
        for (String vlanId: statusInputs.keySet()) {
            VendorStatusResult statusResult = new JnxStatusResult();
            if (!currentVlans.containsKey(vlanId)) {
                statusResult.setCircuitStatus(false);
            } else {
                StringBuilder sb = new StringBuilder();
                JnxStatusResult currentResult = currentVlans.get(vlanId);
                VendorStatusInput statusInput = statusInputs.get(vlanId);
                String connectionStatus = currentResult.getConnectionStatus();
                String op = statusInput.getOperation();
                if (op == null) {
                    sb.append("No operation provided to statusLSP");
                } else if (op.equals("PATH_TEARDOWN")) {
                    sb.append("VLAN still exists with: ");
                    if (currentResult.getConnectionStatus() == null) {
                        sb.append("connection status is null");
                    } else if (this.statuses.containsKey(connectionStatus)) {
                        sb.append(this.statuses.get(connectionStatus));
                    } else {
                        sb.append("unknown status: " + connectionStatus);
                    }
                } else if (op.equals("PATH_SETUP")) {
                    // this case can only happen on setup, since it will always fail
                    String gri = statusInput.getGri();
                    String description = statusInput.getDescription();
                    String circuitName = this.getCircuitName(gri, description);
                    if ((currentResult.getInterfaceDescription() != null) &&
                        !currentResult.getInterfaceDescription().equals(circuitName)) {
                        sb.append("VLAN already in use by another reservation");
                    } else if (connectionStatus == null) {
                        sb.append("connection status is null");
                    } else if (!connectionStatus.equals("Up")) {
                        if (this.statuses.containsKey(connectionStatus)) {
                            sb.append(this.statuses.get(connectionStatus));
                        } else {
                            sb.append("unknown status: " + connectionStatus);
                        }
                    } else if ((currentResult.getInterfaceStatus() == null) ||
                             !currentResult.getInterfaceStatus().equals("Up")) {
                        sb.append("Local interface status for VLAN " + vlanId +
                                " is down");
                    }
                } else {
                    sb.append("invalid operation provided to statusLSP");
                }
                statusResult.setCircuitStatus(true);
                String errMsg = sb.toString();
                if (!errMsg.equals("")) {
                    statusResult.setErrorMessage(errMsg);
                }
            }
            circuitStatuses.put(vlanId, statusResult);
        }
        this.log.info("parseLayer2Response.finish");
        return circuitStatuses;
    }

    private List getElements(Document doc, String elementName)
            throws JDOMException {

        Element root = doc.getRootElement();
        // NOTE WELL: if response format changes, this won't work
        Element rpcReply = (Element) root.getChildren().get(0);
        Element firstChild = (Element) rpcReply.getChildren().get(0);
        String uri = firstChild.getNamespaceURI();
        // XPath doesn't have way to name default namespace
        Namespace ns = Namespace.getNamespace("ns", uri);
        // find all connections with status info
        XPath xpath = XPath.newInstance("//ns:" + elementName);
        xpath.addNamespace(ns);
        List elementList = xpath.selectNodes(doc);
        return elementList;
    }

    /**
     * Parse the DOM response to see if the layer 3 circuit is up or not.
     *
     * @param statusInputs HashMap of circuits to check with associated info
     * @param doc XML document with response from server
     * @return circuitStatuses HashMap with circuits and their statuses
     * @throws IOException
     * @throws JDOMException
     */
    private Map<String,VendorStatusResult>
        parseLayer3Response(Map<String,VendorStatusInput> statusInputs,
                            Document doc)
            throws IOException, JDOMException {

        Map<String,VendorStatusResult> circuitStatuses =
            new HashMap<String,VendorStatusResult>();
        Map<String,JnxStatusResult> currentGris =
            new HashMap<String,JnxStatusResult>();
        List mplsLspList = this.getElements(doc, "mpls-lsp");
        for (Iterator i = mplsLspList.iterator(); i.hasNext();) {
            Element mplsLsp = (Element) i.next();
            List mplsLspChildren = mplsLsp.getChildren();
            String gri = "";
            JnxStatusResult statusResult = new JnxStatusResult();
            for (Iterator j = mplsLspChildren.iterator(); j.hasNext();) {
                Element e = (Element) j.next();
                if (e.getName().equals("name")) {
                    String lspName = e.getText();
                    // remove leading oscars and replace underscores
                    String[] components = lspName.split("_");
                    for (int c = 1; c < components.length-1; c++) {
                        gri += components[c] + ".";
                    }
                    gri += components[components.length-1];
                    this.log.info(gri);
                } else if (e.getName().equals("lsp-state")) {
                    statusResult.setConnectionStatus(e.getText());
                }
            }
            if (!gri.equals("")) {
                currentGris.put(gri, statusResult);
            }
        }
        for (String gri: statusInputs.keySet()) {
            VendorStatusResult statusResult = new JnxStatusResult();
            if (!currentGris.containsKey(gri)) {
                statusResult.setCircuitStatus(false);
            } else {
                StringBuilder sb = new StringBuilder();
                JnxStatusResult currentResult = currentGris.get(gri);
                VendorStatusInput statusInput = statusInputs.get(gri);
                String op = statusInput.getOperation();
                String connectionStatus = currentResult.getConnectionStatus();
                if (op == null) {
                    sb.append("No operation provided to statusLSP");
                } else if (op.equals("PATH_TEARDOWN")) {
                    sb.append("circuit still exists with status: ");
                    if (connectionStatus == null) {
                        sb.append("null");
                    } else {
                        sb.append(connectionStatus);
                    }
                } else if (op.equals("PATH_SETUP")) {
                    if (connectionStatus == null) {
                        sb.append("connection status is null");
                    } else if (!connectionStatus.equals("Up")) {
                        sb.append("connection status is " + connectionStatus);
                    }
                } else {
                    sb.append("invalid operation provided to statusLSP");
                }
                statusResult.setCircuitStatus(true);
                String errMsg = sb.toString();
                if (!errMsg.equals("")) {
                    statusResult.setErrorMessage(errMsg);
                }
            }
            circuitStatuses.put(gri, statusResult);
        }
        return circuitStatuses;
    }

    /**
     * Reads the response from the router to avoid rollback.  Actual status
     * of circuit is gotten with statusLSP.
     *
     * @param in InputStream
     * @throws IOException
     */
    private void readBytes(InputStream in) throws IOException {

        BufferedInputStream buffStream = new BufferedInputStream(in);
        byte[] buf = new byte[1024];
        int len;
        while ((len=buffStream.read(buf, 0, 1024)) >= 0) {
        }
        buffStream.close();
    }
    /**
     * @return the allowLSP
     */
    public boolean isAllowLSP() {
        return allowLSP;
    }
}
