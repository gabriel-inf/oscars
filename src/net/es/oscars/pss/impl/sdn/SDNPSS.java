package net.es.oscars.pss.impl.sdn;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSS;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.PSSFailureManager;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSConnectorConfigBean;
import net.es.oscars.pss.common.PSSEdgeType;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PSSLayer;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.common.SNMPRouterVendorFinder;

import net.es.oscars.pss.eompls.junos.EoMPLSJunosConfigGen;
import net.es.oscars.pss.l2vpn.junos.L2VPNJunosConfigGen;
import net.es.oscars.pss.layer3.junos.Layer3JunosConfigGen;
import net.es.oscars.pss.sw.junos.SWJunosConfigGen;

public class SDNPSS implements PSS {
    private static SDNPSS instance = null;
    private Logger log;


    // FIXME: does not work
    // TODO : implement refresh at some point.
    public String refreshPath(Reservation resv) throws PSSException {
        Path path = null;
        try {
            path = resv.getPath(PathType.LOCAL);
            if (path == null) {
                throw new PSSException("No local path set");
            }
            if (path.isLayer3()) {
                throw new PSSException("Refresh not implemented for L3");
            } else if (path.isLayer2()) {
                throw new PSSException("Refresh not implemented for L2");
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
    }
    
    public String createPath(Reservation resv) throws PSSException {
        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INSETUP);
        } catch (BSSException ex) {
            throw new PSSException(ex);
        }
        this.startAction(resv, PSSAction.SETUP); 
        
        return "";
    }

    public String teardownPath(Reservation resv, String newStatus) throws PSSException {
        if (!newStatus.equals(StateEngine.FAILED)) {
            try {
                StateEngine.canUpdateStatus(resv, StateEngine.INTEARDOWN);
            } catch (BSSException ex) {
                throw new PSSException(ex);
            }
        } else {
           // teardown because of failure
        }
        this.startAction(resv, PSSAction.TEARDOWN); 
        
        return "";
    }

    
    /** 
     * 
     * @param resv
     * @param localPath
     * @param action
     * @throws PSSException
     */
    private void tryStarting(Reservation resv, Path localPath, PSSAction action) throws PSSException  {
        String gri = resv.getGlobalReservationId();
        SDNQueuer q = SDNQueuer.getInstance();
        ArrayList<PSSDirection> directions = new ArrayList<PSSDirection>();
        if (PathUtils.sameNode(localPath)) {
            directions.add(PSSDirection.BIDIRECTIONAL);
        } else {
            directions.add(PSSDirection.A_TO_Z);
            directions.add(PSSDirection.Z_TO_A);
        }
        // TODO: make these configurable
        int maxTries = 12;
        int sleep = 10;
        int currentTries = 0;
        boolean ok = false;
        while (currentTries < maxTries && !ok) {
            try {
                currentTries++;
                q.startAction(gri, directions, action);
                ok = true;
            } catch (PSSException e) {
                if (currentTries == maxTries) {
                    log.error(e);
                    throw new PSSException("could not start "+action+" for "+gri+" after "+currentTries+" tries"+e.getMessage());
                } else {
                    // wait a bit then try again
                    log.info(e);
                    try {
                        Long sleeptime = sleep*1000L;
                        Thread.sleep(sleeptime);
                    } catch (InterruptedException e1) {
                        log.error(e1);
                        throw new PSSException(e1.getMessage());
                    }
                }
                
            }
        }
    }
    

