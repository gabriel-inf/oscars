package net.es.oscars.nsibridge.ifces;

import java.util.UUID;

public interface NsiLifeMdl {

    public UUID localTerm(String correlationId);

    public UUID sendTermCF(String correlationId);

    public UUID notifyForcedEndErrorEvent(String correlationId);



}
