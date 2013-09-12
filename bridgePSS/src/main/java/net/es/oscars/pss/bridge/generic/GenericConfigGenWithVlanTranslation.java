package net.es.oscars.pss.bridge.generic;


import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.api.TemplateDeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.beans.config.TemplateConfig;
import net.es.oscars.pss.bridge.beans.DeviceBridge;
import net.es.oscars.pss.bridge.util.BridgeUtils;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.util.TemplateUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GenericConfigGenWithVlanTranslation implements TemplateDeviceConfigGenerator {
    private Logger log = Logger.getLogger(GenericConfigGenWithVlanTranslation.class);
    private TemplateConfig templateConfig;

    public TemplateConfig getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig;
    }

    public String getConfig(PSSAction action, String deviceId) throws PSSException {
        switch (action.getActionType()) {
            case SETUP :
                return this.getActionConfig(action, deviceId);
            case TEARDOWN:
                return this.getActionConfig(action, deviceId);
            case STATUS:
                return this.getStatus(action, deviceId);
            case MODIFY:
                return this.getActionConfig(action, deviceId);
        }
        throw new PSSException("Invalid action type");
    }
    
    private String getStatus(PSSAction action, String deviceId) throws PSSException {
        action.setStatus(ActionStatus.SUCCESS);
        return "";
    }

    private String getActionConfig(PSSAction action, String deviceId) throws PSSException {
        ActionType at = action.getActionType();
        log.debug("getActionConfig start for "+at+" at "+deviceId);

        ResDetails res;

        switch (at) {
            case SETUP:
                res = action.getRequest().getSetupReq().getReservation();
                break;
            case TEARDOWN:
                res = action.getRequest().getTeardownReq().getReservation();
                break;
            case MODIFY:
                res = action.getRequest().getModifyReq().getReservation();
                break;
            default:
                throw new PSSException("bad actiontype: "+at);
        }

        if (templateConfig == null) {
            throw new PSSException("no root template config!");
        } else if (templateConfig.getTemplates() == null || templateConfig.getTemplates().isEmpty()) {
            throw new PSSException("no template config!");

        } else if (templateConfig.getTemplates().get(at.toString()) == null) {
            throw new PSSException("no template config for "+at);
        }
        String templateFile = templateConfig.getTemplates().get(at.toString());

        String portA;
        String portZ;
        String vlanA;
        String vlanZ;

        String gri = res.getGlobalReservationId();
        String description = res.getDescription();
        int bandwidth = res.getReservedConstraint().getBandwidth();

        DeviceBridge db = BridgeUtils.getDeviceBridge(deviceId, res);
        portA = db.getPortA();
        portZ = db.getPortZ();
        vlanA = db.getVlanA();
        vlanZ = db.getVlanZ();


        Map root = new HashMap();

        root.put("vlanA", vlanA);
        root.put("vlanZ", vlanZ);
        root.put("portA", portA);
        root.put("portZ", portZ);
        root.put("description", description);
        root.put("gri", gri);
        root.put("bandwidth", bandwidth);

        String config       = TemplateUtils.generateConfig(root, templateFile);
        log.debug("getActionConfig done");
        return config;
    }



    
    public void setConfig(GenericConfig config) throws PSSException {
        // TODO Auto-generated method stub
    }
    
 

}
