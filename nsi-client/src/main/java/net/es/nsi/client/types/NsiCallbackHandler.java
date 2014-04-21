package net.es.nsi.client.types;

public interface NsiCallbackHandler {

    public boolean isConfirmed();
    public boolean isCommitted();
    public boolean isProvisioned();

    public void setConfirmed(boolean confirmed);
    public void setCommitted(boolean committed);
    public void setProvisioned(boolean provisioned);

    public int getVersion();
    public void setVersion(int version);

    public String getConnectionId();
    public void setConnectionId(String connectionId);

}
