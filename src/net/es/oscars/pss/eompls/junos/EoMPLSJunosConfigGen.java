package net.es.oscars.pss.eompls.junos;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.ConfigNameGenerator;
import net.es.oscars.pss.common.PSSAction;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.common.TemplateConfigGen;
import net.es.oscars.pss.eompls.EoMPLSUtils;

public class EoMPLSJunosConfigGen extends TemplateConfigGen {
    private static EoMPLSJunosConfigGen instance;
    private ConfigNameGenerator nameGenerator = null;
    private Logger log = Logger.getLogger(EoMPLSJunosConfigGen.class);


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String generateL2Setup(Reservation resv, PSSDirection direction) throws PSSException {
        log.info("generating setup for gri: "+resv.getGlobalReservationId()+" "+direction);

        if (nameGenerator == null) {
            throw new PSSException("no name generator set");
        }

        String templateFileName = "eompls-junos-setup.txt";

        Path localPath;
        try {
            localPath = resv.getPath(PathType.LOCAL);
        } catch (BSSException e) {
            log.error(e);
            throw new PSSException(e);
        }
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
        String oscarsCommunity;
        
        
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        // 
        lspBandwidth = resv.getBandwidth();
        policerBandwidthLimit = lspBandwidth;
        policerBurstSizeLimit = lspBandwidth / 10;
        
        List<PathElem> resvPathElems = localPath.getPathElems();
        log.info("path length: "+resvPathElems.size());
        for (PathElem pe : resvPathElems) {
            try {
                String fqti = pe.getLink().getFQTI();
                log.debug(fqti);
            } catch (org.hibernate.LazyInitializationException ex) {
                OSCARSCore core = OSCARSCore.getInstance();
                core.getBssDbName();
                core.getBssSession();
            }
        }
        
        if (resvPathElems.size() < 4) {
            log.error("Local path too short");
            throw new PSSException("Local path too short");
        }
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        if (direction.equals(PSSDirection.A_TO_Z)) {
            pathElems.addAll(resvPathElems);
        } else if (direction.equals(PSSDirection.Z_TO_A)) {
            pathElems = PathUtils.reversePath(resvPathElems);
        } else {
            log.error("Invalid direction "+direction);
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
        
        if (aPathElem == null) {
            log.error("null pathelem for: hop 1");
            throw new PSSException("null pathelem for: hop 1");
        } else if (yPathElem == null) {
            log.error("null pathelem for: hop N-1");
            throw new PSSException("null pathelem for: hop N-1");
        } else if (zPathElem == null) {
            log.error("null pathelem for: hop N");
            throw new PSSException("null pathelem for: hop N");
        }

        if (aPathElem.getLink() == null) {
            log.error("null link for: hop 1");
            throw new PSSException("null link for: hop 1");
        } else if (yPathElem.getLink() == null) {
            log.error("null link for: hop N-1");
            throw new PSSException("null link for: hop N-1");
        } else if (zPathElem.getLink() == null) {
            log.error("null link for: hop N");
            throw new PSSException("null link for: hop N");
        }
        
        String yIP;
        Ipaddr ipaddr = yPathElem.getLink().getValidIpaddr();
        if (ipaddr != null) {
            yIP = ipaddr.getIP();
            log.info("found IP: "+yIP+" for "+yPathElem.getLink().getFQTI());
        } else {
            log.error("Invalid IP for: "+yPathElem.getLink().getFQTI());
            throw new PSSException("Invalid IP for: "+yPathElem.getLink().getFQTI());
        }

        PathElemParam aVlanPEP;
        PathElemParam zVlanPEP;
        try {
            aVlanPEP = aPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            zVlanPEP = zPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        } catch (BSSException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        }
        if (aVlanPEP == null) {
            log.error("No VLAN set for: "+aPathElem.getLink().getFQTI());
            throw new PSSException("No VLAN set for: "+aPathElem.getLink().getFQTI());
        } else if (zVlanPEP == null) {
            log.error("No VLAN set for: "+zPathElem.getLink().getFQTI());
            throw new PSSException("No VLAN set for: "+zPathElem.getLink().getFQTI());
        }
        
        
        
        
        // decide VC id
        // if port at A is xe-1/3/0 and vlan is 2259
        // vcid is 1302259
        // we only use the port at A.
        String portTopoId = "";
        String vlanIdForVC = "";
        if (direction.equals(PSSDirection.A_TO_Z)) {
             portTopoId = aPathElem.getLink().getPort().getTopologyIdent();
             vlanIdForVC = aVlanPEP.getValue();
        } else if (direction.equals(PSSDirection.Z_TO_A)) {
            portTopoId = zPathElem.getLink().getPort().getTopologyIdent();
            vlanIdForVC = zVlanPEP.getValue();
        }
        Pattern pattern =  Pattern.compile(".*(\\d).(\\d).(\\d).*");
        Matcher matcher =  pattern.matcher(portTopoId);
        String x = null;
        String y = null;
        String z = null;
        
        while (matcher.find()){
            x = matcher.group(1);
            y = matcher.group(2);
            z = matcher.group(3); 
        }
        if (x == null || y == null || z == null) {
            throw new PSSException("could not decide a l2circuit vcid!");
        }
        l2circuitVCID = x+y+z+vlanIdForVC;
        
        
        String aLoopback    = aPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();
        String zLoopback    = zPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();

        pathHops            = EoMPLSUtils.makeHops(pathElems, direction);
        ifceName            = aPathElem.getLink().getPort().getTopologyIdent();
        ifceVlan            = aVlanPEP.getValue();
        remoteVlan          = zVlanPEP.getValue();
        l2circuitEgress     = zLoopback;
        lspFrom             = aLoopback;
        lspTo               = yIP;
        


        

        // community is 30000 - 65500
        Random rand = new Random();
        Integer randInt = 30000 + rand.nextInt(35500);
        if (nameGenerator.getOscarsCommunity(resv) > 65535) {
            oscarsCommunity  = nameGenerator.getOscarsCommunity(resv)+"L";
        } else {
            oscarsCommunity  = nameGenerator.getOscarsCommunity(resv).toString();
        }
        
        communityMembers    = "65000:"+oscarsCommunity+":"+randInt;
        

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

        log.info("generated setup for gri: "+resv.getGlobalReservationId()+" "+direction);

        return this.getConfig(root, templateFileName);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String generateL2Teardown(Reservation resv, PSSDirection direction) throws PSSException {
        String templateFileName = "eompls-junos-teardown.txt";
        Path localPath;
        try {
            localPath = resv.getPath(PathType.LOCAL);
        } catch (BSSException e) {
            throw new PSSException(e);
        }
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

    public String generateL2Status(Reservation resv, PSSDirection direction) throws PSSException {
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
    }


    public ConfigNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public void setNameGenerator(ConfigNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    /**
     * @return true if the status is what it is supposed to be (i.e up for setup, down for teardown)
     */
    public boolean checkStatus(Document statusDoc, PSSAction action, PSSDirection direction, Reservation resv) throws PSSException {
        boolean result = false;
        String gri = resv.getGlobalReservationId();
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        if (pc.getHandlerConfig().isStubMode()) {
            log.debug("stub mode; status is success");
            return true;
        }
        
        
        // collect needed info from reservation: ifce name, unit, remote router IP
        Path localPath;
        try {
            localPath = resv.getPath(PathType.LOCAL);
        } catch (BSSException e) {
            log.error(e);
            
            throw new PSSException(e);
        }
        List<PathElem> resvPathElems = localPath.getPathElems();
        log.info("path length: "+resvPathElems.size());
        for (PathElem pe : resvPathElems) {
            try {
                String fqti = pe.getLink().getFQTI();
                log.debug(fqti);
            } catch (org.hibernate.LazyInitializationException ex) {
                OSCARSCore core = OSCARSCore.getInstance();
                core.getBssDbName();
                core.getBssSession();
            }
        }
        
        if (resvPathElems.size() < 4) {
            log.error("Local path too short");
            throw new PSSException("Local path too short");
        }
        
        ArrayList<PathElem> pathElems = new ArrayList<PathElem>();
        if (direction.equals(PSSDirection.A_TO_Z)) {
            pathElems.addAll(resvPathElems);
        } else if (direction.equals(PSSDirection.Z_TO_A)) {
            pathElems = PathUtils.reversePath(resvPathElems);
        } else {
            log.error("Invalid direction "+direction);
            throw new PSSException("Invalid direction!");
        }
        PathElem aPathElem      = pathElems.get(0);
        PathElem zPathElem      = pathElems.get(pathElems.size()-1);

        PathElemParam aVlanPEP;
        try {
            aVlanPEP = aPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
        } catch (BSSException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        }
        if (aVlanPEP == null) {
            log.error("No VLAN set for: "+aPathElem.getLink().getFQTI());
            throw new PSSException("No VLAN set for: "+aPathElem.getLink().getFQTI());
        }
        
        String zLoopback    = zPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();


        
        

        String ingIfceName              = aPathElem.getLink().getPort().getTopologyIdent();
        String ingIfceUnit              = aVlanPEP.getValue();
        String ingIfceId                = ingIfceName+"."+ingIfceUnit;
        


        
        // XML parsing bit
        // NOTE WELL: if response format changes, this won't work
        
        HashMap<String, String> nsmap = new HashMap<String, String>();
        nsmap.put( "routing", "http://xml.juniper.net/junos/9.3I0/junos-routing");
        
        
        /* ok now go find if we the status doc has the connections set
        * this is a sample xpath :
        *  //l2circuit-neighbor[neighbor-address="134.55.200.116"]/connection[local-interface/interface-name="xe-0/1/0.3501"]
        */
        
        String connectionStatus = "";
        String ifceStatus = "";
        boolean isVCup = false;
        boolean isVCConfigured = false;
        String xpathExpr = "//routing:l2circuit-neighbor[neighbor-address='"+zLoopback+"']/connection[local-interface/interface-name='"+ingIfceId+"']";
        log.debug("xpath is: "+xpathExpr);
        
        XPath xpath;
        Element conn = null;
        try {
            xpath = new JDOMXPath(xpathExpr);
            xpath.setNamespaceContext(new SimpleNamespaceContext(nsmap));
            conn = (Element) xpath.selectSingleNode(statusDoc);
        } catch (JaxenException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
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
        if (isVCup) {
            log.debug(gri+": "+direction+" : VC is up"); 
        } else {
            log.debug(gri+": "+direction+" : VC is down"); 
        }
            
        if (isVCConfigured) {
            log.debug(gri+": "+direction+" : VC is configured"); 
        } else {
            log.debug(gri+": "+direction+" : VC is not configured"); 
            
        }
        
        
        if (action.equals(PSSAction.SETUP)) {
            if (isVCup) {
                result = true;
            } else if (isVCConfigured) {
                result = false;
            }
        } else if (action.equals(PSSAction.TEARDOWN)) {
            if (isVCup) {
                result = false;
            } else if (isVCConfigured) {
                result = false;
            } else {
                result = true;
            }
        }
        
       
        return result;
    }
}
