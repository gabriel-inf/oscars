package net.es.oscars.nsibridge.prov;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.StpConfig;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ChildRecursiveType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ChildSummaryType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ConnectionStatesType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.DataPlaneStatusType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryRecursiveResultCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryRecursiveResultType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryConfirmedType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationRequestCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ScheduleType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.EthernetVlanType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.DirectionalityType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.OrderedStpType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.StpListType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.StpType;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;
import net.es.oscars.utils.topology.PathTools;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.*;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;


public class NSI_OSCARS_Translation {
    private static final Logger log = Logger.getLogger(NSI_OSCARS_Translation.class);
    public static CancelResContent makeOscarsCancel(String oscarsGri) {
        CancelResContent crc = new CancelResContent();
        crc.setGlobalReservationId(oscarsGri);
        return crc;


    }

    public static ModifyResContent makeOscarsModify(ResvRequest req, String oscarsGri) throws TranslationException {
        log.debug("modify gri: "+oscarsGri);

        ReservationRequestCriteriaType crit = req.getReserveType().getCriteria();
        EthernetVlanType evts = null;
        for (Object o : crit.getAny()) {
            if (o instanceof EthernetVlanType ) {
                evts = (EthernetVlanType) o;
            } else {
                try {
                    JAXBElement<EthernetVlanType> payload = (JAXBElement<EthernetVlanType>) o;
                    evts = payload.getValue();
                } catch (ClassCastException ex) {
                    evts = null;
                }

            }
        }

        if (evts == null) {
            throw new TranslationException("no evts element!");
        }

        Long capacity = evts.getCapacity();
        int bandwidth = capacity.intValue();

        Long startTime = crit.getSchedule().getStartTime().toGregorianCalendar().getTimeInMillis() / 1000;
        Long endTime = crit.getSchedule().getEndTime().toGregorianCalendar().getTimeInMillis() / 1000;

        ModifyResContent mrc = new ModifyResContent();
        mrc.setGlobalReservationId(oscarsGri);
        UserRequestConstraintType urc = new UserRequestConstraintType();
        mrc.setUserRequestConstraint(urc);
        urc.setBandwidth(bandwidth);
        urc.setStartTime(startTime);
        urc.setEndTime(endTime);

        return mrc;
    }


