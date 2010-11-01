package net.es.oscars.pss.l2vpn;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.l2vpn.junos.L2VPN_Junos;

public class L2VPNHandlerFactory {
    public static PSSHandler getHandler(RouterVendor rv) throws PSSException {
if (rv.equals(RouterVendor.JUNIPER)) {
            return L2VPN_Junos.getInstance();
        } else {
            throw new PSSException("Unsupported router vendor: "+rv);
        }
    }
}
