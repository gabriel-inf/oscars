package net.es.oscars.pss.common;

import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Vector;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import org.apache.log4j.Logger;


/**
 * SNMP contains methods dealing with SNMP queries.
 *
 * @author Andrew Lake, Jason Lee, David Robertson
 */
public class SNMP {
    private Snmp snmp;
    private CommunityTarget target;
    private Logger log;
    private SNMPConfig config;

    public SNMP() throws PSSException {
        this.log = Logger.getLogger(this.getClass());

        this.config = this.loadConfig();
        
        // Initialize variables needed by SNMP
        try {
            MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            Integer snmpVersion = config.getSnmpVersion();
            // determine SNMP version and create appropriate processor
            if (snmpVersion == 1) {
                dispatcher.addMessageProcessingModel(new MPv1());
            } else if(snmpVersion == 3) {
                dispatcher.addMessageProcessingModel(new MPv3());;
            } else {
                dispatcher.addMessageProcessingModel(new MPv2c());
            }
            transport.listen();
            this.snmp = new Snmp(dispatcher, transport);
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        }

        // Listen for SNMP responses. Required to receive responses
        // Create snmp object
    }

    private SNMPConfig loadConfig() throws PSSException {
        SNMPConfig config = new SNMPConfig();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("snmp", true);
        if (props == null) {
            throw new PSSException("No SNMP config");
        }
        
        Integer version     = Integer.valueOf(props.getProperty("version"));
        Integer port        = Integer.valueOf(props.getProperty("port"));
        Integer timeout     = Integer.valueOf(props.getProperty("timeout"));
        Integer retries     = Integer.valueOf(props.getProperty("retries"));
        String community    = props.getProperty("comunity")
        ;
        config.setCommunity(community);
        config.setPort(port);
        config.setRetries(retries);
        config.setSnmpVersion(version);
        config.setTimeout(timeout);
        return config;
    }

    /**
     * Applies initial SNMP settings.
     *
     * @param dst string representing complete hostname or IP of target
     */
    public void initializeSession(String dst) throws PSSException {
        this.log.info("initializeSession.start");
        // Initialize settings
        this.target = new CommunityTarget();
        InetAddress address = null;
        UdpAddress udpAddress = null;
        Integer snmpVersion;
        
        // Format address
        try {
            address = InetAddress.getByName(dst);
        } catch (UnknownHostException e) {
            this.log.error("Could not determine address for ["+dst+"]");
            throw new PSSException(e.getMessage());
        }
        this.log.debug("Node address: "+address.getCanonicalHostName());
        // Determine SNMP version
        if (this.config.getSnmpVersion() == 1) {
            snmpVersion = SnmpConstants.version1;
        } else if(this.config.getSnmpVersion() == 3) {
            snmpVersion = SnmpConstants.version3;
        } else {
            snmpVersion = SnmpConstants.version2c;
        }

        // Apply community settings for session
        udpAddress = new UdpAddress(address, config.getPort());
        
        this.target.setAddress(udpAddress);
        this.target.setCommunity(new OctetString(config.getCommunity()));
        this.target.setVersion(snmpVersion);
        this.target.setTimeout(config.getTimeout());
        this.target.setRetries(config.getRetries());
        this.log.debug("initializeSession.end");
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
     * @throws PSSException 
     */
    @SuppressWarnings("rawtypes")
    public Vector querySNMP(OID oid, int pduType ) throws IOException, PSSException {

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

        // Verify response 
        if (responsePDU == null) {
            // No response received. Provide error message if it exists.
            if (response.getError() != null) {
                log.error( "No response to SNMP query. Error: " + response.getError().getMessage());
            } else {
                log.error( "No response to SNMP query. Unknown error.");
            }
            throw new PSSException(response.getError().getMessage());

        } else if ((responsePDU.getErrorStatus() == SnmpConstants.SNMP_ERROR_SUCCESS)) {
            // everything ok
            bindings = responsePDU.getVariableBindings();
            return bindings;
        } else {
            // Response received but indicated an error occurred
            throw new PSSException( "SNMP query error: " + responsePDU.getErrorStatusText());
        }
    }

    /**
     * Queries Juniper router for autonomous service number associated with
     *     IP address.
     *
     * @return string containing sysDescr
     * @throws IOException
     * @throws PSSException
     */
    @SuppressWarnings("rawtypes")
    public String queryRouterType() throws PSSException {
        Variable description = null;
        // sysDescr
        OID oid = new OID("1.3.6.1.2.1.1.1.0");
        Vector bindings;
        try {
            bindings = this.querySNMP(oid, PDU.GET);
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        }
        if (bindings != null) {
            description = ((VariableBinding) bindings.elementAt(0)).getVariable();
        } else {
            throw new PSSException("Empty SNMP response");
        }
        return description.toString();
    }


}