    public static ModifyResContent makeOscarsRollback(ResvRecord resvRecord, String oscarsGri) throws TranslationException {
        log.debug("rollback gri: "+oscarsGri);

        Long capacity = resvRecord.getCapacity();
        int bandwidth = capacity.intValue();

        Long startTime = resvRecord.getStartTime().getTime() / 1000;
        Long endTime = resvRecord.getEndTime().getTime() / 1000;

        ModifyResContent mrc = new ModifyResContent();
        mrc.setGlobalReservationId(oscarsGri);
        UserRequestConstraintType urc = new UserRequestConstraintType();
        mrc.setUserRequestConstraint(urc);
        urc.setBandwidth(bandwidth);
        urc.setStartTime(startTime);
        urc.setEndTime(endTime);

        return mrc;
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

        EthernetVlanType evts = null;
        for (Object o : crit.getAny()) {
            if (o instanceof EthernetVlanType ) {
                evts = (EthernetVlanType) o;
            } else {
                try {
                    JAXBElement<EthernetVlanType> payload = (JAXBElement<EthernetVlanType>) o;
                    evts = payload.getValue();
                } catch (ClassCastException ex) {
                    evts = null;
                }

            }
        }

        if (evts == null) {
            throw new TranslationException("no evts element!");
        }

        Long capacity = evts.getCapacity();
        int bandwidth = capacity.intValue();


        urc.setBandwidth(bandwidth);
        urc.setStartTime(crit.getSchedule().getStartTime().toGregorianCalendar().getTimeInMillis() / 1000);
        urc.setEndTime(crit.getSchedule().getEndTime().toGregorianCalendar().getTimeInMillis() / 1000);
        String srcStp = evts.getSourceSTP().getLocalId();
        String dstStp = evts.getDestSTP().getLocalId();


        StpConfig srcStpCfg = findStp(srcStp);
        StpConfig dstStpCfg = findStp(dstStp);

        String nsiSrcVlan = ""+evts.getSourceVLAN();
        String nsiDstVlan = ""+evts.getDestVLAN();
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
    
    public static QuerySummaryResultType makeNSIQueryResult(ConnectionRecord cr)  throws TranslationException {
        QuerySummaryResultType resultType = new QuerySummaryResultType();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();
        
        resultType.setConnectionId(cr.getConnectionId());
        resultType.setRequesterNSA(cr.getRequesterNSA());
        resultType.setGlobalReservationId(cr.getNsiGlobalGri());
        
        //Set connection states
        ConnectionStatesType cst = new ConnectionStatesType();
        NSI_Resv_SM rsm = smh.findNsiResvSM(cr.getConnectionId());
        NSI_Life_SM lsm = smh.findNsiLifeSM(cr.getConnectionId());
        NSI_Prov_SM psm = smh.findNsiProvSM(cr.getConnectionId());
        DataPlaneStatusType dst = new DataPlaneStatusType();
        //null checks to avoid errors, especially in stub mode
        if(lsm != null && lsm.getState() != null){
            LifecycleStateEnumType      lst = (LifecycleStateEnumType) lsm.getState().state();
            cst.setLifecycleState(lst);
        }
        if(psm != null && psm.getState() != null){
           //for now just link dataplane state to provision state since we don't have way to get this directly
            ProvisionStateEnumType      pst = (ProvisionStateEnumType) psm.getState().state();
            if(ProvisionStateEnumType.PROVISIONED.equals(pst.value()) ||
                    ProvisionStateEnumType.RELEASING.equals(pst.value())){
                dst.setActive(true);
            }else{
                dst.setActive(false);
            }
            cst.setProvisionState(pst);
        }
        if(rsm != null && rsm.getState() != null){
            ReservationStateEnumType    rst = (ReservationStateEnumType) rsm.getState().state();
            cst.setReservationState(rst);
        }
        dst.setVersion(1); //TODO: get this for real
        dst.setVersionConsistent(true);//always true for uPA         
        cst.setDataPlaneStatus(dst); //TODO: get this for real
        resultType.setConnectionStates(cst);

        //May not be GRI if there was a failure before hitting OSCARS
        if(cr.getOscarsGri() == null){
            return resultType;
        }
        
        //Set details of reservation based on query
        try {
            QueryResContent qc = NSI_OSCARS_Translation.makeOscarsQuery(cr.getOscarsGri());
            QueryResReply reply = OscarsProxy.getInstance().sendQuery(qc, cr.getSubjectDN(), cr.getIssuerDN());
            if(reply == null || reply.getReservationDetails() == null){
                throw new TranslationException("No matching OSCARS reservation found with oscars GRI " + cr.getOscarsGri());
            }
            //set description
            resultType.setDescription(reply.getReservationDetails().getDescription());
            //set criteria
            resultType.getCriteria().add(
                    NSI_OSCARS_Translation.makeNSIQuerySummaryCriteria(reply.getReservationDetails()));
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
            throw new TranslationException("Error returned from OSCARS query: " + e.getMessage());
        } catch (TranslationException e) {
            e.printStackTrace();
            throw new TranslationException("Unable to translate query request for OSCARS: " + e.getMessage());
        }
        
        return resultType;
    }
    
    public static QuerySummaryResultCriteriaType makeNSIQuerySummaryCriteria(ResDetails oscarsResDetails) throws TranslationException {
        QuerySummaryResultCriteriaType criteriaType = new QuerySummaryResultCriteriaType();
        
        //TODO: Actually determine the correct version
        criteriaType.setVersion(1);
        
        //set serviceType. only one allowed currently, so just pull from config
        criteriaType.setServiceType(findServiceType());
        
        //Determine if we should use UserConstraint or ReservedConstraint
        long startTime = 0;
        long endTime = 0;
        int bandwidth = 0;
        PathInfo pathInfo = null;
        if(oscarsResDetails.getReservedConstraint() != null){
            startTime = oscarsResDetails.getReservedConstraint().getStartTime();
            endTime = oscarsResDetails.getReservedConstraint().getEndTime();
            bandwidth  = oscarsResDetails.getReservedConstraint().getBandwidth(); 
            pathInfo = oscarsResDetails.getReservedConstraint().getPathInfo(); 
        }else if(oscarsResDetails.getUserRequestConstraint() != null){
            startTime = oscarsResDetails.getUserRequestConstraint().getStartTime();
            endTime = oscarsResDetails.getUserRequestConstraint().getEndTime();
            bandwidth  = oscarsResDetails.getUserRequestConstraint().getBandwidth();
            pathInfo = oscarsResDetails.getUserRequestConstraint().getPathInfo();  
        }else {
            //nothing to convert so exit
            return criteriaType;
        }
        
        //Set schedule fields
        ScheduleType scheduleType = new ScheduleType();
        GregorianCalendar startCal = new GregorianCalendar();
        startCal.setTimeInMillis(startTime*1000);
        GregorianCalendar endCal = new GregorianCalendar();
        endCal.setTimeInMillis(endTime*1000);
        try {
            scheduleType.setStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(startCal));
            scheduleType.setEndTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(endCal));
        } catch (DatatypeConfigurationException e) {
            throw new TranslationException("Unable to convert OSCARS time fields to NSI schedule: " + e.getMessage());
        }
        
