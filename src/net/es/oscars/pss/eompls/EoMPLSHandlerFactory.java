package net.es.oscars.pss.eompls;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.eompls.ios.EoMPLS_IOS;
import net.es.oscars.pss.eompls.junos.EoMPLS_Junos;

public class EoMPLSHandlerFactory {
    public static PSSHandler getHandler(RouterVendor rv) throws PSSException {
        if (rv.equals(RouterVendor.CISCO)) {
            return new EoMPLS_IOS();
        } else if (rv.equals(RouterVendor.JUNIPER)) {
            return EoMPLS_Junos.getInstance();
        } else {
            throw new PSSException("Unknown router vendor: "+rv);
        }
    }
}
