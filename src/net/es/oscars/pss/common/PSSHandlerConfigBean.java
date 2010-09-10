package net.es.oscars.pss.common;

import java.util.Properties;

import org.apache.log4j.Logger;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;

public class PSSHandlerConfigBean {
    private static Logger log = Logger.getLogger(PSSHandlerConfigBean.class);

    /**
     * log the generated configuration or not
     */
    private boolean logConfig           = true;

    /**
     * stub mode: if true, no configuration will be sent to the routers
     * and all requests will be successful
     */
    private boolean stubMode            = true;

    /**
     * failure behavior: if set, the teardown config will be sent to the routers
     * if not set (i.e. for debugging purposes), the config will remain
     */
    private boolean teardownOnFailure   = true;

    /**
     * setup behavior: if true, generate a status check job afterwards
     */
    private boolean checkStatusAfterSetup       = true;

    /**
     * teardown behavior: if true, generate a status check job afterwards
     */
    private boolean checkStatusAfterTeardown    = true;
    
    private String templateDir = "";


    public static PSSHandlerConfigBean loadConfig(String propertyFile, String propertyGroup) throws PSSException {
        PSSHandlerConfigBean config = new PSSHandlerConfigBean();
        PropHandler propHandler = new PropHandler(propertyFile);
        Properties props = propHandler.getPropertyGroup(propertyGroup, true);
        if (props == null) {
            throw new PSSException("No PSS config");
        }
        String checkStatusAfterSetupProp    = (String) props.get("checkStatusAfterSetup");
        String checkStatusAfterTeardownProp = (String) props.get("checkStatusAfterTeardown");
        String logConfigProp                = (String) props.get("logConfig");
        String stubModeProp                 = (String) props.get("stubMode");
        String teardownOnFailureProp        = (String) props.get("teardownOnFailure");
        String templateDir                  = (String) props.get("templateDir");
        if (checkStatusAfterSetupProp == null) checkStatusAfterSetupProp = "false";
        if (checkStatusAfterTeardownProp == null) checkStatusAfterTeardownProp = "false";
        if (logConfigProp == null) logConfigProp = "false";
        if (stubModeProp == null) stubModeProp = "false";
        if (teardownOnFailureProp == null) teardownOnFailureProp = "false";
        if (templateDir == null) templateDir = "";
        
        checkStatusAfterSetupProp       = checkStatusAfterSetupProp.trim().toLowerCase();
        checkStatusAfterTeardownProp    = checkStatusAfterTeardownProp.trim().toLowerCase();
        logConfigProp                   = logConfigProp.trim().toLowerCase();
        stubModeProp                    = stubModeProp.trim().toLowerCase();
        teardownOnFailureProp           = teardownOnFailureProp.trim().toLowerCase();

        if (checkStatusAfterSetupProp.equals("true") || 
            checkStatusAfterSetupProp.equals("1")) {
            config.setCheckStatusAfterSetup(true);
        } else {
            config.setCheckStatusAfterSetup(false);
        }
        if (checkStatusAfterTeardownProp.equals("true") || 
            checkStatusAfterTeardownProp.equals("1")) {
            config.setCheckStatusAfterTeardown(true);
        } else {
            config.setCheckStatusAfterTeardown(false);
        }
        
        if (logConfigProp.equals("true") || logConfigProp.equals("1")) {
            config.setLogConfig(true);
        } else {
            config.setLogConfig(false);
        }
        if (stubModeProp.equals("true") || stubModeProp.equals("1")) {
            config.setStubMode(true);
        } else {
            config.setStubMode(false);
        }

        if (teardownOnFailureProp.equals("true") || teardownOnFailureProp.equals("1")) {
            config.setTeardownOnFailure(true);
        } else {
            config.setTeardownOnFailure(false);
        }

        config.setTemplateDir(templateDir);
        log.debug("templateDir: "+config.getTemplateDir());
        log.debug("checkStatusAfterSetup: "+config.isCheckStatusAfterSetup());
        log.debug("checkStatusAfterTeardown: "+config.isCheckStatusAfterTeardown());
        log.debug("teardownOnFailure: "+config.isTeardownOnFailure());
        log.debug("logConfig: "+config.isLogConfig());
        log.debug("stubMode: "+config.isStubMode());
        
        
        return config;
    }
    
    
    public boolean isLogConfig() {
        return logConfig;
    }

    public void setLogConfig(boolean logConfig) {
        this.logConfig = logConfig;
    }

    public boolean isStubMode() {
        return stubMode;
    }

    public void setStubMode(boolean stubMode) {
        this.stubMode = stubMode;
    }

    public boolean isTeardownOnFailure() {
        return teardownOnFailure;
    }

    public void setTeardownOnFailure(boolean teardownOnFailure) {
        this.teardownOnFailure = teardownOnFailure;
    }

    public boolean isCheckStatusAfterSetup() {
        return checkStatusAfterSetup;
    }

    public void setCheckStatusAfterSetup(boolean checkStatusAfterSetup) {
        this.checkStatusAfterSetup = checkStatusAfterSetup;
    }

    public boolean isCheckStatusAfterTeardown() {
        return checkStatusAfterTeardown;
    }

    public void setCheckStatusAfterTeardown(boolean checkStatusAfterTeardown) {
        this.checkStatusAfterTeardown = checkStatusAfterTeardown;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public String getTemplateDir() {
        return templateDir;
    }

}
