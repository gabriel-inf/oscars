package net.es.oscars.nsibridge.prov;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.StpConfig;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationRequestCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.P2PServiceBaseType;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.*;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class NSI_OSCARS_Translation {
    private static final Logger log = Logger.getLogger(NSI_OSCARS_Translation.class);
    public static CancelResContent makeOscarsCancel(String oscarsGri) {
        CancelResContent crc = new CancelResContent();
        crc.setGlobalReservationId(oscarsGri);
        return crc;


    }

    public static ModifyResContent makeOscarsModify(ResvRequest req) throws TranslationException {

        ModifyResContent mrc = new ModifyResContent();
        throw new TranslationException("modify not implemented");
        // TODO
        // return mrc;
    }





    public static ResCreateContent makeOscarsResv(ResvRequest req) throws TranslationException {
        ReservationRequestCriteriaType crit = req.getReserveType().getCriteria();
        String nsiLog;
        String oscarsLog;


        ResCreateContent rc = new ResCreateContent();
        UserRequestConstraintType urc = new UserRequestConstraintType();
        rc.setUserRequestConstraint(urc);
        PathInfo pi = new PathInfo();
        Layer2Info l2i = new Layer2Info();
        CtrlPlanePathContent path = new CtrlPlanePathContent();

        urc.setPathInfo(pi);
        pi.setLayer2Info(l2i);
        pi.setPath(path);
        List<CtrlPlaneHopContent> pathHops = path.getHop();
        pi.setPathType("loose");
        pi.setPathSetupMode("signal-xml");

        rc.setDescription(req.getReserveType().getDescription());

        P2PServiceBaseType p2ps = null;
        for (Object o : crit.getAny()) {
            if (o instanceof P2PServiceBaseType) {
                p2ps = (P2PServiceBaseType) o;
            }
        }
        if (p2ps == null) {
            throw new TranslationException("no p2ps element!");
        }
        Long capacity = p2ps.getCapacity();
        int bandwidth = capacity.intValue();


        urc.setBandwidth(bandwidth);
        urc.setStartTime(crit.getSchedule().getStartTime().toGregorianCalendar().getTimeInMillis() / 1000);
        urc.setEndTime(crit.getSchedule().getEndTime().toGregorianCalendar().getTimeInMillis() / 1000);
        String srcStp = p2ps.getSourceSTP().getLocalId();
        String dstStp = p2ps.getDestSTP().getLocalId();


        StpConfig srcStpCfg = findStp(srcStp);
        StpConfig dstStpCfg = findStp(dstStp);
        String nsiSrcVlan = null;
        String nsiDstVlan = null;
        for (TypeValuePairType tvp : p2ps.getSourceSTP().getLabels().getAttribute()) {
            if (tvp.getType().toUpperCase().equals("VLAN")) {
                nsiSrcVlan = tvp.getValue().get(0);
            }
        }
        for (TypeValuePairType tvp : p2ps.getDestSTP().getLabels().getAttribute()) {
            if (tvp.getType().toUpperCase().equals("VLAN")) {
                nsiDstVlan = tvp.getValue().get(0);
            }
        }
        if (nsiSrcVlan == null) {
            throw new TranslationException("no src vlan in NSI message!");
        }

        if (nsiDstVlan == null) {
            throw new TranslationException("no dst vlan in NSI message!");
        }
        nsiLog = "nsi gri: "+req.getReserveType().getGlobalReservationId()+"\n";
        nsiLog += "nsi connId: "+req.getReserveType().getConnectionId()+"\n";
        nsiLog += "src stp: "+srcStp+" vlan: "+nsiSrcVlan+"\n";
        nsiLog += "dst stp: "+dstStp+" vlan: "+nsiDstVlan+"\n";
        nsiLog += "stime "+crit.getSchedule().getStartTime();
        nsiLog += "etime "+crit.getSchedule().getEndTime();




        pi.getLayer2Info().setSrcEndpoint(srcStpCfg.getOscarsId());
        VlanTag srcVlan = new VlanTag();
        srcVlan.setValue(nsiSrcVlan);
        srcVlan.setTagged(true);
        pi.getLayer2Info().setSrcVtag(srcVlan);

        pi.getLayer2Info().setDestEndpoint(dstStpCfg.getOscarsId());
        VlanTag dstVlan = new VlanTag();
        dstVlan.setValue(nsiDstVlan);
        dstVlan.setTagged(true);
        pi.getLayer2Info().setDestVtag(dstVlan);



        CtrlPlaneHopContent srcHop = new CtrlPlaneHopContent();
        srcHop.setLinkIdRef(srcStpCfg.getOscarsId());
        CtrlPlaneLinkContent srcLink = new CtrlPlaneLinkContent();
        CtrlPlaneSwcapContent srcSwcap = new CtrlPlaneSwcapContent();
        CtrlPlaneSwitchingCapabilitySpecificInfo srcSwcapInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        srcSwcapInfo.setVlanRangeAvailability(nsiSrcVlan);
        srcSwcap.setSwitchingCapabilitySpecificInfo(srcSwcapInfo);
        srcLink.setSwitchingCapabilityDescriptors(srcSwcap);
        srcLink.setId(srcHop.getLinkIdRef());
        srcHop.setLinkIdRef(null);
        srcHop.setLink(srcLink);

        CtrlPlaneHopContent dstHop = new CtrlPlaneHopContent();
        dstHop.setLinkIdRef(dstStpCfg.getOscarsId());
        CtrlPlaneLinkContent dstLink = new CtrlPlaneLinkContent();
        CtrlPlaneSwcapContent dstSwcap = new CtrlPlaneSwcapContent();
        CtrlPlaneSwitchingCapabilitySpecificInfo dstSwcapInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        dstSwcapInfo.setVlanRangeAvailability(nsiDstVlan);
        dstSwcap.setSwitchingCapabilitySpecificInfo(dstSwcapInfo);
        dstLink.setSwitchingCapabilityDescriptors(dstSwcap);
        dstLink.setId(dstHop.getLinkIdRef());
        dstHop.setLinkIdRef(null);
        dstHop.setLink(dstLink);


        pathHops.add(srcHop);
        pathHops.add(dstHop);




        oscarsLog = "osc src: "+pi.getLayer2Info().getSrcEndpoint();
        oscarsLog += "osc dst: "+pi.getLayer2Info().getDestEndpoint();
        log.debug(nsiLog);
        log.debug(oscarsLog);


        return rc;

    }

    public static CreatePathContent makeOscarsSetup(String gri) throws TranslationException {
        CreatePathContent cp = new CreatePathContent();
        cp.setGlobalReservationId(gri);
        return cp;
    }

    public static TeardownPathContent makeOscarsTeardown(String gri) throws TranslationException {
        TeardownPathContent tp = new TeardownPathContent();
        tp.setGlobalReservationId(gri);
        return tp;
    }

    public static QueryResContent makeOscarsQuery(String gri) throws TranslationException {
        QueryResContent tp = new QueryResContent();
        tp.setGlobalReservationId(gri);
        return tp;
    }



    public static StpConfig findStp(String stpId) {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");


        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);

        NsaConfig nc = np.getConfig("local");

        List<StpConfig> stps = nc.getStps();

        for (StpConfig cfg : stps) {
            if (cfg.getStpId().equals(stpId)) return cfg;
        }

        log.info("could not find STP config for: "+stpId+", generating a default one");
        StpConfig def = new StpConfig();
        def.setOscarsId(stpId);
        def.setStpId(stpId);
        return def;
    }

}
