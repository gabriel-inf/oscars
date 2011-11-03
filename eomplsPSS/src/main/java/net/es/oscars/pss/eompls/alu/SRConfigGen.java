package net.es.oscars.pss.eompls.alu;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.eompls.beans.LSP;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.junos.SDNNameGenerator;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;

public class SRConfigGen implements DeviceConfigGenerator {
    private Logger log = Logger.getLogger(SRConfigGen.class);
   

    
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
        action.setStatus(ActionStatus.SUCCESS);
        return "";
    }
    private String getSetup(PSSAction action, String deviceId) throws PSSException {
        log.debug("getSetup start");
        
        ResDetails res = action.getRequest().getSetupReq().getReservation();
        
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        
        if (sameDevice) {
            throw new PSSException("Same device crossconnects not supported on IOS");
        } else {
            return this.getLSPSetup(res, deviceId);
        }
    }
    
    
    private String getTeardown(PSSAction action, String deviceId) throws PSSException {
        log.debug("getTeardown start");
        
        ResDetails res = action.getRequest().getTeardownReq().getReservation();
        
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        boolean sameDevice = srcDeviceId.equals(dstDeviceId);
        
        if (sameDevice) {
            throw new PSSException("Same device crossconnects not supported on IOS");
        } else {
            return this.getLSPTeardown(res, deviceId);
        }
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String getLSPTeardown(ResDetails res, String deviceId) throws PSSException {
        String templateFile = "alu-epipe-teardown.txt";

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        
        String ingQosId;
        String egrQosId;
        String epipeId;
        String sdpId;
        String pathName;
        String lspName;
        String ifceName;
        String ifceVlan;
        
        
        String gri = res.getGlobalReservationId();
        ALUNameGenerator ng = ALUNameGenerator.getInstance();


        pathName                = ng.getPathName(gri);
        lspName                 = ng.getLSPName(gri);
        ingQosId                = ng.getQosId(gri);
        egrQosId                = ng.getQosId(gri);
        sdpId                   = ng.getSdpId(gri);
        epipeId                 = ng.getEpipeId(gri);
        
        ReservedConstraintType rc = res.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();

        CtrlPlaneLinkContent ingressLink = pi.getPath().getHop().get(0).getLink();
        CtrlPlaneLinkContent egressLink = pi.getPath().getHop().get(pi.getPath().getHop().size()-1).getLink();
        
        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
        

        log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
        if (srcDeviceId.equals(deviceId)) {
            // forward direction
            log.debug("forward");
            ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
        } else {
            // reverse direction
            log.debug("reverse");
            ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
        }

        Map root = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();
        Map epipe = new HashMap();
        Map sdp = new HashMap();
        Map ingqos = new HashMap();
        Map egrqos = new HashMap();

        // set up data model structure
        root.put("path", path);
        root.put("lsp", lsp);
        root.put("ifce", ifce);
        root.put("epipe", epipe);
        root.put("sdp", sdp);
        root.put("ingqos", ingqos);
        root.put("egrqos", egrqos);

        // fill in scalars
        
        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);
        
        path.put("name", pathName);
        lsp.put("name", lspName);
        ingqos.put("id", ingQosId);
        egrqos.put("id", egrQosId);
        epipe.put("id", epipeId);
        sdp.put("id", sdpId);
        
        
        
        String config       = EoMPLSUtils.generateConfig(root, templateFile);
        log.debug("getLSPSetup done");
        return config;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String getLSPSetup(ResDetails res, String deviceId) throws PSSException  {

        String templateFile = "alu-epipe-setup.txt";

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);

        String ifceName;
        String ifceVlan;
        
        String pathName;

        String lspName;
        String lspFrom;
        String lspTo;
        
        String ingQosId;
        String ingQosDesc;
        Long ingQosBandwidth;
        
        String egrQosId;
        String egrQosDesc;
        
        String epipeId;
        String epipeDesc;

        String sdpId;
        String sdpDesc;
        
        
        String gri = res.getGlobalReservationId();
        ALUNameGenerator ng = ALUNameGenerator.getInstance();


        
        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */
        

        ReservedConstraintType rc = res.getReservedConstraint();
        Integer bw = rc.getBandwidth();
        PathInfo pi = rc.getPathInfo();
       
        CtrlPlaneLinkContent ingressLink = pi.getPath().getHop().get(0).getLink();
        CtrlPlaneLinkContent egressLink = pi.getPath().getHop().get(pi.getPath().getHop().size()-1).getLink();
        
        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
        
        EoMPLSIfceAddressResolver iar = ecf.getEomplsIfceAddressResolver();
        EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();
        
        

        
        // bandwidth in Mbps 
        ingQosBandwidth = 1L*bw;
        
        String lspTargetDeviceId, lspOriginDeviceId;
        boolean reverse = false;
        log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
        if (srcDeviceId.equals(deviceId)) {
            // forward direction
            log.debug("forward");
            ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspOriginDeviceId = srcRes.getNodeId();
            lspTargetDeviceId = dstRes.getNodeId();
        } else {
            // reverse direction
            log.debug("reverse");
            ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            lspOriginDeviceId = dstRes.getNodeId();
            lspTargetDeviceId = srcRes.getNodeId();
            reverse = true;
        }
        lspFrom         = dar.getDeviceAddress(lspOriginDeviceId);
        lspTo           = dar.getDeviceAddress(lspTargetDeviceId);

        LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);

    
        ingQosId                = ng.getQosId(gri);
        egrQosId                = ng.getQosId(gri);
        sdpId                   = ng.getSdpId(gri);
        epipeId                 = ng.getEpipeId(gri);

        pathName                = ng.getPathName(gri);
        lspName                 = ng.getLSPName(gri);
        ingQosDesc              = gri;
        egrQosDesc              = gri;
        sdpDesc                 = gri;
        epipeDesc               = gri;

        Map root = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();
        Map epipe = new HashMap();
        Map sdp = new HashMap();
        Map ingqos = new HashMap();
        Map egrqos = new HashMap();
        ArrayList hops = new ArrayList();

        // set up data model structure
        root.put("path", path);
        root.put("lsp", lsp);
        root.put("ifce", ifce);
        root.put("epipe", epipe);
        root.put("sdp", sdp);
        root.put("ingqos", ingqos);
        root.put("egrqos", egrqos);

        // fill in scalars
        
        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);
        
        path.put("name", pathName);        
        path.put("hops", hops);
        int i = 5;
        for (String ipaddress : lspBean.getPathAddresses()) {
            Map hop = new HashMap();
            hop.put("address", ipaddress);
            hop.put("order", i);
            i += 5;
            hops.add(hop);
        }

        lsp.put("name", lspName);
        lsp.put("from", lspFrom);
        lsp.put("to", lspTo);
        
        
        ingqos.put("id", ingQosId);
        ingqos.put("description", ingQosDesc);
        ingqos.put("bandwidth", ingQosBandwidth);
        egrqos.put("id", egrQosId);
        egrqos.put("description", egrQosDesc);

        epipe.put("id", epipeId);
        epipe.put("description", epipeDesc);

        sdp.put("id", sdpId);
        sdp.put("description", sdpDesc);

        String config       = EoMPLSUtils.generateConfig(root, templateFile);

        log.debug("getLSPSetup done");
        return config;
    }
    
    
    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }


}
