package net.es.oscars.pss.impl.bridge;

import java.util.ArrayList;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.bridge.alcatel.sr.BridgeAlcatelSRConfigGen;
import net.es.oscars.pss.bridge.brocade.xmr.BridgeBrocadeXMRConfigGen;
import net.es.oscars.pss.bridge.cisco.nexus.BridgeCiscoNexusConfigGen;
import net.es.oscars.pss.bridge.junos.ex.BridgeJunosEXConfigGen;
import net.es.oscars.pss.bridge.junos.mx.BridgeJunosMXConfigGen;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSConnectorConfigBean;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.connect.RancidConnector;

import org.apache.log4j.Logger;

public class BridgeHandler implements PSSHandler {
    private Logger log = Logger.getLogger(BridgeHandler.class);
    
    private static BridgeHandler instance;
    public static BridgeHandler getInstance() {
        if (instance == null) {
            instance = new BridgeHandler();
        }
        return instance;
    }
    private BridgeHandler() { }

    
    public void setup(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("starting setup for: "+resv.getGlobalReservationId());
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        PSSHandlerConfigBean hc = pc.getHandlerConfig();
        PSSConnectorConfigBean cc = pc.getConnectorConfig();
        String templateDir = hc.getTemplateDir();

        String vlanStr = "";
        
        Path localPath = PathUtils.getLocalPath(resv);
        ArrayList<PortPair> portPairs = new ArrayList<PortPair>();
        Port prevPort = null;
        for (PathElem pe : localPath.getPathElems()) {
            if (pe.getLink() != null) {
                Port currPort = pe.getLink().getPort();
                if (prevPort == null) {
                    // first hop should have the VLAN
                    // FIXME: check for nulls
                    try {
                        if (vlanStr.equals("")) {
                            vlanStr = pe.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE).getValue();
                        }
                        log.debug("vlan is: " +vlanStr);
                    } catch (BSSException e) {
                        log.error("error getting VLAN", e);
                        throw new PSSException("Internal PSS error");
                    }
                } else {
                    if (prevPort.getNode().equals(currPort.getNode())) {
                        PortPair pair = new PortPair();
                        pair.setA(prevPort);
                        pair.setZ(currPort);
                        portPairs.add(pair);
                    }
                }
                prevPort = currPort;
                
            } else {
                throw new PSSException("null link for pathelem");
            }
        }
        
