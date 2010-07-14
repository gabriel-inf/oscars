package net.es.oscars.pss.eompls.junos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.ConfigNameGenerator;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.common.TemplateConfigGen;
import net.es.oscars.pss.eompls.EoMPLSUtils;

public class EoMPLSJunosConfigGen extends TemplateConfigGen {
    private Logger log;
    private static EoMPLSJunosConfigGen instance;
    private ConfigNameGenerator nameGenerator;


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String generateL2Setup(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "eompls-junos-setup.txt";

        // these are the leaf values

        String ifceName, ifceDescription;
        String ifceVlan, remoteVlan;
        String policyName, policyTerm;
        String communityName, communityMembers;
        String lspName, lspFrom, lspTo;
        Long lspBandwidth;
        String pathName;
        ArrayList<String> pathHops;
        String l2circuitEgress, l2circuitVCID, l2circuitDescription;
        String policerName;
        Long policerBurstSizeLimit, policerBandwidthLimit;
        String statsFilterName, statsFilterTerm, statsFilterCount;
        String policingFilterName, policingFilterTerm, policingFilterCount;
        
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        // 
        lspBandwidth = resv.getBandwidth();
        policerBandwidthLimit = lspBandwidth;
        policerBurstSizeLimit = lspBandwidth / 10;
        
        List<PathElem> resvPathElems = localPath.getPathElems();
        if (resvPathElems.size() < 4) {
            throw new PSSException("Local path too short");
        }
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        if (direction.equals(PSSDirection.A_TO_Z)) {
            pathElems.addAll(resvPathElems);
        } else if (direction.equals(PSSDirection.Z_TO_A)) {
            pathElems = PathUtils.reversePath(resvPathElems);
        } else {
            throw new PSSException("Invalid direction!");
        }
        

        // need at least 4 path elements for EoMPLS:
        // A: ingress
        // B: internal facing link at ingress router
        // Y: internal facing link at egress router
        // Z: egress
        //
        // but for setup we only care about A, Y, and Z
        PathElem aPathElem      = pathElems.get(0);
        PathElem yPathElem      = pathElems.get(pathElems.size()-2);
        PathElem zPathElem      = pathElems.get(pathElems.size()-1);
        
        if (aPathElem.getLink() == null) {
            throw new PSSException("null link for: hop 1");
        } else if (yPathElem.getLink() == null) {
            throw new PSSException("null link for: hop N-1");
        } else if (zPathElem.getLink() == null) {
            throw new PSSException("null link for: hop N");
        }
        
        String yIP;
        Ipaddr ipaddr = yPathElem.getLink().getValidIpaddr();
        if (ipaddr != null) {
            yIP = ipaddr.getIP();
        } else {
            throw new PSSException("Invalid IP for: "+yPathElem.getLink().getFQTI());
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
        
        String aLoopback    = aPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();
        String zLoopback    = zPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();

        pathHops            = EoMPLSUtils.makeHops(pathElems, direction);
        ifceName            = aPathElem.getLink().getPort().getTopologyIdent();
        ifceVlan            = aVlanPEP.getValue();
        remoteVlan          = zVlanPEP.getValue();
        l2circuitEgress     = zLoopback;
        lspFrom             = aLoopback;
        lspTo               = yIP;
        

        // FIXME: this is WRONG; these should NOT depend on vlans
        communityMembers    = "65000:"+aVlanPEP.getValue();
        l2circuitVCID       = aVlanPEP.getValue()+zVlanPEP.getValue();
        // end FIXME

        // names etc
        policingFilterName      = nameGenerator.getFilterName(resv, "policing");
        policingFilterTerm      = policingFilterName;
        policingFilterCount     = policingFilterName;
        statsFilterName         = nameGenerator.getFilterName(resv, "stats");
        statsFilterTerm         = statsFilterName;
        statsFilterCount        = statsFilterName;
        communityName           = nameGenerator.getCommunityName(resv);
        policyName              = nameGenerator.getPolicyName(resv);
        policyTerm              = policyName;
        policerName             = nameGenerator.getPolicerName(resv);
        pathName                = nameGenerator.getPathName(resv);
        lspName                 = nameGenerator.getLSPName(resv);
        l2circuitDescription    = nameGenerator.getL2CircuitDescription(resv);
        ifceDescription         = nameGenerator.getInterfaceDescription(resv);


        /* ********************** */
        /* DONE POPULATING VALUES */
        /* ********************** */

        
        

        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();
        Map filters = new HashMap();
        Map stats = new HashMap();
        Map policing = new HashMap();
        Map community = new HashMap();
        Map policy = new HashMap();
        Map l2circuit = new HashMap();
        Map policer = new HashMap();

        root.put("lsp", lsp);
        root.put("path", path);
        root.put("ifce", ifce);
        root.put("filters", filters);
        root.put("policy", policy);
        root.put("policer", policer);
        root.put("l2circuit", l2circuit);
        root.put("community", community);
        root.put("remotevlan", remoteVlan);

        filters.put("stats", stats);
        filters.put("policing", policing);

        stats.put("name", statsFilterName);
        stats.put("term", statsFilterTerm);
        stats.put("count", statsFilterCount);
        policing.put("name", policingFilterName);
        policing.put("term", policingFilterTerm);
        policing.put("count", policingFilterCount);


        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);
        ifce.put("description", ifceDescription);

        lsp.put("name", lspName);
        lsp.put("from", lspFrom);
        lsp.put("to", lspTo);
        lsp.put("bandwidth", lspBandwidth);
        
        path.put("hops", pathHops);
        path.put("name", pathName);

        l2circuit.put("egress", l2circuitEgress);
        l2circuit.put("vcid", l2circuitVCID);
        l2circuit.put("description", l2circuitDescription);

        policer.put("name", policerName);
        policer.put("burst_size_limit", policerBurstSizeLimit);
        policer.put("bandwidth_limit", policerBandwidthLimit);


        community.put("name", communityName);
        community.put("members", communityMembers);

        policy.put("name", policyName);
        policy.put("term", policyTerm);


        return this.getConfig(root, templateFileName);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String generateL2Teardown(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "eompls-junos-teardown.txt";

        String ifceName, ifceVlan;
        String policyName;
        String communityName;
        String lspName;
        String pathName;
        String l2circuitEgress;
        String policerName;
        String statsFilterName;
        String policingFilterName;
        
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        List<PathElem> resvPathElems = localPath.getPathElems();
        if (resvPathElems.size() < 4) {
            throw new PSSException("Local path too short");
        }
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        if (direction.equals(PSSDirection.A_TO_Z)) {
            pathElems.addAll(resvPathElems);
        } else if (direction.equals(PSSDirection.Z_TO_A)) {
            pathElems = PathUtils.reversePath(resvPathElems);
        } else {
            throw new PSSException("Invalid direction!");
        }
        

        // need at least 4 path elements for EoMPLS:
        // A: ingress
        // B: internal facing link at ingress router
        // Y: internal facing link at egress router
        // Z: egress
        
        // for teardown we only need info from A and Z
        PathElem aPathElem      = pathElems.get(0);
       PathElem zPathElem      = pathElems.get(pathElems.size()-1);
        
        if (aPathElem.getLink() == null) {
            throw new PSSException("null link for: hop 1");
        } else if (zPathElem.getLink() == null) {
            throw new PSSException("null link for: hop N");
        }
        
        PathElemParam aVlanPEP;
        try {
            aVlanPEP = aPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }
        if (aVlanPEP == null) {
            throw new PSSException("No VLAN set for: "+aPathElem.getLink().getFQTI());
        }
        

        String zLoopback    = zPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();
        ifceName            = aPathElem.getLink().getPort().getTopologyIdent();
        ifceVlan            = aVlanPEP.getValue();
        l2circuitEgress     = zLoopback;
        
        
        // names etc
        policingFilterName      = nameGenerator.getFilterName(resv, "policing");
        statsFilterName         = nameGenerator.getFilterName(resv, "stats");
        communityName           = nameGenerator.getCommunityName(resv);
        policyName              = nameGenerator.getPolicyName(resv);
        policerName             = nameGenerator.getPolicerName(resv);
        pathName                = nameGenerator.getPathName(resv);
        lspName                 = nameGenerator.getLSPName(resv);
        
        

        /* ********************** */
        /* DONE POPULATING VALUES */
        /* ********************** */        
        
        
        
        // create and populate the model
        // this needs to match with the template
        Map root = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();
        Map filters = new HashMap();
        Map stats = new HashMap();
        Map policing = new HashMap();
        Map community = new HashMap();
        Map policy = new HashMap();
        Map l2circuit = new HashMap();
        Map policer = new HashMap();

        root.put("lsp", lsp);
        root.put("path", path);
        root.put("ifce", ifce);
        root.put("filters", filters);
        root.put("policy", policy);
        root.put("policer", policer);
        root.put("l2circuit", l2circuit);
        root.put("community", community);

        filters.put("stats", stats);
        filters.put("policing", policing);

        stats.put("name", statsFilterName);
        policing.put("name", policingFilterName);

        ifce.put("name", ifceName);
        ifce.put("vlan", ifceVlan);

        lsp.put("name", lspName);
        path.put("name", pathName);

        l2circuit.put("egress", l2circuitEgress);

        policer.put("name", policerName);
        
        community.put("name", communityName);

        policy.put("name", policyName);
        return this.getConfig(root, templateFileName);

    }

    public String generateL2Status(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "eompls-junos-status.txt";
        HashMap<String, Object> root = new HashMap<String, Object>();
        return this.getConfig(root, templateFileName);
    }

    
    

    public static EoMPLSJunosConfigGen getInstance() {
        if (instance == null) {
            instance = new EoMPLSJunosConfigGen();
        }
        return instance;
    }

    private EoMPLSJunosConfigGen() {
        this.log = Logger.getLogger(this.getClass());
    }


    public ConfigNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public void setNameGenerator(ConfigNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

}
