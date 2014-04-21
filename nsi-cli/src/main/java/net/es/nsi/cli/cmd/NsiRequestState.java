package net.es.nsi.cli.cmd;

import net.es.nsi.client.types.NsiCallbackHandler;

public class NsiRequestState implements NsiCallbackHandler {
    protected boolean confirmed = false;
    protected boolean provisioned = false;
    protected boolean committed = false;
    protected int version = 0;
    protected String connectionId = "UNKNOWN";

    @Override
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public void setCommitted(boolean committed) {
        this.committed = committed;

    }

    @Override
    public void setProvisioned(boolean provisioned) {
        this.provisioned = provisioned;

    }

    @Override
    public boolean isConfirmed() { return confirmed; }
    @Override
    public boolean isCommitted() { return committed; }
    @Override
    public boolean isProvisioned() { return provisioned; }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;

    }

    @Override
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    @Override
    public String getConnectionId() {
        return connectionId;
    }


}
