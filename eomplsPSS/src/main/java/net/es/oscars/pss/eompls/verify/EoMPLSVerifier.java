package net.es.oscars.pss.eompls.verify;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.api.Verifier;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.service.EoMPLSService;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.util.ConnectorUtils;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;

public class EoMPLSVerifier implements Verifier {
    private GenericConfig config;
    private Logger log = Logger.getLogger(EoMPLSVerifier.class);
    private boolean performVerify;
    private boolean cleanupOnFail;
    private int verifyTries;
    private int delaySec;
    private int tryIntervalSec;
    
    public PSSAction verify(PSSAction action, String deviceId) throws PSSException {
        if (performVerify == false) {
            // automatic immediate success
            action.setStatus(ActionStatus.SUCCESS);
            return action;
        }
        // no need to verify a status
        if (action.getActionType().equals(ActionType.STATUS)) {
            action.setStatus(ActionStatus.SUCCESS);
            return action;
        } else if (action.getActionType().equals(ActionType.MODIFY)) {
            // TODO: modify always fails (for now)
            action.setStatus(ActionStatus.FAIL);
            return action;
        }
            
        // first sleep 
        try {
            Thread.sleep(delaySec * 1000);
        } catch (InterruptedException e) {
            throw new PSSException(e);
        }
        
        // then send a status check
        boolean decided = false;
        boolean success = false;
        
        // keep trying the status check until 
        // either success OR verifyTries is reached
        int tries = 1;
        while (!decided) {
            while (tries <= verifyTries) {
                tries++;
                PSSAction statusAction = new PSSAction();
                statusAction.setActionType(ActionType.STATUS);
                statusAction.setRequest(action.getRequest());
                statusAction.setStatus(ActionStatus.OUTSTANDING);
                success = this.checkStatus(statusAction, deviceId, action);
                if (success) {
                    decided = true;
                } else {
                    try {
                        Thread.sleep(delaySec * 1000);
                    } catch (InterruptedException e) {
                        throw new PSSException(e);
                    }
                }
            }
            decided = true;
        }
        
        // by now it's either successful so return
        if (success) {
            action.setStatus(ActionStatus.SUCCESS);
            return action;
            
        // or failed & we need to clean up 
        } else if (cleanupOnFail) {
            if (action.getActionType().equals(ActionType.SETUP)) {
                PSSAction cleanupAction = new PSSAction();
                cleanupAction.setActionType(ActionType.TEARDOWN);
                cleanupAction.setRequest(action.getRequest());
                cleanupAction.setStatus(ActionStatus.OUTSTANDING);
                this.cleanup(cleanupAction, deviceId);
            } else if (action.getActionType().equals(ActionType.TEARDOWN)) {
                // can't cleanup a failed teardown
                // TODO: notify IDC admins
            }
            action.setStatus(ActionStatus.FAIL);
            return action;
        // or we don't need to clean up
        } else {
            action.setStatus(ActionStatus.FAIL);
            return action;
        }
    }
    
