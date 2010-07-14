package net.es.oscars.pss.layer3.junos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.Layer3Data;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PathUtils;
import net.es.oscars.pss.common.TemplateConfigGen;
import net.es.oscars.pss.eompls.EoMPLSUtils;
import net.es.oscars.pss.impl.SDNNameGenerator;

public class Layer3JunosConfigGen extends TemplateConfigGen {
    private Logger log;
    private static Layer3JunosConfigGen instance;


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String generateL3Setup(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "layer3-junos-setup.txt";

        // these are the leaf values
        String lspName, lspFrom, lspTo;
        Long lspBandwidth;
        String pathName;
        ArrayList<String> pathHops;

        String policingFilterName, policingFilterTerm, policingFilterCount;
        
        String srcPrefixListName, dstPrefixListName;
        String[] srcPrefixes, dstPrefixes;

        String policerName;
        Long policerBurstSizeLimit, policerBandwidthLimit;
        String inetDscp, inetProtocol, inetSrcPort, inetDstPort;
        
        String routingInstanceName, routingInstanceRibName;
        String[] inetFilterNames;
        
        
        
        
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */

        // bandwidth etc
        lspBandwidth = resv.getBandwidth();
        policerBandwidthLimit = lspBandwidth;
        policerBurstSizeLimit = lspBandwidth / 10;

        // path
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

        // need at least 4 path elements for LSP:
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

        String aLoopback    = aPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();
        String zLoopback    = zPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();

        pathHops            = EoMPLSUtils.makeHops(pathElems, direction);
        lspFrom             = aLoopback;
        lspTo               = yIP;
        
        Layer3Data layer3Data = localPath.getLayer3Data();
        String srcHostSpec = layer3Data.getSrcHost();
        String dstHostSpec = layer3Data.getDestHost();
        
        if (direction.equals(PSSDirection.A_TO_Z)) {
            srcPrefixes = srcHostSpec.split("\\s+");
            dstPrefixes = dstHostSpec.split("\\s+");
        } else if (direction.equals(PSSDirection.Z_TO_A)) {
            srcPrefixes = dstHostSpec.split("\\s+");
            dstPrefixes = srcHostSpec.split("\\s+");
        } else {
            throw new PSSException("Invalid direction");
        }
        inetDscp        = layer3Data.getDscp();
        inetProtocol    = layer3Data.getProtocol();
        if (layer3Data.getSrcIpPort() != null) {
            inetSrcPort     = layer3Data.getSrcIpPort().toString();
        } else {
            inetSrcPort = null;
        }
        if (layer3Data.getDestIpPort() != null) {
            inetDstPort     = layer3Data.getDestIpPort().toString();
        } else {
            inetDstPort = null;
        }

 
        pathName                = SDNNameGenerator.getPathName(resv);
        lspName                 = SDNNameGenerator.getLSPName(resv);
        // names etc
        srcPrefixListName       = SDNNameGenerator.getPrefixListName(resv, true);
        dstPrefixListName       = SDNNameGenerator.getPrefixListName(resv, false);
        inetFilterNames         = SDNNameGenerator.getLayer3Filters();
        policerName             = SDNNameGenerator.getPolicerName(resv);

        policingFilterName      = SDNNameGenerator.getFilterName(resv, "policing");
        policingFilterTerm      = policingFilterName;
        policingFilterCount     = policingFilterName;
        
        routingInstanceName     = SDNNameGenerator.getRoutingInstanceName(resv);
        routingInstanceRibName  = SDNNameGenerator.getRoutingInstanceRibName(resv);

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
        Map policing = new HashMap();
        Map policer = new HashMap();
        Map prefixes = new HashMap();
        Map routinginstance = new HashMap();
        Map inet= new HashMap();
        Map srcPrefixList = new HashMap();
        Map dstPrefixList = new HashMap();
        ArrayList inetFilters = new ArrayList();

        root.put("lsp", lsp);
        root.put("path", path);
        root.put("inet", inet);
        root.put("ifce", ifce);
        root.put("filters", filters);
        root.put("policer", policer);
        root.put("prefixes", prefixes);
        root.put("routinginstance", routinginstance);
        
        routinginstance.put("name", routingInstanceName);
        routinginstance.put("ribname", routingInstanceRibName);
        
        filters.put("inet", inetFilters);
        filters.put("policing", policing);

        policing.put("name", policingFilterName);
        policing.put("term", policingFilterTerm);
        policing.put("count", policingFilterCount);

        for (String inetFilterName : inetFilterNames) {
            String filterName = inetFilterName;
            String filterTerm = SDNNameGenerator.getFilterName(resv, "inet");
            String filterCount = filterTerm;
            String filterMarker = SDNNameGenerator.getInetFilterMarker(resv);
            
            HashMap inetFilter = new HashMap();
            inetFilter.put("name", filterName);
            inetFilter.put("term", filterTerm);
            inetFilter.put("count", filterCount);
            inetFilter.put("marker", filterMarker);
            inetFilters.add(inetFilter);
        }
        
        
        lsp.put("name", lspName);
        lsp.put("from", lspFrom);
        lsp.put("to", lspTo);
        lsp.put("bandwidth", lspBandwidth);
        
        path.put("hops", pathHops);
        path.put("name", pathName);


        policer.put("name", policerName);
        policer.put("burst_size_limit", policerBurstSizeLimit);
        policer.put("bandwidth_limit", policerBandwidthLimit);
        

        srcPrefixList.put("items", srcPrefixes);
        srcPrefixList.put("name", srcPrefixListName);
        dstPrefixList.put("items", dstPrefixes);
        dstPrefixList.put("name", dstPrefixListName);
        
        prefixes.put("src", srcPrefixList);
        prefixes.put("dst", dstPrefixList);
        
        inet.put("dscp", inetDscp);
        inet.put("protocol", inetProtocol);
        inet.put("srcport", inetSrcPort);
        inet.put("dstport", inetDstPort);

        return this.getConfig(root, templateFileName);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String generateL3Teardown(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "layer3-junos-teardown.txt";

        // these are the leaf values
        String lspName, pathName;
        String srcPrefixListName, dstPrefixListName;
        String policingFilterName;
        String policerName;
        String routingInstanceName;
        String[] inetFilterNames;

        
        /* *********************** */
        /* BEGIN POPULATING VALUES */
        /* *********************** */
        
        pathName                = SDNNameGenerator.getPathName(resv);
        lspName                 = SDNNameGenerator.getLSPName(resv);
        // names etc
        srcPrefixListName       = SDNNameGenerator.getPrefixListName(resv, true);
        dstPrefixListName       = SDNNameGenerator.getPrefixListName(resv, false);
        inetFilterNames         = SDNNameGenerator.getLayer3Filters();
        policerName             = SDNNameGenerator.getPolicerName(resv);

        policingFilterName      = SDNNameGenerator.getFilterName(resv, "policing");
        
        routingInstanceName     = SDNNameGenerator.getRoutingInstanceName(resv);

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
        Map policing = new HashMap();
        Map policer = new HashMap();
        Map prefixes = new HashMap();
        Map routinginstance = new HashMap();
        Map inet= new HashMap();
        Map srcPrefixList = new HashMap();
        Map dstPrefixList = new HashMap();
        ArrayList inetFilters = new ArrayList();

        root.put("lsp", lsp);
        root.put("path", path);
        root.put("inet", inet);
        root.put("ifce", ifce);
        root.put("filters", filters);
        root.put("policer", policer);
        root.put("prefixes", prefixes);
        root.put("routinginstance", routinginstance);
        
        routinginstance.put("name", routingInstanceName);
        
        filters.put("inet", inetFilters);
        filters.put("policing", policing);

        policing.put("name", policingFilterName);

        for (String inetFilterName : inetFilterNames) {
            String filterName = inetFilterName;
            String filterTerm = SDNNameGenerator.getFilterName(resv, "inet");
            
            HashMap inetFilter = new HashMap();
            inetFilter.put("name", filterName);
            inetFilter.put("term", filterTerm);
            inetFilters.add(inetFilter);
        }
        
        
        lsp.put("name", lspName);
        path.put("name", pathName);
        policer.put("name", policerName);
        srcPrefixList.put("name", srcPrefixListName);
        dstPrefixList.put("name", dstPrefixListName);
        prefixes.put("src", srcPrefixList);
        prefixes.put("dst", dstPrefixList);
        return this.getConfig(root, templateFileName);

    }

    public String generateL3Status(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        return "";
    }

    
    

    public static Layer3JunosConfigGen getInstance() {
        if (instance == null) {
            instance = new Layer3JunosConfigGen();
        }
        return instance;
    }

    private Layer3JunosConfigGen() {
        this.log = Logger.getLogger(this.getClass());
    }


}
