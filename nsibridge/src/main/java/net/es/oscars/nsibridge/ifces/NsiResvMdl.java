package net.es.oscars.nsibridge.ifces;


import java.util.UUID;

public interface NsiResvMdl {

    public UUID localCheck();
    public UUID localHold();
    public UUID localCommit();
    public UUID localAbort();
    public UUID localRollback();

    public UUID sendRsvCF();
    public UUID sendRsvFL();

    public UUID sendRsvCmtCF();
    public UUID sendRsvCmtFL();

    public UUID sendRsvAbtCF();

    public UUID sendRsvTimeout();


}