    /**
     * starts an action (either SETUP or TEARDOWN) for a reservation
     * will split up into sub-actions per direction
     * uses SDNQueuer to schedule the actions
     * 
     * @param resv
     * @param action
     * @throws PSSException
     */
    public void startAction(Reservation resv, PSSAction action) throws PSSException {
        PSSLayer layer = null;

        Path localPath  = PathUtils.getLocalPath(resv);
        
        try {
            if (localPath.isLayer2()) {
                layer = PSSLayer.LAYER2;
            } else if (localPath.isLayer3()) {
                layer = PSSLayer.LAYER2;
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            throw new PSSException(e.getMessage());
        }
        
        SDNQueuer q = SDNQueuer.getInstance();
        
        // ensure another setup or teardown are not in progress
        // for the exact same reservation 
        /// should never happen because PathSetupManager enforces waiting
        this.tryStarting(resv, localPath, action);

        // business logic

        // are the edges on the same node?
        if (PathUtils.sameNode(localPath)) {
            // for layer2 we need to switch
            if (layer.equals(PSSLayer.LAYER2)) {
                PSSHandler handler = this.getHandler(localPath, SDNService.SWITCHED, PSSEdgeType.A);
                if (action.equals(PSSAction.SETUP)) {
                    q.scheduleAction(resv, PSSDirection.BIDIRECTIONAL, action, handler);
                } else if (action.equals(PSSAction.TEARDOWN)) { 
                    q.scheduleAction(resv, PSSDirection.BIDIRECTIONAL, action, handler);
                } else {
                    throw new PSSException("Invalid action "+action);
                }
            } else {
            // for layer3 it is an error
                throw new PSSException("Layer3 not supported on same device");
            }

        // if not, use EoMPLS or L3 - two handlers needed
        } else {
            SDNService service;
            if (layer.equals(PSSLayer.LAYER2)) {
                service = SDNService.EOMPLS;
            } else {
                service = SDNService.LAYER3;
            }
            PSSHandler fwdHandler = this.getHandler(localPath, service, PSSEdgeType.A);
            PSSHandler revHandler = this.getHandler(localPath, service, PSSEdgeType.Z);
            if (action.equals(PSSAction.SETUP)) { 
                q.scheduleAction(resv, PSSDirection.A_TO_Z, action, fwdHandler);
                q.scheduleAction(resv, PSSDirection.Z_TO_A, action, revHandler);
            } else if (action.equals(PSSAction.TEARDOWN)) {
                q.scheduleAction(resv, PSSDirection.A_TO_Z, action, fwdHandler);
                q.scheduleAction(resv, PSSDirection.Z_TO_A, action, revHandler);
                
            } else {
                throw new PSSException("Invalid action: "+action);
            }

        }
    }
    

    private PSSHandler getHandler(Path localPath, SDNService service, PSSEdgeType edgeType) throws PSSException {
        SNMPRouterVendorFinder rv = SNMPRouterVendorFinder.getInstance();
        Map<PSSEdgeType, String> edges = PathUtils.getEdgeNodeAddresses(localPath);
        String address = edges.get(edgeType);
        RouterVendor vendor = rv.getVendor(address);
        PSSHandler handler = SDNHandlerFactory.getHandler(vendor, service);
        return handler;
    }
    
    
    
    
    /**
     * a singleton
     */
    private SDNPSS() throws PSSException {
        this.log = Logger.getLogger(this.getClass());
        
        log.debug("setting up SDN PSS");
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        Layer3JunosConfigGen l3 = Layer3JunosConfigGen.getInstance();
        EoMPLSJunosConfigGen eo = EoMPLSJunosConfigGen.getInstance();
        L2VPNJunosConfigGen l2 = L2VPNJunosConfigGen.getInstance();
        SWJunosConfigGen sw = SWJunosConfigGen.getInstance();
        l3.setNameGenerator(ng);
        eo.setNameGenerator(ng);
        sw.setNameGenerator(ng);
        l2.setNameGenerator(ng);
        
        SDNQueuer q = SDNQueuer.getInstance();
        q.setScheduler(OSCARSCore.getInstance().getScheduleManager().getScheduler());

        PSSFailureManager.getInstance().setFailureHandler(SDNFailureHandler.getInstance());
       
        
        PSSConfigProvider cp = PSSConfigProvider.getInstance();
        if (cp.getConnectorConfig() == null) {
            log.info("loading connector config from oscars.properties");
            PSSConnectorConfigBean cc = PSSConnectorConfigBean.loadConfig("oscars.properties", "pss");
            cp.setConnectorConfig(cc);
        }
        if (cp.getHandlerConfig() == null) {
            log.info("loading handler config from oscars.properties");
            PSSHandlerConfigBean hc = PSSHandlerConfigBean.loadConfig("oscars.properties", "pss");
            cp.setHandlerConfig(hc);
        }
        log.debug("SDN PSS setup complete");

        
    }




    /**
     * singleton constructor
     * @return the instance
     */
    public static SDNPSS getInstance() throws PSSException {
        if (instance == null) {
            instance = new SDNPSS();
        }
        return instance;
    }


    

}
