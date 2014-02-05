package net.es.oscars.pss.eompls.util;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.api.VplsImplementation;
import net.es.oscars.pss.eompls.api.VplsV2ConfigGenerator;
import net.es.oscars.pss.util.ConnectorUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VPLS_RequestParams {
    private Logger log = Logger.getLogger(VPLS_RequestParams.class);
    private HashMap<String, VPLS_DeviceLoopback> loopbackMap = new HashMap<String, VPLS_DeviceLoopback>();
    private HashMap<String, VplsImplementation> implementationMap = new HashMap<String, VplsImplementation>();
    private VPLS_Identifier vplsId;
    private VPLS_ServiceParams params;

    public HashMap<String, VPLS_DeviceLoopback> getLoopbackMap() {
        return loopbackMap;
    }

    public HashMap<String, VplsImplementation> getImplementationMap() {
        return implementationMap;
    }

    public VPLS_Identifier getVplsId() {
        return vplsId;
    }

    public VPLS_ServiceParams getParams() {
        return params;
    }



    public void reserve(String gri, ResDetails res, boolean may_need_secondary_vpls) throws PSSException {
        log.debug("gri: "+gri+" - may need sec? "+may_need_secondary_vpls);

        params = VPLS_ServiceParams.fromResDetails(res);
        boolean needs_secondary_vpls_id = false;
        if (params.isProtection() && may_need_secondary_vpls) {
            needs_secondary_vpls_id = true;
        }
        vplsId = VPLS_Identifier.reserve(gri, needs_secondary_vpls_id);


        List<String> deviceIds = new ArrayList<String>();
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        deviceIds.add(srcDeviceId);
        if (!srcDeviceId.equals(dstDeviceId)) deviceIds.add(dstDeviceId);

        loopbackMap = this.reserveVplsLoopbacks(gri, deviceIds);

    }


    public void release(String gri, ResDetails res) throws PSSException {

        params = VPLS_ServiceParams.fromResDetails(res);
        vplsId = VPLS_Identifier.release(gri);


        List<String> deviceIds = new ArrayList<String>();
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        deviceIds.add(srcDeviceId);
        if (!srcDeviceId.equals(dstDeviceId)) deviceIds.add(dstDeviceId);

        loopbackMap = this.releaseVplsLoopbacks(gri, deviceIds);

        implementationMap = this.getImplementationMap(deviceIds);
    }

    public static HashMap<String, VplsImplementation> getImplementationMap(List<String> deviceIds) throws PSSException {
        HashMap<String, VplsImplementation> impl = new HashMap<String, VplsImplementation>();
        for (String deviceId : deviceIds) {
            DeviceConfigGenerator cg = ConnectorUtils.getDeviceConfigGenerator(deviceId, "eompls");
            if (cg instanceof VplsV2ConfigGenerator) {
                VplsV2ConfigGenerator vcg = (VplsV2ConfigGenerator) cg;
                impl.put(deviceId, vcg.getImplementation());
            } else {
                impl.put(deviceId, VplsImplementation.UNSUPPORTED);
            }
        }
        return impl;
    }





    private HashMap<String, VPLS_DeviceLoopback> reserveVplsLoopbacks(String gri, List<String> deviceIds) throws PSSException {
        HashMap<String, VPLS_DeviceLoopback> result = new HashMap<String, VPLS_DeviceLoopback>();
        for (String deviceId : deviceIds) {
            VPLS_DeviceLoopback loopback = VPLS_DeviceLoopback.reserve(gri, deviceId);
            result.put(deviceId, loopback);
        }
        return result;
    }

    private HashMap<String, VPLS_DeviceLoopback> releaseVplsLoopbacks(String gri, List<String> deviceIds) throws PSSException {
        HashMap<String, VPLS_DeviceLoopback> result = new HashMap<String, VPLS_DeviceLoopback>();
        for (String deviceId : deviceIds) {
            VPLS_DeviceLoopback loopback = VPLS_DeviceLoopback.release(gri, deviceId);
            result.put(deviceId, loopback);
        }
        return result;

    }


}
