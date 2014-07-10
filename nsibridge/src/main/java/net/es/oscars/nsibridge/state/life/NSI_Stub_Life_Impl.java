package net.es.oscars.nsibridge.state.life;


import net.es.oscars.nsibridge.ifces.NsiLifeMdl;

import java.util.UUID;


public class NSI_Stub_Life_Impl implements NsiLifeMdl {
    String connectionId = "";
    public NSI_Stub_Life_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_Stub_Life_Impl() {}



    @Override
    public UUID localTerminate(String correlationId) {
        return null;
    }

    @Override
    public UUID localForcedEnd(String correlationId) {
        return null;
    }

    @Override
    public UUID localEndtime(String correlationId) {
        return null;
    }

    @Override
    public UUID localCancel(String correlationId) {
        return null;
    }

    @Override
    public UUID sendForcedEnd(String correlationId) {
        return null;
    }

    @Override
    public UUID sendTermCF(String correlationId) {
        return null;
    }


}
