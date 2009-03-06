package net.es.oscars.pss.vendor.cisco;

import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.ConfigFinder;
import net.es.oscars.PropHandler;
import net.es.oscars.pss.*;
import net.es.oscars.pss.vendor.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;

/**
 * LSP performs setup/teardown of Cisco LSP paths on the router.
 *
 * @author David Robertson
 */
public class LSP {

    private String dbname;
    private Properties commonProps;
    private Properties props;
    private TemplateHandler th;
    private Logger log;
    private boolean allowLSP;
    private static String staticAllowLSP;

    public LSP(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.commonProps = propHandler.getPropertyGroup("pss", true);
        this.log = Logger.getLogger(this.getClass());
        if (staticAllowLSP != null) {
            this.allowLSP = staticAllowLSP.equals("1") ? true : false;
        } else {
            this.allowLSP =
                this.commonProps.getProperty("allowLSP").equals("1") ? true : false;
        }
        this.props = propHandler.getPropertyGroup("pss.cisco", true);
        this.th = new TemplateHandler();
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

        this.log.info("createPath.start");

        List<String> hops = null;
        String lspFwdTo = null;
        String lspRevTo = null;
        String param = null;
        Link link = null;
        Ipaddr ipaddr = null;

        if (lspData == null) {
            throw new PSSException(
                    "no path related configuration data present");
        }
        if ((direction == null) || (!direction.equals("forward") &&
                                    !direction.equals("reverse"))) {
            throw new PSSException("illegal circuit direction");
        }
        // note that null checks are not necessary where database enforces
        // that a field is not null
        Path path = null;
        try {
        	path = resv.getPath(PathType.LOCAL);
        } catch (BSSException ex) {
        	throw new PSSException(ex.getMessage());
        }
        Layer2Data layer2Data = path.getLayer2Data();
        // just handling layer 2 for Cisco's
        if (layer2Data == null) {
            throw new PSSException(
                    "No layer 2 data associated with path");
        }
        if (lspData.getIngressLink() == null) {
            throw new PSSException("Path endpoints not found yet");
        }
        // finds loopback info for both ingress and egress
        lspData.setLayer2PathInfo(true);

        // Fill in parameters for setting up LSP circuit.
        HashMap<String, String> hm = new HashMap<String, String>();
        this.fillCommonParams(resv, lspData.getVlanTag(), hm);

        hm.put("bandwidth", Long.toString(resv.getBandwidth()));
        hm.put("lsp_setup-priority",
             this.commonProps.getProperty("lsp_setup-priority"));
        hm.put("lsp_reservation-priority",
             this.commonProps.getProperty("lsp_reservation-priority"));

        hm.put("direction", direction);

        if (direction.equals("forward")) {
            // get IP associated with physical interface before egress
            ipaddr = lspData.getLastXfaceElem().getLink().getValidIpaddr();
            if (ipaddr != null) {
                lspFwdTo = ipaddr.getIP();
            } else {
                throw new PSSException("Egress port has no IP in DB!");
            }
            Link ingressLink = lspData.getIngressLink();

            hm.put("port", ingressLink.getPort().getTopologyIdent());
            // router to send configuration command to (forward direction)
            hm.put("router",
                ingressLink.getPort().getNode().getNodeAddress().getAddress());
            hm.put("lsp_to", lspFwdTo);
            hm.put("egress-rtr-loopback", lspData.getEgressRtrLoopback());
            log.info("Filled in hash map for forward setup template");
        } else {
            // reverse direction
            // get IP associated with first in-facing physical interface
            PathElem ingressPathElem = lspData.getIngressPathElem();
            int nextSeqNumber = ingressPathElem.getSeqNumber() + 1;
            ipaddr = path.getPathElems().get(nextSeqNumber).getLink().getValidIpaddr();
            if (ipaddr != null) {
                lspRevTo = ipaddr.getIP();
            } else {
                throw new PSSException("Egress port has no IP in DB!");
            }
            Link egressLink = lspData.getEgressLink();
            hm.put("port", egressLink.getPort().getTopologyIdent());
            hm.put("router", egressLink.getPort().getNode().getNodeAddress().getAddress());
            hm.put("lsp_to", lspRevTo);
            hm.put("egress-rtr-loopback", lspData.getIngressRtrLoopback());
        }
        // reset to beginning, and get hops in correct direction
        hops = lspData.getHops(path.getPathElems(), direction, true);
        this.setupLSP(hops, hm);
        // TODO:  makes assumption that forward will always be called first
        if (direction.equals("forward")) {
            this.log.info("Forward circuit set up done");
        } else {
            this.log.info("Circuit set up done");
        }
        this.log.info("createPath.end");
    }

