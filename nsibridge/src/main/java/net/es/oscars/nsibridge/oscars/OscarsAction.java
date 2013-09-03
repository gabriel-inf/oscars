package net.es.oscars.nsibridge.oscars;

import java.util.UUID;

public class OscarsAction {
    private String connId;
    private OscarsOps op;
    private UUID id;

    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    public OscarsOps getOp() {
        return op;
    }

    public void setOp(OscarsOps op) {
        this.op = op;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
