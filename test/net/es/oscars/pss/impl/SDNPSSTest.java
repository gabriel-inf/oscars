package net.es.oscars.pss.impl;

import java.util.ArrayList;
import java.util.HashSet;

import org.testng.annotations.Test;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.L2SwitchingCapabilityData;
import net.es.oscars.bss.topology.Layer2Data;
import net.es.oscars.bss.topology.Layer3Data;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.NodeAddress;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathDirection;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandlerConfigBean;

@Test(groups={ "pss.sdn" })
public class SDNPSSTest {
    private String aUrn = "urn:ogf:network:domain=foo:node=alpha:port=xe-0/0/0:link=*";
    private String bUrn = "urn:ogf:network:domain=foo:node=alpha:port=xe-1/0/0:link=xe-1/0/0.500";
    private String fUrn = "urn:ogf:network:domain=foo:node=kappa:port=xe-4/0/0:link=xe-4/0/0.500";
    private String gUrn = "urn:ogf:network:domain=foo:node=kappa:port=xe-5/0/0:link=xe-5/0/0.700";
    private String yUrn = "urn:ogf:network:domain=foo:node=zeta:port=xe-8/0/0:link=xe-8/0/0.700";
    private String zUrn = "urn:ogf:network:domain=foo:node=zeta:port=xe-9/0/0:link=*";

    String srcHost = "192.168.1.0/24 192.168.12.0/24";
    String dstHost = "192.168.98.0/24 192.168.99.0/24";
    
    private Link aLink;
    private Link bLink;
    private Link fLink;
    private Link gLink;
    private Link yLink;
    private Link zLink;
    
    private Domain dom;
    
