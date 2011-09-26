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

    private HttpURLConnection httpConn = null;
    private DataOutputStream httpOut = null;
    private BufferedReader httpIn = null;
    
    // OpenFlow NOX JSON connector configs
    private String noxUrl = "http://localhost:11122/ws.v1/OSCARS";
    
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
        if (params.get("noxUrl") != null) {
            this.noxUrl = (String)params.get("noxUrl");
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
            URL noxAddress = new URL(noxUrl);
            httpConn = (HttpURLConnection)noxAddress.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setReadTimeout(10000);
            //httpConn.connect();
            httpOut = new DataOutputStream(httpConn.getOutputStream());
            this.log.info("connected to NOX controller at " + noxUrl);
        } catch (UnknownHostException e) {
            httpConn = null;
            httpOut = null;
            this.log.error("exception when connecting to NOX : " + e.getMessage());
            throw new PSSException("Unknown NOX controller at " + noxUrl);
        } catch (IOException e) {
            httpConn = null;
            httpOut = null;
            this.log.error("exception when connecting to NOX controller at " + noxUrl+", IOException: " + e.getMessage());
            throw new PSSException("Failed to connect NOX controller at " + noxUrl+", IOException: " + e.getMessage());
        }
        this.log.debug(netLogger.end(event));
    }

    /**
     * Shut down gracefully.
     */
    private void disconnect() {
        String event = "JSONConnector.disconnect";
        try {
            if (httpOut != null) {
                httpOut.close();
                httpOut = null;
            }
            if (httpIn != null) {
                httpIn.close();
                httpIn = null;
            }
            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }
            this.log.info("Disconnected from NOX");
        } catch (IOException e) {
            httpConn = null;
            httpOut = null;
            httpIn = null;
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
            log.info("set to stub mode, the following command will not be sent to NOX:\n"+deviceCommand);
            return "{\"type\":\"oscars-reply\", \"status\":\"ACTIVE\", \"err_msg\":\"\"}";
        }
                
        String responseString = "";
        try {
            this.connect();
            if (httpConn == null)
                throw new PSSException("NOX socket connection not ready!");
            this.log.info("sending command to NOX: \n" + deviceCommand);
            httpOut.writeBytes(deviceCommand);
            httpOut.flush();
            httpIn = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line;
            while ((line = httpIn.readLine()) != null) {
                responseString += line;
            }
            this.log.info("received response: \n" + responseString);
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