        Integer vlan = Integer.getInteger(vlanStr);
        RancidConnector conn = new RancidConnector(cc);

        
        
        
        for (PortPair pair : portPairs) {
            log.info("setting up: "+pair.getA().getFQTI() + " " + pair.getZ().getFQTI()) ;
            Node n = pair.getA().getNode();
            String portA = pair.getA().getTopologyIdent();
            String portZ = pair.getZ().getTopologyIdent();
            
            RancidConnector.LOGIN login = null;
            
            
            // SC10 HACKS
            String nodeId = n.getTopologyIdent();
            String command = "";
            if (nodeId.equals("core-sw1")) {
                // MX960
                BridgeJunosMXConfigGen mx = BridgeJunosMXConfigGen.getInstance();
                mx.setTemplateDir(templateDir);
                command = mx.generateL2Setup(portA, portZ, vlan);
                login = RancidConnector.LOGIN.JLOGIN;
            } else if (nodeId.equals("core-rtr1")) {
                // T1600
                BridgeJunosMXConfigGen mx = BridgeJunosMXConfigGen.getInstance();
                mx.setTemplateDir(templateDir);
                command = mx.generateL2Setup(portA, portZ, vlan);
                login = RancidConnector.LOGIN.JLOGIN;
            } else if (nodeId.equals("core-rtr2")) {
                // CRS
                throw new PSSException("core-rtr2 not supported");
            } else if (nodeId.equals("rtr-2042")) {
                BridgeJunosEXConfigGen ex = BridgeJunosEXConfigGen.getInstance();
                ex.setTemplateDir(templateDir);
                command = ex.generateL2Setup(portA, portZ, vlan, "IDC VLAN: "+vlan);
                login = RancidConnector.LOGIN.JLOGIN;
                // EX8200
            } else if (nodeId.equals("rtr-2212")) {
                // ALU SR-7
                BridgeAlcatelSRConfigGen al = BridgeAlcatelSRConfigGen.getInstance();
                al.setTemplateDir(templateDir);
                command = al.generateL2Setup(portA, portZ, vlan);
                login = RancidConnector.LOGIN.ALULOGIN;
            } else if (nodeId.equals("rtr-3230")) {
                // Brocade XMR
                BridgeBrocadeXMRConfigGen bx = BridgeBrocadeXMRConfigGen.getInstance();
                bx.setTemplateDir(templateDir);
                command = bx.generateL2Setup(portA, portZ, vlan, "IDC VLAN: "+vlan);
                login = RancidConnector.LOGIN.CLOGIN;
            } else if (nodeId.equals("rtr-3851")) {
                // Nexus
                BridgeCiscoNexusConfigGen nx = BridgeCiscoNexusConfigGen.getInstance();
                nx.setTemplateDir(templateDir);
                command = nx.generateL2Setup(portA, portZ, vlan, "IDC VLAN: "+vlan);
                login = RancidConnector.LOGIN.CLOGIN;
            }
            log.debug("sending config to "+nodeId);
            if (hc.isLogConfig()) {
                log.info("config for "+nodeId+": \n\n"+command);
            }
            if (hc.isStubMode()) {
                log.info("not sending config because we are in stub mode");
            } else {
                conn.sendCommand(command, nodeId, login);
            }
        }
        log.info("finished setup for: "+resv.getGlobalReservationId());

    }

    public void teardown(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("starting teardown for: "+resv.getGlobalReservationId());
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        PSSHandlerConfigBean hc = pc.getHandlerConfig();
        PSSConnectorConfigBean cc = pc.getConnectorConfig();
        String templateDir = hc.getTemplateDir();

        String vlanStr = "";
        
        Path localPath = PathUtils.getLocalPath(resv);
        ArrayList<PortPair> portPairs = new ArrayList<PortPair>();
        Port prevPort = null;
        for (PathElem pe : localPath.getPathElems()) {
            if (pe.getLink() != null) {
                Port currPort = pe.getLink().getPort();
                if (prevPort == null) {
                    // first hop should have the VLAN
                    // FIXME: check for nulls
                    try {
                        vlanStr = pe.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE).getValue();
                    } catch (BSSException e) {
                        log.error("error getting VLAN", e);
                        throw new PSSException("Internal PSS error");
                    }
                } else {
                    if (prevPort.getNode().equals(currPort.getNode())) {
                        PortPair pair = new PortPair();
                        pair.setA(prevPort);
                        pair.setZ(currPort);
                        portPairs.add(pair);
                    }
                }
                prevPort = currPort;
                
            } else {
                throw new PSSException("null link for pathelem");
            }
        }
        
        Integer vlan = Integer.getInteger(vlanStr);
        RancidConnector conn = new RancidConnector(cc);

        
        
        
        for (PortPair pair : portPairs) {
            log.info("setting up: "+pair.getA().getFQTI() + " " + pair.getZ().getFQTI()) ;
            Node n = pair.getA().getNode();
            String portA = pair.getA().getTopologyIdent();
            String portZ = pair.getZ().getTopologyIdent();
            
            RancidConnector.LOGIN login = null;
            
            
            // SC10 HACKS
            String nodeId = n.getTopologyIdent();
            String command = "";
            if (nodeId.equals("core-sw1")) {
                // MX960
                BridgeJunosMXConfigGen mx = BridgeJunosMXConfigGen.getInstance();
                mx.setTemplateDir(templateDir);
                command = mx.generateL2Teardown(portA, portZ, vlan);
                login = RancidConnector.LOGIN.JLOGIN;
            } else if (nodeId.equals("core-rtr1")) {
                // T1600
                BridgeJunosMXConfigGen mx = BridgeJunosMXConfigGen.getInstance();
                mx.setTemplateDir(templateDir);
                command = mx.generateL2Teardown(portA, portZ, vlan);
                login = RancidConnector.LOGIN.JLOGIN;
            } else if (nodeId.equals("core-rtr2")) {
                // CRS
                throw new PSSException("core-rtr2 not supported");
            } else if (nodeId.equals("rtr-2042")) {
                BridgeJunosEXConfigGen ex = BridgeJunosEXConfigGen.getInstance();
                ex.setTemplateDir(templateDir);
                command = ex.generateL2Teardown(portA, portZ, vlan);
                login = RancidConnector.LOGIN.JLOGIN;
                // EX8200
            } else if (nodeId.equals("rtr-2212")) {
                // ALU SR-7
                BridgeAlcatelSRConfigGen al = BridgeAlcatelSRConfigGen.getInstance();
                al.setTemplateDir(templateDir);
                command = al.generateL2Teardown(portA, portZ, vlan);
                login = RancidConnector.LOGIN.ALULOGIN;
            } else if (nodeId.equals("rtr-3230")) {
                // Brocade XMR
                BridgeBrocadeXMRConfigGen bx = BridgeBrocadeXMRConfigGen.getInstance();
                bx.setTemplateDir(templateDir);
                command = bx.generateL2Teardown(portA, portZ, vlan);
                login = RancidConnector.LOGIN.CLOGIN;
            } else if (nodeId.equals("rtr-3851")) {
                // Nexus
                BridgeCiscoNexusConfigGen nx = BridgeCiscoNexusConfigGen.getInstance();
                nx.setTemplateDir(templateDir);
                command = nx.generateL2Teardown(portA, portZ, vlan);
                login = RancidConnector.LOGIN.CLOGIN;
            }
            log.debug("sending config to "+nodeId);
            if (hc.isLogConfig()) {
                log.info("config for "+nodeId+": \n\n"+command);
            }
            
            if (hc.isStubMode()) {
                log.info("not sending config because we are in stub mode");
            } else {
                conn.sendCommand(command, nodeId, login);
            }
        }
        log.info("finished teardown for: "+resv.getGlobalReservationId());
    }


    private class PortPair {
        private Port a;
        private Port z;
        public void setA(Port a) {
            this.a = a;
        }
        public Port getA() {
            return a;
        }
        public void setZ(Port z) {
            this.z = z;
        }
        public Port getZ() {
            return z;
        }
        
        
    }
}
