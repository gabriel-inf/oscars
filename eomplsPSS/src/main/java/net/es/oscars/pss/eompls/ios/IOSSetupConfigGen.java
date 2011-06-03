package net.es.oscars.pss.eompls.ios;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class IOSSetupConfigGen {
    private String bandwidth;
    private String vlan;
    private String pseudowireName;
    private String policyName;
    private String tunnelDescription;
    private String lspDestination;
    private String pathName;
    private String vcid;

    private String[] hops;
    private String egressLoopback;
    private String ifceName;
    private String ifceDescription;

    @SuppressWarnings("unchecked")
    public String generateConfig() throws PSSException, ConfigException {

        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        String templateDir = cc.getFilePath("templateDir");

        // create data model objects
        Map root = new HashMap();
        Map pseudowire = new HashMap();
        Map policy = new HashMap();
        Map tunnel = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();
        Map l2circuit = new HashMap();

        // set up data model structure
        root.put("pseudowire", pseudowire);
        root.put("ifce", ifce);
        root.put("tunnel", tunnel);
        root.put("policy", policy);
        root.put("path", path);
        root.put("l2circuit", l2circuit);

        // fill in scalars
        root.put("vlan", vlan);
        root.put("bandwidth", bandwidth);
        pseudowire.put("name", pseudowireName);
        policy.put("name", policyName);
        tunnel.put("description", tunnelDescription);
        tunnel.put("destination", lspDestination);
        ifce.put("name", ifceName);
        ifce.put("description", ifceDescription);
        path.put("name", pathName);
        path.put("hops", hops);
        path.put("egressLoopback", egressLoopback);
        l2circuit.put("vcid", vcid);

        /* Merge data-model with template */

        String config = "";
        Template temp = null;
        Configuration cfg = new Configuration();
        try {
            cfg.setDirectoryForTemplateLoading(new File(templateDir));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Writer out = new StringWriter();
            temp = cfg.getTemplate("eompls-ios-setup.txt");
            temp.process(root, out);
            out.flush();
            config = out.toString();
        } catch (IOException e) {
            throw new ConfigException(e.getMessage());
        } catch (TemplateException e) {
            throw new ConfigException(e.getMessage());
        }
        return config;
    }

    public String getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getVlan() {
        return vlan;
    }

    public void setVlan(String vlan) {
        this.vlan = vlan;
    }

    public String getPseudowireName() {
        return pseudowireName;
    }

    public void setPseudowireName(String pseudowireName) {
        this.pseudowireName = pseudowireName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getTunnelDescription() {
        return tunnelDescription;
    }

    public void setTunnelDescription(String tunnelDescription) {
        this.tunnelDescription = tunnelDescription;
    }

    public String getLspDestination() {
        return lspDestination;
    }

    public void setLspDestination(String lspDestination) {
        this.lspDestination = lspDestination;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String[] getHops() {
        return hops;
    }

    public void setHops(String[] hops) {
        this.hops = hops;
    }

    public String getEgressLoopback() {
        return egressLoopback;
    }

    public void setEgressLoopback(String egressLoopback) {
        this.egressLoopback = egressLoopback;
    }

    public String getIfceName() {
        return ifceName;
    }

    public void setIfceName(String ifceName) {
        this.ifceName = ifceName;
    }

    public String getIfceDescription() {
        return ifceDescription;
    }

    public void setIfceDescription(String ifceDescription) {
        this.ifceDescription = ifceDescription;
    }

    public String getVcid() {
        return vcid;
    }

    public void setVcid(String vcid) {
        this.vcid = vcid;
    }

}
