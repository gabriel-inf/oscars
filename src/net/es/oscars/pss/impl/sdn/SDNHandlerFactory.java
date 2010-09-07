package net.es.oscars.pss.impl.sdn;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.eompls.EoMPLSHandlerFactory;
import net.es.oscars.pss.l2vpn.L2VPNHandlerFactory;
import net.es.oscars.pss.layer3.Layer3HandlerFactory;
import net.es.oscars.pss.sw.SWHandlerFactory;

public class SDNHandlerFactory {
    public static PSSHandler getHandler(RouterVendor rv, SDNService service) throws PSSException {
        if (service.equals(SDNService.EOMPLS)) {
            return EoMPLSHandlerFactory.getHandler(rv);
        } else if (service.equals(SDNService.SWITCHED)) {
            return L2VPNHandlerFactory.getHandler(rv);
        } else if (service.equals(SDNService.L2VPN)) {
            return SWHandlerFactory.getHandler(rv);
        } else if (service.equals(SDNService.LAYER3)) {
            return Layer3HandlerFactory.getHandler(rv);
        } else {
            throw new PSSException("unsupported vendor / service combination");
        }

    }
}
