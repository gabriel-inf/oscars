package net.es.oscars.pss.eompls.junos;


import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.beans.LSP;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import java.util.*;


public class MX_VPLS_ConfigGen implements DeviceConfigGenerator {
    private Logger log = Logger.getLogger(MX_VPLS_ConfigGen.class);

    protected String policy = "";



    protected HashMap community = new HashMap();
    protected HashMap filters = new HashMap();
    protected HashMap policer = new HashMap();
    protected HashMap vpls = new HashMap();
    protected ArrayList ifces = new ArrayList();
    protected ArrayList paths = new ArrayList();
    protected ArrayList lsps = new ArrayList();


    public String getConfig(PSSAction action, String deviceId) throws PSSException {
        switch (action.getActionType()) {
            case SETUP :
                return this.getSetup(action, deviceId);
            case TEARDOWN:
                return this.getTeardown(action, deviceId);
            case STATUS:
                return this.getStatus(action, deviceId);
            case MODIFY:
                throw new PSSException("Modify not supported");
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
    private String getSetup(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");
        
        ResDetails res = action.getRequest().getSetupReq().getReservation();
        
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        
        if (sameDevice) {
            // TODO: not null!
            return null;

        } else {
            return this.gen_VPLS_setup(res, deviceId);
        }
    }
    
    
    private String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        
        if (sameDevice) {
            // TODO: not null!
            return null;
        } else {
            return this.gen_VPLS_teardown(res, deviceId);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private String getLSPStatus(PSSAction action, String deviceId)  throws PSSException {
        String templateFile = "junos-mx-lsp-status.txt";
        Map root = new HashMap();
        String config       = EoMPLSUtils.generateConfig(root, templateFile);
        return config;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String gen_VPLS_setup(ResDetails res, String deviceId) throws PSSException  {
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

        log.debug("gen_VPLS_setup start");
        if (res != null) {
            this.populateSetupFromResDetails(res, deviceId);
        }
        Map root = new HashMap();
        root.put("ifces", ifces);
        root.put("paths", paths);
        root.put("lsps", lsps);
        root.put("filters", filters);
        root.put("policy", policy);
        root.put("policer", policer);
        root.put("vpls", vpls);
        root.put("community", community);


        String templateFile = "junos-mx-vpls-setup.txt";
        String config       = EoMPLSUtils.generateConfig(root, templateFile);

        log.debug("gen_VPLS_setup done");
        return config;
    }


    private void populateSetupFromResDetails(ResDetails res, String deviceId) throws PSSException  {
        community = new HashMap();
        filters = new HashMap();
        policer = new HashMap();
        vpls = new HashMap();
        ifces = new ArrayList();
        paths = new ArrayList();
        lsps = new ArrayList();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);

        String ifceName;
        String ifceDescription;
        String ifceVlan;

        String policyName;
        String communityName;
        String communityMembers;
        Long lspBandwidth;
        String pathName;
        String lspName;
        String vplsName;

        String lspNeighbor;
        String policerName;
        Long policerBurstSizeLimit;
        Long policerBandwidthLimit;
        String statsFilterName;
        String policingFilterName;

        
        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */
        

        ReservedConstraintType rc = res.getReservedConstraint();
        Integer bw = rc.getBandwidth();
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
        
        EoMPLSIfceAddressResolver iar = ecf.getEomplsIfceAddressResolver();
        EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();
        
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        String gri = res.getGlobalReservationId();
        

        
        // bandwidth in Mbps 
        lspBandwidth = 1000000L*bw;
        
        policerBandwidthLimit = lspBandwidth;
        policerBurstSizeLimit = lspBandwidth / 10;

        String lspTargetDeviceId;
        boolean reverse = false;
        log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
        if (srcDeviceId.equals(deviceId)) {
            // forward direction
            log.debug("forward");
            ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspTargetDeviceId = dstRes.getNodeId();
        } else {
            // reverse direction
            log.debug("reverse");
            ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspTargetDeviceId = srcRes.getNodeId();
            reverse = true;
        }

        lspNeighbor         = dar.getDeviceAddress(lspTargetDeviceId);

        LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);

    
        ifceDescription = ng.getInterfaceDescription(gri, lspBandwidth);

        policingFilterName      = ng.getFilterName(gri, "policing");
        statsFilterName         = ng.getFilterName(gri, "stats");
        communityName           = ng.getCommunityName(gri);
        policyName              = ng.getPolicyName(gri);
        policerName             = ng.getPolicerName(gri);
        pathName                = ng.getPathName(gri);
        lspName                 = ng.getLSPName(gri);
        vplsName                = ng.getVplsName(gri);


        // community is 30000 - 65500
        String oscarsCommunity;
        Random rand = new Random();
        Integer randInt = 30000 + rand.nextInt(35500);
        if (ng.getOscarsCommunity(gri) > 65535) {
            oscarsCommunity  = ng.getOscarsCommunity(gri)+"L";
        } else {
            oscarsCommunity  = ng.getOscarsCommunity(gri).toString();
        }
        
        communityMembers    = "65000:"+oscarsCommunity+":"+randInt;

        // TODO: not this
        String vplsId = randInt.toString();

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
        filters.put("policing", policingFilterName);
        policer.put("name", policerName);
        policer.put("bandwidth_limit", policerBandwidthLimit);
        policer.put("burst_size_limit", policerBurstSizeLimit);
        vpls.put("name", vplsName);
        vpls.put("id", vplsId);


        Map ifce = new HashMap();
        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);
        ifce.put("description", ifceDescription);
        ifces.add(ifce);

        Map path = new HashMap();
        path.put("name", pathName);
        ArrayList hops = lspBean.getPathAddresses();
        path.put("hops", hops);
        paths.add(path);


        Map lsp = new HashMap();
        lsp.put("name", lspName);
        lsp.put("from", lspBean.getFrom());
        lsp.put("to", lspBean.getTo());
        lsp.put("neighbor", lspNeighbor);
        lsp.put("bandwidth", lspBandwidth);
        lsp.put("path", pathName);

        lsps.add(lsp);

        return ;
    }

    private void populateTeardownFromResDetails(ResDetails res, String deviceId) throws PSSException  {
        community = new HashMap();
        filters = new HashMap();
        policer = new HashMap();
        vpls = new HashMap();
        ifces = new ArrayList();
        paths = new ArrayList();
        lsps = new ArrayList();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);

        String ifceName;
        String ifceVlan;

        String policyName;
        String communityName;
        String pathName;
        String lspName;
        String vplsName;

        String policerName;
        String statsFilterName;
        String policingFilterName;


        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();

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


        EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();

        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        String gri = res.getGlobalReservationId();
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */


        String lspTargetDeviceId;
        log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
        if (srcDeviceId.equals(deviceId)) {
            // forward direction
            log.debug("forward");
            ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspTargetDeviceId = dstRes.getNodeId();
        } else {
            // reverse direction
            log.debug("reverse");
            ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspTargetDeviceId = srcRes.getNodeId();
        }
        policingFilterName      = ng.getFilterName(gri, "policing");
        statsFilterName         = ng.getFilterName(gri, "stats");
        communityName           = ng.getCommunityName(gri);
        policyName              = ng.getPolicyName(gri);
        policerName             = ng.getPolicerName(gri);
        pathName                = ng.getPathName(gri);
        lspName                 = ng.getLSPName(gri);
        vplsName                = ng.getVplsName(gri);

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
        filters.put("policing", policingFilterName);
        policer.put("name", policerName);
        vpls.put("name", vplsName);


        Map ifce = new HashMap();
        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);
        ifces.add(ifce);

