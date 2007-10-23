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
import net.es.oscars.pathfinder.Utils;
import net.es.oscars.pathfinder.PathfinderException;
import net.es.oscars.pss.*;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;

/**
 * JnxLSP performs setup/teardown of LSP paths on the router.
 *
 * @author Jason Lee, David Robertson
 */
public class JnxLSP implements PSS {

    private Properties props;
    private Logger log;
    private TemplateHandler th;
    private String dbname;
    private Utils utils;

    public JnxLSP(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss.jnx", true);
        this.log = Logger.getLogger(this.getClass());
        this.th = new TemplateHandler();
        this.utils = new Utils(dbname);
        this.dbname = dbname;
    }

    /**
     * Formats reservation parameters for circuit setup.
     *
     * @param resv a reservation instance
     * @param opstring a string indicating whether to do set up or tear down
     * @throws PSSException
     */
    public String createPath(Reservation resv) throws PSSException {

        Map<String,String> lspInfo = null;
        // only used if an explicit path was given
        List<String> hops = null;
        String lspFrom = null;
        String lspTo = null;
        String param = null;
        Link link = null;
        Link fromLink = null;
        Ipaddr ipaddr = null;

        Path path = resv.getPath();
        this.log.info("path id: " + path.getId());
        // temporary for unavailable layer 2 handling; TODO:  FIX
        Layer2Data layer2Data = path.getLayer2Data();
        if (layer2Data != null) {
            this.log.info("problem: layer 2 path with Juniper");
            resv.setStatus("ACTIVE");
            return "ACTIVE";
        }
        // end of temporary fix
        PathElem pathElem = path.getPathElem();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        // find ingress and egress IP's
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                // not an ingress or egress unless already checked to
                // see if has an OSCARS loopback
                if (pathElem.getDescription().equals("ingress")) {
                    fromLink = pathElem.getLink();
                    ipaddr = ipaddrDAO.queryByParam("linkId", fromLink.getId());
                    try {
                        lspFrom = this.utils.getLoopback(ipaddr.getIP(),
                                                         "Juniper");
                    } catch (PathfinderException e) {
                        throw new PSSException(e.getMessage());
                    }
                } else if (pathElem.getDescription().equals("egress")) {
                    link = pathElem.getLink();
                    lspTo =
                        link.getPort().getNode().getNodeAddress().getAddress();
                }
            }
            pathElem = pathElem.getNextElem();
        }
        if (lspFrom == null) {
            throw new PSSException("no ingress loopback in path");
        }
        if (lspTo == null) {
            throw new PSSException("no egress loopback in path");
        }
        MPLSData mplsData = path.getMplsData();
        Layer3Data layer3Data = path.getLayer3Data();
        if (layer3Data == null) {
            this.log.info("problem: no layer 3 path for Juniper");
        }
        // Create an LSP object.
        lspInfo = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in name parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        lspInfo.put("name", circuitName);
        lspInfo.put("from", lspFrom);
        lspInfo.put("to", lspTo);
        lspInfo.put("bandwidth", Long.toString(resv.getBandwidth()));
        if (mplsData != null) {
            if (mplsData.getLspClass() != null) {
                lspInfo.put("lsp_class-of-service", mplsData.getLspClass());
            } else {
                lspInfo.put("lsp_class-of-service", "4");
            }
            lspInfo.put("policer_burst-size-limit",
                    Long.toString(mplsData.getBurstLimit()));
        } else {
            throw new PSSException(
                    "No MPLS data associated with path");
        }
        if ((layer2Data == null) && (layer3Data == null)) {
            throw new PSSException(
                    "No layer 2 or layer 3 data associated with path");
        }
        if (layer2Data != null) {
            // TODO:  get vlan id's from links, handle src endpoint
            //        dest endpoint
            ;
        } else if (layer3Data != null) {
            lspInfo.put("source-address", layer3Data.getSrcHost());
            lspInfo.put("destination-address", layer3Data.getDestHost());
            Integer intParam = layer3Data.getSrcIpPort();
            // Axis2 bug workaround
            if ((intParam != null) && (intParam != 0)) {
                lspInfo.put("source-port", Integer.toString(intParam));
            }
            intParam = layer3Data.getSrcIpPort();
            if ((intParam != null) && (intParam != 0)) {
                lspInfo.put("destination-port", Integer.toString(intParam));
            }
            param = layer3Data.getDscp();
            if (param != null) {
                lspInfo.put("dscp", param);
            }
            param = layer3Data.getProtocol();
            if (param != null) {
                lspInfo.put("protocol", param);
            }
        }
        param = resv.getDescription();
        if (param != null) {
            lspInfo.put("lsp_description", param);
        } else {
            lspInfo.put("lsp_description", "no description provided");
        }
        lspInfo.put("lsp_setup-priority",
             this.props.getProperty("lsp_setup-priority"));
        lspInfo.put("lsp_reservation-priority",
             this.props.getProperty("lsp_reservation-priority"));
        lspInfo.put("internal_interface_filter",
             this.props.getProperty("internal_interface_filter"));
        lspInfo.put("external_interface_filter",
             this.props.getProperty("external_interface_filter"));
        lspInfo.put("firewall_filter_marker", 
             this.props.getProperty("firewall_filter_marker"));

        // Additional information from the template will be used if
        // an explicit path was given.
        if (path.isExplicit()) {
            hops = new ArrayList<String>();
            // reset to beginning
            pathElem = path.getPathElem();
            while (pathElem != null) {
                link = pathElem.getLink();
                // this gets everything but ingress and egress, which we don't
                // want
                if (pathElem.getDescription() == null) {
                    ipaddr = ipaddrDAO.fromLink(link);
                    if (ipaddr == null) {
                        throw new PSSException(
                            "link in path doesn't have an associated IP");
                    }
                }
                hops.add(ipaddr.getIP());
                pathElem = pathElem.getNextElem();
            }
        }
        this.setupLogin(lspInfo, fromLink);
        this.setupLSP(lspInfo, hops);
        resv.setStatus("ACTIVE");
        return resv.getStatus();
    }

    /**
     * Verifies LSP is still active: TODO
     *
     * @param resv the reservation whose path will be refreshed
     * @throws PSSException
     */
    public String refreshPath(Reservation resv) throws PSSException {
        return "ACTIVE";
    }

    /**
     * Formats reservation parameters for circuit teardown.
     *
     * @param resv a reservation instance
     * @throws PSSException
     */
    public String teardownPath(Reservation resv) throws PSSException {

        Link fromLink = null;
        String newStatus = null;

        Path path = resv.getPath();
        // temporary for unavailable layer 2 handling; TODO:  FIX
        Layer2Data layer2Data = path.getLayer2Data();
        if (layer2Data != null) {
            String prevStatus = resv.getStatus();
            if (!prevStatus.equals("PRECANCEL")) {
                newStatus = "FINISHED";
            } else {
                newStatus = "CANCELLED";
            }
            resv.setStatus(newStatus);
            return newStatus;
        }
        // end of temporary fix
        PathElem pathElem = path.getPathElem();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        // find ingress and egress IP's
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                if (pathElem.getDescription().equals("ingress")) {
                    fromLink = pathElem.getLink();
                }
            }
            pathElem = pathElem.getNextElem();
        }
        // Create an LSP object.
        Map<String,String> lspInfo = new HashMap<String, String>();
        lspInfo.put("name", "oscars_" + resv.getGlobalReservationId());
        lspInfo.put("internal_interface_filter",
             this.props.getProperty("internal_interface_filter"));
        lspInfo.put("external_interface_filter",
             this.props.getProperty("external_interface_filter"));
        this.setupLogin(lspInfo, fromLink);
        this.teardownLSP(lspInfo);
        String prevStatus = resv.getStatus();
        if (!prevStatus.equals("PRECANCEL")) {
            newStatus = "FINISHED";
        } else {
            newStatus = "CANCELLED";
        }
        resv.setStatus(newStatus);
        return newStatus;
    }

    /**
     * Sets up login to a router.
     *
     * @param hm hash map to fill with login fields
     */
     private void setupLogin(Map<String,String> hm, Link link) {
        hm.put("login", this.props.getProperty("login"));
        hm.put("router", link.getPort().getNode().getNodeAddress().getAddress());
        String keyfile = System.getenv("CATALINA_HOME") +
                             "/shared/oscars.conf/server/oscars.key";
        hm.put("keyfile", keyfile);
        hm.put("passphrase", this.props.getProperty("passphrase"));
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

        LSPConnection conn = null;

        this.log.info("setupLSP.start");
        try {
            // this allows testing the template without trying to configure
            // a router
            if (this.props.getProperty("allowLSP").equals("1")) {
                conn = new LSPConnection();
                conn.createSSHConnection(hm);
            }
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/oscars.conf/server/";
            if (hm.get("vlanTag") == null) {
                fname += this.props.getProperty("setupFile");
            } else {
                fname += this.props.getProperty("setupVpnFile");
            }
            this.configureLSP(hm, hops, fname, conn);
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
     * @param hm A hash map with configuration values
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean teardownLSP(Map<String,String> hm) 
            throws PSSException {

        LSPConnection conn = null;

        this.log.info("teardownLSP.start");
        try {
            if (this.props.getProperty("allowLSP").equals("1")) {
                conn = new LSPConnection();
                conn.createSSHConnection(hm);
            }
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/oscars.conf/server/" +
                this.props.getProperty("teardownFile");
            this.configureLSP(hm, null, fname, conn);
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
     * @param hm a hash map with configuration values
     * @return an int, with value -1 if NA (e.g not found), 0 if down, 1 if up
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    private int statusLSP(Map<String,String> hm)
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
     * @param hm hash map of info from reservation and OSCARS' configuration
     * @param hops a list of hops only used if explicit path was given
     * @param fname full path of template file
     * @param conn LSP connection
     * @return boolean indicating status (not done yet)
     * @throws IOException
     * @throws JDOMException
     */
    private boolean configureLSP(Map<String,String> hm, List<String> hops,
                                 String fname, LSPConnection conn)
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
        outputter.setFormat(format);
        // log, and then send to router
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        // this will only be null if allowLSP was set to 0 in oscars.properties
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