    private void makeDomain() {
        // topology
        dom = new Domain();
        dom.setLocal(true);
        dom.setTopologyIdent("foo.net");
        
        Node alpha = new Node();
        alpha.setDomain(dom);
        NodeAddress alphaAddr = new NodeAddress();
        alphaAddr.setAddress("10.0.0.1");
        alpha.setNodeAddress(alphaAddr);
        alpha.setTopologyIdent("alpha");

        Node kappa = new Node();
        kappa.setDomain(dom);
        NodeAddress kappaAddr = new NodeAddress();
        kappaAddr.setAddress("10.0.127.1");
        kappa.setNodeAddress(kappaAddr);
        kappa.setTopologyIdent("kappa");

        
        Node zeta = new Node();
        zeta.setDomain(dom);
        NodeAddress zetaAddr = new NodeAddress();
        zetaAddr.setAddress("10.0.254.1");
        zeta.setNodeAddress(zetaAddr);
        zeta.setTopologyIdent("zeta");
        
        HashSet<Node> nodes = new HashSet<Node>();
        nodes.add(alpha);
        nodes.add(kappa);
        nodes.add(zeta);
        dom.setNodes(nodes);
        
        HashSet<Ipaddr> bAddrs = new HashSet<Ipaddr>();
        HashSet<Ipaddr> yAddrs = new HashSet<Ipaddr>();
        HashSet<Ipaddr> fAddrs = new HashSet<Ipaddr>();
        HashSet<Ipaddr> gAddrs = new HashSet<Ipaddr>();

        
        Ipaddr bAddr = new Ipaddr();
        bAddr.setIP("10.0.100.1");
        bAddr.setValid(true);
        bAddrs.add(bAddr);
        
        Ipaddr fAddr = new Ipaddr();
        fAddr.setIP("10.0.100.2");
        fAddrs.add(fAddr);
        fAddr.setValid(true);
        
        Ipaddr gAddr = new Ipaddr();
        gAddr.setIP("10.0.200.1");
        gAddrs.add(gAddr);
        gAddr.setValid(true);

        Ipaddr yAddr = new Ipaddr();
        yAddr.setIP("10.0.200.2");
        yAddrs.add(yAddr);
        yAddr.setValid(true);

        
        L2SwitchingCapabilityData aCap = new L2SwitchingCapabilityData();
        L2SwitchingCapabilityData bCap = new L2SwitchingCapabilityData();
        L2SwitchingCapabilityData fCap = new L2SwitchingCapabilityData();
        L2SwitchingCapabilityData gCap = new L2SwitchingCapabilityData();
        L2SwitchingCapabilityData yCap = new L2SwitchingCapabilityData();
        L2SwitchingCapabilityData zCap = new L2SwitchingCapabilityData();
        aCap.setVlanRangeAvailability("2-4095");
        bCap.setVlanRangeAvailability("2-4095");
        fCap.setVlanRangeAvailability("2-4095");
        gCap.setVlanRangeAvailability("2-4095");
        yCap.setVlanRangeAvailability("2-4095");
        zCap.setVlanRangeAvailability("2-4095");
        aCap.setVlanTranslation(true);
        zCap.setVlanTranslation(true);
        
        Port aPort = new Port();
        aPort.setNode(alpha);
        aPort.setTopologyIdent("xe-0/0/0");
        
        Port bPort = new Port();
        bPort.setNode(alpha);
        bPort.setTopologyIdent("xe-1/0/0");
        
        Port fPort = new Port();
        fPort.setNode(kappa);
        fPort.setTopologyIdent("xe-4/0/0");
        
        Port gPort = new Port();
        gPort.setNode(kappa);
        gPort.setTopologyIdent("xe-5/0/0");

        Port yPort = new Port();
        yPort.setNode(zeta);
        yPort.setTopologyIdent("xe-8/0/0");
        
        Port zPort = new Port();
        zPort.setNode(zeta);
        zPort.setTopologyIdent("xe-9/0/0");

        aLink = new Link();
        aLink.setPort(aPort);
        aLink.setTopologyIdent("*");
        aLink.setL2SwitchingCapabilityData(aCap);

        bLink = new Link();
        bLink.setPort(bPort);
        bLink.setTopologyIdent("xe-1/0/0.500");
        bLink.setIpaddrs(bAddrs);
        bLink.setL2SwitchingCapabilityData(bCap);

        fLink = new Link();
        fLink.setPort(fPort);
        fLink.setTopologyIdent("xe-4/0/0.500");
        fLink.setIpaddrs(fAddrs);
        fLink.setL2SwitchingCapabilityData(fCap);
        
        gLink = new Link();
        gLink.setPort(gPort);
        gLink.setTopologyIdent("xe-5/0/0.700");
        gLink.setIpaddrs(gAddrs);
        gLink.setL2SwitchingCapabilityData(gCap);
        
        yLink = new Link();
        yLink.setPort(yPort);
        yLink.setTopologyIdent("xe-8/0/0.700");
        yLink.setIpaddrs(yAddrs);
        yLink.setL2SwitchingCapabilityData(yCap);
        
        zLink = new Link();
        zLink.setPort(zPort);
        zLink.setTopologyIdent("*");
        zLink.setL2SwitchingCapabilityData(zCap);
        
    }
    
    protected Path makeL3Path() throws BSSException {
        this.makeDomain();
        
        Path localPath = new Path();
        localPath.setPathType(PathType.LOCAL);
        localPath.setDirection(PathDirection.BIDIRECTIONAL);
        Layer3Data layer3Data = new Layer3Data();
        localPath.setLayer3Data(layer3Data);
        layer3Data.setSrcHost(srcHost);
        layer3Data.setDestHost(dstHost);
        layer3Data.setDscp(null);
        layer3Data.setSrcIpPort(80);
        layer3Data.setDestIpPort(null);
        layer3Data.setProtocol(null);

        HashSet<PathElemParam> aPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> bPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> fPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> gPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> yPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> zPeps = new HashSet<PathElemParam>();
        
        PathElem a = new PathElem();
        a.setUrn(aUrn);
        a.setLink(aLink);
        a.setPathElemParams(aPeps);
        
        PathElem b = new PathElem();
        b.setUrn(bUrn);
        b.setLink(bLink);
        b.setPathElemParams(bPeps);

        PathElem f = new PathElem();
        f.setUrn(fUrn);
        f.setLink(fLink);
        f.setPathElemParams(fPeps);
        
        PathElem g = new PathElem();
        g.setUrn(gUrn);
        g.setLink(gLink);
        g.setPathElemParams(gPeps);
        
        PathElem y = new PathElem();
        y.setUrn(yUrn);
        y.setLink(yLink);
        y.setPathElemParams(yPeps);

        PathElem z = new PathElem();
        z.setUrn(zUrn);
        z.setLink(zLink);
        z.setPathElemParams(zPeps);
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        pathElems.add(a);
        pathElems.add(b);
        pathElems.add(f);
        pathElems.add(g);
        pathElems.add(y);
        pathElems.add(z);
        
        localPath.setPathElems(pathElems);
        
        return localPath;
    }
    
