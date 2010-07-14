package net.es.oscars.pss.sw.junos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.TemplateConfigGen;
import net.es.oscars.pss.impl.SDNNameGenerator;

public class SWJunosConfigGen extends TemplateConfigGen {
    private Logger log;
    private static SWJunosConfigGen instance;


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String generateL2Setup(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "sw-junos-setup.txt";

        // these are the leaf values

        String aIfceName, aIfceDescription, aIfceVlan;
        String zIfceName, zIfceDescription, zIfceVlan;
        
        String filterName, filterTerm, filterCount;
        String policerName;
        Long policerBurstSizeLimit, policerBandwidthLimit;
        String iswitchName;
        
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        // 
        policerBandwidthLimit = resv.getBandwidth();
        policerBurstSizeLimit = resv.getBandwidth() / 10;
        
        List<PathElem> resvPathElems = localPath.getPathElems();
        if (resvPathElems.size() != 2) {
            throw new PSSException("Local path must have exactly 2 hops");
        }
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        pathElems.addAll(resvPathElems);
        if (!direction.equals(PSSDirection.BIDIRECTIONAL)) {
            throw new PSSException("Invalid direction!");
        }
        

        // need at 2 path elements for local switching:
        // A: ingress
        // Z: egress
        PathElem aPathElem      = pathElems.get(0);
        PathElem zPathElem      = pathElems.get(1);
        
        if (aPathElem.getLink() == null) {
            throw new PSSException("null link for: hop 1");
        } else if (zPathElem.getLink() == null) {
            throw new PSSException("null link for: hop 2");
        }
        
        if (!aPathElem.getLink().getPort().getNode().equalsTopoId(aPathElem.getLink().getPort().getNode())){ 
            throw new PSSException("path hops not on same node");
        }

        PathElemParam aVlanPEP;
        PathElemParam zVlanPEP;
        try {
            aVlanPEP = aPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            zVlanPEP = zPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
        if (aVlanPEP == null) {
            throw new PSSException("No VLAN set for: "+aPathElem.getLink().getFQTI());
        } else if (zVlanPEP == null) {
            throw new PSSException("No VLAN set for: "+zPathElem.getLink().getFQTI());
        }
        
        aIfceName               = aPathElem.getLink().getPort().getTopologyIdent();
        zIfceName               = zPathElem.getLink().getPort().getTopologyIdent();
        
        aIfceVlan               = aVlanPEP.getValue();
        zIfceVlan               = zVlanPEP.getValue();
        // names etc
        filterName              = SDNNameGenerator.getFilterName(resv, "combo");
        filterTerm              = filterName;
        filterCount             = filterName;
        policerName             = SDNNameGenerator.getPolicerName(resv);
        aIfceDescription        = SDNNameGenerator.getInterfaceDescription(resv);
        zIfceDescription        = SDNNameGenerator.getInterfaceDescription(resv);
        
        // FIXME 
        iswitchName             = filterName;

        /* ********************** */
        /* DONE POPULATING VALUES */
        /* ********************** */

        
        

        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        Map ifce_a = new HashMap();
        Map ifce_z = new HashMap();
        Map filter = new HashMap();
        Map policer = new HashMap();
        Map iswitch = new HashMap();

        root.put("ifce_a", ifce_a);
        root.put("ifce_z", ifce_z);
        root.put("filter", filter);
        root.put("policer", policer);
        root.put("iswitch", iswitch);

        iswitch.put("name", iswitchName);

        ifce_a.put("name", aIfceName);
        ifce_a.put("vlan", aIfceVlan);
        ifce_a.put("description", aIfceDescription);

        ifce_z.put("name", zIfceName);
        ifce_z.put("vlan", zIfceVlan);
        ifce_z.put("description", zIfceDescription);

        policer.put("name", policerName);
        policer.put("burst_size_limit", policerBurstSizeLimit);
        policer.put("bandwidth_limit", policerBandwidthLimit);

        filter.put("name", filterName);
        filter.put("term", filterTerm);
        filter.put("count", filterCount);



        return this.getConfig(root, templateFileName);
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String generateL2Teardown(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "sw-junos-teardown.txt";
        // these are the leaf values

        String aIfceName, aIfceVlan;
        String zIfceName, zIfceVlan;
        
        String filterName;
        String policerName;
        String iswitchName;
        
        

        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        List<PathElem> resvPathElems = localPath.getPathElems();
        if (resvPathElems.size() != 2) {
            throw new PSSException("Local path must have exactly 2 hops");
        }
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        pathElems.addAll(resvPathElems);
        if (!direction.equals(PSSDirection.BIDIRECTIONAL)) {
            throw new PSSException("Invalid direction!");
        }
        

        // need at 2 path elements for local switching:
        // A: ingress
        // Z: egress
        PathElem aPathElem      = pathElems.get(0);
        PathElem zPathElem      = pathElems.get(1);
        
        if (aPathElem.getLink() == null) {
            throw new PSSException("null link for: hop 1");
        } else if (zPathElem.getLink() == null) {
            throw new PSSException("null link for: hop 2");
        }
        
        if (!aPathElem.getLink().getPort().getNode().equalsTopoId(aPathElem.getLink().getPort().getNode())){ 
            throw new PSSException("path hops not on same node");
        }

        PathElemParam aVlanPEP;
        PathElemParam zVlanPEP;
        try {
            aVlanPEP = aPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            zVlanPEP = zPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
        if (aVlanPEP == null) {
            throw new PSSException("No VLAN set for: "+aPathElem.getLink().getFQTI());
        } else if (zVlanPEP == null) {
            throw new PSSException("No VLAN set for: "+zPathElem.getLink().getFQTI());
        }
        
        aIfceName               = aPathElem.getLink().getPort().getTopologyIdent();
        zIfceName               = zPathElem.getLink().getPort().getTopologyIdent();
        
        aIfceVlan               = aVlanPEP.getValue();
        zIfceVlan               = zVlanPEP.getValue();
        // names etc
        filterName              = SDNNameGenerator.getFilterName(resv, "combo");
        policerName             = SDNNameGenerator.getPolicerName(resv);
        
        // FIXME 
        iswitchName             = filterName;

        /* ********************** */
        /* DONE POPULATING VALUES */
        /* ********************** */
        

        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        Map ifce_a = new HashMap();
        Map ifce_z = new HashMap();
        Map filter = new HashMap();
        Map policer = new HashMap();
        Map iswitch = new HashMap();

        root.put("ifce_a", ifce_a);
        root.put("ifce_z", ifce_z);
        root.put("filter", filter);
        root.put("policer", policer);
        root.put("iswitch", iswitch);        
        iswitch.put("name", iswitchName);

        ifce_a.put("name", aIfceName);
        ifce_a.put("vlan", aIfceVlan);

        ifce_z.put("name", zIfceName);
        ifce_z.put("vlan", zIfceVlan);

        policer.put("name", policerName);

        filter.put("name", filterName);
        return this.getConfig(root, templateFileName);
    }

    public String generateL2Status(Reservation resv, Path localPath, PSSDirection direction) {
        // TODO
        String config = "";
        return config;
    }

    
    

    public static SWJunosConfigGen getInstance() {
        if (instance == null) {
            instance = new SWJunosConfigGen();
        }
        return instance;
    }

    private SWJunosConfigGen() {
        this.log = Logger.getLogger(this.getClass());
    }


}
