package net.es.oscars.pss.connect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSConnectorConfigBean;

public class RancidConnector {
    public enum LOGIN {
        CLOGIN,
        ALULOGIN,
        JLOGIN,
    }
    private Logger log = Logger.getLogger(RancidConnector.class);
    private PSSConnectorConfigBean config = null;
    public RancidConnector(PSSConnectorConfigBean config) {
        this.config = config;
    }
    /**
     * Sends a configure command using RANCID to a router, and 
     * returns any output. Credentials are stored with RANCID.
     *
     * @param command string with command to send to router
     * @param router the router to send the command to
     * @throws PSSException
     */
    public String sendCommand(String command, String router, LOGIN login) throws PSSException {
        log.info("sendCommand.start "+router);
        if (config == null) {
            throw new PSSException("Null config");
        }
        String loginExec = "";
        if (login == null) {
            throw new PSSException("Null login parameter");
        } else if (login.equals(LOGIN.ALULOGIN)) {
            loginExec = "alulogin";
        } else if (login.equals(LOGIN.CLOGIN)) {
            loginExec = "clogin";
        } else if (login.equals(LOGIN.JLOGIN)) {
            loginExec = "jlogin";
        } 
        
        if (config.isLogRequest()) {
            this.log.info("\nCOMMAND:\n\n"+command);
        }
        
        String response = "";
        try {
            // a temp file 
            File tmpFile = File.createTempFile("oscars", "txt");
            String path = tmpFile.getAbsolutePath();
            BufferedWriter outputStream = new BufferedWriter(new FileWriter(tmpFile));
            // write command to temporary file
            outputStream.write(command);
            outputStream.close();
            

            
            this.log.info(loginExec+" -x " + path + " " + router);
            
            String cmd[] = { loginExec, "-x", path, router };
            BufferedReader cmdOutput;
            cmdOutput = this.runCommand(cmd);
            String outputLine = null;
            StringBuilder sb = new StringBuilder();
            while ((outputLine = cmdOutput.readLine()) != null) {
                sb.append(outputLine + "\n");
            }
            response = sb.toString();
            cmdOutput.close();
            tmpFile.delete();
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        }
        if (config.isLogResponse()) {
            this.log.info("\nRESPONSE:\n\n"+response);
        }
        log.info("sendCommand.finish "+router);
        return response;
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
        BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader cmdError  = new BufferedReader(new InputStreamReader(p.getErrorStream()));
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

}