    /**
     * Verifies LSP is still active.
     *
     * @param resv the reservation whose path will be refreshed
     * @param lspData LSPData instance containing path-related configuration
     * @throws PSSException
     */
    public String refreshPath(Reservation resv, LSPData lspData)
            throws PSSException {

        if (lspData == null) {
            throw new PSSException("no path related configuration data present");
        }
        HashMap<String, String> hm = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        // capitalize circuit names for production circuits
        if (resv.getDescription().contains("PRODUCTION")) {
            circuitName = circuitName.toUpperCase();
        }
        if (lspData.getIngressLink() == null) {
            throw new PSSException("refreshPath called before getting path endpoints");
        }
        lspData.setLayer2PathInfo(false);
        String routerName = lspData.getIngressLink().getPort().getNode().getNodeAddress().getAddress();

        hm.put("resv-id", circuitName);
        hm.put("vlan-id", lspData.getVlanTag());
        hm.put("router", routerName);

        return resv.getStatus();
    }

    /**
     * Formats reservation parameters for circuit teardown.
     *
     * @param resv a reservation instance
     * @param lspData LSPData instance containing path-related configuration
     * @param direction boolean indicating whether forward path
     * @throws PSSException
     */
    public void teardownPath(Reservation resv, LSPData lspData, String direction)
            throws PSSException {
        this.log.info("teardownPath.start");


        if (lspData == null) {
            throw new PSSException(
                    "no path related configuration data present");
        }
        if ((direction == null) || (!direction.equals("forward") &&
                                    !direction.equals("reverse"))) {
            throw new PSSException("illegal circuit direction");
        }
        if (lspData.getIngressLink() == null) {
            throw new PSSException(
                    "teardownPath called before getting path endpoints");
        }
        lspData.setLayer2PathInfo(false);
        // Set up parameters for tearing down an LSP circuit.
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("direction", direction);
        this.fillCommonParams(resv, lspData.getVlanTag(), hm);
        Link ingressLink = lspData.getIngressLink();
        if (direction.equals("forward")) {
            // router to send configuration command to
            hm.put("router", ingressLink.getPort().getNode().getNodeAddress().getAddress());
            hm.put("port", ingressLink.getPort().getTopologyIdent());
            this.teardownLSP(hm);
        } else if (direction.equals("reverse")) {
            Link egressLink = lspData.getEgressLink();
            hm.put("router", egressLink.getPort().getNode().getNodeAddress().getAddress());
            hm.put("port", egressLink.getPort().getTopologyIdent());
            this.teardownLSP(hm);
        }
        this.log.info("teardownPath.end");
    }

    /**
     * Allows overriding the allowLSP property.  Primarily for tests.
     * Must be called before LSP is instantiated.
     * @param overrideAllowLSP "0" or "1" indicating whether LSP can be set up
     */
    public static void setConfigurable(String overrideAllowLSP) {
        staticAllowLSP = overrideAllowLSP;
    }

    /**
     * Sets up an LSP circuit.
     *
     * @param hops list of hops
     * @throws PSSException
     */
    public void setupLSP(List<String> hops, HashMap<String, String> hm)
            throws PSSException {

        this.log.info("setupLSP.start");
        try {
            String fname = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR, 
                    this.props.getProperty("setupL2Template"));
            this.log.info("Filename: ["+fname+"]");
            this.configureLSP(hops, fname, hm);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("setupLSP.finish");
    }

    /**
     * Tears down an LSP circuit.
     *
     * @throws PSSException
     */
    public void teardownLSP(HashMap<String, String> hm) throws PSSException {

        this.log.info("teardownLSP.start");
        try {
            String fname = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR,
                this.props.getProperty("teardownL2Template"));
            this.configureLSP(null, fname, hm);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("teardownLSP.finish");
    }

