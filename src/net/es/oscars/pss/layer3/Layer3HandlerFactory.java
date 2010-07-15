package net.es.oscars.pss.layer3;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.layer3.junos.Layer3_Junos;

public class Layer3HandlerFactory {
    public static PSSHandler getHandler(RouterVendor rv) throws PSSException {
        if (rv.equals(RouterVendor.JUNIPER)) {
            return new Layer3_Junos();
        } else {
            throw new PSSException("Unsupported router vendor: "+rv);
        }
    }
}
