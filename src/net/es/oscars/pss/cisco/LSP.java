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
/*
                    if (vlanTag == null) {
                        throw new PSSException("VLAN tag is null!");
                    }
*/
                    // temporary, to test rest of template
                    vlanTag = "3552";
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
        
        // Create an LSP object.
        lspInfo = new HashMap<String, String>();
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        lspInfo.put("resv-id", circuitName);
        
        String[] columns = circuitName.split("-");
        if (columns.length != 2) {
            throw new PSSException("Couldn't parse GRI! ["+circuitName+"]");
        }
        int resvNum = Integer.parseInt(columns[1].trim());
        
        // get IP associated with physical interface before egress
        ipaddr = ipaddrDAO.fromLink(lastXfacePathElem.getLink());
        if (ipaddr != null) {
            lspFwdTo = ipaddr.getIP();
        } else {
            throw new PSSException("Egress port has no IP in DB!");
        }
        // wrap at 65534 (65535 reserved for test)
        resvNum = resvNum % 65534;
        lspInfo.put("resv-num", Integer.toString(resvNum));
        lspInfo.put("port", ingressLink.getPort().getTopologyIdent());
        // used for login, not template
        lspInfo.put("router",
            ingressLink.getPort().getNode().getNodeAddress().getAddress());
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
        // used for login, not template
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

        Link ingressLink = null;
        Link egressLink = null;
        String vlanTag = null;

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
        // TODO:  temporary fix
        vlanTag = "3552";
        // Create an LSP object.
        Map<String,String> lspInfo = new HashMap<String, String>();
        String newStatus = null;
        String circuitStr = "oscars_" + resv.getGlobalReservationId();
        // "." is illegal character in resv-id parameter
        String circuitName = circuitStr.replaceAll("\\.", "_");
        String[] columns = circuitName.split("-");
        if (columns.length != 2) {
            throw new PSSException("Couldn't parse GRI! ["+circuitName+"]");
        }
        int resvNum = Integer.parseInt(columns[1].trim());
        // wrap at 65534 (65535 reserved for test)
        resvNum = resvNum % 65534;
        
        // forward direction
        lspInfo.put("resv-id", circuitName);
        lspInfo.put("resv-num", Integer.toString(resvNum));
        lspInfo.put("port", ingressLink.getPort().getTopologyIdent());
        lspInfo.put("vlan-id", vlanTag);
        this.teardownLSP(lspInfo);
        // reverse direction
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
     * @return boolean indicating success
     * @throws PSSException
     */
    public boolean setupLSP(Map<String,String> hm, List<String> hops)
            throws PSSException {

        this.log.info("setupLSP.start");
        try {
            String fname =  System.getenv("CATALINA_HOME") +
                "/shared/classes/server/";
            fname += this.props.getProperty("setupFile");
            this.log.info("Filename: ["+fname+"]");
            this.configureLSP(hm, hops, fname);
        } catch (IOException ex) {
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

        this.log.info("teardownLSP.start");
        try {
            String fname = System.getenv("CATALINA_HOME") +
                "/shared/classes/server/" +
                this.props.getProperty("teardownFile");
            this.configureLSP(hm, null, fname);
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.log.info("teardownLSP.finish");
        return true;
    }

    /** 
     * Gets the LSP status on a Cisco router.
     *
     * @param hm a hash map with configuration values
     * @return an int, with value -1 if NA (e.g not found), 0 if down, 1 if up
     * @throws IOException
     * @throws PSSException
     */
    private int statusLSP(Map<String,String> hm)
            throws IOException, PSSException {

        int status = -1;

        this.log.info("statusLSP.start");
        this.log.info("statusLSP.finish");
        return status;
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
        // create a temporary file for use by RANCID
        String outputName =  "/tmp/" + hm.get("resv-id");
        File tmpFile = new File(outputName);
        BufferedWriter outputStream = 
            new BufferedWriter(new FileWriter(tmpFile));
        // write template to temporary file
        outputStream.write(filledTemplate);
        outputStream.close();
        String cmd = "clogin -x " + outputName + " " + hm.get("router");
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader cmdOutput = 
            new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader cmdError =
            new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String errInfo = cmdError.readLine();
        if (errInfo != null ) {
            this.log.warn("RANCID error: " + errInfo );
            throw new PSSException("RANCID error: " + errInfo);
        }
        String outputInfo = cmdOutput.readLine();
        // TODO:  RANCID status check given output of command
        if (outputInfo != null) {
            this.log.info(outputInfo);
        }
        cmdOutput.close();
        cmdError.close();
        // delete temporary file
        tmpFile.delete();
        this.log.info("configureLSP.end");
        return true;
    }
}
