package net.es.oscars.nsibridge.state.life;


import net.es.oscars.nsibridge.ifces.NsiLifeMdl;


public class NSI_Stub_Life_Impl implements NsiLifeMdl {
    String connectionId = "";
    public NSI_Stub_Life_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_Stub_Life_Impl() {}



    @Override
    public void localTerm() {
    }

    @Override
    public void sendTermCF() {
    }


    @Override
    public void notifyForcedEndErrorEvent() {

    }
}