    /**
     * Gets the LSP status of a set of VLAN's on a Cisco router.
     *
     * @param router string with router id
     * @param statusInputs HashMap of VLAN's to check with associated info
     * @return vlanStatuses HashMap with VLAN's and their statuses
     * @throws PSSException
     */
    public Map<String, VendorStatusResult> statusLSP(String router,
                                     Map<String,VendorStatusInput> statusInputs)
            throws PSSException {

        Map<String,VendorStatusResult> vlanStatuses =
            new HashMap<String,VendorStatusResult>();
        Map<String,VendorStatusResult> currentVlans =
            new HashMap<String,VendorStatusResult>();
        BufferedReader cmdOutput = null;
        StringBuilder sb = null;

        this.log.info("statusLSP.start");

        this.log.info("clogin -c \"show mpls l2transport vc\" " +
                      router);
        String[] cmd = { "clogin", "-c", "show mpls l2transport vc", router };
        try {
            cmdOutput = this.runCommand(cmd);
            String outputLine = null;
            sb = new StringBuilder();
            while ((outputLine = cmdOutput.readLine()) != null) {
                sb.append(outputLine + "\n");
                // in this case no VLAN can be checked
                if (outputLine.startsWith("Error:")) {
                    for (String vlanId: statusInputs.keySet()) {
                        VendorStatusResult statusResult =
                                new CiscoStatusResult();
                        statusResult.setErrorMessage(outputLine);
                        vlanStatuses.put(vlanId, statusResult);
                    }
                    this.log.info("output: " + sb.toString());
                    return vlanStatuses;
                }
            }
            cmdOutput.close();
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("output: " + sb.toString());
        Pattern pattern = Pattern.compile(".*Eth VLAN (\\d{3,4}).*(UP|DOWN)");
        List<MatchResult> results = TemplateHandler.findAll(pattern, sb.toString());
        for (MatchResult r: results) {
            VendorStatusResult statusResult = new CiscoStatusResult();
            if (r.group(2).equals("UP")) {
                statusResult.setCircuitStatus(true);
                currentVlans.put(r.group(1), statusResult);
//                this.log.debug("Found that vlan:" + r.group(1) + " is UP");
            } else {
                statusResult.setCircuitStatus(false);
                currentVlans.put(r.group(1), statusResult);
//                this.log.debug("Found that vlan:" + r.group(1) + " is DOWN");
            }
        }
        for (String vlanId: statusInputs.keySet()) {
            if (!currentVlans.containsKey(vlanId)) {
                VendorStatusResult statusResult = new CiscoStatusResult();
                statusResult.setCircuitStatus(false);
                vlanStatuses.put(vlanId, statusResult);
//                this.log.debug("Decided that vlan:" + vlanId + " is DOWN");
            } else {
                vlanStatuses.put(vlanId, currentVlans.get(vlanId));
//                this.log.debug("Decided that vlan:" + vlanId + " is "+currentVlans.get(vlanId));
            }
        }
        this.log.info("statusLSP.finish");
        return vlanStatuses;
    }

    /**
     * Configure an LSP. Sends the template using RANCID to the server.
     *
     * @param hops list of hops
     * @param fname full path of template file
     * @throws IOException
     * @throws PSSException
     */
    private void configureLSP(List<String> hops, String fname, HashMap<String, String> hm)
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
        if (!this.allowLSP) {
            return;
        }
        this.sendConfigCommand(filledTemplate, hm);
        this.log.info("configureLSP.end");
        return;
    }

    /**
     * Sends a configure command using RANCID to the server, and gets back
     * output.
     *
     * @param cmdStr string with command to send to router
     * @throws PSSException
     */
    private void sendConfigCommand(String cmdStr, HashMap<String, String> hm)
            throws PSSException {

        // create a temporary file for use by RANCID
        String fname =  "/tmp/" + hm.get("resv-id")+"-"+hm.get("direction");
        File tmpFile = new File(fname);
        try {
            BufferedWriter outputStream =
                new BufferedWriter(new FileWriter(tmpFile));
            // write command to temporary file
            outputStream.write(cmdStr);
            outputStream.close();
            this.log.info("clogin -x " + fname + " " + hm.get("router"));
            String cmd[] = { "clogin", "-x", fname, hm.get("router") };
            BufferedReader cmdOutput = this.runCommand(cmd);
            String outputLine = null;
            StringBuilder sb = new StringBuilder();
            while ((outputLine = cmdOutput.readLine()) != null) {
                // just log
                sb.append(outputLine + "\n");
            }
            cmdOutput.close();
            this.log.debug(sb.toString());
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        // delete temporary file
        tmpFile.delete();
    }

    /**
     * Sends a command using RANCID to the server, and gets back
     * output.
     *
     * @param cmdStr array with command and arguments to exec
     * @return cmdOutput BufferedReader for output from the router
     * @throws IOException
     * @throws PSSException
     */
    private BufferedReader runCommand(String[] cmd)
            throws IOException, PSSException {

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
        cmdError.close();
        return cmdOutput;
    }

    /**
     * Common parameter setup for setup and teardown.
     *
     * @param resv Reservation instance (to get globalReservationId)
     * @param vlanTag string with VLAN id
     * @throws PSSException
     */
    private void fillCommonParams(Reservation resv, String vlanTag, HashMap<String, String> hm)
            throws PSSException {

        double resvNumTmp;

        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        // capitalize circuit names for production circuits
        if (resv.getDescription().contains("PRODUCTION")) {
            circuitName = circuitName.toUpperCase();
        }
        String[] columns = circuitName.split("-");
        if (columns.length != 2) {
            throw new PSSException("Couldn't parse GRI! ["+circuitName+"]");
        }
        try {
            resvNumTmp = Double.parseDouble(columns[1].trim());
        } catch (NumberFormatException ex) {
            this.log.error("Invalid number format for GRI numerical part" +
                         ex.getMessage());
            throw ex;
        }

        // wrap at 65534 (65535 reserved for test)
        int resvNum = (int) resvNumTmp % 65534;
        String resvNumForTpt = Integer.toString(resvNum);
        this.log.info("Reservation number after cleanup is: "+resvNumForTpt+
                       " initially: "+columns[1]);

        hm.put("resv-id", circuitName);
        hm.put("resv-num", resvNumForTpt);
        hm.put("vlan-id", vlanTag);
    }

    /**
     * @return the allowLSP
     */
    public boolean isAllowLSP() {
        return allowLSP;
    }

}