    @SuppressWarnings("rawtypes")
    private boolean checkStatus(PSSAction action, String deviceId, PSSAction prevAction) throws PSSException {
        boolean success = false;
        String resultString = ConnectorUtils.sendAction(action, deviceId, EoMPLSService.SVC_ID);
        
        
        ActionType prevActionType = prevAction.getActionType();
        ResDetails res;
        if (prevActionType.equals(ActionType.SETUP)) {
            res = action.getRequest().getSetupReq().getReservation();
        } else if (prevActionType.equals(ActionType.TEARDOWN)) {
            res = action.getRequest().getTeardownReq().getReservation();
        } else {
            throw new PSSException("cannot check status unless previous actionw as setup or teardown");
        }
        ReservedConstraintType rc = res.getReservedConstraint();
        PathInfo pi = rc.getPathInfo();

        CtrlPlaneLinkContent ingressLink = pi.getPath().getHop().get(0).getLink();
        CtrlPlaneLinkContent egressLink = pi.getPath().getHop().get(pi.getPath().getHop().size()-1).getLink();
        
        String srcLinkId = ingressLink.getId();
        URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId, null);
        String dstLinkId = egressLink.getId();
        URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId, null);
        String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);

        String ifceName, ifceVlan, lspTargetDeviceId;
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
        String ifceFullName = ifceName+"."+ifceVlan;
        EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
        EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();

        String l2circuitEgress         = dar.getDeviceAddress(lspTargetDeviceId);

        
        // XML parsing bit
        // NOTE WELL: if response format changes, this won't work
        SAXBuilder sb = new SAXBuilder();
        Document responseDoc = null;
        try {
            responseDoc = sb.build(new StringReader(resultString));
        } catch (JDOMException e) {
            throw new PSSException(e);
        } catch (IOException e) {
            throw new PSSException(e);
        }
        Element root = responseDoc.getRootElement();
        // this is element "rpc-reply"
        Element rpcReply = (Element) root.getChildren().get(0);
        // firstChild will be "l2circuit-connection-information"
        // we should get the namespace from that element because it changes
        // with each JunOS release.. 
        Element firstChild = (Element) rpcReply.getChildren().get(0);
        String uri = firstChild.getNamespaceURI();
        
        HashMap<String, String> nsmap = new HashMap<String, String>();
        nsmap.put( "ns", uri);

        String xpathExpr = "//ns:l2circuit-neighbor[ns:neighbor-address='"+l2circuitEgress+"']/ns:connection[ns:local-interface/ns:interface-name='"+ifceFullName+"']";
        log.debug("xpath is: "+xpathExpr);
        
        XPath xpath;
        Element conn = null;
        try {
            xpath = new JDOMXPath(xpathExpr);
            xpath.setNamespaceContext(new SimpleNamespaceContext(nsmap));
            conn = (Element) xpath.selectSingleNode(responseDoc);
        } catch (JaxenException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String connectionStatus = "";
        String ifceStatus = "";
        boolean isVCup = false;
        boolean isVCConfigured = false;

        if (conn == null) {
            log.info("could not locate connection XML node, will retry");
        } else {
            isVCConfigured = true;

            List connectionChildren = conn.getChildren();
            for (Iterator j = connectionChildren.iterator(); j.hasNext();) {
                Element e = (Element) j.next();
    
                if (e.getName().equals("connection-status")) {
                    connectionStatus = e.getText();
                    log.debug("conn status : "+connectionStatus);
                } else if (e.getName().equals("local-interface")) {
                    List localInterfaces = e.getChildren();
                    for (Iterator k = localInterfaces.iterator(); k.hasNext();) {
                        Element ifceElem = (Element) k.next();
                        if (ifceElem.getName().equals("interface-status")) {
                            ifceStatus = ifceElem.getText();
                            log.debug("ifce status : "+ifceStatus);
                        } else if (ifceElem.getName().equals("interface-description")) {
                            String ifceDescription = ifceElem.getText();
                            log.debug("ifce description: "+ifceDescription);
                        }
                    }
                }
            }
            if (connectionStatus != null && connectionStatus.toLowerCase().trim().equals("up")) {
                isVCup = true;
            }
        } 
        
        
        
        
        if (prevActionType.equals(ActionType.SETUP)) {
            if (isVCConfigured && isVCup) {
                success = true;
            }
        } else if (prevActionType.equals(ActionType.TEARDOWN)) {
            if (!isVCConfigured && !isVCup) {
                success = true;
            }
        }
        return success;
    }
    
    private String cleanup(PSSAction action, String deviceId) throws PSSException {
        return ConnectorUtils.sendAction(action, deviceId, EoMPLSService.SVC_ID);
    }
    

    public void setConfig(GenericConfig configToSet) throws PSSException {
        this.config = configToSet;
        if (config == null) { 
            throw new PSSException("no verifier configuration");
        } else if (config.getParams() == null) {
            throw new PSSException("no verifier parameters stanza");
        }
        try {
            performVerify   = (Boolean) config.getParams().get("performVerify");
            cleanupOnFail   = (Boolean) config.getParams().get("cleanupOnFail");
            verifyTries     = (Integer) config.getParams().get("verifyTries");
            delaySec        = (Integer) config.getParams().get("delaySec");
            tryIntervalSec  = (Integer) config.getParams().get("tryIntervalSec");
            if (verifyTries <= 0 || verifyTries > 10) {
                throw new PSSException("verifyTries must be in the range 1..10");
            }
            if (delaySec < 0 || delaySec > 600) {
                throw new PSSException("delaySec must be in the range 0..600");
            }
            if (tryIntervalSec <= 0 || tryIntervalSec > 600) {
                throw new PSSException("tryIntervalSec must be in the range 0..600");
            }
            
        } catch (Exception e) {
            throw new PSSException(e);
        }
        
    }

}