package net.es.oscars.pss.common;

import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;

public class PSSConnectorConfigBean {
    private String login        = null;
    private String keystore     = null;
    private String passphrase   = null;
    private boolean logRequest  = false;
    private boolean logResponse = false;



    public static PSSConnectorConfigBean loadConfig(String propertyFile, String propertyGroup) throws PSSException {
        PSSConnectorConfigBean config = new PSSConnectorConfigBean();
        
        PropHandler propHandler = new PropHandler(propertyFile);
        Properties props = propHandler.getPropertyGroup(propertyGroup, true);
        if (props == null) {
            throw new PSSException("No PSS config");
        }
        String loginProp        = (String) props.get("login");
        String keystoreProp     = (String) props.get("keystore");
        String passphraseProp   = (String) props.get("passphrase");
        String logRequestProp   = (String) props.get("logRequest");
        String logResponseProp  = (String) props.get("logResponse");
        if (logRequestProp == null)     logRequestProp = "false";
        if (logResponseProp == null)    logResponseProp = "false";
        if (passphraseProp == null)     passphraseProp = "";
        if (keystoreProp == null)       keystoreProp = "";
        if (loginProp == null)          loginProp = "";
        
        logRequestProp  = logRequestProp.trim().toLowerCase();
        logResponseProp = logResponseProp.trim().toLowerCase();
        passphraseProp  = passphraseProp.trim();
        keystoreProp    = keystoreProp.trim();
        loginProp       = loginProp.trim();

        if (logRequestProp.equals("true") || 
                logRequestProp.equals("1")) {
            config.setLogRequest(true);
        } else {
            config.setLogRequest(false);
        }
        if (logResponseProp.equals("true") || 
                logResponseProp.equals("1")) {
            config.setLogResponse(true);
        } else {
            config.setLogResponse(false);
        }
        
        config.setLogin(loginProp);
        config.setKeystore(keystoreProp);
        config.setPassphrase(passphraseProp);

        return config;
    }



    public String getLogin() {
        return login;
    }



    public void setLogin(String login) {
        this.login = login;
    }



    public String getKeystore() {
        return keystore;
    }



    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }



    public String getPassphrase() {
        return passphrase;
    }



    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }



    public boolean isLogRequest() {
        return logRequest;
    }



    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }



    public boolean isLogResponse() {
        return logResponse;
    }



    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }
    
}
