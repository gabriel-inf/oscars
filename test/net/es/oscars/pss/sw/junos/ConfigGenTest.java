package net.es.oscars.pss.sw.junos;
import java.util.ArrayList;
import java.util.HashSet;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.L2SwitchingCapabilityData;
import net.es.oscars.bss.topology.Layer2Data;
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
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.impl.SDNNameGenerator;

import org.testng.annotations.*;

@Test(groups={ "pss.sw" })
public class ConfigGenTest {
    private String aUrn = "urn:ogf:network:domain=foo:node=alpha:port=xe-0/0/0:link=*";
    private String zUrn = "urn:ogf:network:domain=foo:node=alpha:port=xe-9/0/0:link=*";
    
    private Link aLink;
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

        
        HashSet<Node> nodes = new HashSet<Node>();
        nodes.add(alpha);
        dom.setNodes(nodes);

        
        L2SwitchingCapabilityData aCap = new L2SwitchingCapabilityData();
        L2SwitchingCapabilityData zCap = new L2SwitchingCapabilityData();
        aCap.setVlanRangeAvailability("2-4095");
        zCap.setVlanRangeAvailability("2-4095");
        aCap.setVlanTranslation(true);
        zCap.setVlanTranslation(true);
        
        Port aPort = new Port();
        aPort.setNode(alpha);
        aPort.setTopologyIdent("xe-0/0/0");
        

        Port zPort = new Port();
        zPort.setNode(alpha);
        zPort.setTopologyIdent("xe-9/0/0");

        aLink = new Link();
        aLink.setPort(aPort);
        aLink.setTopologyIdent("*");
        aLink.setL2SwitchingCapabilityData(aCap);

        zLink = new Link();
        zLink.setPort(zPort);
        zLink.setTopologyIdent("*");
        zLink.setL2SwitchingCapabilityData(zCap);
        
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
        

        PathElem z = new PathElem();
        z.setUrn(zUrn);
        z.setLink(zLink);
        z.setPathElemParams(zPeps);
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        pathElems.add(a);
        pathElems.add(z);
        
        localPath.setPathElems(pathElems);
        
        return localPath;
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
        
        return resv;
    }
    
    @Test
    public void testL2Setup() throws BSSException, PSSException {
        Reservation resv = this.makeL2();
        SWJunosConfigGen th = SWJunosConfigGen.getInstance();
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        th.setNameGenerator(ng);
        
        th.setTemplateDir("conf/pss");
        String out;
        out = th.generateL2Setup(resv, resv.getPath(PathType.LOCAL), PSSDirection.BIDIRECTIONAL);
        System.out.println(out);
    }
    @Test
    public void testL2Teardown() throws BSSException, PSSException {
        Reservation resv = this.makeL2();
        SWJunosConfigGen th = SWJunosConfigGen.getInstance();
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        th.setNameGenerator(ng);

        th.setTemplateDir("conf/pss");
        String out;
        out = th.generateL2Teardown(resv, resv.getPath(PathType.LOCAL), PSSDirection.BIDIRECTIONAL);
        System.out.println(out);
    }

    @Test
    public void testL2Status() throws BSSException, PSSException {
        Reservation resv = this.makeL2();
        SWJunosConfigGen th = SWJunosConfigGen.getInstance();
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        th.setNameGenerator(ng);
        th.setTemplateDir("conf/pss");
        String out;
        out = th.generateL2Status(resv, resv.getPath(PathType.LOCAL), PSSDirection.BIDIRECTIONAL);
        System.out.println(out);
    }

}
