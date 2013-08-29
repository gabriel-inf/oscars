package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.ifces.NsiProvMdl;

import java.util.UUID;


public class NSI_Stub_Prov_Impl implements NsiProvMdl {
    String connectionId = "";
    public NSI_Stub_Prov_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_Stub_Prov_Impl() {}



    @Override
    public UUID localProv(String correlationId) {
        return null;
    }

    @Override
    public UUID sendProvCF(String correlationId) {
        return null;
    }

    @Override
    public UUID notifyProvFL(String correlationId) {
        return null;
    }

    @Override
    public UUID localRel(String correlationId) {
        return null;
    }

    @Override
    public UUID sendRelCF(String correlationId) {
        return null;
    }

    @Override
    public UUID notifyRelFL(String correlationId) {
        return null;
    }
}
