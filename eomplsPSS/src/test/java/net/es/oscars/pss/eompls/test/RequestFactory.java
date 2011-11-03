package net.es.oscars.pss.eompls.test;

import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.VlanTag;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

public class RequestFactory {
    public static ResDetails getTwoHop() {
        String gri = "twoHop-768";
        String srcEdge = "urn:ogf:network:foo.net:alpha:xe-1/1/0:edge";
        String hop1    = "urn:ogf:network:foo.net:alpha:xe-2/0/0:beta";
        String hop2    = "urn:ogf:network:foo.net:beta:1/1/1:alpha";
        String hop3    = "urn:ogf:network:foo.net:beta:2/1/1:gamma";
        String hop4    = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet0/1:beta";
        String dstEdge = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet3/0:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("999");
        dstVlan.setTagged(true);
        dstVlan.setValue("999");
        rc.setBandwidth(1000);

        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "999");
        
        CtrlPlaneHopContent cpHop1          = RequestFactory.makeInternalHop(hop1);
        CtrlPlaneHopContent cpHop2          = RequestFactory.makeInternalHop(hop2);
        CtrlPlaneHopContent cpHop3          = RequestFactory.makeInternalHop(hop3);
        CtrlPlaneHopContent cpHop4          = RequestFactory.makeInternalHop(hop4);
        
        
        

        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "999");
        

        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(cpHop1);
        path.getHop().add(cpHop2);
        path.getHop().add(cpHop3);
        path.getHop().add(cpHop4);
        path.getHop().add(dstHop);
        return resDet;
    }
    public static ResDetails getAB() {
        String gri = "oneHop-311";
        String srcEdge = "urn:ogf:network:foo.net:beta:1/1/3:edge";
        String hop1    = "urn:ogf:network:foo.net:beta:2/1/1:gamma";
        String hop2    = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet0/1:beta";
        String dstEdge = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet3/0:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("999");
        dstVlan.setTagged(true);
        dstVlan.setValue("999");
        rc.setBandwidth(1000);

        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "999");
        
        CtrlPlaneHopContent cpHop1          = RequestFactory.makeInternalHop(hop1);
        CtrlPlaneHopContent cpHop2          = RequestFactory.makeInternalHop(hop2);
        
        
        

        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "999");
        

        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(cpHop1);
        path.getHop().add(cpHop2);
        path.getHop().add(dstHop);
        return resDet;
    }
    
    public static ResDetails getCD() {
        String gri = "oneHopCD-311";
        String srcEdge = "urn:ogf:network:foo.net:beta:1/1/3:edge";
        String hop1    = "urn:ogf:network:foo.net:beta:3/1/1:delta";
        String hop2    = "urn:ogf:network:foo.net:delta:1/1/1:beta";
        String dstEdge = "urn:ogf:network:foo.net:delta:3/1/1:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("999");
        dstVlan.setTagged(true);
        dstVlan.setValue("999");
        rc.setBandwidth(1000);

        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "999");
        
        CtrlPlaneHopContent cpHop1          = RequestFactory.makeInternalHop(hop1);
        CtrlPlaneHopContent cpHop2          = RequestFactory.makeInternalHop(hop2);
        
        
        

        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "999");
        

        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(cpHop1);
        path.getHop().add(cpHop2);
        path.getHop().add(dstHop);
        return resDet;
    }
    
    
    public static ResDetails getSameDevice() {
        String gri = "sameDev-333";
        String srcEdge = "urn:ogf:network:foo.net:alpha:xe-1/1/0:edge";
        String dstEdge = "urn:ogf:network:foo.net:alpha:xe-2/1/0:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("999");
        dstVlan.setTagged(true);
        dstVlan.setValue("999");
        rc.setBandwidth(1000);
        
        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "999");
        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "999");
        
        
        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(dstHop);
        return resDet;        
    }
    
    public static CtrlPlaneHopContent makeInternalHop(String linkId) {
        CtrlPlaneHopContent hop          = new CtrlPlaneHopContent();
        CtrlPlaneLinkContent link        = new CtrlPlaneLinkContent();
        hop.setLinkIdRef(linkId);
        link.setId(linkId);
        hop.setLink(link);
        return hop;
        
    }
    
    public static CtrlPlaneHopContent makeEdgeHop(String linkId, String vlan) {
        
        CtrlPlaneHopContent hop          = new CtrlPlaneHopContent();
        
        CtrlPlaneLinkContent link        = new CtrlPlaneLinkContent();
        CtrlPlaneSwcapContent scp        = new CtrlPlaneSwcapContent();
        CtrlPlaneSwitchingCapabilitySpecificInfo ssi
                                         = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        
        hop.setLinkIdRef(linkId);
        link.setId(linkId);
        ssi.setSuggestedVLANRange("999");
        ssi.setVlanRangeAvailability("999");
        scp.setSwitchingCapabilitySpecificInfo(ssi);
        link.setSwitchingCapabilityDescriptors(scp);
        hop.setLink(link);
        return hop;
    }
    
}