    protected Path makeL2Path() throws BSSException {
        this.makeDomain();
        Path localPath = new Path();
        localPath.setPathType(PathType.LOCAL);
        localPath.setDirection(PathDirection.BIDIRECTIONAL);
        Layer2Data layer2Data = new Layer2Data();
        localPath.setLayer2Data(layer2Data);
        layer2Data.setSrcEndpoint(aUrn);
        layer2Data.setDestEndpoint(zUrn);

        HashSet<PathElemParam> aPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> bPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> fPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> gPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> yPeps = new HashSet<PathElemParam>();
        HashSet<PathElemParam> zPeps = new HashSet<PathElemParam>();
        PathElemParam aPep = new PathElemParam();
        aPep.setSwcap(PathElemParamSwcap.L2SC);
        aPep.setType(PathElemParamType.L2SC_VLAN_RANGE);
        aPep.setValue("100");
        aPeps.add(aPep);

        PathElemParam zPep = new PathElemParam();
        zPep.setSwcap(PathElemParamSwcap.L2SC);
        zPep.setType(PathElemParamType.L2SC_VLAN_RANGE);
        zPep.setValue("100");
        zPeps.add(zPep);
        
        
        PathElem a = new PathElem();
        a.setUrn(aUrn);
        a.setLink(aLink);
        a.setPathElemParams(aPeps);
        
        PathElem b = new PathElem();
        b.setUrn(bUrn);
        b.setLink(bLink);
        b.setPathElemParams(bPeps);

        PathElem f = new PathElem();
        f.setUrn(fUrn);
        f.setLink(fLink);
        f.setPathElemParams(fPeps);
        
        PathElem g = new PathElem();
        g.setUrn(gUrn);
        g.setLink(gLink);
        g.setPathElemParams(gPeps);
        
        PathElem y = new PathElem();
        y.setUrn(yUrn);
        y.setLink(yLink);
        y.setPathElemParams(yPeps);

        PathElem z = new PathElem();
        z.setUrn(zUrn);
        z.setLink(zLink);
        z.setPathElemParams(zPeps);
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        pathElems.add(a);
        pathElems.add(b);
        pathElems.add(f);
        pathElems.add(g);
        pathElems.add(y);
        pathElems.add(z);
        
        localPath.setPathElems(pathElems);
        
        return localPath;
    }
    protected Reservation makeL3() throws BSSException {
        
        Reservation resv = new Reservation();
        Path localPath = this.makeL3Path();
        resv.setPath(localPath);
        // 1Mbps = 1 000 000 
        resv.setBandwidth(1000000L);
        resv.setDescription("description");
        resv.setLogin("username");
        resv.setGlobalReservationId("foo.net-123");
        resv.setStatus(StateEngine.RESERVED);
        resv.setLocalStatus(StateEngine.CONFIRMED);
        
        return resv;
    }
    
    protected Reservation makeL2() throws BSSException {
        
        Reservation resv = new Reservation();
        Path localPath = this.makeL2Path();
        resv.setPath(localPath);
        // 1Mbps = 1 000 000 
        resv.setBandwidth(1000000L);
        resv.setDescription("description");
        resv.setLogin("username");
        resv.setGlobalReservationId("foo.net-123");
        resv.setStatus(StateEngine.RESERVED);
        resv.setLocalStatus(StateEngine.CONFIRMED);
        
        return resv;
    }
    
    @Test
    public void testL2Setup() throws BSSException, PSSException {
        Reservation resv = this.makeL2();
        PSSHandlerConfigBean config = new PSSHandlerConfigBean();
        config.setCheckStatusAfterSetup(false);
        config.setCheckStatusAfterTeardown(false);
        config.setLogConfig(false);
        config.setStubMode(true);
        config.setTeardownOnFailure(false);
        config.setTemplateDir("conf/pss");
        
        SDNPSS pss = SDNPSS.getInstance(config);
        

        pss.createPath(resv);
        
    }

}
