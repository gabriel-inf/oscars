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
    public UUID localTerm() {
        return null;
    }

    @Override
    public UUID sendTermCF() {
        return null;
    }


    @Override
    public UUID notifyForcedEndErrorEvent() {
        return null;
    }
}
