package net.es.oscars.pss.vendor.jnx;

import net.es.oscars.pss.vendor.VendorStatusResult;

public class JnxStatusResult implements VendorStatusResult {
    private String statusMsg;
    private String errorMsg;
    private boolean isUp;
    // Juniper-specific information; not part of interface
    private String connectionStatus;
    private String interfaceStatus;
    private String interfaceDescription;

    public JnxStatusResult() {
        this.errorMsg = "";
    }

    public boolean isCircuitUp() {
        return this.isUp;
    }

    public void setCircuitStatus(boolean isUp) {
        this.isUp = isUp;
    }

    public String getErrorMessage() {
        return this.errorMsg;
    }

    public void setErrorMessage(String msg) {
        this.errorMsg = msg;
    }

    public String getStatusMessage() {
        return this.statusMsg;
    }

    public void setStatusMessage(String msg) {
        this.statusMsg = msg;
    }

    protected String getConnectionStatus() {
        return this.connectionStatus;
    }

    protected void setConnectionStatus(String status) {
        this.connectionStatus = status;
    }

    protected String getInterfaceStatus() {
        return this.interfaceStatus;
    }

    protected void setInterfaceStatus(String status) {
        this.interfaceStatus = status;
    }

    protected String getInterfaceDescription() {
        return this.interfaceDescription;
    }

    protected void setInterfaceDescription(String descr) {
        this.interfaceDescription = descr;
    }
}
