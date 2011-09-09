package net.es.oscars.pss.openflow.nox;


import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import net.es.oscars.logging.*;


import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.config.ConfigHolder;

import org.ogf.schema.network.topology.ctrlplane.*;

/**
 * a connector for DRAGON VLSR using CLI
 * 
 * @author xi
 *
 */

public class JSONConnector implements Connector {
    private Logger log = Logger.getLogger(JSONConnector.class);
    private OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();

    private Socket socketConn = null;
    private PrintWriter socketOut = null;
    private InputStreamReader socketIn = null;
    
    // OpenFlow NOX JSON connector configs
    private String noxHost = "localhost";
    private int noxPort = 11122;
    private boolean useSsl = false;
    private String messageVersion = "1.0";

    
    public JSONConnector() {
        netLogger.init(ModuleName.PSS, "0000");
    }

    public void setConfig(GenericConfig connectorConfig) throws PSSException {
        if (connectorConfig == null) {
            throw new PSSException("no config set!");
        } else if (connectorConfig.getParams() == null) {
            throw new PSSException("login null ");
        }

        HashMap<String, Object> params = connectorConfig.getParams();
        if (params.get("noxHost") != null) {
            this.noxHost = (String)params.get("noxHost");
        }
        if (params.get("noxPort") != null) {
            this.noxPort = (Integer)params.get("noxPort");
        }
        if (params.get("useSSL") != null && ((Boolean)params.get("useSSL"))) {
            this.useSsl = true;
        }
        if (params.get("messageVersion") != null) {
            this.messageVersion = (String)params.get("messageVersion");
        }
    }

    /**
     *  @throws IOException
     *  @throws PSSException
     */
    private void connect() throws IOException, PSSException {
        String event = "JSONConnector.connect";
        this.log.debug(netLogger.start(event));
        try {
            socketConn = new Socket(noxHost, noxPort);
            socketOut = new PrintWriter(socketConn.getOutputStream(), true);
            socketIn = new InputStreamReader(socketConn.getInputStream());
            this.log.debug("connected to NOX host " + noxHost + " on port " 
                    + Integer.toString(noxPort));
        } catch (UnknownHostException e) {
            socketConn = null;
            socketOut = null;
            socketIn = null;
            this.log.error("exception when connecting to NOX : " + e.getMessage());
            throw new PSSException("Unknown NOX host " + noxHost);
        } catch (IOException e) {
            socketConn = null;
            socketOut = null;
            socketIn = null;
            this.log.error("exception when connecting to NOX : " + e.getMessage());
            throw new PSSException("failed to connect NOX "+noxHost+" on port "+Integer.toString(noxPort)+": " + e.getMessage());
        }
        this.log.debug(netLogger.end(event));
    }

    /**
     * Shut down gracefully.
     */
    private void disconnect() {
        String event = "JSONConnector.disconnect";
        try {
            socketOut.close();
            socketOut = null;
            socketIn.close();
            socketIn = null;
            socketConn.close();
            socketConn = null;
            this.log.debug("Disconnected from NOX");
        } catch (IOException e) {
            socketConn = null;
            socketOut = null;
            socketIn = null;
            this.log.error("Exception when discconnecting from NOX : " + e.getMessage());
        }
        this.log.debug(netLogger.end(event));
    }

    /**
     * Sends the CLI commands to VLSR -- Unused in DRAGON JSONConnector
     * @param PSSCommand
\    * @throws PSSException
     */
    public String sendCommand(PSSCommand command) throws PSSException {
        String event = "JSONConnector.sendCommand";
        this.log.debug(netLogger.start(event));
        
        String deviceCommand = command.getDeviceCommand();
        if (deviceCommand == null) {
            throw new PSSException("null device command");
        }
        log.debug("sendCommand: "+deviceCommand);
        if (ConfigHolder.getInstance().getBaseConfig().getCircuitService().isStub()) {
            log.debug("set to stub mode: command will not be sent to NOX.");
            return "{\"type\":\"oscars-reply\", \"status\":\"ACTIVE\", \"err_msg\":\"\"}";
        }
                
        String responseString = "";
        try {
            this.connect();
            if (socketConn == null || socketConn.isConnected())
                throw new PSSException("NOX socket connection not ready!");
            this.log.debug("sending command to NOX...");
            socketOut.print(deviceCommand);
            socketOut.flush();
            char ch;
            while((ch = (char)socketIn.read()) > 0) {
                responseString += ch;
                if (ch == '}')
                    break;
            }
        } catch (IOException e) {
            log.error("exception when sending command to NOX, msg=" + e.getMessage());
            this.disconnect();
            throw new PSSException("exception when sending command to NOX, msg=" + e.getMessage());
        } finally {
            this.disconnect();
        }
        this.log.debug(netLogger.end(event));
        return responseString;
    }
}
