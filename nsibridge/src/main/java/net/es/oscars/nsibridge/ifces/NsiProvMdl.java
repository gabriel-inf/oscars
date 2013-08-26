package net.es.oscars.nsibridge.ifces;


import java.util.UUID;

public interface NsiProvMdl {

    public UUID localProv();
    public UUID localRel();

    public UUID sendProvCF();
    public UUID notifyProvFL();

    public UUID sendRelCF();
    public UUID notifyRelFL();


}
