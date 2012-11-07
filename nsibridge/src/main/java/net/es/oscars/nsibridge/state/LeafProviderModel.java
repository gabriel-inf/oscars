package net.es.oscars.nsibridge.state;


import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.nsibridge.ifces.ProviderMDL;
import net.es.oscars.nsibridge.prov.CoordHolder;

import net.es.oscars.utils.soap.OSCARSServiceException;



/**
 * @haniotak Date: 2012-08-08
 */
public class LeafProviderModel implements ProviderMDL {


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
        ResCreateContent rc = new ResCreateContent();
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