        //Set P2P fields
        EthernetVlanType evtsType = new EthernetVlanType();
        evtsType.setCapacity(bandwidth);
        evtsType.setDirectionality(DirectionalityType.BIDIRECTIONAL); //change if oscars extended in future
        evtsType.setSymmetricPath(true);//change if oscars extended in future
        StpType sourceStp = new StpType();
        StpType destStp = new StpType();
        
        String nsaId = findNsaId();
        if(nsaId != null){
            sourceStp.setNetworkId(nsaId);
            destStp.setNetworkId(nsaId);
        }
        if(pathInfo.getPath() != null &&  pathInfo.getPath().getHop() != null && pathInfo.getPath().getHop().size() >= 2){
            List<CtrlPlaneHopContent> hops = pathInfo.getPath().getHop();
            sourceStp.setLocalId(findStpByOSCARSId(NMWGParserUtil.getURN(hops.get(0))).getStpId());
            if(PathTools.isVlanHop(hops.get(0))){
                String srcVlan = hops.get(0).getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();;
                evtsType.setSourceVLAN(Integer.valueOf(srcVlan));
            }
            
            destStp.setLocalId(findStpByOSCARSId(NMWGParserUtil.getURN(hops.get(hops.size() - 1))).getStpId());
            if(PathTools.isVlanHop(hops.get(hops.size() - 1))){
                String destVlan = hops.get(hops.size() - 1).getLink().getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();;
                evtsType.setDestVLAN(Integer.valueOf(destVlan));
            }

            //set ero
            StpListType ero = new StpListType();
            evtsType.setEro(ero);
            for(int i = 1; i < hops.size() - 2; i++){
                OrderedStpType ordStp = new OrderedStpType();
                StpType hopStp = new StpType();
                if(nsaId != null){
                    hopStp.setNetworkId(nsaId);
                }
                hopStp.setLocalId(findStpByOSCARSId(NMWGParserUtil.getURN(hops.get(i))).getStpId());
                ordStp.setOrder(i);
                ordStp.setStp(hopStp);
                ero.getOrderedSTP().add(ordStp);
            }
        }else if(pathInfo.getLayer2Info() != null){
            sourceStp.setLocalId(findStpByOSCARSId(pathInfo.getLayer2Info().getSrcEndpoint()).getStpId());
            if(pathInfo.getLayer2Info().getSrcVtag() != null && pathInfo.getLayer2Info().getSrcVtag().getValue() != null){
                String srcVlan = pathInfo.getLayer2Info().getSrcVtag().getValue();
                evtsType.setSourceVLAN(Integer.valueOf(srcVlan));
            }
            
            destStp.setLocalId(findStpByOSCARSId(pathInfo.getLayer2Info().getDestEndpoint()).getStpId());
            if(pathInfo.getLayer2Info().getDestVtag() != null && pathInfo.getLayer2Info().getDestVtag().getValue() != null){
                String dstVlan = pathInfo.getLayer2Info().getDestVtag().getValue();
                evtsType.setDestVLAN(Integer.valueOf(dstVlan));
            }
        }else{
            throw new TranslationException("OSCARS reservation has no path or Layer2Info set so cannot determine STPs");
        }
        evtsType.setSourceSTP(sourceStp);
        evtsType.setDestSTP(destStp);
        ObjectFactory objFactory = new ObjectFactory();
        criteriaType.getAny().add(objFactory.createEvts(evtsType));
        
