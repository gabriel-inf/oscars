package net.es.oscars.nsibridge.state;


import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.ifces.ProviderMDL;
import net.es.oscars.nsibridge.prov.CoordHolder;

import net.es.oscars.nsibridge.prov.NSI_OSCARS_Translation;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.utils.soap.OSCARSServiceException;


public class LeafProviderModel implements ProviderMDL {
    String connectionId = "";
    public LeafProviderModel(String connId) {
        connectionId = connId;
    }
    private  LeafProviderModel() {}


    @Override
    public void cleanup() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void activate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void release() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendResvRQ() {
        ResvRequest req = RequestHolder.getInstance().findResvRequest(connectionId);
        ResCreateContent rc;
        if (req == null) {
            rc = new ResCreateContent();

        } else {
            rc = NSI_OSCARS_Translation.makeOscarsResv(req);
        }

        try {
            CoordHolder.getInstance().sendCreate(rc);
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void sendResvCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendResvFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendActCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendActFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRelRQ() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRelCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRelFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendProvRQ() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendProvCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendProvFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }



}
