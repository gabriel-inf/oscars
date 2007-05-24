package net.es.oscars.pss;

import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.*;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;

/**
 * JnxSNMP contains methods dealing with SNMP queries.
 *
 * @author Andrew Lake, Jason Lee, David Robertson
 */
public class JnxSNMP {
    private Snmp snmp;
    private CommunityTarget target;
    private String errMsg;
    private HashMap<String, Variable> lspInfo;
    private HashMap lsp;
    private Properties props;
    private Logger log;

    /**
     * Constructor.
     */
    public JnxSNMP() throws IOException {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("snmp", true);
        this.lspInfo = new HashMap<String, Variable>();
        this.lsp = new HashMap();
        this.log = Logger.getLogger(this.getClass());

        // Initialize variables needed by SNMP
        MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
        this.errMsg = null;

        // determine SNMP version and create appropriate processor
        if (this.props.getProperty("version").equals("1")) {
            dispatcher.addMessageProcessingModel(new MPv1());
        } else if(this.props.getProperty("version").equals("3")) {
            dispatcher.addMessageProcessingModel(new MPv3());;
        } else {
            dispatcher.addMessageProcessingModel(new MPv2c());
        }
        // Listen for SNMP responses. Required to receive responses
        transport.listen();
        // Create snmp object
        this.snmp = new Snmp(dispatcher, transport);
    }

    /**
     * Applies initial SNMP settings.
     *
     * @param dst string representing hostname or IP or target
     */
    public void initializeSession(String dst) {
        // Initialize settings
        this.target = new CommunityTarget();
        InetAddress address = null;
        UdpAddress udpAddress = null;
        int snmpVersion = 0;
        int portNum = -1;
        int timeout = 0;
        int retries = 0;

        // Format address
        dst += this.props.getProperty("domainSuffix");
        try {
            address = InetAddress.getByName(dst);
        } catch (UnknownHostException e) {
            this.errMsg = "ERROR: SNMP destination "+ dst + " not defined";
            return;
        }
        // Determine SNMP version
        if (this.props.getProperty("version").equals("1")) {
            snmpVersion = SnmpConstants.version1;
        } else if(this.props.getProperty("version").equals("3")) {
            snmpVersion = SnmpConstants.version3;
        } else {
            snmpVersion = SnmpConstants.version2c;
        }

        // Apply community settings for session
        portNum = Integer.valueOf(this.props.getProperty("port"));
        udpAddress = new UdpAddress(address, portNum);
        this.target.setAddress(udpAddress);
        this.target.setCommunity(new OctetString(
                                    this.props.getProperty("community")));
        this.target.setVersion(snmpVersion);
        timeout = Integer.valueOf(this.props.getProperty("timeout"));
        this.target.setTimeout(timeout);
        retries = Integer.valueOf(this.props.getProperty("retries"));
        this.target.setRetries(retries);
    }

    /**
     * Closes SNMP session and frees resources.
     *
     * @throws IOException
     */
    public void closeSession() throws IOException {
        if (this.snmp != null) this.snmp.close();
    }

    /**
     * Runs SNMP queries.
     *
     * @param oid OID of request and request type
     * @return vector of VariableBinding objects
     * @throws IOException
     */
    public Vector querySNMP(OID oid, int pduType ) throws IOException {

        // Initial variables
        PDU pdu = null;
        PDU responsePDU = null;
        Vector bindings = null;
        ResponseEvent response = null;

        // determine type of PDU to send/receive
        if (this.target.getVersion() == SnmpConstants.version1) {
            pdu = new PDUv1();
        } else if (this.target.getVersion() == SnmpConstants.version3) {
            pdu = new ScopedPDU();
        } else {
            pdu = new PDU();
        }

        // set oid and pdu type for request
        pdu.add(new VariableBinding(oid));
        pdu.setType(pduType);

        // try to send SNMP request and receieve response
        response = this.snmp.send(pdu, this.target);
        responsePDU = response.getResponse();

        // Verify response received and that it returned success
        if ((responsePDU != null) && (responsePDU.getErrorStatus() == 
                     SnmpConstants.SNMP_ERROR_SUCCESS)) {
            bindings = responsePDU.getVariableBindings();
            return bindings;
        } else if (responsePDU == null) {
            // No response received. Provide error message if it exists.
            if (response.getError() != null) {
                this.errMsg = "ERROR: Cannot make SNMP query: " + response.getError().getMessage();
            } else {
                this.errMsg = "ERROR: Cannot make SNMP query: Unable to receive response";
            }
        } else {
            // Response received but indicated an error ocurred
            this.errMsg = "ERROR: SNMP uery returned an error: " + 
                          responsePDU.getErrorStatusText();
        }
        return null;
    }

