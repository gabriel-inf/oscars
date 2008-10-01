package net.es.oscars.pss.vendor;

public interface VendorStatusResult {

    public boolean isCircuitUp();
    public void setCircuitStatus(boolean isActive);
    public String getErrorMessage();
    public void setErrorMessage(String msg);
    public String getStatusMessage();
    public void setStatusMessage(String msg);
}
