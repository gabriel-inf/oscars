package net.es.oscars.pss.sw;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.sw.junos.SW_Junos;

public class SWHandlerFactory {
    public static PSSHandler getHandler(RouterVendor rv) throws PSSException {
        if (rv.equals(RouterVendor.JUNIPER)) {
            return new SW_Junos();
        } else {
            throw new PSSException("Unsupported router vendor: "+rv);
        }
    }
}
