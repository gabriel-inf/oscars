package net.es.oscars.pss.eompls.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.es.oscars.pss.api.*;
import net.es.oscars.pss.eompls.api.VplsImplementation;
import net.es.oscars.pss.eompls.api.VplsV2ConfigGenerator;
import net.es.oscars.pss.eompls.util.VPLS_RequestParamHolder;
import net.es.oscars.pss.eompls.util.VPLS_RequestParams;
import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.util.ActionUtils;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.pss.util.ConnectorUtils;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.topology.PathTools;

public class EoMPLSService implements CircuitService {
    private Logger log = Logger.getLogger(EoMPLSService.class);
    public static final String SVC_ID = "eompls";

    public List<PSSAction> modify(List<PSSAction> actions) throws PSSException {
        return processActions(actions, ActionType.MODIFY);
    }
    
    public List<PSSAction> setup(List<PSSAction> actions) throws PSSException {
        return processActions(actions, ActionType.SETUP);
    }
    
    public List<PSSAction> teardown(List<PSSAction> actions) throws PSSException {
        return processActions(actions, ActionType.TEARDOWN);
    }

    // TODO: implement status checking
    public List<PSSAction> status(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }




    private List<PSSAction> processActions(List<PSSAction> actions, ActionType actionType) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();

        for (PSSAction action : actions) {
            ResDetails res = null;
            switch (actionType) {
                case MODIFY:
                    res = action.getRequest().getModifyReq().getReservation();
                    break;
                case SETUP:
                    res = action.getRequest().getSetupReq().getReservation();
                    break;
                case TEARDOWN:
                    res = action.getRequest().getTeardownReq().getReservation();
                    break;
                case STATUS:
                    res = action.getRequest().getSetupReq().getReservation();
                    break;
                default:
                    throw new PSSException("invalid actiontype: "+actionType);
            }

            this.prepareAction(action, res);


            String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
            String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
            boolean sameDevice = srcDeviceId.equals(dstDeviceId);
            if (!sameDevice) {
                log.debug("source edge device id is: "+srcDeviceId+", starting "+actionType);
                action = this.processActionForDevice(action, srcDeviceId);
                log.debug("destination edge device id is: "+dstDeviceId+", starting "+actionType);
                action = this.processActionForDevice(action, dstDeviceId);
            } else {
                log.debug("only device id is: "+srcDeviceId+", starting same-device "+actionType);
                action = this.processActionForDevice(action, dstDeviceId);

            }

            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            // this notifies the coordinator we have succeeded
            ClassFactory.getInstance().getWorkflow().update(action);

            // now if there's a post-commit for this action, do it
            if (!sameDevice) {
                processPostCommitActionForDevice(action, srcDeviceId);
                log.debug("destination edge device id is: "+dstDeviceId+", starting "+actionType+" post-commit command");
                processPostCommitActionForDevice(action, dstDeviceId);
            } else {
                log.debug("only device id is: "+srcDeviceId+", starting same-device "+actionType+" post-commit command");
                processPostCommitActionForDevice(action, dstDeviceId);

            }
        }
        return results;
    }

    public void prepareAction(PSSAction action, ResDetails res) throws PSSException {
        ActionType actionType = action.getActionType();
        switch (actionType) {
            case SETUP:
                break;
            case TEARDOWN:
                break;
            case MODIFY:
                return;
            case STATUS:
                return;
        }


        List<String> deviceIds = new ArrayList<String>();
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        deviceIds.add(srcDeviceId);
        if (!srcDeviceId.equals(dstDeviceId)) deviceIds.add(dstDeviceId);

        String gri = res.getGlobalReservationId();
        HashMap<String, VplsImplementation> implMap = VPLS_RequestParams.getImplementationMap(deviceIds);

        boolean all_supported = true;
        boolean one_supported = false;
        boolean one_junos = false;
        boolean one_alu = false;
        boolean may_need_secondary_vpls_id = false;

        for (String deviceId : deviceIds) {
            log.debug(deviceId +" implementation: "+implMap.get(deviceId));
            if (implMap.get(deviceId).equals(VplsImplementation.UNSUPPORTED)) {
                all_supported = false;
            } else {
                one_supported = true;
            }
            if (implMap.get(deviceId).equals(VplsImplementation.JUNOS)) {
                one_junos = true;
            }
            if (implMap.get(deviceId).equals(VplsImplementation.ALU)) {
                one_alu = true;
            }
        }
        if (one_supported && !all_supported) {
            throw new PSSException("VPLS implementation mismatch");
        }
        if (!all_supported) return;

        if (one_alu && one_junos) {
            may_need_secondary_vpls_id = true;
        }

        VPLS_RequestParamHolder holder = VPLS_RequestParamHolder.getInstance();
        VPLS_RequestParams rp = new VPLS_RequestParams();
        switch (actionType) {
            case SETUP:
                rp.reserve(res, may_need_secondary_vpls_id);
                break;
            case TEARDOWN:
                rp.release(res);
                break;
        }

        holder.getRequestParams().put(gri, rp);
    }

    private void processPostCommitActionForDevice(PSSAction action, String deviceId) {
        String errorMessage = null;
        OSCARSFaultReport faultReport = new OSCARSFaultReport ();
        faultReport.setDomainId(PathTools.getLocalDomainId());


        DeviceConfigGenerator cg;
        try {
            cg = ConnectorUtils.getDeviceConfigGenerator(deviceId, SVC_ID);
        } catch (PSSException e) {
            log.error(e);
            return;
        }
        PostCommitConfigGen pcg;
        if (cg instanceof PostCommitConfigGen) {
            pcg = (PostCommitConfigGen) cg;
        } else {
            return;
        }



        String deviceCommand = pcg.getPostCommitConfig(action, deviceId);
        String deviceAddress = null;
        Connector conn = null;
        try {
            deviceAddress = ConnectorUtils.getDeviceAddress(deviceId);
            conn = ClassFactory.getInstance().getDeviceConnectorMap().getDeviceConnector(deviceId);
        } catch (PSSException ex) {
            log.error(ex.getMessage(), ex);
            return;
        }
        log.debug("connector for "+deviceId+" is: "+conn.getClass());

        if (ConfigHolder.getInstance().getBaseConfig().getCircuitService().isStub()) {
            log.debug("stub mode! connector will not send commands");
        }


        PSSCommand comm = new PSSCommand();
        comm.setDeviceCommand(deviceCommand);
        comm.setDeviceAddress(deviceAddress);
        try {
            conn.sendCommand(comm);
        } catch (PSSException e) {
            log.error("post-commit command failed");
        }
        log.info("sent post-commit command!");

    }

    
    private PSSAction processActionForDevice(PSSAction action, String deviceId) throws PSSException {
        String errorMessage = null;
        OSCARSFaultReport faultReport = new OSCARSFaultReport ();
        faultReport.setDomainId(PathTools.getLocalDomainId());
        ResDetails res = null;

        try {
            res = ActionUtils.getReservation(action);
        } catch (PSSException e) {
            log.error(e);
            errorMessage = "Could not locate ResDetails for device "+deviceId+"\n"+e.getMessage();
            action.setStatus(ActionStatus.FAIL);
            faultReport.setErrorMsg(errorMessage);
            faultReport.setErrorType(ErrorReport.SYSTEM);
            faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
            action.setFaultReport(faultReport);
            ClassFactory.getInstance().getWorkflow().update(action);
            throw new PSSException(e);
        }
        
        DeviceConfigGenerator cg;
        try {
            cg = ConnectorUtils.getDeviceConfigGenerator(deviceId, SVC_ID);
        } catch (PSSException e) {
            log.error(e);
            errorMessage = "Could not locate config generator for device "+deviceId+"\n"+e.getMessage();
            action.setStatus(ActionStatus.FAIL);
            faultReport.setErrorMsg(errorMessage);
            faultReport.setGri(res.getGlobalReservationId());
            faultReport.setErrorType(ErrorReport.SYSTEM);
            faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
            action.setFaultReport(faultReport);
            ClassFactory.getInstance().getWorkflow().update(action);
            throw new PSSException(e);
        }
        
        String deviceCommand = cg.getConfig(action, deviceId);
        String deviceAddress = ConnectorUtils.getDeviceAddress(deviceId);
        
        Connector conn = ClassFactory.getInstance().getDeviceConnectorMap().getDeviceConnector(deviceId);
        log.debug("connector for "+deviceId+" is: "+conn.getClass());
        
        if (ConfigHolder.getInstance().getBaseConfig().getCircuitService().isStub()) {
            log.debug("stub mode! connector will not send commands");
        } 
        
        PSSCommand comm = new PSSCommand();
        comm.setDeviceCommand(deviceCommand);
        comm.setDeviceAddress(deviceAddress);
        try {
            conn.sendCommand(comm);
        } catch (PSSException e) {
            log.error(e.getMessage());
            action.setStatus(ActionStatus.FAIL);
            faultReport.setErrorMsg(errorMessage);
            faultReport.setGri(res.getGlobalReservationId());
            faultReport.setErrorType(ErrorReport.SYSTEM);
            if (action.getActionType().equals(ActionType.MODIFY)) {
                faultReport.setErrorCode(ErrorCodes.PATH_MODIFY_FAILED);
            } else if (action.getActionType().equals(ActionType.SETUP)) {
                faultReport.setErrorCode(ErrorCodes.PATH_SETUP_FAILED);
            } else if (action.getActionType().equals(ActionType.STATUS)) {
                faultReport.setErrorCode(ErrorCodes.UNKNOWN);
            } else if (action.getActionType().equals(ActionType.TEARDOWN)) {
                faultReport.setErrorCode(ErrorCodes.PATH_TEARDOWN_FAILED);
                
            }
            action.setFaultReport(faultReport);
            
            ClassFactory.getInstance().getWorkflow().update(action);
            throw e;
        }
        log.info("sent command!");
        return action;
    }






    public void setConfig(CircuitServiceConfig config) {
    }

}
