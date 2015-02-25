package net.es.oscars.pss.eompls.alu;


import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.api.PostCommitConfigGen;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.api.VplsImplementation;
import net.es.oscars.pss.eompls.api.VplsV2ConfigGenerator;
import net.es.oscars.pss.eompls.beans.LSP;
import net.es.oscars.pss.eompls.dao.GCUtils;
import net.es.oscars.pss.eompls.junos.SDNNameGenerator;
import net.es.oscars.pss.eompls.util.*;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.pss.util.VlanGroupConfig;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SR_VPLS_V2_ConfigGen implements DeviceConfigGenerator, PostCommitConfigGen, VplsV2ConfigGenerator {
    private Logger log = Logger.getLogger(SR_VPLS_V2_ConfigGen.class);
    public SR_VPLS_V2_ConfigGen() throws ConfigException, PSSException {
        VlanGroupConfig.configure();

    }
    public VplsImplementation getImplementation() {
        return VplsImplementation.ALU;
    }

    public String getPostCommitConfig(PSSAction action, String deviceId) {
        return "admin rollback save\n" +
               "admin save\n";
    }

    public String getConfig(PSSAction action, String deviceId) throws PSSException {
        switch (action.getActionType()) {
            case SETUP :
                return this.getSetup(action, deviceId);
            case TEARDOWN:
                return this.getTeardown(action, deviceId);
            case STATUS:
                return this.getStatus(action, deviceId);
            case MODIFY:
                return this.getModify(action, deviceId);
        }
        throw new PSSException("Invalid action type");
    }


    public String getModify(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        return this.onModify(action, deviceId);
    }
    
    private String getStatus(PSSAction action, String deviceId) throws PSSException {
        action.setStatus(ActionStatus.SUCCESS);
        return "";
    }
    public String getSetup(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");
        return this.onSetup(action, deviceId);
    }

    public String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        return this.onTeardown(action, deviceId);
    }

    private String onModify(PSSAction action, String deviceId) throws PSSException {

        ResDetails res = action.getRequest().getModifyReq().getReservation();
        String gri = res.getGlobalReservationId();
        SR_VPLS_DeviceIdentifiers ids = SR_VPLS_DeviceIdentifiers.retrieve(gri, deviceId);
        if (ids == null) {
            this.onError("no saved identifiers found for gri: "+gri+" device: "+deviceId);
        }
        SR_VPLS_TemplateParams params = this.getModifyTemplateParams(res, ids);
        String modifyConfig = this.generateConfig(params, ActionType.MODIFY);

        return modifyConfig;
    }


    private String onSetup(PSSAction action, String deviceId) throws PSSException {

        ResDetails res = action.getRequest().getSetupReq().getReservation();
        String gri = res.getGlobalReservationId();
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);

        String[] deviceIds =  { srcDeviceId, dstDeviceId };


        /*
        for both devices:
        - check if identifiers exist
        - generate identifiers as needed
        - generate setup and teardown config (unless it has been already generated)
         */

        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(gri);
        VPLS_Identifier vplsIds = rp.getVplsId();
        VPLS_DeviceLoopback loopback = rp.getLoopbackMap().get(deviceId);
        boolean need_secondary_sdp_id = false;
        if (rp.getParams().isProtection()) {
            need_secondary_sdp_id = true;
        }

        SR_VPLS_DeviceIdentifiers ids = SR_VPLS_DeviceIdentifiers.retrieve(gri, deviceId);
        if (ids == null) {
            log.info("no saved device identifiers found for gri: "+gri+" device: "+deviceId+", generating now");
            ids = SR_VPLS_DeviceIdentifiers.reserve(gri, deviceId, vplsIds, need_secondary_sdp_id);
        }

        String devSetupConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.SETUP);
        if (devSetupConfig == null) {
            log.info("no saved setup config found for gri: "+gri+" device: "+deviceId+", generating now");
            SR_VPLS_TemplateParams params = this.getSetupTemplateParams(res, deviceId, vplsIds, ids, loopback);
            devSetupConfig= this.generateConfig(params, ActionType.SETUP);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.SETUP, devSetupConfig);
        }

        String devTeardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
        if (devTeardownConfig == null) {
            log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
            SR_VPLS_TemplateParams params = this.getTeardownTemplateParams(res, deviceId, vplsIds, ids, loopback);
            devTeardownConfig = this.generateConfig(params, ActionType.TEARDOWN);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.TEARDOWN, devTeardownConfig);
        }

        String setupConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.SETUP);
        if (setupConfig == null) {
            this.onError("could not retrieve setup device config for gri: "+gri+" device: "+deviceId);
        }

        return setupConfig;
    }

    private String onTeardown(PSSAction action, String deviceId) throws PSSException {

        /*
       for this device:
       - check if identifiers exist
       - throw error if not
       - generate teardown config (unless it has been already generated)
       - release identifiers
        */
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        String gri = res.getGlobalReservationId();

        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(gri);

        VPLS_Identifier vplsIds = rp.getVplsId();
        VPLS_DeviceLoopback loopback = rp.getLoopbackMap().get(deviceId);


        SR_VPLS_DeviceIdentifiers ids = SR_VPLS_DeviceIdentifiers.retrieve(gri, deviceId);
        if (ids == null) {
            this.onError("no saved identifiers found for gri: "+gri+" device: "+deviceId);
        }

        String teardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
        if (teardownConfig == null) {
            log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
            SR_VPLS_TemplateParams params = this.getTeardownTemplateParams(res, deviceId, vplsIds, ids, loopback);
            teardownConfig = this.generateConfig(params, ActionType.TEARDOWN);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.TEARDOWN, teardownConfig);
        }

        SR_VPLS_DeviceIdentifiers.release(gri, deviceId);
        return teardownConfig;

    }

    private void onError(String errStr) throws PSSException {
        log.error(errStr);
        throw new PSSException(errStr);
    }

    public String generateConfig(SR_VPLS_TemplateParams params, ActionType phase)  throws PSSException {
        String templateFile = null;

        if (phase.equals(ActionType.SETUP)) {
            templateFile = "alu-vpls-v2-setup.txt";
        } else if (phase.equals(ActionType.TEARDOWN)) {
            templateFile = "alu-vpls-v2-teardown.txt";
        } else if (phase.equals(ActionType.MODIFY)) {
            templateFile = "alu-vpls-v2-modify.txt";
        } else {
            this.onError("invalid phase");
        }
        Map root = new HashMap();

        root.put("vpls", params.getVpls());
        root.put("ingqos", params.getIngqos());
        root.put("egrqos", params.getEgrqos());
        root.put("ifces", params.getIfces());
        root.put("paths", params.getPaths());
        root.put("sdps", params.getSdps());
        root.put("lsps", params.getLsps());

        String config = EoMPLSUtils.generateConfig(root, templateFile);
        return config;
    }

    private SR_VPLS_TemplateParams getModifyTemplateParams(ResDetails res, SR_VPLS_DeviceIdentifiers ids) throws PSSException  {
        SR_VPLS_TemplateParams params = new SR_VPLS_TemplateParams();

        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(res.getGlobalReservationId());

        boolean softPolice = rp.getParams().isSoftPolice();

        ReservedConstraintType rc = res.getReservedConstraint();
        Integer bw = rc.getBandwidth();
        Long ingQosBandwidth = 1L*bw;

        HashMap ingqos = new HashMap();
        ingqos.put("id", ids.getQosId().toString());
        ingqos.put("bandwidth", ingQosBandwidth);
        ingqos.put("soft", softPolice);


        params.setIngqos(ingqos);
        return params;

    }

    private SR_VPLS_TemplateParams getTeardownTemplateParams(ResDetails res, String deviceId, VPLS_Identifier vplsIds, SR_VPLS_DeviceIdentifiers ids, VPLS_DeviceLoopback loopback) throws PSSException  {
        String gri = res.getGlobalReservationId();

        ArrayList ifces = new ArrayList();
        ArrayList paths = new ArrayList();
        ArrayList lsps = new ArrayList();
        ArrayList sdps = new ArrayList();
        HashMap vpls = new HashMap();
        HashMap ingqos = new HashMap();
        HashMap egrqos = new HashMap();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);

        SDNNameGenerator sdng = SDNNameGenerator.getInstance();
        ALUNameGenerator ng = ALUNameGenerator.getInstance();

        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(res.getGlobalReservationId());
        String vplsName = sdng.getVplsName(gri, res.getDescription());
        String ifceName = ng.getIfceName(gri);

        boolean softPolice = rp.getParams().isSoftPolice();
        boolean applyQos = rp.getParams().isApplyQos();
        boolean protect = rp.getParams().isProtection();

        // fill in scalars
        /*
        1. vpls: id
        2. ingqos: id
        3. ifces: list_of <name, vlan>
        4. paths: list_of <name>
        5. lsps: list_of <name>
        6. sdps: list_of <id>
        */


        HashMap<String, ArrayList<SRIfceInfo>> allIfceInfos = this.getDeviceIfceInfo(res);
        ArrayList<SRIfceInfo> deviceIfceInfos = allIfceInfos.get(deviceId);
        for (SRIfceInfo ifceInfo : deviceIfceInfos) {
            Map ifce = new HashMap();
            ifce.put("name", ifceInfo.getName());
            ifce.put("vlan", ifceInfo.getVlan());
            ifces.add(ifce);
        }


        if (!sameDevice) {
            String pathName                = ng.getPathName(gri);
            String lspName                 = ng.getLSPName(gri);

            Map lsp = new HashMap();
            Map path = new HashMap();

            path.put("primary", pathName+"_wrk");
            path.put("protect", pathName+"_prt");

            lsp.put("primary", lspName+"_wrk");
            lsp.put("protect", lspName+"_prt");

            paths.add(path);
            lsps.add(lsp);


            // SDPs
            Integer sdpId_wrk = ids.getSdpIds().get(0);
            HashMap sdp = new HashMap();
            sdp.put("primary_id", sdpId_wrk.toString());
            if (protect) {
                Integer sdpId_prt= ids.getSdpIds().get(1);
                sdp.put("protect_id", sdpId_prt.toString());
            }
            sdps.add(sdp);
        } else {
            paths = null;
            lsps = null;
            sdps = null;

        }

        ingqos.put("applyqos", applyQos);
        ingqos.put("id", ids.getQosId().toString());
        egrqos.put("id", ids.getQosId().toString());

        vpls.put("primary_id", vplsIds.getVplsId().toString());

        if (vplsIds.getSecondaryVplsId().equals(VPLS_Identifier.NONE)) {
            vpls.put("protect_id", vplsIds.getVplsId().toString());
        } else {
            vpls.put("protect_id", vplsIds.getSecondaryVplsId().toString());
        }

        vpls.put("loopback_ifce", ifceName);
        vpls.put("loopback_address", loopback.getVplsLoopback());
        vpls.put("endpoint", vplsName);
        vpls.put("has_protect", protect);

        SR_VPLS_TemplateParams params = new SR_VPLS_TemplateParams();
        params.setIfces(ifces);
        params.setIngqos(ingqos);
        params.setEgrqos(egrqos);
        params.setLsps(lsps);
        params.setPaths(paths);
        params.setSdps(sdps);
        params.setVpls(vpls);

        return params;

    }
    private SR_VPLS_TemplateParams getSetupTemplateParams(ResDetails res, String deviceId, VPLS_Identifier vplsIds, SR_VPLS_DeviceIdentifiers ids, VPLS_DeviceLoopback loopback) throws PSSException  {

        ArrayList ifces = new ArrayList();
        ArrayList paths = new ArrayList();
        ArrayList lsps = new ArrayList();
        ArrayList sdps = new ArrayList();
        HashMap vpls = new HashMap();
        HashMap ingqos = new HashMap();
        HashMap egrqos = new HashMap();


        String gri = res.getGlobalReservationId();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);

        System.out.println("gri: "+gri+" src device: " + srcDeviceId + " this one: " + deviceId);


        ALUNameGenerator ng = ALUNameGenerator.getInstance();
        SDNNameGenerator sdng = SDNNameGenerator.getInstance();

        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
        /* *********************** */


        /* BEGIN POPULATING VALUES */
        /* *********************** */


        ReservedConstraintType rc = res.getReservedConstraint();
        Integer bw = rc.getBandwidth();
        PathInfo pi = rc.getPathInfo();
        String description = res.getDescription();

        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }

        CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
        CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();

        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);

        EoMPLSIfceAddressResolver iar = ecf.getEomplsIfceAddressResolver();
        EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();


        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(res.getGlobalReservationId());

        boolean softPolice = rp.getParams().isSoftPolice();
        boolean applyQos = rp.getParams().isApplyQos();
        boolean protect = rp.getParams().isProtection();

        boolean isEndpoint = false;


        // LSPs, SDPs and paths only for multi-device config
        if (!sameDevice) {
            String lspTargetDeviceId, lspOriginDeviceId;
            boolean reverse = false;
            log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
            if (srcDeviceId.equals(deviceId)) {
                // forward direction
                log.debug("forward");

                lspOriginDeviceId = srcRes.getNodeId();
                lspTargetDeviceId = dstRes.getNodeId();
            } else {
                // reverse direction
                log.debug("reverse");
                lspOriginDeviceId = dstRes.getNodeId();
                lspTargetDeviceId = srcRes.getNodeId();
                reverse = true;
            }
            LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);

            // paths
            int i = 5;
            ArrayList hops = new ArrayList();
            for (String ipaddress : lspBean.getPathAddresses()) {
                Map hop = new HashMap();
                hop.put("address", ipaddress);
                hop.put("order", i);
                i += 5;
                hops.add(hop);
            }
            HashMap path = new HashMap();
            path.put("primary", ng.getPathName(gri)+"_wrk");
            path.put("protect", ng.getPathName(gri)+"_prt");
            path.put("hops", hops);
            paths.add(path);

            String lspTo           = dar.getDeviceAddress(lspTargetDeviceId);

            // LSPs
            HashMap lsp = new HashMap();
            lsp.put("to", lspTo);
            lsp.put("primary", ng.getLSPName(gri)+"_wrk");
            lsp.put("primary_path", ng.getPathName(gri)+"_wrk");
            if (protect) {
                lsp.put("protect", ng.getLSPName(gri)+"_prt");
                lsp.put("protect_path", ng.getPathName(gri)+"_prt");
            }
            lsps.add(lsp);

            // SDPs
            Integer sdpId_wrk = ids.getSdpIds().get(0);
            HashMap sdp = new HashMap();
            sdp.put("primary_id", sdpId_wrk.toString());
            sdp.put("description", gri+"_wrk");
            sdp.put("far_end", lspTo);
            sdp.put("primary_lsp_name", ng.getLSPName(gri)+"_wrk");

            if (protect) {
                Integer sdpId_prt= ids.getSdpIds().get(1);
                sdp.put("protect_id", sdpId_prt.toString());
                sdp.put("protect_description", gri+"_prt");
                sdp.put("protect_lsp_name", ng.getLSPName(gri)+"_prt");
            }
            sdps.add(sdp);

            VplsImplementation otherImpl;
            List<String> deviceIds = new ArrayList<String>();
            deviceIds.add(srcDeviceId);
            deviceIds.add(dstDeviceId);
            HashMap<String, VplsImplementation> implMap = VPLS_RequestParams.getImplementationMap(deviceIds);
            if (reverse) {
                otherImpl = implMap.get(srcDeviceId);
            } else {
                otherImpl = implMap.get(dstDeviceId);
            }

            // both ALU: just forward direction should get is_endpoint
            if (otherImpl.equals(VplsImplementation.ALU)) {
                if (!reverse) {
                    log.debug("other implementation is an ALU, forward direction, isEndpoint is true");
                    isEndpoint = true;
                } else if (reverse) {
                    log.debug("other implementation is an ALU, reverse direction, isEndpoint is false");
                    isEndpoint = false;
                }
            } else {
                log.debug("other implementation is not an ALU, isEndpoint is true");
                // just one is ALU: isEndpoint is true
                isEndpoint = true;
            }


        } else {
            sdps = null;
            paths = null;
            lsps = null;
        }

        /*
        1. vpls: id, description
        2. ingqos: id, description, bandwidth
        3. ifces: list_of <name, vlan>
        4. paths: list_of <name, hops>
                                 hops: list_of: <address, order>
        5. lsps: list_of <from, to, name, path>
        6. sdps: list_of <id, description, far_end, lsp_name>

        notes
           each sdp.lsp_name should correspond to an lsp.name,
              and the sdp.far_end for that should correspond to the lsp.to
        */


        // bandwidth in Mbps
        Long ingQosBandwidth = 1L*bw;


        String vplsId = vplsIds.getVplsId().toString();
        String vplsDesc = sdng.getVplsDescription(gri, ingQosBandwidth * 1000000, description);
        String vplsName = sdng.getVplsName(gri, description);
        String ifceName = ng.getIfceName(gri);


        vpls.put("description", vplsDesc);
        vpls.put("name", vplsName);
        vpls.put("primary_id", vplsId);
        vpls.put("endpoint", vplsName);
        vpls.put("is_endpoint", isEndpoint);

        vpls.put("has_protect", protect);
        if (!sameDevice) {
            vpls.put("protect_id", vplsIds.getSecondaryVplsId().toString());
            vpls.put("loopback_address", loopback.getVplsLoopback());
            vpls.put("loopback_ifce", ifceName);
        }





        // qos params
        String qosId  = ids.getQosId().toString();
        ingqos.put("applyqos", applyQos);
        ingqos.put("soft", softPolice);

        ingqos.put("id", qosId);
        ingqos.put("description", gri);
        ingqos.put("bandwidth", ingQosBandwidth);
        egrqos.put("id", qosId);
        egrqos.put("description", gri);



        HashMap<String, ArrayList<SRIfceInfo>> allIfceInfos = this.getDeviceIfceInfo(res);
        ArrayList<SRIfceInfo> deviceIfceInfos = allIfceInfos.get(deviceId);
        for (SRIfceInfo ifceInfo : deviceIfceInfos) {
            Map ifce = new HashMap();
            ifce.put("name", ifceInfo.getName());
            ifce.put("vlan", ifceInfo.getVlan());
            String descGri = ng.getName(gri, "", 24);
            String sapDesc = sdng.getInterfaceDescription(descGri, ingQosBandwidth*1000000, description);
            ifce.put("description", sapDesc);
            ifces.add(ifce);
        }



        SR_VPLS_TemplateParams params = new SR_VPLS_TemplateParams();
        params.setIfces(ifces);
        params.setIngqos(ingqos);
        params.setEgrqos(egrqos);
        params.setLsps(lsps);
        params.setPaths(paths);
        params.setSdps(sdps);
        params.setVpls(vpls);
        return params;

    }
    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }
    private HashMap<String, ArrayList<SRIfceInfo>> getDeviceIfceInfo(ResDetails res) throws PSSException {
        String gri = res.getGlobalReservationId();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);

        ReservedConstraintType rc = res.getReservedConstraint();

        PathInfo pi = rc.getPathInfo();

        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }

        CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
        CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();

        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);

        String srcVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
        String dstVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();

        ArrayList<String> srcVlans = VlanGroupConfig.getVlans(srcDeviceId, srcRes.getPortId(), srcVlan);
        ArrayList<String> dstVlans = VlanGroupConfig.getVlans(dstDeviceId, dstRes.getPortId(), dstVlan);


        HashMap<String, ArrayList<SRIfceInfo>> deviceIfceInfo = new HashMap<String, ArrayList<SRIfceInfo>>();
        deviceIfceInfo.put(srcDeviceId, new ArrayList<SRIfceInfo>());
        deviceIfceInfo.put(dstDeviceId, new ArrayList<SRIfceInfo>());
        for (String vlan : srcVlans) {
            SRIfceInfo aIfceInfo = new SRIfceInfo();
            aIfceInfo.setDescription(gri);
            aIfceInfo.setName(srcRes.getPortId());
            aIfceInfo.setVlan(vlan);
            log.debug("setting "+srcDeviceId+" : "+aIfceInfo.getName());
            deviceIfceInfo.get(srcDeviceId).add(aIfceInfo);
        }

        for (String vlan : dstVlans) {
            SRIfceInfo zIfceInfo = new SRIfceInfo();
            zIfceInfo.setDescription(gri);
            zIfceInfo.setName(dstRes.getPortId());
            zIfceInfo.setVlan(vlan);
            log.debug("setting "+dstDeviceId+" : "+zIfceInfo.getName());
            deviceIfceInfo.get(dstDeviceId).add(zIfceInfo);
        }
        return deviceIfceInfo;
    }


}
