package net.es.oscars.pss.cisco;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.*;
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
    private Map<String,String> hm;
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

        this.log.info("create.start");

        // only used if an explicit path was given
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
        Path path = resv.getPath();
        Layer2Data layer2Data = path.getLayer2Data();
        // just handling layer 2 for Cisco's
        if (layer2Data == null) {
            throw new PSSException(
                    "No layer 2 data associated with path");
        }
        /*
        if (!path.isExplicit()) {
            throw new PSSException(
                    "Cisco configuration currently requires an explicit path");
        }
        */
        if (lspData.getIngressLink() == null) {
            throw new PSSException("Path endpoints not found yet");
        }
        // finds loopback info for both ingress and egress
        lspData.setLayer2PathInfo(true);

        // Fill in parameters for setting up LSP circuit.
        this.hm = new HashMap<String, String>();
        this.fillCommonParams(resv, lspData.getVlanTag());

        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        this.hm.put("bandwidth", Long.toString(resv.getBandwidth()));
        this.hm.put("lsp_setup-priority",
             this.commonProps.getProperty("lsp_setup-priority"));
        this.hm.put("lsp_reservation-priority",
             this.commonProps.getProperty("lsp_reservation-priority"));

        if (direction.equals("forward")) {
            // get IP associated with physical interface before egress
            ipaddr = ipaddrDAO.fromLink(lspData.getLastXfaceElem().getLink());
            if (ipaddr != null) {
                lspFwdTo = ipaddr.getIP();
            } else {
                throw new PSSException("Egress port has no IP in DB!");
            }
            Link ingressLink = lspData.getIngressLink();

            this.hm.put("port", ingressLink.getPort().getTopologyIdent());
            // router to send configuration command to (forward direction)
            this.hm.put("router",
                ingressLink.getPort().getNode().getNodeAddress().getAddress());
            this.hm.put("lsp_to", lspFwdTo);
            this.hm.put("egress-rtr-loopback", lspData.getEgressRtrLoopback());
            this.log.info("Filled in hash map for forward setup template");
        } else {
            // reverse direction
            // get IP associated with first in-facing physical interface
            ipaddr = ipaddrDAO.fromLink(
                     lspData.getIngressPathElem().getNextElem().getLink());
            if (ipaddr != null) {
                lspRevTo = ipaddr.getIP();
            } else {
                throw new PSSException("Egress port has no IP in DB!");
            }
            Link egressLink = lspData.getEgressLink();
            this.hm.put("port", egressLink.getPort().getTopologyIdent());
            this.hm.put("router",
                egressLink.getPort().getNode().getNodeAddress().getAddress());
            this.hm.put("lsp_to", lspRevTo);
            this.hm.put("egress-rtr-loopback",
                    lspData.getIngressRtrLoopback());
        }
        // reset to beginning, and get hops in correct direction
        hops = lspData.getHops(path.getPathElem(), direction, true);
        this.setupLSP(hops);
        // TODO:  makes assumption that forward will always be called first
        if (direction.equals("forward")) {
            this.log.info("Forward circuit set up done");
        } else {
            this.log.info("Circuit set up done");
            resv.setStatus("ACTIVE");
        }
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

        PathElem ingressPathElem = null;
        boolean active = false;

        if (lspData == null) {
            throw new PSSException(
                    "no path related configuration data present");
        }
        this.hm = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        // capitalize circuit names for production circuits
        if (resv.getDescription().contains("PRODUCTION")) {
            circuitName = circuitName.toUpperCase();
        }
        if (lspData.getIngressLink() == null) {
            throw new PSSException(
                    "refreshPath called before getting path endpoints");
        }
        lspData.setLayer2PathInfo(false);
        String routerName =
            lspData.getIngressLink().getPort().getNode().getNodeAddress().getAddress();
        this.hm.put("resv-id", circuitName);
        this.hm.put("vlan-id", lspData.getVlanTag());
        this.hm.put("router", routerName);
        String status = resv.getStatus();
        if (status.equals("PENDING") || status.equals("CANCELLED") ||
            status.equals("FINISHED") || status.equals("FAILED")) {
            return status;
        }
        if (!this.allowLSP) {
            return status;
        }
        active = this.statusLSP();
        if (!active) {
            // try one more time a minute later
            try {
                Thread.sleep(60000);
            } catch (Exception ex) {
                throw new PSSException(ex.getMessage());
            }
            active = this.statusLSP();
            if (!active) {
                resv.setStatus("FAILED");
            }
        }
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
    public void teardownPath(Reservation resv, LSPData lspData,
                             String direction)
            throws PSSException {

        String newStatus = null;

        Path path = resv.getPath();
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
        this.hm = new HashMap<String, String>();
        this.fillCommonParams(resv, lspData.getVlanTag());
        Link ingressLink = lspData.getIngressLink();
        if (direction.equals("forward")) {
            // router to send configuration command to
            this.hm.put("router",
                ingressLink.getPort().getNode().getNodeAddress().getAddress());
            this.hm.put("port", ingressLink.getPort().getTopologyIdent());
            this.teardownLSP();
        // TODO: makes assumption forward called first
        } else if (direction.equals("reverse")) {
            Link egressLink = lspData.getEgressLink();
            this.hm.put("router",
                egressLink.getPort().getNode().getNodeAddress().getAddress());
            this.hm.put("port", egressLink.getPort().getTopologyIdent());
            this.teardownLSP();
            String prevStatus = resv.getStatus();
            if (!prevStatus.equals("PRECANCEL")) {
                newStatus = "FINISHED";
            } else {
                newStatus = "CANCELLED";
            }
            // will be reset if turned out teardown failed
            resv.setStatus(newStatus);
        }
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
     * @param hops a list of hops only used if explicit path was given
     * @throws PSSException
     */
    public void setupLSP(List<String> hops)
            throws PSSException {

        boolean active = false;

        this.log.info("setupLSP.start");
        try {
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/classes/server/";
            fname += this.props.getProperty("setupL2Template");
            this.log.info("Filename: ["+fname+"]");
            this.configureLSP(hops, fname);
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
    public void teardownLSP() throws PSSException {

        boolean active = false;

        this.log.info("teardownLSP.start");
        try {
            String fname = System.getenv("CATALINA_HOME") +
                "/shared/classes/server/" +
                this.props.getProperty("teardownL2Template");
            this.configureLSP(null, fname);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("teardownLSP.finish");
    }

    /**
     * Gets the LSP status on a Cisco router.
     *
     * @return boolean indicating whether the circuit is up or down
     * @throws PSSException
     */
    public boolean statusLSP() throws PSSException {

//        boolean status = false;
        String outputStr = null;

        this.log.info("statusLSP.start");
        this.log.info("clogin -c \"show mpls l2transport vc\" " +
                      this.hm.get("router"));
        String[] cmd = { "clogin", "-c", "show mpls l2transport vc",
                         this.hm.get("router") };

        // this is a regexp; the parentheses indicate a capture group
        String outputExpression = "Eth VLAN " + this.hm.get("vlan-id") + ".*(UP|DOWN)";

        // the capture group will be compared to desiredOutput
        String desiredOutput = "UP";
        try {
            outputStr = this.runCommand(cmd, outputExpression, desiredOutput, 5);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        if (outputStr == null) {
            return false;
        } else if (outputStr.equals("UP")) {
            return true;
        } else {
            return false;
        }
        /*
        String[] fields = outputStr.trim().split(" ");
        int lastField = fields.length - 1;
        status = fields[lastField].equals("UP") ? true : false;
        this.log.info("statusLSP.finish with status " + status);
        return status;
        */

    }

    /**
     * Configure an LSP. Sends the template using RANCID to the server.
     *
     * @param hops a list of hops only used if explicit path was given
     * @param fname full path of template file
     * @throws IOException
     * @throws PSSException
     */
    private void configureLSP(List<String> hops, String fname)
            throws IOException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("configureLSP.start");
        for (String key: this.hm.keySet()) {
            sb.append(key + ": " + this.hm.get(key) + "\n");
        }
        this.log.info(sb.toString());
        String filledTemplate = this.th.buildString(this.hm, hops, fname);

        // log, and then send to router
        this.log.info("\n" + filledTemplate);
        // this allows testing the template without trying to configure
        // a router
        if (!this.allowLSP) {
            return;
        }
        this.sendConfigCommand(filledTemplate);
        this.log.info("configureLSP.end");
        return;
    }

    /**
     * Sends a configure command using RANCID to the server, and gets back
     * output.
     *
     * @param cmdStr string with command to send to router
     * @throws IOException
     * @throws PSSException
     */
    private void sendConfigCommand(String cmdStr)
            throws IOException, PSSException {

        // create a temporary file for use by RANCID
        String fname =  "/tmp/" + this.hm.get("resv-id");
        File tmpFile = new File(fname);
        BufferedWriter outputStream =
            new BufferedWriter(new FileWriter(tmpFile));
        // write command to temporary file
        outputStream.write(cmdStr);
        outputStream.close();
        this.log.info("clogin -x " + fname + " " + this.hm.get("router"));
        String cmd[] = { "clogin", "-x", fname, this.hm.get("router") };
        this.runCommand(cmd, null, null, 1);
        // delete temporary file
        tmpFile.delete();
    }

    /**
     * Sends a command using RANCID to the server, and gets back
     * output.
     *
     * @param cmdStr array with command and arguments to exec
     * @param requiredOutput string with a regexp to match output, if any
     * @param times times to attempt the command until desired result
     * @return outputStr first line of output with the required string
     * @throws IOException
     * @throws PSSException
     */
    private String runCommand(String[] cmd, String outputExpression, String desiredOutput, int times)
            throws IOException, PSSException {

        String outputStr = null;

        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader cmdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String errInfo = cmdError.readLine();

        if (errInfo != null ) {
            this.log.warn("RANCID command error: " + errInfo );
            cmdOutput.close();
            cmdError.close();
            throw new PSSException("RANCID commnd error: " + errInfo);
        }

        boolean done = false;

        int i = 1;
        while (!done) {
            String outputLine = null;
            while ((outputLine = cmdOutput.readLine()) != null) {
                this.log.info("output: " + outputLine);
                // Did user ask for a specific output?
                if (outputExpression != null) {
                    if (outputLine.matches(outputExpression)) {
                        Pattern patt = Pattern.compile(outputExpression);
                        Matcher m = patt.matcher(outputLine);
                        String matched = m.group(1);
                        // We don't care about output? OK, done
                        // but do not return, let's get & log the whole output first
                        if (desiredOutput == null) {
                            this.log.info("Output matched expression");
                            outputStr = matched;
                            done = true;
                        // We do care about output and it's correct? OK, done
                        } else if (matched != null && matched.equals(desiredOutput)) {
                            this.log.info("Output matched expression and desired result");
                            outputStr = matched;
                            done = true;
                        // We do care about output and it's incorrect? Get the next line
                        } else {
                            outputStr = matched;
                        }
                    }
                }
            }
            // after this attempt we haven't gotten a result yet
            if (outputStr == null && !done) {
                // this is not the last time so wanObjecte can try again in a bit
                if (i < times) {
                    try {
                        Thread.sleep(10000);
                    } catch (Exception ex) {
                        cmdOutput.close();
                        cmdError.close();
                        this.log.warn("Error getting status: "+ex.getMessage());
                        throw new PSSException("Error getting status: "+ex.getMessage());
                    }
                // this was the last attempt, return whatever we have
                } else {
                    done = true;
                }
            }
            i++;
        }
        cmdOutput.close();
        cmdError.close();
        return outputStr;
    }

    /**
     * Common parameter setup for setup and teardown.
     *
     * @param resv Reservation instance (to get globalReservationId)
     * @param vlanTag string with VLAN id
     * @throws PSSException
     */
    private void fillCommonParams(Reservation resv, String vlanTag)
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

        this.hm.put("resv-id", circuitName);
        this.hm.put("resv-num", resvNumForTpt);
        this.hm.put("vlan-id", vlanTag);
    }
}
