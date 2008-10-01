package net.es.oscars.pss.vendor.cisco;

import net.es.oscars.pss.vendor.VendorStatusResult;

public class CiscoStatusResult implements VendorStatusResult {
    private String statusMsg;
    private String errorMsg;
    private boolean isUp;

    public CiscoStatusResult() {
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
}
