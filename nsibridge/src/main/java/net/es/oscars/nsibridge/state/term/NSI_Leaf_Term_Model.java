package net.es.oscars.nsibridge.state.term;


import net.es.oscars.nsibridge.ifces.NsiTermModel;



public class NSI_Leaf_Term_Model implements NsiTermModel {
    String connectionId = "";
    public NSI_Leaf_Term_Model(String connId) {
        connectionId = connId;
    }
    private NSI_Leaf_Term_Model() {}



    @Override
    public void doLocalTerm() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendNsiTermCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendNsiTermFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