        Map path = new HashMap();
        path.put("name", pathName);
        paths.add(path);

        Map lsp = new HashMap();
        lsp.put("name", lspName);
        lsps.add(lsp);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String gen_VPLS_teardown(ResDetails res, String deviceId) throws PSSException {
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
        log.debug("gen_VPLS_teardown start");
        if (res != null) {
            this.populateTeardownFromResDetails(res, deviceId);
        }
        Map root = new HashMap();
        root.put("ifces", ifces);
        root.put("paths", paths);
        root.put("lsps", lsps);
        root.put("filters", filters);
        root.put("policy", policy);
        root.put("policer", policer);
        root.put("vpls", vpls);
        root.put("community", community);


        String templateFile = "junos-mx-vpls-teardown.txt";
        String config       = EoMPLSUtils.generateConfig(root, templateFile);

        log.debug("gen_VPLS_teardown done");
        return config;
    }


    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }

    public String getPolicy() {
        return policy;
    }
    public void setPolicy(String p) {
        this.policy = p;
    }

    public HashMap getCommunity() {
        return community;
    }

    public HashMap getFilters() {
        return filters;
    }

    public HashMap getPolicer() {
        return policer;
    }

    public HashMap getVpls() {
        return vpls;
    }

    public ArrayList getIfces() {
        return ifces;
    }

    public ArrayList getPaths() {
        return paths;
    }

    public ArrayList getLsps() {
        return lsps;
    }

}
