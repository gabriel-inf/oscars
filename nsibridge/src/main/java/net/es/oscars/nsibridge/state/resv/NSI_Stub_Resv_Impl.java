package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.ifces.NsiResvMdl;


public class NSI_Stub_Resv_Impl implements NsiResvMdl {
    String connectionId = "";
    public NSI_Stub_Resv_Impl(String connId) {
        connectionId = connId;
    }

    @Override
    public void localCheck() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void localHold() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void localCommit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void localAbort() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvCmtCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvCmtFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvAbtCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvTimeout() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
