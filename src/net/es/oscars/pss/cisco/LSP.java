package net.es.oscars.pss.cisco;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.*;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.Utils;

/**
 * LSP performs setup/teardown of Cisco LSP paths on the router.
 *
 * @author David Robertson
 */
public class LSP implements PSS {

    private Properties props;
    private Logger log;
    private TemplateHandler th;
    private String dbname;
    private Utils utils;

    public LSP(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss.cisco", true);
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
        this.log.info("create.start");

        Map<String,String> lspInfo = null;
        // only used if an explicit path was given
        List<String> hops = null;
        String lspFwdTo = null;
        String lspRevTo = null;
        String ingressRtrLoopback = null;
        String egressRtrLoopback = null;
        String param = null;
        Link link = null;
        Link ingressLink = null;
        Link egressLink = null;
        Ipaddr ipaddr = null;
        String vlanTag = null;

        // note that null checks are not necessary where database enforces
        // that a field is not null
        Path path = resv.getPath();
        Layer2Data layer2Data = path.getLayer2Data();
        // just handling layer 2 for Cisco's
        if (layer2Data == null) {
            throw new PSSException(
                    "No layer 2 data associated with path");
        }
        if (!path.isExplicit()) {
            throw new PSSException(
                    "Cisco configuration currently requires an explicit path");
        }
        PathElem pathElem = path.getPathElem();
        PathElem ingressPathElem = null;
        PathElem lastXfacePathElem = null;
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        // find ingress and egress IP's and get loopback info for both directions
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                // not an ingress or egress unless already checked at
                // reservation time
                if (pathElem.getDescription().equals("ingress")) {
                    ingressPathElem = pathElem;
                    ingressLink = pathElem.getLink();
                    // assume just one VLAN for now
                    vlanTag = pathElem.getLinkDescr();
                    if (vlanTag == null) {
                        throw new PSSException("VLAN tag is null!");
                    }
                    // find ingress loopback
                    NodeAddress ingressNodeAddress =
                        ingressLink.getPort().getNode().getNodeAddress();
                    String ingressAddr = ingressNodeAddress.getAddress();
                    ingressRtrLoopback = this.utils.getIP(ingressAddr);
                } else if (pathElem.getDescription().equals("egress")) {
                    egressLink = pathElem.getLink();
                    NodeAddress egressNodeAddress =
                        egressLink.getPort().getNode().getNodeAddress();
                    String egressAddr = egressNodeAddress.getAddress();
                    egressRtrLoopback = this.utils.getIP(egressAddr);
                }
            } else {
                // used in finding next to last interface
                lastXfacePathElem = pathElem;
            }
            pathElem = pathElem.getNextElem();
        }
        if (ingressRtrLoopback == null) {
            throw new PSSException("no ingress loopback in path");
        }
        if (egressRtrLoopback == null) {
            throw new PSSException("no egress loopback in path");
        }
        
        // Fill in parameters for setting up LSP circuit.
        lspInfo = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        lspInfo.put("resv-id", circuitName);
        
        String[] columns = circuitName.split("-");
        if (columns.length != 2) {
            throw new PSSException("Couldn't parse GRI! ["+circuitName+"]");
        }
        double resvNumTmp;
        try {
        	resvNumTmp = Double.parseDouble(columns[1].trim());
        } catch (NumberFormatException ex) {
        	this.log.error("Invalid number format for GRI numerical part"+ex.getMessage());
        	throw ex;
        }
        // wrap at 65534 (65535 reserved for test)
        int resvNum = (int) resvNumTmp % 65534;
        String resvNumForTpt = Integer.toString(resvNum);
        
        // get IP associated with physical interface before egress
        ipaddr = ipaddrDAO.fromLink(lastXfacePathElem.getLink());
        if (ipaddr != null) {
            lspFwdTo = ipaddr.getIP();
        } else {
            throw new PSSException("Egress port has no IP in DB!");
        }
        // wrap at 65534 (65535 reserved for test)

        
        this.log.info("Reservation number after cleanup is: "+resvNumForTpt+ " initially: "+columns[1]);
        
        
        lspInfo.put("resv-num", resvNumForTpt);
        lspInfo.put("port", ingressLink.getPort().getTopologyIdent());
        // router to send configuration command to (forward direction)
        lspInfo.put("router", ingressLink.getPort().getNode().getNodeAddress().getAddress());
        lspInfo.put("lsp_to", lspFwdTo);
        lspInfo.put("egress-rtr-loopback", egressRtrLoopback);
        lspInfo.put("bandwidth", Long.toString(resv.getBandwidth()));
        lspInfo.put("vlan-id", vlanTag);
        lspInfo.put("lsp_setup-priority",
             this.props.getProperty("lsp_setup-priority"));
        lspInfo.put("lsp_reservation-priority",
             this.props.getProperty("lsp_reservation-priority"));
        this.log.info("Filled in hash map for setup template"); 
        
        hops = new ArrayList<String>();
        // reset to beginning
        pathElem = path.getPathElem();
        while (pathElem != null) {
            link = pathElem.getLink();
            // this gets everything except the ingress and egress, which we
            // don't want
            if (pathElem.getDescription() ==  null) {
                ipaddr = ipaddrDAO.fromLink(link);
                if (ipaddr == null) {
                    throw new PSSException(
                            "No IP for link: ["+TopologyUtil.getFQTI(link)+"]");
                }
                hops.add(ipaddr.getIP());
            }
            pathElem = pathElem.getNextElem();
        }
        this.log.info("Set up path hops addresses"); 
        
        // forward direction
        this.setupLSP(lspInfo, hops);

        this.log.info("Forward LSP done"); 
        
        // reverse direction
        // get IP associated with first in-facing physical interface
        ipaddr = ipaddrDAO.fromLink(ingressPathElem.getNextElem().getLink());
        if (ipaddr != null) {
            lspRevTo = ipaddr.getIP();
        } else {
          throw new PSSException("Egress port has no IP in DB!");
        }
        lspInfo.put("port", egressLink.getPort().getTopologyIdent());
        lspInfo.put("router",
            egressLink.getPort().getNode().getNodeAddress().getAddress());
        lspInfo.put("lsp_to", lspRevTo);
        lspInfo.put("egress-rtr-loopback", ingressRtrLoopback);
        ArrayList<String> reverseHops = new ArrayList<String>();
        for (int i=hops.size()-1; i >= 0; i--) {
            reverseHops.add(hops.get(i));
        }
        this.setupLSP(lspInfo, reverseHops);
        this.log.info("Reverse LSP done"); 
        resv.setStatus("ACTIVE");
        this.log.info("create.end");
        return resv.getStatus();
    }

    /**
     * Verifies LSP is still active.
     *
     * @param resv the reservation whose path will be refreshed
     * @throws PSSException
     */
    public String refreshPath(Reservation resv) throws PSSException {

        Link ingressLink = null;
        boolean active = false;

        Map<String,String> hm = null;
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        hm.put("resv-id", circuitName);
        PathElem pathElem = resv.getPath().getPathElem();
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                if (pathElem.getDescription().equals("ingress")) {
                    ingressLink = pathElem.getLink();
                    break;
                }
            }
            pathElem = pathElem.getNextElem();
        }
        String routerName =
            ingressLink.getPort().getNode().getNodeAddress().getAddress();
        hm.put("router", routerName);
        try {
            active = this.statusLSP(hm);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        return active ? "ACTIVE" : "FAILED";
    }

    /**
     * Formats reservation parameters for circuit teardown.
     *
     * @param resv a reservation instance
     * @throws PSSException
     */
    public String teardownPath(Reservation resv) throws PSSException {

        Link ingressLink = null;
        Link egressLink = null;
        String vlanTag = null;
        String newStatus = null;

        Path path = resv.getPath();
        PathElem pathElem = path.getPathElem();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        // find ingress and egress IP's
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                // not an ingress or egress unless checked in setup to
                // see if has the right loopback
                if (pathElem.getDescription().equals("ingress")) {
                    ingressLink = pathElem.getLink();
                    // assume just one VLAN for now
                    vlanTag = pathElem.getLinkDescr();
                } else if (pathElem.getDescription().equals("egress")) {
                    egressLink = pathElem.getLink();
                }
            }
            pathElem = pathElem.getNextElem();
        }
        // Set up parameters for tearing down an LSP circuit.
        Map<String,String> lspInfo = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        String[] columns = circuitName.split("-");
        if (columns.length != 2) {
            throw new PSSException("Couldn't parse GRI! ["+circuitName+"]");
        }
        double resvNumTmp;
        try {
        	resvNumTmp = Double.parseDouble(columns[1].trim());
        } catch (NumberFormatException ex) {
        	this.log.error("Invalid number format for GRI numerical part"+ex.getMessage());
        	throw ex;
        }

        // wrap at 65534 (65535 reserved for test)
        int resvNum = (int) resvNumTmp % 65534;
        String resvNumForTpt = Integer.toString(resvNum);
        
        this.log.info("Reservation number after cleanup is: "+resvNumForTpt+ " initially: "+columns[1]);

        // forward direction
        // router to send configuration command to
        lspInfo.put("router", ingressLink.getPort().getNode().getNodeAddress().getAddress());
        lspInfo.put("resv-id", circuitName);
        lspInfo.put("resv-num", resvNumForTpt);
        lspInfo.put("port", ingressLink.getPort().getTopologyIdent());
        lspInfo.put("vlan-id", vlanTag);
        this.teardownLSP(lspInfo);
        // reverse direction
        lspInfo.put("router",
            egressLink.getPort().getNode().getNodeAddress().getAddress());
        lspInfo.put("port", egressLink.getPort().getTopologyIdent());
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
     * Sets up an LSP circuit.
     *
     * @param hm a hash map with configuration values
     * @param hops a list of hops only used if explicit path was given
     * @throws PSSException
     */
    public void setupLSP(Map<String,String> hm, List<String> hops)
            throws PSSException {

        boolean active = false;

        this.log.info("setupLSP.start");
        try {
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/classes/server/";
            fname += this.props.getProperty("setupFile");
            this.log.info("Filename: ["+fname+"]");
            this.configureLSP(hm, hops, fname);
            active = this.statusLSP(hm);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        if (!active) {
            throw new PSSException("circuit setup for " + 
                                   hm.get("resv-id") + " failed");
        }
        this.log.info("setupLSP.finish");
    }

    /**
     * Tears down an LSP circuit.
     *
     * @param hm A hash map with configuration values
     * @throws PSSException
     */
    public void teardownLSP(Map<String,String> hm) throws PSSException {

        boolean active = false;

        this.log.info("teardownLSP.start");
        try {
            String fname = System.getenv("CATALINA_HOME") +
                "/shared/classes/server/" +
                this.props.getProperty("teardownFile");
            this.configureLSP(hm, null, fname);
            active = this.statusLSP(hm);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        if (active) {
            throw new PSSException("circuit teardown for " + 
                                   hm.get("resv-id") + " failed");
        }
        this.log.info("teardownLSP.finish");
    }

    /** 
     * Gets the LSP status on a Cisco router.
     *
     * @param hm hash map with configuration parameters
     * @return boolean indicating whether the circuit is up or down
     * @throws IOException
     * @throws PSSException
     */
    private boolean statusLSP(Map<String,String> hm)
            throws IOException, PSSException {

        this.log.info("statusLSP.start");
        String configCmd = "show mpls traffic-eng tunnels name " +
                           hm.get("resv-id");
        if (!this.props.getProperty("allowLSP").equals("1")) {
            return true;
        }
        boolean active = this.sendCommand(configCmd, hm, "connection is up");
        this.log.info("statusLSP.finish");
        return active;
    }

    /**
     * Configure an LSP. Sends the template using RANCID to the server.
     *
     * @param hm hash map of info from reservation and OSCARS' configuration
     * @param hops a list of hops only used if explicit path was given
     * @param fname full path of template file
     * @return boolean indicating status (not done yet)
     * @throws IOException
     */
    private boolean configureLSP(Map<String,String> hm, List<String> hops,
                                 String fname)
            throws IOException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());
        String filledTemplate = this.th.buildString(hm, hops, fname);

        // log, and then send to router
        this.log.info("\n" + filledTemplate);
        // this allows testing the template without trying to configure
        // a router
        if (!this.props.getProperty("allowLSP").equals("1")) {
            return true;
        }
        boolean success = this.sendCommand(filledTemplate, hm, null);
        this.log.info("configureLSP.end");
        return true;
    }

    /**
     * Sends a command using RANCID to the server, and gets back output.
     *
     * @param cmdStr string with command to send to router
     * @param hm hash map of info from reservation and OSCARS' configuration
     * @param requiredOutput string with required output, if any
     * @param status boolean indicating whether required output was present
     * @throws PSSException
     */
    private boolean sendCommand(String cmdStr, Map<String,String> hm,
                             String requiredOutput)
            throws IOException, PSSException {

        boolean status = false ;

        if (requiredOutput == null) {
            status = true;
        }
        // create a temporary file for use by RANCID
        String fname =  "/tmp/" + hm.get("resv-id");
        File tmpFile = new File(fname);
        BufferedWriter outputStream = 
            new BufferedWriter(new FileWriter(tmpFile));
        // write command to temporary file
        outputStream.write(cmdStr);
        outputStream.close();
        String cmd = "clogin -x " + fname + " " + hm.get("router");
        this.log.info(cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader cmdOutput = 
            new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader cmdError =
            new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String errInfo = cmdError.readLine();
        if (errInfo != null ) {
            this.log.warn("RANCID command error: " + errInfo );
            cmdOutput.close();
            cmdError.close();
            throw new PSSException("RANCID commnd error: " + errInfo);
        }
        String outputLine = null;
        while ((outputLine = cmdOutput.readLine()) != null) {
            this.log.info("output: " + outputLine);
            if (requiredOutput != null) {
                if (outputLine.contains(requiredOutput)) {
                    status = true;
                }
            }
        }
        cmdOutput.close();
        cmdError.close();
        // delete temporary file
        tmpFile.delete();
        return status;
    }
}
