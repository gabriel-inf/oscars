package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.ifces.NsiResvMdl;

import java.util.UUID;


public class NSI_Stub_Resv_Impl implements NsiResvMdl {
    String connectionId = "";
    public NSI_Stub_Resv_Impl(String connId) {
        connectionId = connId;
    }

    @Override
    public UUID localCheck(String correlationId) {
        return null;
    }

    @Override
    public UUID localHold(String correlationId) {
        return null;
    }

    @Override
    public UUID localCommit(String correlationId) {
        return null;
    }

    @Override
    public UUID localTimeout(String correlationId) {
        return null;
    }

    @Override
    public UUID localAbort(String correlationId) {
        return null;
    }

    @Override
    public UUID localRollback(String correlationId) {
        return null;
    }

    @Override
    public UUID sendRsvCF(String correlationId) {
        return null;
    }

    @Override
    public UUID sendRsvFL(String correlationId) {
        return null;
    }

    @Override
    public UUID sendRsvCmtCF(String correlationId) {
        return null;
    }

    @Override
    public UUID sendRsvCmtFL(String correlationId) {
        return null;
    }

    @Override
    public UUID sendRsvAbtCF(String correlationId) {
        return null;
    }

    @Override
    public UUID sendRsvTimeout(String correlationId) {
        return null;
    }

}
