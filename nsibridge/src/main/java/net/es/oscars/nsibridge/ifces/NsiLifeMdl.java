package net.es.oscars.nsibridge.ifces;

import java.util.UUID;

public interface NsiLifeMdl {

    public UUID localTerm();

    public UUID sendTermCF();

    public UUID notifyForcedEndErrorEvent();



}
