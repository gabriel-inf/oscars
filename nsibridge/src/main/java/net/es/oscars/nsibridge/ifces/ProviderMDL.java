package net.es.oscars.nsibridge.ifces;

/**
 * @haniotak Date: 2012-08-08
 */
public interface ProviderMDL {




    public void cleanup();

    public void activate();

    public void release();


    public void sendResvRQ();

    public void sendResvCF();

    public void sendResvFL();

    public void sendActCF();

    public void sendActFL();


    public void sendRelRQ();

    public void sendRelCF();

    public void sendRelFL();

    public void sendProvRQ();

    public void sendProvCF();

    public void sendProvFL();

}