    /**
     * Queries Juniper router for autonomous service number associated with
     *     IP address.
     *
     * @param  ipaddr string containing IP address
     * @return string containing AS number
     * @throws IOException
     */
    public String queryAsNumber(String ipaddr) throws IOException {
        // OID is for bgpPeerRemoteAs, concatenated with ipaddr
        OID oid = new OID("1.3.6.1.2.1.15.3.1.9." + ipaddr);

        // run a GET query
        Vector bindings = querySNMP(oid, PDU.GET);

        /* If returns, parse AS number, if not return null. errMsg already 
           will contain data from querySNMP. */
        if (bindings != null) {
            Variable asNum = 
                         ((VariableBinding)bindings.elementAt(0)).getVariable();
            this.log.debug("queryAsNumber: " + asNum.toString());
            return asNum.toString();
        }
        return null;
    }

    /**
     * Queries Juniper router for autonomous service number.
     *
     * @throws IOException
     */
    public void queryLSPSnmp() throws IOException {
        /* Do the bulkwalk of the 0 (default) non-repeaters, and the repeaters. 
           Ask for no more than 8 values per response packet.  If the caller 
           already knows how many instances will be returned for the repeaters,
           it can ask only for that many repeaters. */
        // TODO:  figure out mpls mib, this oid is for bgpPeerRemoteAs
        OID oid = new OID("1.3.6.1.2.1.15.3.1.9");

        // Run a GETBULK query.
        Vector bindings = querySNMP(oid, PDU.GETBULK);

        /* If returns, parse AS number, if not return null. errMsg already 
           will contain data from querySNMP. */
        if (bindings != null) {
            for (Enumeration e = bindings.elements(); e.hasMoreElements(); e.nextElement()) {
                Variable var = ((VariableBinding)e).getVariable();
                this.lspInfo.put(var.toString(), var); // may need to change
            }
        }
    }

    /**
     * Returns LSP information retrieved from SNMP query.
     *
     * @param lspName string containing name of LSP (optional)
     * @param lspVar string containing which OID value to return 
     *               (e.g. "mplsLspState") (optional)
     * @return lspInfoArray ArrayList containing the lspName.lspVar and value
     */
    public ArrayList queryLspInfo(String lspName, String lspVar) {
        ArrayList<String> lspNameArray = new ArrayList<String>();
        ArrayList<String> lspInfoArray = new ArrayList<String>();

        /* Figure out which LSP to pull info from.  If lspName is not 
           specified, grab all LSPs. */
        if (lspName != null) {
            lspNameArray.add(lspName);
        } else {
            Iterator i = this.lspInfo.keySet().iterator();
            while (i.hasNext()) {
                lspNameArray.add(i.next().toString());
            }
        }
        ListIterator li = lspNameArray.listIterator();
        while (li.hasNext()) {
            String lspNameElem = li.next().toString();
            if (!this.lsp.containsKey(lspNameElem)) {
                this.errMsg = "ERROR: No such LSP \"" + lspNameElem + "\"\n";
                return null;
            } else {
                if(lspVar != null) {
                    // TODO:  these if/else's need to be checked
                    if (!this.lspInfo.containsKey(this.lsp.get(lspNameElem))) {
                        this.errMsg = "No such LSP variable \"" + 
                                      lspNameElem + "." + lspVar + "\"\n";
                        return null;
                    } else {
                        lspInfoArray.add(lspNameElem + "." + lspVar);
                        lspInfoArray.add(this.lspInfo.get(
                            this.lsp.get(lspNameElem)).toString());
                    }
                 } else {
                     /* TODO: figue out exactly what the hashes are supposed 
                        to contain */
                     continue;
                 }
             }
        }
        return lspInfoArray;
    }

    /**
     * Returns errMsg property.
     *
     * @return string containing error message(s) if exist. null otherwise.
     */
    public String getError() {
        return this.errMsg;
    }
}
