package net.es.oscars.nsibridge.ifces;

import java.util.UUID;

public interface NsiLifeMdl {

    public UUID localForcedEnd(String correlationId);

    public UUID localTerminate(String correlationId);

    public UUID localEndtime(String correlationId);

    public UUID localCancel(String correlationId);

    public UUID sendTermCF(String correlationId);

    public UUID sendForcedEnd(String correlationId);




}
