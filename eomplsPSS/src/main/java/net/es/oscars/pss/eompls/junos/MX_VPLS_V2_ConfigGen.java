package net.es.oscars.pss.eompls.junos;


import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.api.VplsImplementation;
import net.es.oscars.pss.eompls.api.VplsV2ConfigGenerator;
import net.es.oscars.pss.eompls.beans.LSP;
import net.es.oscars.pss.eompls.util.*;
import net.es.oscars.pss.eompls.dao.GCUtils;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MX_VPLS_V2_ConfigGen implements DeviceConfigGenerator, VplsV2ConfigGenerator {
    private Logger log = Logger.getLogger(MX_VPLS_V2_ConfigGen.class);

    public VplsImplementation getImplementation() {
        return VplsImplementation.JUNOS;
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

    private String getStatus(PSSAction action, String deviceId) throws PSSException {
        ResDetails res = action.getRequest().getSetupReq().getReservation();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        if (sameDevice) {
            return "";
        } else {
            return this.getLSPStatus(action, deviceId);
        }

    }
    public String getSetup(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");

        return this.onSetup(action, deviceId);
    }
    public String getModify(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");
        return this.onModify(action, deviceId);
    }


    public String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        return this.onTeardown(action, deviceId);
    }

    private String getLSPStatus(PSSAction action, String deviceId)  throws PSSException {
        String templateFile = "junos-mx-lsp-status.txt";
        Map root = new HashMap();
        String config       = EoMPLSUtils.generateConfig(root, templateFile);
        return config;
    }


    private String onModify(PSSAction action, String deviceId) throws PSSException {

        ResDetails res = action.getRequest().getModifyReq().getReservation();
        String gri = res.getGlobalReservationId();
        MX_VPLS_TemplateParams params = this.getModifyTemplateParams(res);
        if (params == null) {
            this.onError("could not generate template parameters for modify! gri: "+gri);
        }
        String devModifyConfig = this.generateConfig(params, ActionType.MODIFY);
        if (devModifyConfig == null ) {
            this.onError("could not fill template for modify!");
        }

        return devModifyConfig;

    }

    private String onSetup(PSSAction action, String deviceId) throws PSSException {

        ResDetails res = action.getRequest().getSetupReq().getReservation();
        String gri = res.getGlobalReservationId();

        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(gri);


        /*
       for both devices:
       - check if identifiers exist
       - generate identifiers as needed
       - generate setup and teardown config (unless it has been already generated)
        */
        VPLS_Identifier gids = rp.getVplsId();
        VPLS_DeviceLoopback loopback = rp.getLoopbackMap().get(deviceId);


        String devSetupConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.SETUP);
        if (devSetupConfig == null) {
            log.info("no saved setup config found for gri: "+gri+" device: "+deviceId+", generating now");
            MX_VPLS_TemplateParams params = this.getSetupTemplateParams(res, deviceId, gids, loopback);
            devSetupConfig= this.generateConfig(params, ActionType.SETUP);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.SETUP, devSetupConfig);
        }

        String devTeardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
        if (devTeardownConfig == null) {
            log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
            MX_VPLS_TemplateParams params = this.getTeardownTemplateParams(res, deviceId, gids, loopback);
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
       - release identifiers
       - generate teardown config (unless it has been already generated)
        */
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        String gri = res.getGlobalReservationId();

        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(gri);

        VPLS_Identifier vplsIds = rp.getVplsId();
        VPLS_DeviceLoopback loopback = rp.getLoopbackMap().get(deviceId);

        String teardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
        if (teardownConfig == null) {
            log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
            MX_VPLS_TemplateParams params = this.getTeardownTemplateParams(res, deviceId, vplsIds, loopback);
            teardownConfig = this.generateConfig(params, ActionType.TEARDOWN);
            GCUtils.storeDeviceConfig(gri, deviceId, ActionType.TEARDOWN, teardownConfig);
        }


        return teardownConfig;

    }


    private MX_VPLS_TemplateParams getSetupTemplateParams(ResDetails res, String deviceId, VPLS_Identifier vplsIds, VPLS_DeviceLoopback loopback) throws PSSException  {
        String description = res.getDescription();
        String gri = res.getGlobalReservationId();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);

        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = holder.getRequestParams().get(gri);

        String remoteDeviceId = srcDeviceId;

        if (srcDeviceId.equals(deviceId)) {
            remoteDeviceId = dstDeviceId;
        }

        String remoteVplsLoopback = rp.getLoopbackMap().get(remoteDeviceId).getVplsLoopback();



        String policy = "";
        HashMap community = new HashMap();
        HashMap filters = new HashMap();
        HashMap policer = new HashMap();
        HashMap vpls = new HashMap();
        ArrayList ifces = new ArrayList();
        ArrayList paths = new ArrayList();
        ArrayList lsps = new ArrayList();


        String policyName;
        String communityName;
        String communityMembers;
        Long lspBandwidth;
        String wrk_pathName;
        String prt_pathName;
        String lspName;
        String vplsName;

        String lspNeighbor;
        String policerName;
        Long policerBurstSizeLimit;
        Long policerBandwidthLimit;
        String statsFilterName;
        String primaryFilterName;
        String protectFilterName;



        VPLS_ServiceParams serviceParams = VPLS_ServiceParams.fromResDetails(res);



        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        
        SDNNameGenerator ng = SDNNameGenerator.getInstance();

        ReservedConstraintType rc = res.getReservedConstraint();

        Integer bw = rc.getBandwidth();
        PathInfo pi = rc.getPathInfo();

        // bandwidth is in Mbps
        lspBandwidth = 1000000L*bw;
        
        policerBandwidthLimit = lspBandwidth;
        policerBurstSizeLimit = lspBandwidth / 10;
        if (policerBandwidthLimit < 8000L) {
            policerBandwidthLimit = 8000L;
        }
        if (policerBandwidthLimit > 50000000000L) {
            policerBandwidthLimit = 50000000000L;
        }

        if (policerBurstSizeLimit < 1500L) {
            policerBurstSizeLimit = 1500L;
        }
        if (policerBurstSizeLimit > 100000000000L) {
            policerBurstSizeLimit = 100000000000L;
        }


        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();

        EoMPLSIfceAddressResolver iar = ecf.getEomplsIfceAddressResolver();
        EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();


        primaryFilterName       = ng.getFilterName(gri, "primary", description)+"_wrk";
        protectFilterName       = ng.getFilterName(gri, "protect", description)+"_prt";
        statsFilterName         = ng.getFilterName(gri, "stats", description);
        communityName           = ng.getCommunityName(gri, description);
        policyName              = ng.getPolicyName(gri, description);
        policerName             = ng.getPolicerName(gri, description);
        wrk_pathName                = ng.getPathName(gri, description)+"_wrk";
        prt_pathName                = ng.getPathName(gri, description)+"_prt";
        lspName                 = ng.getLSPName(gri, description);
        vplsName                = ng.getVplsName(gri, description);

        String vplsId = vplsIds.getVplsId().toString();

        // community is 30000 - 65500
        String oscarsCommunity;
        if (ng.getOscarsCommunity(gri) > 65535) {
            oscarsCommunity  = ng.getOscarsCommunity(gri)+"L";
        } else {
            oscarsCommunity  = ng.getOscarsCommunity(gri).toString();
        }
        
        communityMembers    = "65000:"+oscarsCommunity+":"+vplsId;


        /*
        setup:
        1. policy (string)
        2. community: name, id
        3. filters: stats, policing
        4. policer: name, bandwidth_limit, burst_size_limit
        5. vpls: name, id

        6. ifces: list_of <name, vlan, description>
        7. paths: list_of <name, hops>
                                 hops: list of string >
        8. lsps: list_of <name, from, to, path, neighbor, bandwidth>
        */

        policy = policyName;
        community.put("name", communityName);
        community.put("members", communityMembers);

        filters.put("stats", statsFilterName);
        filters.put("primary", primaryFilterName);

        policer.put("name", policerName);
        policer.put("bandwidth_limit", policerBandwidthLimit);
        policer.put("burst_size_limit", policerBurstSizeLimit);
        policer.put("soft",serviceParams.isSoftPolice());
        policer.put("applyqos", serviceParams.isApplyQos());


        vpls.put("name", vplsName);
        vpls.put("id", vplsId);
        vpls.put("loopback", loopback.getVplsLoopback());




        if (serviceParams.isProtection()) {
            filters.put("protect", protectFilterName);
            vpls.put("has_protect", true);
            if (vplsIds.getSecondaryVplsId().equals(VPLS_Identifier.NONE)) {
                vpls.put("has_two_ids", false);
                vpls.put("protect", vplsIds.getSecondaryVplsId().toString());

            } else {
                vpls.put("has_two_ids", true);
                vpls.put("protect", vplsIds.getSecondaryVplsId().toString());

            }

        } else {
            vpls.put("has_protect", false);
            vpls.put("has_two_ids", false);
        }


        HashMap<String, ArrayList<MXIfceInfo>> allIfceInfos = this.getDeviceIfceInfo(res, lspBandwidth);
        if (allIfceInfos == null) {
            throw new PSSException("no ifce infos found!");
        }

        ArrayList<MXIfceInfo> deviceIfceInfos = allIfceInfos.get(deviceId);
        if (deviceIfceInfos == null) {
            throw new PSSException("no ifce infos found for: "+deviceId);
        }

        for (MXIfceInfo ifceInfo : deviceIfceInfos) {
            Map ifce = new HashMap();
            ifce.put("name", ifceInfo.getName());
            ifce.put("vlan", ifceInfo.getVlan());
            ifce.put("description", ifceInfo.getDescription());
            ifces.add(ifce);
        }

        MX_VPLS_TemplateParams params = new MX_VPLS_TemplateParams();

        // only if different devices
        if (!sameDevice) {
            String lspTargetDeviceId;
            boolean reverse = false;
            log.debug("different devices, setting up LSPs and paths");
            List<CtrlPlaneHopContent> localHops;
            try {
                localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
            } catch (OSCARSServiceException e) {
                throw new PSSException(e);
            }

            CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
            CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();
            if (!deviceId.equals(srcDeviceId)) {
                egressLink = ingressLink;
                reverse = true;
            }


            String dstLinkId = egressLink.getId();
            URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
            lspTargetDeviceId = dstRes.getNodeId();

            lspNeighbor       = dar.getDeviceAddress(lspTargetDeviceId);
            LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);

            Map path = new HashMap();
            Map lsp = new HashMap();

            path.put("primary", wrk_pathName);
            path.put("protect", prt_pathName);
            ArrayList hops = lspBean.getPathAddresses();
            path.put("hops", hops);
            paths.add(path);


            lsp.put("primary", lspName+"_wrk");
            lsp.put("protect", lspName+"_prt");
            lsp.put("to", remoteVplsLoopback);
            lsp.put("neighbor", lspNeighbor);
            lsp.put("primary_path", wrk_pathName);
            lsp.put("protect_path", prt_pathName);

            lsps.add(lsp);

            params.setLsps(lsps);
            params.setPaths(paths);




        } else {
            params.setLsps(null);
            params.setPaths(null);
        }





        params.setCommunity(community);
        params.setFilters(filters);
        params.setIfces(ifces);
        params.setPolicer(policer);
        params.setPolicy(policy);
        params.setVpls(vpls);

        return params;
    }
    private MX_VPLS_TemplateParams getModifyTemplateParams(ResDetails res) throws PSSException  {

        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        String gri = res.getGlobalReservationId();

        ReservedConstraintType rc = res.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();
        Integer bw = rc.getBandwidth();
        String description = res.getDescription();


        // bandwidth in Mbps
        Long lspBandwidth = 1000000L*bw;
        Long policerBandwidthLimit = lspBandwidth;
        Long policerBurstSizeLimit = lspBandwidth / 10;
        if (policerBandwidthLimit < 8000L) {
            policerBandwidthLimit = 8000L;
        }
        if (policerBandwidthLimit > 50000000000L) {
            policerBandwidthLimit = 50000000000L;
        }

        if (policerBurstSizeLimit < 1500L) {
            policerBurstSizeLimit = 1500L;
        }
        if (policerBurstSizeLimit > 100000000000L) {
            policerBurstSizeLimit = 100000000000L;
        }

        String policerName;
        policerName             = ng.getPolicerName(gri, description);

        HashMap policer = new HashMap();

        policer.put("name", policerName);
        policer.put("bandwidth_limit", policerBandwidthLimit);
        policer.put("burst_size_limit", policerBurstSizeLimit);

        MX_VPLS_TemplateParams params = new MX_VPLS_TemplateParams();
        params.setPolicer(policer);
        return params;


    }

    private MX_VPLS_TemplateParams getTeardownTemplateParams(ResDetails res, String deviceId, VPLS_Identifier vplsIds, VPLS_DeviceLoopback loopback) throws PSSException  {

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);

        boolean sameDevice = srcDeviceId.equals(dstDeviceId);


        String policy = "";
        HashMap community = new HashMap();
        HashMap filters = new HashMap();
        HashMap policer = new HashMap();
        HashMap vpls = new HashMap();
        ArrayList ifces = new ArrayList();
        ArrayList paths = new ArrayList();
        ArrayList lsps = new ArrayList();


        String ifceName;
        String ifceVlan;

        String policyName;
        String communityName;
        String wrk_pathName;
        String prt_pathName;
        String lspName;
        String vplsName;

        String policerName;
        String statsFilterName;
        String primaryFilterName;
        String protectFilterName;


        ReservedConstraintType rc = res.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();
        Integer bw = rc.getBandwidth();
        // bandwidth in Mbps
        Long lspBandwidth = 1000000L*bw;

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



        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        String gri = res.getGlobalReservationId();
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        String description = res.getDescription();



        String lspTargetDeviceId;
        log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
        if (srcDeviceId.equals(deviceId)) {
            // forward direction
            log.debug("forward");
            ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
        } else {
            // reverse direction
            log.debug("reverse");
            ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
        }
        log.debug("srcLinkId: "+srcLinkId);
        log.debug("dstLinkId: "+dstLinkId);
        log.debug("ifceName: "+ifceName);
        log.debug("ifceVlan: "+ifceVlan);


        primaryFilterName       = ng.getFilterName(gri, "primary", description)+"_wrk";
        protectFilterName       = ng.getFilterName(gri, "protect", description)+"_prt";
        statsFilterName         = ng.getFilterName(gri, "stats", description);
        communityName           = ng.getCommunityName(gri, description);
        policyName              = ng.getPolicyName(gri, description);
        policerName             = ng.getPolicerName(gri, description);
        wrk_pathName            = ng.getPathName(gri, description)+"_wrk";
        prt_pathName            = ng.getPathName(gri, description)+"_prt";
        lspName                 = ng.getLSPName(gri, description);
        vplsName                = ng.getVplsName(gri, description);

        VPLS_ServiceParams serviceParams = VPLS_ServiceParams.fromResDetails(res);

        /*
        teardown:
        1. policy (string)
        2. community: name
        3. filters: stats, policing
        4. policer: name
        5. vpls: name

        6. ifces: list_of <name, vlan>
        7. paths: list_of <name>
        8. lsps: list_of <name>
        */
        policy = policyName;
        community.put("name", communityName);
        filters.put("stats", statsFilterName);
        filters.put("primary", primaryFilterName);
        policer.put("name", policerName);
        policer.put("applyqos", serviceParams.isApplyQos());
        vpls.put("name", vplsName);
        vpls.put("loopback", loopback.getVplsLoopback());



        if (serviceParams.isProtection()) {
            filters.put("protect", protectFilterName);
            vpls.put("has_protect", true);
            vpls.put("protect", vplsIds.getSecondaryVplsId().toString());

        } else {
            vpls.put("has_protect", false);
            vpls.put("has_two_ids", false);
        }


        HashMap<String, ArrayList<MXIfceInfo>> allIfceInfos = this.getDeviceIfceInfo(res, lspBandwidth);
        ArrayList<MXIfceInfo> deviceIfceInfos = allIfceInfos.get(deviceId);
        for (MXIfceInfo ifceInfo : deviceIfceInfos) {
            Map ifce = new HashMap();
            ifce.put("name", ifceInfo.getName());
            ifce.put("vlan", ifceInfo.getVlan());
            ifces.add(ifce);
        }


        MX_VPLS_TemplateParams params = new MX_VPLS_TemplateParams();
        if (!sameDevice) {
            Map path = new HashMap();
            path.put("primary", wrk_pathName);
            path.put("protect", prt_pathName);
            paths.add(path);

            Map lsp = new HashMap();
            lsp.put("primary", lspName+"_wrk");
            lsp.put("protect", lspName+"_prt");
            lsps.add(lsp);

            params.setLsps(lsps);
            params.setPaths(paths);
        } else {
            params.setLsps(null);
            params.setPaths(null);
        }

        params.setCommunity(community);
        params.setFilters(filters);
        params.setIfces(ifces);
        params.setPolicer(policer);
        params.setPolicy(policy);
        params.setVpls(vpls);
        return params;


    }
    private HashMap<String, ArrayList<MXIfceInfo>> getDeviceIfceInfo(ResDetails res, Long lspBandwidth) throws PSSException {
        String gri = res.getGlobalReservationId();
        SDNNameGenerator ng = SDNNameGenerator.getInstance();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);

        ReservedConstraintType rc = res.getReservedConstraint();
        String description = res.getDescription();




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


        HashMap<String, ArrayList<MXIfceInfo>> deviceIfceInfo = new HashMap<String, ArrayList<MXIfceInfo>>();
        deviceIfceInfo.put(srcDeviceId, new ArrayList<MXIfceInfo>());
        deviceIfceInfo.put(dstDeviceId, new ArrayList<MXIfceInfo>());
        MXIfceInfo aIfceInfo = new MXIfceInfo();
        aIfceInfo.setDescription(ng.getInterfaceDescription(gri, lspBandwidth, description));
        aIfceInfo.setName(srcRes.getPortId());
        aIfceInfo.setVlan(ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability());
        log.debug("setting "+srcDeviceId+" : "+aIfceInfo.getName());
        deviceIfceInfo.get(srcDeviceId).add(aIfceInfo);

        MXIfceInfo zIfceInfo = new MXIfceInfo();
        zIfceInfo.setDescription(ng.getInterfaceDescription(gri, lspBandwidth, description));
        zIfceInfo.setName(dstRes.getPortId());
        zIfceInfo.setVlan(egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability());
        log.debug("setting "+dstDeviceId+" : "+zIfceInfo.getName());
        deviceIfceInfo.get(dstDeviceId).add(zIfceInfo);
        return deviceIfceInfo;
    }


    public String generateConfig(MX_VPLS_TemplateParams params, ActionType phase)  throws PSSException {
        String templateFile = null;

        if (phase.equals(ActionType.SETUP)) {
            templateFile = "junos-mx-vpls-v2-setup.txt";
        } else if (phase.equals(ActionType.TEARDOWN)) {
            templateFile = "junos-mx-vpls-v2-teardown.txt";
        } else if (phase.equals(ActionType.MODIFY)) {
            templateFile = "junos-mx-vpls-v2-modify.txt";
        } else {
            this.onError("invalid phase");
        }
        Map root = new HashMap();
        root.put("ifces", params.getIfces());
        root.put("paths", params.getPaths());
        root.put("lsps", params.getLsps());
        root.put("filters", params.getFilters());
        root.put("policy", params.getPolicy());
        root.put("policer", params.getPolicer());
        root.put("vpls", params.getVpls());
        root.put("community", params.getCommunity());

        String config = EoMPLSUtils.generateConfig(root, templateFile);
        return config;
    }
    private void onError(String errStr) throws PSSException {
        log.error(errStr);
        throw new PSSException(errStr);
    }


    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }



}
