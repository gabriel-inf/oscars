package net.es.oscars.pss.eompls.util;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.beans.config.LoopbackConfig;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.dao.SRLUtils;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;

import java.util.List;

public class VPLS_DeviceLoopback {
    private static Logger log = Logger.getLogger(VPLS_DeviceLoopback.class);

    protected String vplsLoopback;

    public String getVplsLoopback() {
        return vplsLoopback;
    }

    public void setVplsLoopback(String vplsLoopback) {
        this.vplsLoopback = vplsLoopback;
    }


    public static VPLS_DeviceLoopback reserve(String gri, String deviceId) throws PSSException {

        LoopbackConfig lc = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getLoopback();
        String cidr = lc.getCidr();
        Ipv4AddressRange ar = new Ipv4AddressRange(cidr);
        int range = ar.getRange();
        int max = range - 1;

        String rangeExpr = "1-"+max;



        VPLS_DeviceLoopback devIds = new VPLS_DeviceLoopback();


        String newGri = gri+":"+deviceId;
        String loopbackScope = PathTools.getLocalDomainId() +":vpls-loopback";

        Integer offset = SRLUtils.getIdentifier(loopbackScope, newGri, 1, rangeExpr);

        String loopback = ar.getAddressInRange(offset);
        log.debug("VPLS loopback is: " + loopback+ " offset: "+offset);
        devIds.setVplsLoopback(loopback);
        return devIds;
    }



    public static VPLS_DeviceLoopback release(String gri, String deviceId) throws PSSException {
        String newGri = gri+":"+deviceId;
        String loopbackScope = PathTools.getLocalDomainId() + ":vpls-loopback";

        List<Integer> ids = SRLUtils.releaseIdentifiers(loopbackScope, newGri);


        VPLS_DeviceLoopback devIds = new VPLS_DeviceLoopback();
        Integer offset = ids.get(0);
        LoopbackConfig lc = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getLoopback();
        String cidr = lc.getCidr();
        Ipv4AddressRange ar = new Ipv4AddressRange(cidr);


        String loopback = ar.getAddressInRange(offset);
        devIds.setVplsLoopback(loopback);

        return devIds;
    }


}
