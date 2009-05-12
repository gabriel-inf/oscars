package net.es.oscars.client.improved;

import java.util.Map;

public abstract class ImprovedClient {

    public static final String DEFAULT_SOAP_CONFIG_FILE = "soap.yaml";
    public static final String DEFAULT_SOAP_CONFIG_ID = "default";

    protected String soapConfigFile;
    @SuppressWarnings("unchecked")
    protected Map soapConfig;

    protected String wsdlUrl;
    protected String repoDir;
    protected String rampConfigFilename;


    protected String configFile;
    @SuppressWarnings("unchecked")
    protected Map config;


    public void configureSoap() {
        this.configureSoap(DEFAULT_SOAP_CONFIG_ID);
    }

    @SuppressWarnings("unchecked")
    public void configureSoap(String soapConfId) {
        if (soapConfigFile == null) {
            soapConfigFile = DEFAULT_SOAP_CONFIG_FILE;
        }

        ConfigHelper cfg = ConfigHelper.getInstance();
        soapConfig = cfg.getConfiguration(soapConfigFile);

        assert soapConfig != null : "Could not load soap configuration from file: "+ soapConfigFile+"\nconfig:\n"+soapConfig.toString();
        Map soap = (Map) soapConfig.get("soap");
        assert soap != null : "No SOAP configuration stanza at file: "+ soapConfigFile+"\nconfig:\n"+soapConfig.toString();
        Map soapConf = (Map) soap.get(soapConfId);
        assert soapConf != null : "No specific SOAP configuration for id: "+soapConfId+ " at file: "+ soapConfigFile+"\nconfig:\n"+soapConfig.toString();
        wsdlUrl = (String) soapConf.get("wsdlUrl");
        assert wsdlUrl != null : "No WSDL URL defined";
        repoDir = (String) soapConf.get("repoDir");
        assert repoDir != null : "No repo directory defined";
        rampConfigFilename = (String) soapConf.get("rampConfigFname");
        assert rampConfigFilename != null : "No rampart config filename defined";
        PWCallback.rampConfigFname = rampConfigFilename;
    }


    @SuppressWarnings("unchecked")
    public Map getSoapConfig() {
        return soapConfig;
    }


    @SuppressWarnings("unchecked")
    public void setSoapConfig(Map soapConfig) {
        this.soapConfig = soapConfig;
    }


    public String getConfigFile() {
        return configFile;
    }


    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }


    @SuppressWarnings("unchecked")
    public Map getConfig() {
        return config;
    }


    @SuppressWarnings("unchecked")
    public void setConfig(Map config) {
        this.config = config;
    }


    public String getSoapConfigFile() {
        return soapConfigFile;
    }


    public void setSoapConfigFile(String soapConfigFile) {
        this.soapConfigFile = soapConfigFile;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getRepoDir() {
        return repoDir;
    }

    public void setRepoDir(String repoDir) {
        this.repoDir = repoDir;
    }


    public String getRampConfigFilename() {
        return rampConfigFilename;
    }


    public void setRampConfigFilename(String rampConfigFilename) {
        this.rampConfigFilename = rampConfigFilename;
    }


}
