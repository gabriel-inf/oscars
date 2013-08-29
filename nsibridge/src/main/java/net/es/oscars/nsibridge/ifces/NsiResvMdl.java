package net.es.oscars.nsibridge.ifces;


import java.util.UUID;

public interface NsiResvMdl {

    public UUID localCheck(String correlationId);
    public UUID localHold(String correlationId);
    public UUID localCommit(String correlationId);
    public UUID localAbort(String correlationId);
    public UUID localRollback(String correlationId);

    public UUID sendRsvCF(String correlationId);
    public UUID sendRsvFL(String correlationId);

    public UUID sendRsvCmtCF(String correlationId);
    public UUID sendRsvCmtFL(String correlationId);

    public UUID sendRsvAbtCF(String correlationId);

    public UUID sendRsvTimeout(String correlationId);


}
