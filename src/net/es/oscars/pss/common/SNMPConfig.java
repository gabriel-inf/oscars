package net.es.oscars.pss.common;

public class SNMPConfig {
    private Integer snmpVersion;
    private Integer port;
    private Integer retries;
    private Integer timeout;
    private String community;
    public Integer getSnmpVersion() {
        return snmpVersion;
    }
    public void setSnmpVersion(Integer snmpVersion) {
        this.snmpVersion = snmpVersion;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public Integer getRetries() {
        return retries;
    }
    public void setRetries(Integer retries) {
        this.retries = retries;
    }
    public Integer getTimeout() {
        return timeout;
    }
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    public String getCommunity() {
        return community;
    }
    public void setCommunity(String community) {
        this.community = community;
    }
    
}