        return criteriaType;
    }

    public static StpConfig findStp(String stpId) {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();


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
    
    public static StpConfig findStpByOSCARSId(String oscarsId) {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        oscarsId = NMWGParserUtil.normalizeURN(oscarsId);

        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);

        NsaConfig nc = np.getConfig("local");

        List<StpConfig> stps = nc.getStps();

        for (StpConfig cfg : stps) {
            if (NMWGParserUtil.normalizeURN(cfg.getOscarsId()).equals(oscarsId)) return cfg;
        }

        log.info("could not find STP config for: "+oscarsId+", generating a default one");
        StpConfig def = new StpConfig();
        def.setOscarsId(oscarsId);
        def.setStpId(oscarsId);
        return def;
    }
    
    public static String findNsaId(){
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);
        NsaConfig nc = np.getConfig("local");
        
        return nc.getNsaId();
    }
    
    public static String findServiceType(){
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);
        NsaConfig nc = np.getConfig("local");
        
        return nc.getServiceType();
    }

    public static List<QueryRecursiveResultType> querySummToRecursive(
            QuerySummaryConfirmedType summResult) {
        List<QueryRecursiveResultType> recursiveList = new ArrayList<QueryRecursiveResultType>();
        
        for(QuerySummaryResultType summResv : summResult.getReservation()){
            QueryRecursiveResultType recResult = new QueryRecursiveResultType();
            recResult.setConnectionId(summResv.getConnectionId());
            recResult.setConnectionStates(summResv.getConnectionStates());
            recResult.setDescription(summResv.getDescription());
            recResult.setGlobalReservationId(summResv.getGlobalReservationId());
            recResult.setNotificationId(summResv.getNotificationId());
            recResult.setRequesterNSA(summResv.getRequesterNSA());
            for(QuerySummaryResultCriteriaType summCrit : summResv.getCriteria()){
                if(summCrit.getChildren() != null){
                    throw new RuntimeException("Connection has children but " +
                            "recursively querying children not supported at this time.");
                }
                QueryRecursiveResultCriteriaType recCrit = new QueryRecursiveResultCriteriaType();
                recCrit.setSchedule(summCrit.getSchedule());
                recCrit.setServiceType(summCrit.getServiceType());
                recCrit.setVersion(summCrit.getVersion());
                recCrit.getAny().addAll(summCrit.getAny());
                recCrit.getOtherAttributes().putAll(summCrit.getOtherAttributes());
                
            }
            recursiveList.add(recResult);
        }
        
        return recursiveList;
    }

}
