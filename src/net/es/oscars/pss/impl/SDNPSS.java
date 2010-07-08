package net.es.oscars.pss.impl;

import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSS;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.common.RouterVendor;
import net.es.oscars.pss.common.SNMPRouterVendorFinder;
import net.es.oscars.pss.common.PathUtils.EdgeType;
import net.es.oscars.pss.eompls.EoMPLSHandler;
import net.es.oscars.pss.eompls.EoMPLSHandlerFactory;

public class SDNPSS implements PSS {
    private static SDNPSS instance = null;
    private Logger log;
    private PSSHandlerConfigBean config;


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
                return this.createL2(resv, path);
            } else if (path.isLayer3()) {
                return this.createL3(resv, path);
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
    }


    // TODO not finished
    public String teardownPath(Reservation resv, String newStatus)
            throws PSSException {
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
                return this.teardownL2(resv, path);
            } else if (path.isLayer3()) {
                return this.teardownL3(resv, path);
            } else {
                throw new PSSException("Local path has neither L2 nor L3 information");
            }
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
    }


    
    public String createL2(Reservation resv, Path localPath) throws PSSException {
        SNMPRouterVendorFinder rv = SNMPRouterVendorFinder.getInstance();

        Map<EdgeType, String> edges = PathUtils.getEdgeNodeAddresses(localPath);

        // check if ingress and egress are on the same device
        if (PathUtils.sameNode(localPath)) {
            // if yes we only need to configure one device
            String address = edges.get(PathUtils.EdgeType.INGRESS);
            RouterVendor vendor = rv.getVendor(address);
            EoMPLSHandler handler = EoMPLSHandlerFactory.getHandler(vendor);
            handler.setConfig(config);
            handler.setup(resv, localPath, PSSDirection.BIDIRECTIONAL);
        } else {
            String ingressAddress = edges.get(PathUtils.EdgeType.INGRESS);
            RouterVendor iVendor = rv.getVendor(ingressAddress);
            EoMPLSHandler fwdHandler = EoMPLSHandlerFactory.getHandler(iVendor);
            fwdHandler.setConfig(config);
            fwdHandler.setup(resv, localPath, PSSDirection.A_TO_Z);

            String egressAddress = edges.get(PathUtils.EdgeType.EGRESS);
            RouterVendor eVendor = rv.getVendor(egressAddress);
            EoMPLSHandler revHandler = EoMPLSHandlerFactory.getHandler(eVendor);
            revHandler.setConfig(config);
            revHandler.setup(resv, localPath, PSSDirection.Z_TO_A);
        }

        return "";
    }


    public String teardownL2(Reservation resv, Path localPath) throws PSSException {
        SNMPRouterVendorFinder rv = SNMPRouterVendorFinder.getInstance();

        Map<EdgeType, String> edges = PathUtils.getEdgeNodeAddresses(localPath);

        // check if ingress and egress are on the same device
        if (PathUtils.sameNode(localPath)) {
            // if yes we only need to configure one device
            String address = edges.get(PathUtils.EdgeType.INGRESS);
            RouterVendor vendor = rv.getVendor(address);
            EoMPLSHandler handler = EoMPLSHandlerFactory.getHandler(vendor);
            handler.setConfig(config);
            handler.teardown(resv, localPath, PSSDirection.BIDIRECTIONAL);
        } else {
            String ingressAddress = edges.get(PathUtils.EdgeType.INGRESS);
            RouterVendor iVendor = rv.getVendor(ingressAddress);
            EoMPLSHandler fwdHandler = EoMPLSHandlerFactory.getHandler(iVendor);
            fwdHandler.setConfig(config);
            fwdHandler.teardown(resv, localPath, PSSDirection.A_TO_Z);

            String egressAddress = edges.get(PathUtils.EdgeType.EGRESS);
            RouterVendor eVendor = rv.getVendor(egressAddress);
            EoMPLSHandler revHandler = EoMPLSHandlerFactory.getHandler(eVendor);
            revHandler.setConfig(config);
            revHandler.teardown(resv, localPath, PSSDirection.Z_TO_A);
        }

        return "";
    }


    // TODO
    public String createL3(Reservation resv, Path localPath) {

        return "";
    }

    // TODO
    public String teardownL3(Reservation resv, Path localPath) {

        return "";
    }



    /**
     * a singleton
     */
    private SDNPSS() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * singleton constructor
     * @return the instance
     */
    public static SDNPSS getInstance() {
        if (instance == null) {
            instance = new SDNPSS();
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
