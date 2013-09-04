package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import org.apache.log4j.Logger;

import java.util.UUID;


public class NSI_Stub_Prov_Impl implements NsiProvMdl {
    String connectionId = "";
    public NSI_Stub_Prov_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_Stub_Prov_Impl() {}

    private static final Logger log = Logger.getLogger(NSI_Stub_Prov_Impl.class);


    @Override
    public UUID localProv(String correlationId) {
        log.debug("local prov stub");
        return null;
    }

    @Override
    public UUID sendProvCF(String correlationId) {
        log.debug("local prov stub");
        return null;
    }

    @Override
    public UUID notifyProvFL(String correlationId) {
        log.debug("local prov stub");
        return null;
    }

    @Override
    public UUID localRel(String correlationId) {
        log.debug("local prov stub");
        return null;
    }

    @Override
    public UUID sendRelCF(String correlationId) {
        log.debug("local prov stub");
        return null;
    }

    @Override
    public UUID notifyRelFL(String correlationId) {
        log.debug("local prov stub");
        return null;
    }
    @Override
    public UUID dataplaneUpdate(String correlationId) {
        return null;
    }

}
