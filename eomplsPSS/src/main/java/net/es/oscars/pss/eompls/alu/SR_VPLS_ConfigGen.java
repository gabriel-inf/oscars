package net.es.oscars.pss.eompls.alu;


import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.beans.GeneratedConfig;
import net.es.oscars.pss.eompls.beans.LSP;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.dao.GeneratedConfigDAO;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SR_VPLS_ConfigGen implements DeviceConfigGenerator {
    private Logger log = Logger.getLogger(SR_VPLS_ConfigGen.class);


    protected ArrayList ifces = new ArrayList();
    protected ArrayList paths = new ArrayList();
    protected ArrayList lsps = new ArrayList();
    protected ArrayList sdps = new ArrayList();
    protected HashMap vpls = new HashMap();
    protected HashMap ingqos = new HashMap();




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
            throw new PSSException("Same device crossconnects not supported with VPLS ");
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
            throw new PSSException("Same device crossconnects not supported with VPLS");
        } else {
            return this.gen_VPLS_teardown(res, deviceId);
        }
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String gen_VPLS_teardown(ResDetails res, String deviceId) throws PSSException {
        String templateFile = "alu-vpls-teardown.txt";
        Map root = new HashMap();
        String teardownConfig = null;


        if (res != null) {
            String gri = res.getGlobalReservationId();
            String dbname = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname();
            SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
            sf.getCurrentSession().beginTransaction();
            GeneratedConfigDAO gcDAO =  new GeneratedConfigDAO(dbname);



            List<GeneratedConfig> gcs= gcDAO.getGC("TEARDOWN", gri, deviceId);
            if (gcs.isEmpty()) {
                log.error("could not find saved generated config for gri "+gri+" device "+deviceId+" TEARDOWN ");
            } else if (gcs.size() > 1) {

                log.error("found multiple generated configs for gri "+gri+" device "+deviceId+" for TEARDOWN ");
            } else {
                log.debug("found a saved config for gri "+gri+" device "+deviceId+" for TEARDOWN");
                teardownConfig = gcs.get(0).getConfig();
            }

            if (teardownConfig == null) {
                this.populateTeardownFromResDetails(res, deviceId);
            } else {
                ALUNameGenerator ng = ALUNameGenerator.getInstance();

                List<Integer> ids = ng.releaseVplsIds(deviceId, gri);
                ids = ng.releaseQosIds(deviceId, gri);
                ids = ng.releaseSdpIds(deviceId, gri);
                return teardownConfig;
            }
        }


        root.put("vpls", vpls);
        root.put("ingqos", ingqos);
        root.put("ifces", ifces);
        root.put("paths", paths);
        root.put("sdps", sdps);
        root.put("lsps", lsps);

        String config       = EoMPLSUtils.generateConfig(root, templateFile);
        log.debug("get_VPLS_teardown done");



        return config;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String gen_VPLS_setup(ResDetails res, String deviceId) throws PSSException  {
        String templateFile = "alu-vpls-setup.txt";
        Map root = new HashMap();


        if (res != null) {
            this.populateSetupFromResDetails(res, deviceId);
        }

        root.put("vpls", vpls);
        root.put("ingqos", ingqos);
        root.put("ifces", ifces);
        root.put("paths", paths);
        root.put("sdps", sdps);
        root.put("lsps", lsps);

        String setupConfig       = EoMPLSUtils.generateConfig(root, templateFile);



        if (res != null) {
            String dbname = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname();
            SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
            sf.getCurrentSession().beginTransaction();
            GeneratedConfigDAO gcDAO =  new GeneratedConfigDAO(dbname);
            GeneratedConfig setupGC = new GeneratedConfig();
            setupGC.setConfig(setupConfig);
            setupGC.setDeviceId(deviceId);
            setupGC.setGri(res.getGlobalReservationId());
            setupGC.setPhase("SETUP");
            gcDAO.update(setupGC);
            log.debug("saved setup config");


            String teardownConfig = this.gen_VPLS_teardown(res, deviceId);

            GeneratedConfig teardownGC = new GeneratedConfig();
            teardownGC.setConfig(teardownConfig);
            teardownGC.setDeviceId(deviceId);
            teardownGC.setGri(res.getGlobalReservationId());
            teardownGC.setPhase("TEARDOWN");
            gcDAO.update(teardownGC);
            log.debug("saved teardown config");

        }

        log.debug("get_VPLS_setup done");
        return setupConfig;
    }


    private void populateTeardownFromResDetails(ResDetails res, String deviceId) throws PSSException  {
        ifces = new ArrayList();
        paths = new ArrayList();
        lsps = new ArrayList();
        sdps = new ArrayList();
        vpls = new HashMap();
        ingqos = new HashMap();


        // TODO: fix when multipoint is designed

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String gri = res.getGlobalReservationId();
        ALUNameGenerator ng = ALUNameGenerator.getInstance();

        String pathName;
        String lspName;
        String ifceName;
        String ifceVlan;



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



        pathName                = ng.getPathName(ifceVlan);
        lspName                 = ng.getLSPName(ifceVlan);
        String vplsId;
        String qosId;
        String sdpId;

        String error = "";
        List<Integer> ids = ng.releaseVplsIds(deviceId, gri);
        if (ids.isEmpty()) {
            error = "Could not find saved VPLS id for gri "+gri+" at device"+deviceId;
            log.error(error);
            throw new PSSException(error);
        } else if (ids.size() > 1) {
            error = "DB error: more than one saved VPLS id for gri "+gri+" at device"+deviceId;
            log.error(error);
            throw new PSSException(error);
        } else {
            vplsId = ids.get(0).toString();
        }

        ids = ng.releaseQosIds(deviceId, gri);
        if (ids.isEmpty()) {
            error = "Could not find saved QoS id for gri "+gri+" at device"+deviceId;
            log.error(error);
            throw new PSSException(error);
        } else if (ids.size() > 1) {
            error = "DB error: more than one saved QoS id for gri "+gri+" at device"+deviceId;
            log.error(error);
            throw new PSSException(error);
        } else {
            qosId = ids.get(0).toString();
        }
        ids = ng.releaseSdpIds(deviceId, gri);
        if (ids.isEmpty()) {
            error = "Could not find saved SDP id for gri "+gri+" at device"+deviceId;
            log.error(error);
            throw new PSSException(error);
        } else if (ids.size() > 1) {
            error = "DB error: more than one saved SDP id for gri "+gri+" at device"+deviceId;
            log.error(error);
            throw new PSSException(error);
        } else {
            sdpId = ids.get(0).toString();
        }



        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();
        Map sdp = new HashMap();


        // fill in scalars
        /*
        1. vpls: id
        2. ingqos: id
        3. ifces: list_of <name, vlan>
        4. paths: list_of <name>
        5. lsps: list_of <name>
        6. sdps: list_of <id>
        */
        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);

        path.put("name", pathName);
        lsp.put("name", lspName);
        sdp.put("id", sdpId);

        ifces.add(ifce);
        paths.add(path);
        lsps.add(lsp);
        sdps.add(sdp);

        ingqos.put("id", qosId);
        vpls.put("id", vplsId);



    }
    private void populateSetupFromResDetails(ResDetails res, String deviceId) throws PSSException  {

        ifces = new ArrayList();
        paths = new ArrayList();
        lsps = new ArrayList();
        sdps = new ArrayList();
        vpls = new HashMap();
        ingqos = new HashMap();

        // TODO: fix when multipoint is designed
        String gri = res.getGlobalReservationId();

        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        System.out.println("gri: "+gri+" src device: " + srcDeviceId + " this one: " + deviceId);


        ALUNameGenerator ng = ALUNameGenerator.getInstance();

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



        String ifceName;
        String ifceVlan;


        // bandwidth in Mbps
        Long ingQosBandwidth = 1L*bw;

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

        String vplsId = ng.getVplsId(deviceId, null, gri);
        String qosId  = ng.getQosId(deviceId, Integer.getInteger(vplsId), gri);
        String sdpId  = ng.getSdpId(deviceId, Integer.getInteger(vplsId), gri);

        vpls.put("id", vplsId);
        vpls.put("description", gri);

        ingqos.put("id", qosId);
        ingqos.put("description", gri);
        ingqos.put("bandwidth", ingQosBandwidth);


        HashMap ifceInfo = new HashMap();
        ifceInfo.put("name", ifceName);
        ifceInfo.put("vlan", ifceVlan);
        ifces.add(ifceInfo);

        LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);



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
        path.put("name", ng.getPathName(gri));
        path.put("hops", hops);
        paths.add(path);



        String lspFrom         = dar.getDeviceAddress(lspOriginDeviceId);
        String lspTo           = dar.getDeviceAddress(lspTargetDeviceId);

        HashMap lsp = new HashMap();
        lsp.put("from", lspFrom);
        lsp.put("to", lspTo);
        lsp.put("name", ng.getLSPName(gri));
        lsp.put("path", ng.getPathName(gri));
        lsps.add(lsp);



        HashMap sdp = new HashMap();
        sdp.put("id", sdpId);
        sdp.put("description", gri);
        sdp.put("far_end", lspTo);
        sdp.put("lsp_name", ng.getLSPName(gri));
        sdps.add(sdp);


    }
    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
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

    public ArrayList getSdps() {
        return sdps;
    }

    public HashMap getVpls() {
        return vpls;
    }

    public HashMap getIngqos() {
        return ingqos;
    }


}
