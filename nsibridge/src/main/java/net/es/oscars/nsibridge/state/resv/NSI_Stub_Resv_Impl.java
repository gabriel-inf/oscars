package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.ifces.NsiResvMdl;

import java.util.UUID;


public class NSI_Stub_Resv_Impl implements NsiResvMdl {
    String connectionId = "";
    public NSI_Stub_Resv_Impl(String connId) {
        connectionId = connId;
    }

    @Override
    public UUID localCheck() {
        return null;
    }

    @Override
    public UUID localHold() {
        return null;
    }

    @Override
    public UUID localCommit() {
        return null;
    }

    @Override
    public UUID localAbort() {
        return null;
    }

    @Override
    public UUID sendRsvCF() {
        return null;
    }

    @Override
    public UUID sendRsvFL() {
        return null;
    }

    @Override
    public UUID sendRsvCmtCF() {
        return null;
    }

    @Override
    public UUID sendRsvCmtFL() {
        return null;
    }

    @Override
    public UUID sendRsvAbtCF() {
        return null;
    }

    @Override
    public UUID sendRsvTimeout() {
        return null;
    }

}
