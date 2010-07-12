package net.es.oscars.pss.eompls.junos;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.NodeAddress;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.impl.SDNNameGenerator;

public class TemplateHandler {
    private String templateDir = "";
    private Logger log;
    private static TemplateHandler instance;


    @SuppressWarnings("unchecked")
    private String getConfig(Map root, String templateFileName) throws PSSException {
        String config = "";
        Template temp = null;
        Configuration cfg = new Configuration();
        try {
            cfg.setDirectoryForTemplateLoading(new File(templateDir));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Writer out = new StringWriter();
            temp = cfg.getTemplate(templateFileName);
            temp.process(root, out);
            out.flush();
            config = out.toString();
        } catch (IOException e) {
            throw new PSSException(e.getMessage());
        } catch (TemplateException e) {
            throw new PSSException(e.getMessage());
        }
        return config;
    }

    @SuppressWarnings("unchecked")
    public String generateEoMPLSSetup(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        String templateFileName = "eompls-junos-setup.txt";

        // these are the leaf values
        Long bandwidth = 0L;
        String remoteVlan;

        String ifceName;
        String ifceDescription = "";
        String ifceVlan;

        String policyName = "";
        String policyTerm = "";

        String communityName;
        String communityMembers = "";

        String lspName = "";
        String lspFrom = "";
        String lspTo = "";

        String l2circuitEgress = "";
        String l2circuitVCID = "";
        String l2circuitDescription = "";

        String policerName;
        Long policerBurstSizeLimit;
        Long policerBandwidthLimit;

        String pathName = "";
        ArrayList<String> pathHops = new ArrayList<String>();

        String statsFilterName;
        String statsFilterTerm = "";
        String statsFilterCount = "";

        String policingFilterName;
        String policingFilterTerm = "";
        String policingFilterCount = "";



        // create config directives based on reservation parameters
        String gri = resv.getGlobalReservationId();
        String description = resv.getDescription();
        bandwidth = resv.getBandwidth();
        policerBandwidthLimit = bandwidth;
        policerBurstSizeLimit = bandwidth / 10;
        List<PathElem> pathElems = localPath.getPathElems();
        if (pathElems.size() < 4) {
            throw new PSSException("Local path too short");
        }
        // need at least 4 path elements for EoMPLS:
        // ingress
        // internal facing link at ingress router
        // internal facing link at egress router
        // egress
        PathElem ingressPathElem    = pathElems.get(0);
        PathElem ingressInPathElem  = pathElems.get(1);
        PathElem egressInPathElem   = pathElems.get(pathElems.size()-2);
        PathElem egressPathElem     = pathElems.get(pathElems.size()-1);
        String ingressInIP;
        String egressInIP;
        Ipaddr ipaddr = ingressInPathElem.getLink().getValidIpaddr();
        if (ipaddr != null) {
            ingressInIP = ipaddr.getIP();
        } else {
            throw new PSSException("Invalid IP for: "+egressInPathElem.getLink().getFQTI());
        }
        ipaddr = egressInPathElem.getLink().getValidIpaddr();
        if (ipaddr != null) {
            egressInIP = ipaddr.getIP();
        } else {
            throw new PSSException("Invalid IP for: "+egressInPathElem.getLink().getFQTI());
        }

        try {



            PathElemParam inVlanPEP     = ingressPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            PathElemParam egVlanPEP     = egressPathElem.getPathElemParam(PathElemParamSwcap.L2SC, PathElemParamType.L2SC_VLAN_RANGE);
            String ingressLoopback      = ingressPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();
            String egressLoopback       = egressPathElem.getLink().getPort().getNode().getNodeAddress().getAddress();

            if (direction.equals(PSSDirection.A_TO_Z)) {
                ifceName = ingressPathElem.getLink().getPort().getTopologyIdent();
                ifceVlan = inVlanPEP.getValue();
                remoteVlan = egVlanPEP.getValue();
                lspFrom = ingressLoopback;
                lspTo = egressInIP;

            } else if (direction.equals(PSSDirection.Z_TO_A)) {
                ifceName = egressPathElem.getLink().getPort().getTopologyIdent();
                ifceVlan = egVlanPEP.getValue();
                remoteVlan = inVlanPEP.getValue();
                lspFrom = egressLoopback;
                lspTo = ingressInIP;
            } else {
                throw new PSSException("Invalid direction!");
            }



            policingFilterName      = SDNNameGenerator.getFilterName(gri, description, "policing");
            statsFilterName         = SDNNameGenerator.getFilterName(gri, description, "stats");
            communityName           = SDNNameGenerator.getCommunityName(gri, description);
            policyName              = SDNNameGenerator.getPolicyName(gri, description);
            policerName             = SDNNameGenerator.getPolicerName(gri, description);

        } catch (BSSException e) {
            this.log.error(e);
            throw new PSSException(e.getMessage());
        }



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
        root.put("policer", policer);
        root.put("l2circuit", l2circuit);
        root.put("community", community);
        root.put("remotevlan", remoteVlan);
        root.put("bandwidth", bandwidth);

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

        path.put("hops", pathHops);
        path.put("name", pathName);

        return this.getConfig(root, templateFileName);
    }

    public String generateEoMPLSTeardown(Reservation resv, Path localPath, PSSDirection direction) {
        String config = "";


        return config;
    }


    public String generateL2SwitchedlSetup(Reservation resv, Path localPath) {
        String config = "";

        return config;
    }

    public String generateL2SwitchedTeardown(Reservation resv, Path localPath) {
        String config = "";


        return config;
    }



    public String generateL3Setup(Reservation resv, Path localPath, PSSDirection direction) {
        String config = "";


        return config;
    }

    public String generateL3Teardown(Reservation resv, Path localPath, PSSDirection direction) {
        String config = "";


        return config;
    }

    public String generateL2Status(Reservation resv, Path localPath, PSSDirection direction) {
        String config = "";


        return config;
    }

    public String generateL3Status(Reservation resv, Path localPath, PSSDirection direction) {
        String config = "";


        return config;
    }


    public static TemplateHandler getInstance() {
        if (instance == null) {
            instance = new TemplateHandler();
        }
        return instance;
    }

    private TemplateHandler() {
        this.log = Logger.getLogger(this.getClass());
    }


    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public String getTemplateDir() {
        return templateDir;
    }

}
