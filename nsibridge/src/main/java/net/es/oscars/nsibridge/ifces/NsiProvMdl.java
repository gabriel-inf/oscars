package net.es.oscars.nsibridge.ifces;


import java.util.UUID;

public interface NsiProvMdl {

    public UUID localProv(String correlationId);
    public UUID localRel(String correlationId);

    public UUID sendProvCF(String correlationId);
    public UUID notifyProvFL(String correlationId);

    public UUID sendRelCF(String correlationId);
    public UUID notifyRelFL(String correlationId);

    public UUID dataplaneUpdate(String correlationId);

    public UUID localEndtime(String correlationId);

}
