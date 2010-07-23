package net.es.oscars.pss.impl.sdn;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSS;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSEdgeType;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.common.SNMPRouterVendorFinder;

import net.es.oscars.pss.eompls.junos.EoMPLSJunosConfigGen;
import net.es.oscars.pss.layer3.junos.Layer3JunosConfigGen;
import net.es.oscars.pss.sw.junos.SWJunosConfigGen;

public class SDNPSS implements PSS {
    private static SDNPSS instance = null;
    private Logger log;
    private PSSHandlerConfigBean config;
    public enum Layer {
        LAYER2,
        LAYER3
    }

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



    // TODO not finished
    public String createPath(Reservation resv) throws PSSException {
        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INSETUP);
        } catch (BSSException ex) {
            throw new PSSException(ex);
        }

        Path path = null;
        try {
            path = resv.getPath(PathType.LOCAL);
            if (path == null) {
                throw new PSSException("No local path set");
            }
            PathUtils.checkPath(path);
            if (path.isLayer2()) {
                this.startSetup(resv, path, Layer.LAYER2);
            } else if (path.isLayer3()) {
                this.startSetup(resv, path, Layer.LAYER3);
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            System.out.println("createpath done");
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
        return "";

    }


    // TODO not finished
    public String teardownPath(Reservation resv, String newStatus) throws PSSException {
        try {
            StateEngine.canUpdateStatus(resv, StateEngine.INTEARDOWN);
        } catch (BSSException ex) {
            throw new PSSException(ex);
        }

        Path path = null;
        try {
            path = resv.getPath(PathType.LOCAL);
            if (path == null) {
                throw new PSSException("No local path set");
            }
            PathUtils.checkPath(path);
            if (path.isLayer2()) {
                this.startTeardown(resv, path, Layer.LAYER2);
            } else if (path.isLayer3()) {
                this.startTeardown(resv, path, Layer.LAYER3);
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
        
        return "";
    }

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
        while (currentTries < maxTries) {
            try {
                currentTries++;
                q.startAction(gri, directions, action);
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
     * gets 
     * 
     * @param localPath
     * @param service
     * @param edgeType
     * @return
     * @throws PSSException
     */
    private PSSHandler getHandler(Path localPath, SDNService service, PSSEdgeType edgeType) throws PSSException {
        SNMPRouterVendorFinder rv = SNMPRouterVendorFinder.getInstance();
        Map<PSSEdgeType, String> edges = PathUtils.getEdgeNodeAddresses(localPath);
        String address = edges.get(edgeType);
        RouterVendor vendor = rv.getVendor(address);
        PSSHandler handler = SDNHandlerFactory.getHandler(vendor, service);
        handler.setConfig(config);
        return handler;
    }
    
    
    public void startSetup(Reservation resv, Path localPath, Layer layer) throws PSSException {
        // ensure another setup or teardown are not in progress
        // for the exact same reservation
        this.tryStarting(resv, localPath, PSSAction.SETUP);
        
        // are the edges on the same node?
        if (PathUtils.sameNode(localPath)) {
            // for layer2 we need to switch
            if (layer.equals(Layer.LAYER2)) {
                PSSHandler handler = this.getHandler(localPath, SDNService.SWITCHED, PSSEdgeType.A);
                handler.setup(resv, localPath, PSSDirection.BIDIRECTIONAL);
            } else {
            // for layer3 it is an error
                throw new PSSException("Layer3 not supported on same device");
            }

        // if not, use EoMPLS or L3 - two handlers needed
        } else {
            SDNService service;
            if (layer.equals(Layer.LAYER2)) {
                service = SDNService.EOMPLS;
            } else {
                service = SDNService.LAYER3;
            }
            PSSHandler fwdHandler = this.getHandler(localPath, service, PSSEdgeType.A);
            fwdHandler.setup(resv, localPath, PSSDirection.A_TO_Z);
            PSSHandler revHandler = this.getHandler(localPath, service, PSSEdgeType.Z);
            revHandler.setup(resv, localPath, PSSDirection.Z_TO_A);
            if (config.isCheckStatusAfterSetup()) {
                fwdHandler.status(resv, localPath, PSSDirection.A_TO_Z);
                revHandler.status(resv, localPath, PSSDirection.Z_TO_A);
            }
        }
    }
    
    
    public void startTeardown(Reservation resv, Path localPath, Layer layer) throws PSSException {
        // ensure another setup or teardown are not in progress
        // for the exact same reservation
        this.tryStarting(resv, localPath, PSSAction.TEARDOWN);
        
        // are the edges on the same node?
        if (PathUtils.sameNode(localPath)) {
            // for layer2 we need to switch
            if (layer.equals(Layer.LAYER2)) {
                PSSHandler handler = this.getHandler(localPath, SDNService.SWITCHED, PSSEdgeType.A);
                handler.teardown(resv, localPath, PSSDirection.BIDIRECTIONAL);
            } else {
            // for layer3 it is an error
                throw new PSSException("Layer3 not supported on same device");
            }

        // if not, use EoMPLS or L3 - two handlers needed
        } else {
            SDNService service;
            if (layer.equals(Layer.LAYER2)) {
                service = SDNService.EOMPLS;
            } else {
                service = SDNService.LAYER3;
            }
            PSSHandler fwdHandler = this.getHandler(localPath, service, PSSEdgeType.A);
            fwdHandler.teardown(resv, localPath, PSSDirection.A_TO_Z);
            PSSHandler revHandler = this.getHandler(localPath, service, PSSEdgeType.Z);
            revHandler.teardown(resv, localPath, PSSDirection.A_TO_Z);
            if (config.isCheckStatusAfterTeardown()) {
                fwdHandler.status(resv, localPath, PSSDirection.A_TO_Z);
                revHandler.status(resv, localPath, PSSDirection.Z_TO_A);
            }
        }
    }




    
    
    
    /**
     * a singleton
     */
    private SDNPSS() throws PSSException {
        this.log = Logger.getLogger(this.getClass());
        this.config = PSSHandlerConfigBean.loadConfig();
        
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        Layer3JunosConfigGen l3 = Layer3JunosConfigGen.getInstance();
        EoMPLSJunosConfigGen eo = EoMPLSJunosConfigGen.getInstance();
        SWJunosConfigGen sw = SWJunosConfigGen.getInstance();
        l3.setNameGenerator(ng);
        eo.setNameGenerator(ng);
        sw.setNameGenerator(ng);
    }

    private SDNPSS(PSSHandlerConfigBean config) throws PSSException {
        this.log = Logger.getLogger(this.getClass());
        this.config = config;
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        Layer3JunosConfigGen l3 = Layer3JunosConfigGen.getInstance();
        EoMPLSJunosConfigGen eo = EoMPLSJunosConfigGen.getInstance();
        SWJunosConfigGen sw = SWJunosConfigGen.getInstance();
        l3.setNameGenerator(ng);
        eo.setNameGenerator(ng);
        sw.setNameGenerator(ng);
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

    public static SDNPSS getInstance(PSSHandlerConfigBean config) throws PSSException {
        if (instance == null) {
            instance = new SDNPSS(config);
        }
        return instance;
    }
    
    
    public void setConfig(PSSHandlerConfigBean config) {
        this.config = config;
    }

    public PSSHandlerConfigBean getConfig() {
        return config;
    }

}
